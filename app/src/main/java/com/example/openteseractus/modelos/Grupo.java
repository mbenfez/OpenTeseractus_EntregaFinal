package com.example.openteseractus.modelos;

import java.util.Random;

public class Grupo {

    private String id;
    private String uidCreador;
    private String nomGrupo;
    private String fotoGrupoUrl;
    private String codInvitacion;
    private long fechaCreacion;

    public Grupo() {}

    public Grupo(String id, String uidCreador, String nomGrupo) {
        this.id = id;
        this.uidCreador = uidCreador;
        this.nomGrupo = nomGrupo;
        this.fechaCreacion = System.currentTimeMillis();
        this.codInvitacion = generarCodigoInvitacion();
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    public String generarCodigoInvitacion() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            codigo.append(chars.charAt(random.nextInt(chars.length())));
        }
        return codigo.toString();
    }

    public void regenerarCodigo() {
        this.codInvitacion = generarCodigoInvitacion();
    }

    public boolean validarNombreGrupo() {
        if (nomGrupo == null || nomGrupo.trim().isEmpty()) return false;
        return nomGrupo.length() >= 3 && nomGrupo.length() <= 50;
    }

    public boolean esCreador(String uid) {
        return this.uidCreador != null && this.uidCreador.equals(uid);
    }

    // ==================== GETTERS Y SETTERS ====================

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
