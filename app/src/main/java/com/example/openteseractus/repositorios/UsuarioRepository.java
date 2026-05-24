package com.example.openteseractus.repositorios;

import android.util.Log;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    private static final String TAG = "UsuarioRepository";
    private static final String COLLECTION_USUARIOS = "usuarios";
    private static final String PFP_PLACEHOLDER =
            "https://firebasestorage.googleapis.com/v0/b/openteseractus-firebase.firebasestorage.app/o/Dise%C3%B1o%20sin%20t%C3%ADtulo%20(3).png?alt=media&token=0f3dceca-1e8b-406d-8952-15df5efd1f7f";
    private FirebaseFirestore db;
    public UsuarioRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Crea un nuevo usuario en Firestore
    public void crearUsuario(Usuario usuario, FirestoreCallback<Usuario> callback) {
        if (usuario.getFotoPerfilUrl() == null) {
            usuario.setFotoPerfilUrl(PFP_PLACEHOLDER);
        }

        db.collection(COLLECTION_USUARIOS)
                .document(usuario.getUid())
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario creado: " + usuario.getUid());
                    if (callback != null) callback.onSuccess(usuario);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear usuario", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // Obtiene un usuario por su UID
    public void obtenerUsuario(String uid, FirestoreCallback<Usuario> callback) {
        db.collection(COLLECTION_USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Usuario usuario = documentSnapshot.toObject(Usuario.class);
                        Log.d(TAG, "Usuario obtenido: " + uid);
                        if (callback != null) callback.onSuccess(usuario);
                    } else {
                        Log.w(TAG, "Usuario no encontrado: " + uid);
                        if (callback != null) callback.onFailure("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener usuario", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void buscarUsuarios(String query, FirestoreCallback<List<Usuario>> callback) {
        db.collection("usuarios")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<Usuario> usuarios =
                            queryDocumentSnapshots.toObjects(Usuario.class);

                    callback.onSuccess(usuarios);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    // Actualiza los datos de un usuario
    public void actualizarUsuario(Usuario usuario, FirestoreCallback<Usuario> callback) {
        db.collection(COLLECTION_USUARIOS)
                .document(usuario.getUid())
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario actualizado: " + usuario.getUid());
                    if (callback != null) callback.onSuccess(usuario);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar usuario", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // Verifica si un username ya existe
    public void existeUsername(String username, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean existe = !queryDocumentSnapshots.isEmpty();
                    Log.d(TAG, "Username '" + username + "' existe: " + existe);
                    if (callback != null) callback.onSuccess(existe);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar username", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void actualizarEstadoUsuario(String uid, boolean activo) {
        db.collection("usuarios")
                .document(uid)
                .update("activo", activo)
                .addOnSuccessListener(unused ->
                        Log.d("USUARIO_ESTADO", "Estado actualizado"))
                .addOnFailureListener(e ->
                        Log.e("USUARIO_ESTADO", e.getMessage()));
    }

    // Obtiene todos los usuarios activos (para búsqueda/sugerencias)
    public void obtenerUsuariosActivos(FirestoreCallback<List<Usuario>> callback) {
        db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("activo", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Usuario> usuarios = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        Usuario usuario = doc.toObject(Usuario.class);
                        usuarios.add(usuario);
                    });
                    Log.d(TAG, "Usuarios activos obtenidos: " + usuarios.size());
                    if (callback != null) callback.onSuccess(usuarios);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener usuarios activos", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

}
