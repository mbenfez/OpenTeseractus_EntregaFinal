package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Valoracion;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repositorio para las valoraciones de teseractos almacenadas en la subcolección
 * {@code teseractos/{id}/valoraciones}. El ID del documento es el UID del usuario,
 * de modo que cada usuario tiene como máximo una valoración por teseracto.
 */
public class ValoracionRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Guarda o sobreescribe la valoración del usuario para un teseracto.
     * Si el usuario ya había valorado anteriormente, la actualiza.
     *
     * @param idTeseracto identificador del teseracto
     * @param v           valoración a persistir
     * @param callback    resultado de la operación
     */
    public void valorar(String idTeseracto, Valoracion v, FirestoreCallback<Void> callback) {

        DocumentReference ref = db.collection("teseractos")
                .document(idTeseracto)
                .collection("valoraciones")
                .document(v.getUidValoradoPor());

        ref.set(v)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Calcula la media de todas las puntuaciones generales del teseracto
     * ignorando valoraciones con puntuación 0.
     *
     * @param idTeseracto identificador del teseracto
     * @param callback    resultado: media calculada (0 si no hay valoraciones) o error
     */
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

    /**
     * Obtiene la valoración de un usuario concreto para un teseracto.
     *
     * @param idTeseracto identificador del teseracto
     * @param uidUsuario  UID del usuario
     * @param callback    resultado: la valoración del usuario (puede ser {@code null}) o error
     */
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