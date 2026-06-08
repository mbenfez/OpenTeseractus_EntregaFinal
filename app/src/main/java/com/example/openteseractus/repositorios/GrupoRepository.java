package com.example.openteseractus.repositorios;

import android.util.Log;

import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Grupo;
import com.example.openteseractus.modelos.MiembroGrupo;
import com.example.openteseractus.modelos.UsuarioGrupoRef;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Repositorio que centraliza todas las operaciones CRUD sobre grupos en Firestore.
 * Gestiona la colección {@code grupos} y las subcolecciones {@code miembros},
 * así como la referencia inversa en {@code usuarios/{uid}/grupos}.
 */
public class GrupoRepository {

    private static final String TAG = "GrupoRepository";
    private static final String COLLECTION_GRUPOS = "grupos";
    private static final String COLLECTION_MIEMBROS = "miembros";

    private FirebaseFirestore db;

    public GrupoRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Crea un grupo nuevo en Firestore usando un WriteBatch atómico que persiste
     * simultáneamente el documento del grupo, el miembro creador (con rol {@code "admin"})
     * y la referencia inversa en el perfil del usuario.
     *
     * @param grupo      datos del grupo a crear
     * @param uidCreador UID del usuario creador
     * @param callback   resultado: el grupo creado o el error
     */
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

        batch.set(db.collection("grupos").document(grupo.getId()), grupo);

        batch.set(db.collection("grupos")
                .document(grupo.getId())
                .collection("miembros")
                .document(uidCreador), admin);

        batch.set(db.collection("usuarios")
                .document(uidCreador)
                .collection("grupos")
                .document(grupo.getId()), ref);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(grupo))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Obtiene un grupo por su ID.
     *
     * @param idGrupo  identificador del grupo
     * @param callback resultado: el objeto {@link Grupo} o un mensaje de error
     */
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

    /**
     * Obtiene todos los grupos a los que pertenece un usuario, ordenados por actividad
     * reciente. Internamente usa la subcolección de referencias del usuario y luego
     * recupera los documentos completos en lotes de 10 (límite de Firestore {@code whereIn}).
     *
     * @param uid      UID del usuario
     * @param callback resultado: lista de grupos ordenada por actividad o error
     */
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

    /**
     * Busca un grupo por su código de invitación.
     *
     * @param codigo   código de 6 caracteres
     * @param callback resultado: el grupo encontrado o un error si el código no existe
     */
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

    /**
     * Une a un usuario a un grupo existente mediante un WriteBatch atómico que crea
     * el documento de miembro y la referencia inversa en el perfil del usuario.
     *
     * @param idGrupo  identificador del grupo
     * @param uid      UID del usuario que se une
     * @param callback resultado de la operación
     */
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

    public void eliminarMiembro(String idGrupo, String uid, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .collection(COLLECTION_MIEMBROS)
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Miembro expulsado del grupo: " + uid);
                    quitarParticipanteDeTeseractosDelGrupo(idGrupo, uid, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al expulsar miembro", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void eliminarRefGrupoDeUsuario(String uid, String idGrupo, FirestoreCallback<Void> callback) {
        db.collection("usuarios")
                .document(uid)
                .collection("grupos")
                .document(idGrupo)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ref de grupo eliminada para usuario: " + uid);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar ref de grupo", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    private String generarCodigoInvitacion() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return codigo.toString();
    }

    public void actualizarUltimaActividad(String uid, String idGrupo) {
        db.collection("usuarios")
                .document(uid)
                .collection("grupos")
                .document(idGrupo)
                .update("ultimaActividad", System.currentTimeMillis())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar ultimaActividad: " + e.getMessage()));
    }

    public void actualizarNombreGrupo(String idGrupo, String nuevoNombre, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .update("nomGrupo", nuevoNombre)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void actualizarFotoGrupo(String idGrupo, String fotoUrl, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .update("fotoGrupoUrl", fotoUrl)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void ascenderAAdmin(String idGrupo, String uidMiembro, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_GRUPOS)
                .document(idGrupo)
                .collection(COLLECTION_MIEMBROS)
                .document(uidMiembro)
                .update("rol", "admin")
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Gestiona la salida de un usuario de un grupo con lógica de consistencia:
     * <ul>
     *   <li>Si era el último miembro, elimina el grupo completo (teseractos, valoraciones,
     *       mensajes e invitaciones).</li>
     *   <li>Si era admin sin otro admin, asciende aleatoriamente a otro miembro.</li>
     *   <li>En cualquier caso, elimina al usuario de la lista de participantes
     *       de todos los teseractos del grupo.</li>
     * </ul>
     *
     * @param idGrupo  identificador del grupo
     * @param uid      UID del usuario que sale
     * @param callback resultado de la operación
     */
    public void salirDelGrupo(String idGrupo, String uid, FirestoreCallback<Void> callback) {
        obtenerMiembrosGrupo(idGrupo, new FirestoreCallback<List<MiembroGrupo>>() {
            @Override
            public void onSuccess(List<MiembroGrupo> miembros) {
                boolean erAdmin = false;
                boolean hayOtroAdmin = false;
                List<MiembroGrupo> otrosMiembros = new ArrayList<>();

                for (MiembroGrupo m : miembros) {
                    if (uid.equals(m.getUidMiembro())) {
                        erAdmin = "admin".equalsIgnoreCase(m.getRol());
                    } else {
                        otrosMiembros.add(m);
                        if ("admin".equalsIgnoreCase(m.getRol())) hayOtroAdmin = true;
                    }
                }

                if (otrosMiembros.isEmpty()) {
                    
                    ejecutarSalida(idGrupo, uid, new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) { eliminarGrupoCompleto(idGrupo, callback); }
                        @Override
                        public void onFailure(String error) { callback.onFailure(error); }
                    });
                } else if (erAdmin && !hayOtroAdmin) {
                    
                    MiembroGrupo nuevoAdmin = otrosMiembros.get(
                            new Random().nextInt(otrosMiembros.size()));
                    ascenderAAdmin(idGrupo, nuevoAdmin.getUidMiembro(), new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) { ejecutarSalida(idGrupo, uid, callback); }
                        @Override
                        public void onFailure(String error) { callback.onFailure(error); }
                    });
                } else {
                    ejecutarSalida(idGrupo, uid, callback);
                }
            }

            @Override
            public void onFailure(String error) { callback.onFailure(error); }
        });
    }

    private void ejecutarSalida(String idGrupo, String uid, FirestoreCallback<Void> callback) {
        WriteBatch batch = db.batch();
        batch.delete(db.collection(COLLECTION_GRUPOS)
                .document(idGrupo).collection(COLLECTION_MIEMBROS).document(uid));
        batch.delete(db.collection("usuarios")
                .document(uid).collection("grupos").document(idGrupo));
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario " + uid + " salió del grupo " + idGrupo);
                    quitarParticipanteDeTeseractosDelGrupo(idGrupo, uid, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void quitarParticipanteDeTeseractosDelGrupo(String idGrupo, String uid,
                                                         FirestoreCallback<Void> callback) {
        db.collection("teseractos")
                .whereEqualTo("idGrupo", idGrupo)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        if (callback != null) callback.onSuccess(null);
                        return;
                    }
                    WriteBatch batch = db.batch();
                    snapshots.forEach(doc ->
                            batch.update(doc.getReference(), "participantes",
                                    FieldValue.arrayRemove(uid)));
                    batch.commit()
                            .addOnSuccessListener(v -> {
                                if (callback != null) callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al quitar participante de teseractos", e);
                                if (callback != null) callback.onSuccess(null); 
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar teseractos del grupo", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    private void eliminarGrupoCompleto(String idGrupo, FirestoreCallback<Void> callback) {
        db.collection("teseractos")
                .whereEqualTo("idGrupo", idGrupo)
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> ids = new ArrayList<>();
                    snap.forEach(d -> ids.add(d.getId()));
                    eliminarTeseractosSecuencial(ids, 0, idGrupo, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void eliminarTeseractosSecuencial(List<String> ids, int idx, String idGrupo,
                                               FirestoreCallback<Void> callback) {
        if (idx >= ids.size()) {
            eliminarInvitacionesYGrupo(idGrupo, callback);
            return;
        }
        String idT = ids.get(idx);
        borrarSubcoleccion(idT, "valoraciones", () ->
                borrarSubcoleccion(idT, "mensajes", () ->
                        db.collection("teseractos").document(idT).delete()
                                .addOnSuccessListener(v ->
                                        eliminarTeseractosSecuencial(ids, idx + 1, idGrupo, callback))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage())),
                        callback),
                callback);
    }

    private void borrarSubcoleccion(String idTeseracto, String subcol,
                                     Runnable onDone, FirestoreCallback<Void> onError) {
        db.collection("teseractos").document(idTeseracto).collection(subcol).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) { onDone.run(); return; }
                    WriteBatch b = db.batch();
                    snap.forEach(d -> b.delete(d.getReference()));
                    b.commit()
                            .addOnSuccessListener(v -> onDone.run())
                            .addOnFailureListener(e -> onError.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> onError.onFailure(e.getMessage()));
    }

    private void eliminarInvitacionesYGrupo(String idGrupo, FirestoreCallback<Void> callback) {
        db.collectionGroup("invitacionesGrupo")
                .whereEqualTo("idGrupo", idGrupo)
                .get()
                .addOnSuccessListener(snap -> {
                    WriteBatch b = db.batch();
                    snap.forEach(d -> b.delete(d.getReference()));
                    b.delete(db.collection(COLLECTION_GRUPOS).document(idGrupo));
                    b.commit()
                            .addOnSuccessListener(v -> {
                                Log.d(TAG, "Grupo " + idGrupo + " eliminado completamente");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Genera un nuevo código de invitación aleatorio y lo persiste en el documento del grupo.
     *
     * @param idGrupo  identificador del grupo
     * @param callback resultado: el nuevo código generado o un error
     */
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