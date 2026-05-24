package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Mensaje;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;

public class MensajeRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void enviarMensaje(Mensaje m, FirestoreCallback<Void> callback) {

        db.collection("teseractos")
                .document(m.getIdTeseracto())
                .collection("mensajes")
                .add(m)
                .addOnSuccessListener(doc -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Escucha mensajes en tiempo real. Devuelve el listener para poder cancelarlo.
    public ListenerRegistration escucharMensajes(
            String idTeseracto,
            FirestoreCallback<List<Mensaje>> callback
    ) {
        return db.collection("teseractos")
                .document(idTeseracto)
                .collection("mensajes")
                .orderBy("fechaEnvio", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    if (snapshot != null) {
                        callback.onSuccess(snapshot.toObjects(Mensaje.class));
                    }
                });
    }
}