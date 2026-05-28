package com.example.openteseractus.repositorios;

import android.util.Log;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Grupo;
import com.example.openteseractus.modelos.MiembroGrupo;
import com.example.openteseractus.modelos.UsuarioGrupoRef;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GrupoRepository {

    private static final String TAG = "GrupoRepository";
    private static final String COLLECTION_GRUPOS = "grupos";
    private static final String COLLECTION_MIEMBROS = "miembros";

    private FirebaseFirestore db;

    public GrupoRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Crea un nuevo grupo y añade al creador como administrador
    public void crearGrupo(Grupo grupo, String uidCreador, FirestoreCallback<Grupo> callback) {

        if (grupo.getId() == null) {
            grupo.setId(UUID.randomUUID().toString());
        }

        grupo.setUidCreador(uidCreador);
        grupo.setFechaCreacion(System.currentTimeMillis());
        grupo.generarCodigoInvitacion();

        MiembroGrupo admin = new MiembroGrupo();
        admin.setUidMiembro(uidCreador);
        admin.setRol("admin");
        admin.setFechaUnion(System.currentTimeMillis());

        UsuarioGrupoRef ref = new UsuarioGrupoRef(grupo.getId(), grupo.getNomGrupo());

        WriteBatch batch = db.batch();

        // grupo
        batch.set(db.collection("grupos").document(grupo.getId()), grupo);

        // miembro
        batch.set(db.collection("grupos")
                .document(grupo.getId())
                .collection("miembros")
                .document(uidCreador), admin);

        // referencia usuario
        batch.set(db.collection("usuarios")
                .document(uidCreador)
                .collection("grupos")
                .document(grupo.getId()), ref);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(grupo))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Obtiene un grupo por su ID
    public void obtenerGrupo(String idGrupo, FirestoreCallback<Grupo> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Grupo grupo = documentSnapshot.toObject(Grupo.class);
                        Log.d(TAG, "Grupo obtenido: " + idGrupo);
                        if (callback != null) callback.onSuccess(grupo);
                    } else {
                        Log.w(TAG, "Grupo no encontrado: " + idGrupo);
                        if (callback != null) callback.onFailure("Grupo no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener grupo", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // Obtiene todos los grupos de un usuario
    public void obtenerGruposDeUsuario(String uid, FirestoreCallback<List<Grupo>> callback) {

        db.collection("usuarios")
                .document(uid)
                .collection("grupos")
                .orderBy("ultimaActividad", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<String> idsGrupos = new ArrayList<>();
                    query.forEach(doc -> {
                        UsuarioGrupoRef ref = doc.toObject(UsuarioGrupoRef.class);
                        idsGrupos.add(ref.getIdGrupo());
                    });

                    if (idsGrupos.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                    } else {
                        obtenerGruposPorIds(idsGrupos, new FirestoreCallback<List<Grupo>>() {
                            @Override
                            public void onSuccess(List<Grupo> grupos) {
                                // Restore the ultimaActividad order from the ref query
                                grupos.sort((a, b) -> {
                                    int ia = idsGrupos.indexOf(a.getId());
                                    int ib = idsGrupos.indexOf(b.getId());
                                    return Integer.compare(ia, ib);
                                });
                                callback.onSuccess(grupos);
                            }
                            @Override
                            public void onFailure(String error) {
                                callback.onFailure(error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Obtiene grupos por lista de IDs (maneja el límite de 10 de Firestore)
    private void obtenerGruposPorIds(List<String> ids, FirestoreCallback<List<Grupo>> callback) {
        List<Grupo> todosGrupos = new ArrayList<>();
        int batchSize = 10;
        int numBatches = (int) Math.ceil((double) ids.size() / batchSize);
        final int[] batchesCompletados = {0};

        for (int i = 0; i < numBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, ids.size());
            List<String> batchIds = ids.subList(start, end);

            db.collection(COLLECTION_GRUPOS)
                    .whereIn("id", batchIds)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        queryDocumentSnapshots.forEach(doc -> {
                            Grupo grupo = doc.toObject(Grupo.class);
                            todosGrupos.add(grupo);
                        });

                        batchesCompletados[0]++;
                        if (batchesCompletados[0] == numBatches) {
                            Log.d(TAG, "Grupos obtenidos: " + todosGrupos.size());
                            if (callback != null) callback.onSuccess(todosGrupos);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error en batch de grupos", e);
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
        }
    }

    // Busca un grupo por código de invitación
    public void buscarGrupoPorCodigo(String codigo, FirestoreCallback<Grupo> callback) {
        db.collection(COLLECTION_GRUPOS)
                .whereEqualTo("codInvitacion", codigo)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Grupo grupo = queryDocumentSnapshots.getDocuments().get(0).toObject(Grupo.class);
                        Log.d(TAG, "Grupo encontrado con código: " + codigo);
                        if (callback != null) callback.onSuccess(grupo);
                    } else {
                        Log.w(TAG, "No existe grupo con código: " + codigo);
                        if (callback != null) callback.onFailure("Código de invitación inválido");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar grupo por código", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void agregarMiembro(String idGrupo, MiembroGrupo miembro, FirestoreCallback<Void> callback) {
        db.collection("grupos")
                .document(idGrupo)
                .collection("miembros")
                .document(miembro.getUidMiembro())
                .set(miembro)
                .addOnSuccessListener(aVoid ->
                        callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    // Añade un miembro a un grupo
    public void unirseAGrupo(String idGrupo, String uid, FirestoreCallback<Void> callback) {
        db.collection("grupos").document(idGrupo).get()
                .addOnSuccessListener(doc -> {

                    Grupo grupo = doc.toObject(Grupo.class);

                    MiembroGrupo miembro = new MiembroGrupo();
                    miembro.setUidMiembro(uid);
                    miembro.setRol("miembro");
                    miembro.setFechaUnion(System.currentTimeMillis());

                    UsuarioGrupoRef ref = new UsuarioGrupoRef(idGrupo, grupo.getNomGrupo());

                    WriteBatch batch = db.batch();

                    batch.set(db.collection("grupos")
                            .document(idGrupo)
                            .collection("miembros")
                            .document(uid), miembro);

                    batch.set(db.collection("usuarios")
                            .document(uid)
                            .collection("grupos")
                            .document(idGrupo), ref);

                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                });
    }

    // Obtiene los miembros de un grupo
    public void obtenerMiembrosGrupo(String idGrupo, FirestoreCallback<List<MiembroGrupo>> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .collection(COLLECTION_MIEMBROS)
                .orderBy("fechaUnion", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MiembroGrupo> miembros = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        MiembroGrupo miembro = doc.toObject(MiembroGrupo.class);
                        miembros.add(miembro);
                    });
                    Log.d(TAG, "Miembros obtenidos: " + miembros.size());
                    if (callback != null) callback.onSuccess(miembros);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener miembros", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void obtenerCantidadMiembros(String idGrupo, FirestoreCallback<Integer> callback) {

        db.collection("grupos")
                .document(idGrupo)
                .collection("miembros")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    // Actualiza los datos de un grupo
    public void actualizarGrupo(Grupo grupo, FirestoreCallback<Grupo> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(grupo.getId())
                .set(grupo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Grupo actualizado: " + grupo.getId());
                    if (callback != null) callback.onSuccess(grupo);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar grupo", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // Elimina un miembro de un grupo
    public void eliminarMiembro(String idGrupo, String uid, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .collection(COLLECTION_MIEMBROS)
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Miembro eliminado del grupo: " + uid);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar miembro", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // Genera un código de invitación aleatorio de 6 caracteres alfanuméricos
    private String generarCodigoInvitacion() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return codigo.toString();
    }

    // Actualiza el timestamp de última actividad del usuario en el grupo
    public void actualizarUltimaActividad(String uid, String idGrupo) {
        db.collection("usuarios")
                .document(uid)
                .collection("grupos")
                .document(idGrupo)
                .update("ultimaActividad", System.currentTimeMillis())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar ultimaActividad: " + e.getMessage()));
    }

    // Regenera el código de invitación de un grupo
    public void regenerarCodigoInvitacion(String idGrupo, FirestoreCallback<String> callback) {
        String nuevoCodigo = generarCodigoInvitacion();

        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .update("codInvitacion", nuevoCodigo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Código de invitación regenerado: " + nuevoCodigo);
                    if (callback != null) callback.onSuccess(nuevoCodigo);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al regenerar código", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

}
