package com.example.openteseractus.servicios;

import android.util.Log;

import com.example.openteseractus.callbacks.AuthCallback;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

/**
 * Servicio de autenticación que encapsula Firebase Authentication y coordina
 * la creación/lectura del perfil del usuario en Firestore mediante {@link com.example.openteseractus.repositorios.UsuarioRepository}.
 */
public class AuthService {

    private static final String TAG = "AuthService";

    private FirebaseAuth mAuth;
    private UsuarioRepository usuarioRepository;

    public AuthService() {
        this.mAuth = FirebaseAuth.getInstance();
        this.usuarioRepository = new UsuarioRepository();
    }

    /**
     * Registra un nuevo usuario: valida email, contraseña y username, comprueba
     * que el username no esté en uso, crea la cuenta en Firebase Auth, persiste
     * el perfil en Firestore y envía un email de verificación.
     * Hace sign-out al finalizar; el usuario debe verificar su email antes de acceder.
     *
     * @param email    dirección de correo electrónico
     * @param password contraseña (mínimo 6 caracteres)
     * @param usuario  datos de perfil del nuevo usuario
     * @param callback resultado: el usuario creado o un mensaje de error localizado
     */
    public void registrarUsuario(String email, String password, Usuario usuario, AuthCallback callback) {
        
        if (email == null || email.isEmpty()) {
            if (callback != null) callback.onFailure("El email es obligatorio");
            return;
        }

        if (password == null || password.length() < 6) {
            if (callback != null) callback.onFailure("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!usuario.validarUsername()) {
            if (callback != null) callback.onFailure("El nombre de usuario no es válido");
            return;
        }

        usuarioRepository.existeUsername(usuario.getUsername(), new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean existe) {
                if (existe) {
                    if (callback != null) callback.onFailure("El nombre de usuario ya está en uso");
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser firebaseUser = authResult.getUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                usuario.setUid(uid);
                                usuario.setEmail(email);

                                usuarioRepository.crearUsuario(usuario, new FirestoreCallback<Usuario>() {
                                    @Override
                                    public void onSuccess(Usuario u) {
                                        firebaseUser.sendEmailVerification()
                                                .addOnCompleteListener(task ->
                                                        Log.d(TAG, "Email de verificación enviado a: " + email));
                                        mAuth.signOut();
                                        Log.d(TAG, "Usuario registrado exitosamente: " + uid);
                                        if (callback != null) callback.onSuccess(u);
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Log.e(TAG, "Error al guardar datos del usuario: " + error);
                                        
                                        firebaseUser.delete();
                                        if (callback != null) callback.onFailure("Error al guardar datos: " + error);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al crear cuenta", e);
                            String mensajeError = parsearErrorAuth(e.getMessage());
                            if (callback != null) callback.onFailure(mensajeError);
                        });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error al verificar username: " + error);
                if (callback != null) callback.onFailure("Error al verificar nombre de usuario");
            }
        });
    }

    /**
     * Inicia sesión admitiendo tanto email como nombre de usuario como identificador.
     * Si el identificador no contiene '@', resuelve el email asociado al username
     * antes de intentar el login.
     *
     * @param identificador email o nombre de usuario
     * @param password       contraseña del usuario
     * @param callback       resultado: el usuario autenticado o un error.
     *                       Si el email no está verificado, el error es {@code "email_not_verified"}.
     */
    public void iniciarSesion(String identificador, String password, AuthCallback callback) {
        if (identificador == null || identificador.isEmpty()) {
            if (callback != null) callback.onFailure("El campo de acceso es obligatorio");
            return;
        }

        if (password == null || password.isEmpty()) {
            if (callback != null) callback.onFailure("La contraseña es obligatoria");
            return;
        }

        if (identificador.contains("@")) {
            loginConEmail(identificador, password, callback);
        } else {
            usuarioRepository.obtenerEmailPorUsername(identificador, new FirestoreCallback<String>() {
                @Override
                public void onSuccess(String email) {
                    loginConEmail(email, password, callback);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Username no encontrado: " + error);
                    if (callback != null) callback.onFailure("Nombre de usuario no encontrado");
                }
            });
        }
    }

    private void loginConEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        if (!firebaseUser.isEmailVerified()) {
                            mAuth.signOut();
                            if (callback != null) callback.onFailure("email_not_verified");
                            return;
                        }

                        String uid = firebaseUser.getUid();

                        usuarioRepository.obtenerUsuario(uid, new FirestoreCallback<Usuario>() {
                            @Override
                            public void onSuccess(Usuario usuario) {
                                usuarioRepository.actualizarEstadoUsuario(uid, true);
                                Log.d(TAG, "Sesión iniciada: " + uid);
                                if (callback != null) callback.onSuccess(usuario);
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e(TAG, "Error al obtener datos del usuario: " + error);
                                if (callback != null) callback.onFailure("Error al cargar datos del usuario");
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al iniciar sesión", e);
                    String mensajeError = parsearErrorAuth(e.getMessage());
                    if (callback != null) callback.onFailure(mensajeError);
                });
    }

    /**
     * Cierra la sesión del usuario: marca su estado como inactivo en Firestore
     * y llama a {@code FirebaseAuth.signOut()}.
     */
    public void cerrarSesion() {
        String uid = getUidActual();
        if (uid != null) {
            usuarioRepository.actualizarEstadoUsuario(uid, false);
        }
        mAuth.signOut();
        Log.d(TAG, "Sesión cerrada");
    }

    public FirebaseUser getUsuarioActual() {
        return mAuth.getCurrentUser();
    }

    /**
     * Indica si hay una sesión activa en Firebase Authentication.
     *
     * @return {@code true} si existe un usuario autenticado
     */
    public boolean hayUsuarioAutenticado() {
        return mAuth.getCurrentUser() != null;
    }

    public String getUidActual() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String parsearErrorAuth(String error) {
        if (error == null) return "Error desconocido";

        if (error.equals("email_not_verified")) {
            return "email_not_verified";
        } else if (error.contains("email address is already in use")) {
            return "Este email ya está registrado";
        } else if (error.contains("invalid email")) {
            return "Email inválido";
        } else if (error.contains("weak password")) {
            return "La contraseña es demasiado débil";
        } else if (error.contains("wrong password")) {
            return "Contraseña incorrecta";
        } else if (error.contains("no user record")) {
            return "No existe una cuenta con este email";
        } else if (error.contains("network error")) {
            return "Error de conexión. Verifica tu internet";
        } else {
            return "Error de autenticación: " + error;
        }
    }
}