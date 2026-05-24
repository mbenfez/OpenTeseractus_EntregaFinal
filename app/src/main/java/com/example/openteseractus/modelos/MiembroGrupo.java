package com.example.openteseractus.modelos;

public class MiembroGrupo {

    private String uidMiembro;
    private String rol;
    private int cantTeseractos;
    private int cantValoraciones;
    private long fechaUnion;

    public MiembroGrupo() {}

    public MiembroGrupo(String uidMiembro, String rol) {
        this.uidMiembro = uidMiembro;
        this.rol = rol;
        this.cantTeseractos = 0;
        this.cantValoraciones = 0;
        this.fechaUnion = System.currentTimeMillis();
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    public boolean esAdmin() {
        return "admin".equalsIgnoreCase(rol);
    }

    public void incrementarTeseractos() {
        this.cantTeseractos++;
    }

    public void incrementarValoraciones() {
        this.cantValoraciones++;
    }

    public void ascenderAAdmin() {
        this.rol = "admin";
    }

    public void degradarAMiembro() {
        this.rol = "miembro";
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getUidMiembro() { return uidMiembro; }
    public void setUidMiembro(String uidMiembro) { this.uidMiembro = uidMiembro; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public int getCantTeseractos() { return cantTeseractos; }
    public void setCantTeseractos(int cantTeseractos) { this.cantTeseractos = cantTeseractos; }

    public int getCantValoraciones() { return cantValoraciones; }
    public void setCantValoraciones(int cantValoraciones) { this.cantValoraciones = cantValoraciones; }

    public long getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(long fechaUnion) { this.fechaUnion = fechaUnion; }
}