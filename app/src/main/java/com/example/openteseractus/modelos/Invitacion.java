package com.example.openteseractus.modelos;

/**
 * Representa una invitación para que un usuario se una a un grupo.
 * Se almacena en la subcolección {@code invitacionesGrupo} del usuario invitado.
 */
public class Invitacion {

    private String id;
    private String idGrupo;

    private String uidInvitado;
    private String uidInvita;

    private String nombreGrupo;
    private String nombreUsuario;

    private long fecha;

    public Invitacion() {}

    /**
     * Crea una invitación con todos sus datos y registra el momento de creación.
     *
     * @param id           identificador único de la invitación
     * @param idGrupo      identificador del grupo al que se invita
     * @param uidInvitado  UID del usuario que recibe la invitación
     * @param uidInvita    UID del usuario que envía la invitación
     * @param nombreGrupo  nombre del grupo (para mostrar en la notificación)
     */
    public Invitacion( String id, String idGrupo, String uidInvitado, String uidInvita, String nombreGrupo) {
        this.id = id;
        this.idGrupo = idGrupo;

        this.uidInvitado = uidInvitado;
        this.uidInvita = uidInvita;

        this.nombreGrupo = nombreGrupo;

        this.fecha = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getIdGrupo() { return idGrupo; }
    public String getUidInvitado() { return uidInvitado; }
    public String getUidInvita() { return uidInvita; }
    public String getNombreGrupo() { return nombreGrupo; }
    public long getFecha() { return fecha; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}