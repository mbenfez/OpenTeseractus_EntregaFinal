package com.example.openteseractus.repositorios;

import android.util.Log;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Usuario;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para la gestión de usuarios en la colección {@code usuarios} de Firestore.
 * También gestiona las subcolecciones {@code lecturas} y {@code preferencias/notificaciones}
 * del perfil del usuario.
 */
public class UsuarioRepository {

    private static final String TAG = "UsuarioRepository";
    private static final String COLLECTION_USUARIOS = "usuarios";
    private FirebaseFirestore db;

    public UsuarioRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void crearUsuario(Usuario usuario, FirestoreCallback<Usuario> callback) {

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

    /**
     * Busca usuarios cuyo {@code username} comience por la cadena indicada
     * usando una consulta de rango en Firestore (máximo 20 resultados).
     *
     * @param query    prefijo de búsqueda
     * @param callback resultado: lista de usuarios coincidentes o error
     */
    public void buscarUsuarios(String query, FirestoreCallback<List<Usuario>> callback) {
        db.collection("usuarios")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Usuario> usuarios = queryDocumentSnapshots.toObjects(Usuario.class);
                    callback.onSuccess(usuarios);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

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

    /**
     * Resuelve el email asociado a un nombre de usuario. Se usa en el flujo de login
     * cuando el usuario introduce su username en lugar del email.
     *
     * @param username nombre de usuario a resolver
     * @param callback resultado: el email encontrado o un mensaje de error
     */
    public void obtenerEmailPorUsername(String username, FirestoreCallback<String> callback) {
        db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String email = queryDocumentSnapshots.getDocuments().get(0).getString("email");
                        if (email != null && !email.isEmpty()) {
                            callback.onSuccess(email);
                        } else {
                            callback.onFailure("No se encontró el email asociado a este usuario");
                        }
                    } else {
                        callback.onFailure("Nombre de usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

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

    public void eliminarUsuario(String uid, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_USUARIOS)
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario eliminado: " + uid);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar usuario", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * Actualiza el timestamp de última lectura del teseracto para el usuario indicado.
     * Se llama al entrar y al salir del chat para marcar mensajes como leídos.
     *
     * @param uid         UID del usuario
     * @param idTeseracto identificador del teseracto
     */
    public void marcarLeido(String uid, String idTeseracto) {
        Map<String, Object> data = Collections.singletonMap(
                "ultimaLectura", FieldValue.serverTimestamp());
        db.collection(COLLECTION_USUARIOS).document(uid)
                .collection("lecturas").document(idTeseracto)
                .set(data);
    }

    /**
     * Añade un teseracto a la lista de silenciados del usuario para suprimir
     * sus notificaciones en {@link com.example.openteseractus.servicios.MensajeNotificacionService}.
     *
     * @param uid         UID del usuario
     * @param idTeseracto identificador del teseracto a silenciar
     * @param callback    resultado de la operación
     */
    public void silenciarTeseracto(String uid, String idTeseracto, FirestoreCallback<Void> callback) {
        Map<String, Object> data = Collections.singletonMap(
                "silenciados", FieldValue.arrayUnion(idTeseracto));
        db.collection(COLLECTION_USUARIOS).document(uid)
                .collection("preferencias").document("notificaciones")
                .set(data, SetOptions.merge())
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /**
     * Elimina un teseracto de la lista de silenciados del usuario para
     * reanudar sus notificaciones.
     *
     * @param uid         UID del usuario
     * @param idTeseracto identificador del teseracto
     * @param callback    resultado de la operación
     */
    public void activarNotificacionesTeseracto(String uid, String idTeseracto, FirestoreCallback<Void> callback) {
        Map<String, Object> data = Collections.singletonMap(
                "silenciados", FieldValue.arrayRemove(idTeseracto));
        db.collection(COLLECTION_USUARIOS).document(uid)
                .collection("preferencias").document("notificaciones")
                .set(data, SetOptions.merge())
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    public void obtenerEstadoSilencio(String uid, String idTeseracto, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_USUARIOS).document(uid)
                .collection("preferencias").document("notificaciones")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { callback.onSuccess(false); return; }
                    List<String> silenciados = (List<String>) doc.get("silenciados");
                    callback.onSuccess(silenciados != null && silenciados.contains(idTeseracto));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

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