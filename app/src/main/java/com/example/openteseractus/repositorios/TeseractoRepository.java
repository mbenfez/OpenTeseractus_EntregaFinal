package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Teseracto;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class TeseractoRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void crearTeseracto(Teseracto t, FirestoreCallback<Void> callback) {

        WriteBatch batch = db.batch();
        batch.set(db.collection("teseractos").document(t.getId()), t);
        // actualizar actividad del grupo en usuario
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