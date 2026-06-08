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

/**
 * Repositorio para el envío y escucha de mensajes de chat en los teseractos.
 * Los mensajes se almacenan en la subcolección {@code mensajes} del teseracto
 * usando {@code FieldValue.serverTimestamp()} para garantizar un orden consistente.
 */
public class MensajeRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Envía un mensaje al chat de un teseracto mediante WriteBatch atómico:
     * crea el documento del mensaje con timestamp de servidor y añade al autor
     * a la lista de participantes del teseracto.
     *
     * @param m        mensaje a enviar
     * @param callback resultado de la operación
     */
    public void enviarMensaje(Mensaje m, FirestoreCallback<Void> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uidAutor", m.getUidAutor());
        data.put("idTeseracto", m.getIdTeseracto());
        data.put("contenido", m.getContenido());
        data.put("fechaEnvio", FieldValue.serverTimestamp());

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

    /**
     * Registra un listener en tiempo real que recibe los mensajes de un teseracto
     * ordenados cronológicamente. Deserializa el campo {@code fechaEnvio} que puede
     * llegar como {@link com.google.firebase.Timestamp}, {@code Long} o {@code Number}
     * para compatibilidad con datos escritos por clientes distintos.
     *
     * @param idTeseracto identificador del teseracto
     * @param callback    invocado en cada cambio con la lista completa de mensajes
     * @return {@link ListenerRegistration} para cancelar la suscripción
     */
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
     * Cuenta los mensajes no leídos de otros usuarios en un teseracto.
     * Consulta el documento {@code usuarios/{uid}/lecturas/{idTeseracto}} para obtener
     * el timestamp de la última lectura y filtra mensajes más recientes escritos por otros.
     *
     * @param uid          UID del usuario para quien se cuentan los no leídos
     * @param idTeseracto  identificador del teseracto
     * @param callback     resultado: número de mensajes no leídos o error
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
                
                m.setFechaEnvio(System.currentTimeMillis());
            }

            return m;
        } catch (Exception e) {
            return null;
        }
    }
}