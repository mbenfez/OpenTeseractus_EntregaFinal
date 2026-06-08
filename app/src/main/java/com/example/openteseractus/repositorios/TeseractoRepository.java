package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Teseracto;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

/**
 * Repositorio para operaciones CRUD sobre teseractos en la colección {@code teseractos}
 * de Firestore.
 */
public class TeseractoRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void crearTeseracto(Teseracto t, FirestoreCallback<Void> callback) {

        WriteBatch batch = db.batch();
        batch.set(db.collection("teseractos").document(t.getId()), t);
        
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void obtenerTeseracto(String id, FirestoreCallback<Teseracto> callback) {

        db.collection("teseractos")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(doc.toObject(Teseracto.class));
                    } else {
                        callback.onFailure("Teseracto no encontrado");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Suscribe un listener en tiempo real a todos los teseractos de un grupo.
     *
     * @param idGrupo  identificador del grupo
     * @param callback invocado en cada cambio con la lista completa de teseractos
     */
    public void obtenerPorGrupo(String idGrupo, FirestoreCallback<List<Teseracto>> callback) {

        db.collection("teseractos")
                .whereEqualTo("idGrupo", idGrupo)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        callback.onSuccess(value.toObjects(Teseracto.class));
                    }
                });
    }

    /**
     * Comprueba si ya existe un teseracto para una combinación de grupo, ID de TMDB y tipo de media.
     * Se usa antes de crear un teseracto para evitar duplicados dentro del mismo grupo.
     *
     * @param idGrupo   identificador del grupo
     * @param tmdbId    identificador de la película o serie en TMDB
     * @param mediaType tipo de media: {@code "movie"} o {@code "tv"}
     * @param callback  resultado: {@code true} si ya existe
     */
    public void existeTeseracto(String idGrupo, int tmdbId, String mediaType, FirestoreCallback<Boolean> callback) {

        db.collection("teseractos")
                .whereEqualTo("idGrupo", idGrupo)
                .whereEqualTo("tmdbId", tmdbId)
                .whereEqualTo("mediaType", mediaType)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean existe = !queryDocumentSnapshots.isEmpty();
                    callback.onSuccess(existe);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public void eliminarTeseracto(String idTeseracto, FirestoreCallback<Void> callback) {
        db.collection("teseractos")
                .document(idTeseracto)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Actualiza el campo {@code notaMedia} del teseracto en Firestore.
     * Se llama tras guardar una valoración para mantener sincronizada la nota visible.
     *
     * @param idTeseracto identificador del teseracto
     * @param notaMedia   nueva nota media calculada
     * @param callback    resultado de la operación
     */
    public void actualizarNotaMedia(String idTeseracto, double notaMedia, FirestoreCallback<Void> callback) {

        db.collection("teseractos")
                .document(idTeseracto)
                .update("notaMedia", notaMedia)
                .addOnSuccessListener(aVoid ->
                        callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
}