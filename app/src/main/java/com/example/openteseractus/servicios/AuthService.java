package com.example.openteseractus.servicios;

import android.util.Log;

import com.example.openteseractus.callbacks.AuthCallback;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

public class AuthService {

    private static final String TAG = "AuthService";

    private FirebaseAuth mAuth;
    private UsuarioRepository usuarioRepository;

    public AuthService() {
        this.mAuth = FirebaseAuth.getInstance();
        this.usuarioRepository = new UsuarioRepository();
    }

    /**
     * Registra un nuevo usuario
     * 1. Crea cuenta en Firebase Auth
     * 2. Guarda datos del usuario en Firestore
     */
    public void registrarUsuario(String email, String password, Usuario usuario, AuthCallback callback) {
        // Validaciones previas
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

        // Verificar que el username no esté en uso
        usuarioRepository.existeUsername(usuario.getUsername(), new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean existe) {
                if (existe) {
                    if (callback != null) callback.onFailure("El nombre de usuario ya está en uso");
                    return;
                }

                // Crear cuenta
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser firebaseUser = authResult.getUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                usuario.setUid(uid);

                                // Guardar datos del usuario en Firestore
                                usuarioRepository.crearUsuario(usuario, new FirestoreCallback<Usuario>() {
                                    @Override
                                    public void onSuccess(Usuario u) {
                                        Log.d(TAG, "Usuario registrado exitosamente: " + uid);
                                        if (callback != null) callback.onSuccess(u);
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Log.e(TAG, "Error al guardar datos del usuario: " + error);
                                        // La cuenta de Auth se creó pero Firestore falló
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

    // Inicia sesión con email y contraseña
    public void iniciarSesion(String email, String password, AuthCallback callback) {
        if (email == null || email.isEmpty()) {
            if (callback != null) callback.onFailure("El email es obligatorio");
            return;
        }

        if (password == null || password.isEmpty()) {
            if (callback != null) callback.onFailure("La contraseña es obligatoria");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();

                        // Obtener datos del usuario de Firestore
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

    // Cierra la sesión actual
    // Cierra la sesión actual
    public void cerrarSesion() {
        String uid = getUidActual();
        if (uid != null) {
            usuarioRepository.actualizarEstadoUsuario(uid, false);
        }
        mAuth.signOut();
        Log.d(TAG, "Sesión cerrada");
    }

    // Obtiene el usuario actualmente autenticado

    public FirebaseUser getUsuarioActual() {
        return mAuth.getCurrentUser();
    }

    // Verifica si hay un usuario autenticado
    public boolean hayUsuarioAutenticado() {
        return mAuth.getCurrentUser() != null;
    }

    // Obtiene el UID del usuario actual
    public String getUidActual() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Parsea los mensajes de error de Firebase Auth a mensajes más amigables
    private String parsearErrorAuth(String error) {
        if (error == null) return "Error desconocido";

        if (error.contains("email address is already in use")) {
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