package com.example.openteseractus.modelos;

import java.util.Random;

/**
 * Representa un grupo de usuarios dentro de la aplicación.
 * Un grupo agrupa a varios usuarios que comparten teseractos y pueden invitar a otros
 * mediante un código alfanumérico de 6 caracteres.
 */
public class Grupo {

    private String id;
    private String uidCreador;
    private String nomGrupo;
    private String fotoGrupoUrl;
    private String codInvitacion;
    private long fechaCreacion;

    public Grupo() {}

    /**
     * Crea un grupo con los datos básicos y genera automáticamente la fecha de creación
     * y un código de invitación aleatorio.
     *
     * @param id          identificador único del grupo
     * @param uidCreador  UID del usuario que crea el grupo
     * @param nomGrupo    nombre visible del grupo
     */
    public Grupo(String id, String uidCreador, String nomGrupo) {
        this.id = id;
        this.uidCreador = uidCreador;
        this.nomGrupo = nomGrupo;
        this.fechaCreacion = System.currentTimeMillis();
        this.codInvitacion = generarCodigoInvitacion();
    }

    /**
     * Genera un código de invitación aleatorio de 6 caracteres alfanuméricos en mayúsculas.
     *
     * @return el código generado
     */
    public String generarCodigoInvitacion() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            codigo.append(chars.charAt(random.nextInt(chars.length())));
        }
        return codigo.toString();
    }

    /** Reemplaza el código de invitación actual por uno nuevo generado aleatoriamente. */
    public void regenerarCodigo() {
        this.codInvitacion = generarCodigoInvitacion();
    }

    /**
     * Valida que el nombre del grupo no esté vacío y tenga entre 3 y 50 caracteres.
     *
     * @return {@code true} si el nombre es válido
     */
    public boolean validarNombreGrupo() {
        if (nomGrupo == null || nomGrupo.trim().isEmpty()) return false;
        return nomGrupo.length() >= 3 && nomGrupo.length() <= 50;
    }

    /**
     * Comprueba si el UID dado corresponde al creador de este grupo.
     *
     * @param uid UID del usuario a comprobar
     * @return {@code true} si el usuario es el creador
     */
    public boolean esCreador(String uid) {
        return this.uidCreador != null && this.uidCreador.equals(uid);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUidCreador() { return uidCreador; }
    public void setUidCreador(String uidCreador) { this.uidCreador = uidCreador; }

    public String getNomGrupo() { return nomGrupo; }
    public void setNomGrupo(String nomGrupo) { this.nomGrupo = nomGrupo; }

    public String getFotoGrupoUrl() { return fotoGrupoUrl; }
    public void setFotoGrupoUrl(String fotoGrupoUrl) { this.fotoGrupoUrl = fotoGrupoUrl; }

    public String getCodInvitacion() { return codInvitacion; }
    public void setCodInvitacion(String codInvitacion) { this.codInvitacion = codInvitacion; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

}