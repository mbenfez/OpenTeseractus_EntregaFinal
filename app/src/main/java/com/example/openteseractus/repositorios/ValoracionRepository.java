package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Valoracion;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ValoracionRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void valorar(String idTeseracto, Valoracion v, FirestoreCallback<Void> callback) {

        DocumentReference ref = db.collection("teseractos")
                .document(idTeseracto)
                .collection("valoraciones")
                .document(v.getUidValoradoPor());

        ref.set(v)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void obtenerMediaValoraciones(String idTeseracto, FirestoreCallback<Double> callback) {

        db.collection("teseractos")
                .document(idTeseracto)
                .collection("valoraciones")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    double suma = 0;
                    int total = 0;

                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Valoracion valoracion = doc.toObject(Valoracion.class);
                        if (valoracion != null && valoracion.getPuntuacion() > 0) {
                            suma += valoracion.getPuntuacion();
                            total++;
                        }
                    }

                    double media = total > 0 ? suma / total : 0;
                    callback.onSuccess(media);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void obtenerValoracionUsuario(
            String idTeseracto,
            String uidUsuario,
            FirestoreCallback<Valoracion> callback
    ) {

        db.collection("teseractos")
                .document(idTeseracto)
                .collection("valoraciones")
                .document(uidUsuario)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Valoracion valoracion = documentSnapshot.toObject(Valoracion.class);
                    callback.onSuccess(valoracion);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}