package com.example.openteseractus.modelos;

public class Mensaje {

    private String id;
    private String uidAutor;
    private String idTeseracto;
    private String contenido;
    private long fechaEnvio;

    public Mensaje() {}

    public Mensaje(String id, String uidAutor, String idTeseracto, String contenido) {
        this.id = id;
        this.uidAutor = uidAutor;
        this.idTeseracto = idTeseracto;
        this.contenido = contenido;
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    public boolean esValido() {
        return contenido != null && !contenido.trim().isEmpty();
    }

    public boolean esAutor(String uid) {
        return this.uidAutor != null && this.uidAutor.equals(uid);
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUidAutor() { return uidAutor; }
    public void setUidAutor(String uidAutor) { this.uidAutor = uidAutor; }

    public String getIdTeseracto() { return idTeseracto; }
    public void setIdTeseracto(String idTeseracto) { this.idTeseracto = idTeseracto; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public long getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(long fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}