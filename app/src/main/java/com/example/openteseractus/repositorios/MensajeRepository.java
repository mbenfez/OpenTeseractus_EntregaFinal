package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Mensaje;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MensajeRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void enviarMensaje(Mensaje m, FirestoreCallback<Void> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uidAutor", m.getUidAutor());
        data.put("idTeseracto", m.getIdTeseracto());
        data.put("contenido", m.getContenido());
        data.put("fechaEnvio", FieldValue.serverTimestamp());

        // Batch atómico: escribe el mensaje y registra al autor como participante.
        // "participantes" en el doc del teseracto alimenta el servicio de notificaciones.
        DocumentReference mensajeRef = db.collection("teseractos")
                .document(m.getIdTeseracto())
                .collection("mensajes")
                .document();

        DocumentReference teseractoRef = db.collection("teseractos")
                .document(m.getIdTeseracto());

        WriteBatch batch = db.batch();
        batch.set(mensajeRef, data);
        batch.update(teseractoRef, "participantes", FieldValue.arrayUnion(m.getUidAutor()));

        batch.commit()
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

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
                        List<Mensaje> mensajes = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Mensaje mensaje = parsearMensaje(doc);
                            if (mensaje != null) mensajes.add(mensaje);
                        }
                        callback.onSuccess(mensajes);
                    }
                });
    }

    /**
     * Devuelve el número de mensajes de otros usuarios recibidos después de la última
     * vez que el usuario leyó este chat (usuarios/{uid}/lecturas/{idTeseracto}.ultimaLectura).
     */
    public void contarNoLeidos(String uid, String idTeseracto, FirestoreCallback<Integer> callback) {
        db.collection("usuarios").document(uid)
                .collection("lecturas").document(idTeseracto)
                .get()
                .addOnSuccessListener(lectura -> {
                    Timestamp ultimaLectura = lectura.exists()
                            ? lectura.getTimestamp("ultimaLectura") : null;

                    Query q = db.collection("teseractos")
                            .document(idTeseracto).collection("mensajes");
                    if (ultimaLectura != null) {
                        q = q.whereGreaterThan("fechaEnvio", ultimaLectura);
                    }

                    q.get().addOnSuccessListener(docs -> {
                        int count = 0;
                        for (DocumentSnapshot doc : docs.getDocuments()) {
                            if (!uid.equals(doc.getString("uidAutor"))) count++;
                        }
                        callback.onSuccess(count);
                    }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Soporta fechaEnvio tanto como Timestamp (mensajes nuevos) como Long (mensajes viejos)
    private Mensaje parsearMensaje(DocumentSnapshot doc) {
        try {
            Mensaje m = new Mensaje();
            m.setId(doc.getId());
            m.setUidAutor(doc.getString("uidAutor"));
            m.setIdTeseracto(doc.getString("idTeseracto"));
            m.setContenido(doc.getString("contenido"));

            Object fechaObj = doc.get("fechaEnvio");
            if (fechaObj instanceof Timestamp) {
                m.setFechaEnvio(((Timestamp) fechaObj).toDate().getTime());
            } else if (fechaObj instanceof Long) {
                m.setFechaEnvio((Long) fechaObj);
            } else if (fechaObj instanceof Number) {
                m.setFechaEnvio(((Number) fechaObj).longValue());
            } else {
                // Mensaje recién enviado aún sin timestamp del servidor: usar hora actual
                m.setFechaEnvio(System.currentTimeMillis());
            }

            return m;
        } catch (Exception e) {
            return null;
        }
    }
}
