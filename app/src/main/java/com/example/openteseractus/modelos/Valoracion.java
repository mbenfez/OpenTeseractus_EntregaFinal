package com.example.openteseractus.modelos;

/**
 * Representa la valoración que un usuario realiza sobre un teseracto.
 * Incluye una puntuación general (0–10) y, opcionalmente, puntuaciones detalladas
 * por actuaciones, guion y dirección. Se almacena en la subcolección
 * {@code valoraciones} del teseracto, usando el UID del usuario como ID de documento
 * para que cada usuario solo pueda tener una valoración activa por teseracto.
 */
public class Valoracion {

    private String uidValoradoPor;
    private String idTeseracto;

    private double puntuacion;
    private double puntuacionActores;
    private double puntuacionGuion;
    private double puntuacionDireccion;

    private long fechaValoracion;

    public Valoracion() {}

    /**
     * Crea una valoración con puntuación general y registra el momento de valoración.
     *
     * @param uidValoradoPor UID del usuario que valora
     * @param idTeseracto    identificador del teseracto valorado
     * @param puntuacion     puntuación general entre 0 y 10
     */
    public Valoracion(String uidValoradoPor, String idTeseracto, double puntuacion) {
        this.uidValoradoPor = uidValoradoPor;
        this.idTeseracto = idTeseracto;
        this.puntuacion = puntuacion;
        this.fechaValoracion = System.currentTimeMillis();
    }

    /**
     * Comprueba que la puntuación general está dentro del rango permitido.
     *
     * @return {@code true} si la puntuación está entre 0 y 10 (ambos inclusive)
     */
    public boolean esValida() {
        return puntuacion >= 0 && puntuacion <= 10;
    }

    /**
     * Calcula la media aritmética de las tres puntuaciones detalladas
     * (actores, guion y dirección).
     *
     * @return media de las puntuaciones de detalle
     */
    public double calcularMediaDetalle() {
        return (puntuacionActores + puntuacionGuion + puntuacionDireccion) / 3.0;
    }

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