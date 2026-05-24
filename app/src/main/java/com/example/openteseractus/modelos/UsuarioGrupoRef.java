package com.example.openteseractus.modelos;

public class UsuarioGrupoRef {

    private String idGrupo;
    private String nombre;
    private String fotoUrl;
    private long ultimaActividad;

    public UsuarioGrupoRef() {}

    public UsuarioGrupoRef(String idGrupo, String nombre) {
        this.idGrupo = idGrupo;
        this.nombre = nombre;
        this.ultimaActividad = System.currentTimeMillis();
    }

    public String getIdGrupo() { return idGrupo; }
    public void setIdGrupo(String idGrupo) { this.idGrupo = idGrupo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public long getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(long ultimaActividad) { this.ultimaActividad = ultimaActividad; }
}