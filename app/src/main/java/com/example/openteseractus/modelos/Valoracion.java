package com.example.openteseractus.modelos;

public class Valoracion {

    private String uidValoradoPor;
    private String idTeseracto;

    private double puntuacion;
    private double puntuacionActores;
    private double puntuacionGuion;
    private double puntuacionDireccion;

    private long fechaValoracion;

    public Valoracion() {}

    public Valoracion(String uidValoradoPor, String idTeseracto, double puntuacion) {
        this.uidValoradoPor = uidValoradoPor;
        this.idTeseracto = idTeseracto;
        this.puntuacion = puntuacion;
        this.fechaValoracion = System.currentTimeMillis();
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    public boolean esValida() {
        return puntuacion >= 0 && puntuacion <= 10;
    }

    public double calcularMediaDetalle() {
        return (puntuacionActores + puntuacionGuion + puntuacionDireccion) / 3.0;
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getUidValoradoPor() { return uidValoradoPor; }
    public void setUidValoradoPor(String uidValoradoPor) { this.uidValoradoPor = uidValoradoPor; }

    public String getIdTeseracto() { return idTeseracto; }
    public void setIdTeseracto(String idTeseracto) { this.idTeseracto = idTeseracto; }

    public double getPuntuacion() { return puntuacion; }
    public void setPuntuacion(double puntuacion) { this.puntuacion = puntuacion; }

    public double getPuntuacionActores() { return puntuacionActores; }
    public void setPuntuacionActores(double puntuacionActores) { this.puntuacionActores = puntuacionActores; }

    public double getPuntuacionGuion() { return puntuacionGuion; }
    public void setPuntuacionGuion(double puntuacionGuion) { this.puntuacionGuion = puntuacionGuion; }

    public double getPuntuacionDireccion() { return puntuacionDireccion; }
    public void setPuntuacionDireccion(double puntuacionDireccion) { this.puntuacionDireccion = puntuacionDireccion; }

    public long getFechaValoracion() { return fechaValoracion; }
    public void setFechaValoracion(long fechaValoracion) { this.fechaValoracion = fechaValoracion; }
}