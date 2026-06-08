package com.example.openteseractus.repositorios;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Invitacion;
import com.example.openteseractus.modelos.MiembroGrupo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

/**
 * Repositorio para la gestión de invitaciones a grupos.
 * Las invitaciones se almacenan en la subcolección {@code invitacionesGrupo}
 * del documento del usuario invitado en Firestore.
 */
public class InvitacionRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Persiste una invitación en el perfil del usuario invitado.
     *
     * @param invitacion    datos de la invitación
     * @param nombreUsuario nombre del usuario que invita (para mostrar en la UI)
     * @param callback      resultado de la operación
     */
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

    /**
     * Registra un listener en tiempo real sobre las invitaciones pendientes del usuario.
     *
     * @param uid      UID del usuario cuyas invitaciones se observan
     * @param callback invocado en cada cambio con la lista actualizada de invitaciones
     * @return {@link ListenerRegistration} para cancelar la suscripción cuando no sea necesaria
     */
    public ListenerRegistration escucharInvitaciones(String uid, FirestoreCallback<List<Invitacion>> callback) {
        return db.collection("usuarios")
                .document(uid)
                .collection("invitacionesGrupo")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    if (snapshot != null) {
                        callback.onSuccess(snapshot.toObjects(Invitacion.class));
                    }
                });
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

    /**
     * Acepta la invitación: une al usuario al grupo mediante {@link GrupoRepository}
     * y elimina el documento de invitación al completarse con éxito.
     *
     * @param invitacion datos de la invitación a aceptar
     * @param callback   resultado de la operación
     */
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

    /**
     * Rechaza una invitación eliminando su documento del perfil del usuario.
     *
     * @param idInvitacion identificador de la invitación
     * @param uid          UID del usuario que rechaza
     * @param callback     resultado de la operación
     */
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