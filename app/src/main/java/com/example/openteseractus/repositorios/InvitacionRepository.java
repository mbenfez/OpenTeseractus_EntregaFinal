package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Invitacion;
import com.example.openteseractus.modelos.MiembroGrupo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class InvitacionRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void crearInvitacion(Invitacion invitacion, String nombreUsuario, FirestoreCallback<Void> callback) {

        invitacion.setNombreUsuario(nombreUsuario);

        db.collection("usuarios")
                .document(invitacion.getUidInvitado())
                .collection("invitacionesGrupo")
                .document(invitacion.getId())
                .set(invitacion)
                .addOnSuccessListener(aVoid ->
                        callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void obtenerInvitaciones(String uid, FirestoreCallback<List<Invitacion>> callback) {

        db.collection("usuarios")
                .document(uid)
                .collection("invitacionesGrupo")
                .get()
                .addOnSuccessListener(q ->
                        callback.onSuccess(q.toObjects(Invitacion.class)))
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void aceptarInvitacion(Invitacion invitacion, FirestoreCallback<Void> callback) {

        GrupoRepository grupoRepository = new GrupoRepository();

        grupoRepository.unirseAGrupo(
                invitacion.getIdGrupo(),
                invitacion.getUidInvitado(),
                new FirestoreCallback<Void>() {

                    @Override
                    public void onSuccess(Void resultado) {
                        db.collection("usuarios")
                                .document(invitacion.getUidInvitado())
                                .collection("invitacionesGrupo")
                                .document(invitacion.getId())
                                .delete()
                                .addOnSuccessListener(unused ->
                                        callback.onSuccess(null))
                                .addOnFailureListener(e ->
                                        callback.onFailure(e.getMessage()));
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
    }

    public void rechazarInvitacion(String idInvitacion, String uid, FirestoreCallback<Void> callback) {

        db.collection("usuarios")
                .document(uid)
                .collection("invitacionesGrupo")
                .document(idInvitacion)
                .delete()
                .addOnSuccessListener(unused ->
                        callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void borrarInvitacionesDeUsuario(String uid, FirestoreCallback<Void> callback) {

        db.collection("usuarios")
                .document(uid)
                .collection("invitacionesGrupo")
                .get()
                .addOnSuccessListener(query -> {
                    WriteBatch batch = db.batch();
                    query.getDocuments().forEach(doc ->
                            batch.delete(doc.getReference()));
                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess(null))
                            .addOnFailureListener(e ->
                                    callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void eliminarInvitacion(String uid, String idInvitacion, FirestoreCallback<Void> callback) {

        db.collection("usuarios")
                .document(uid)
                .collection("invitacionesGrupo")
                .document(idInvitacion)
                .delete()
                .addOnSuccessListener(aVoid ->
                        callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
}