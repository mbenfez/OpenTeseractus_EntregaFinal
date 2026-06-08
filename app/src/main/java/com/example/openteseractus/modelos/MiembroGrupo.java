package com.example.openteseractus.modelos;

/**
 * Representa la pertenencia de un usuario a un grupo, incluyendo su rol y estadísticas de participación.
 * Se almacena en la subcolección {@code miembros} del documento de grupo en Firestore.
 * Los valores de rol reconocidos son {@code "admin"} y {@code "miembro"}.
 */
public class MiembroGrupo {

    private String uidMiembro;
    private String rol;
    private int cantTeseractos;
    private int cantValoraciones;
    private long fechaUnion;

    public MiembroGrupo() {}

    /**
     * Crea un miembro con su UID y rol, inicializando contadores a cero y
     * registrando la fecha de unión al grupo.
     *
     * @param uidMiembro UID del usuario
     * @param rol        rol del miembro: {@code "admin"} o {@code "miembro"}
     */
    public MiembroGrupo(String uidMiembro, String rol) {
        this.uidMiembro = uidMiembro;
        this.rol = rol;
        this.cantTeseractos = 0;
        this.cantValoraciones = 0;
        this.fechaUnion = System.currentTimeMillis();
    }

    /**
     * Indica si este miembro tiene rol de administrador.
     *
     * @return {@code true} si el rol es {@code "admin"} (insensible a mayúsculas)
     */
    public boolean esAdmin() {
        return "admin".equalsIgnoreCase(rol);
    }

    /** Incrementa en uno el contador de teseractos abiertos por este miembro. */
    public void incrementarTeseractos() {
        this.cantTeseractos++;
    }

    /** Incrementa en uno el contador de valoraciones realizadas por este miembro. */
    public void incrementarValoraciones() {
        this.cantValoraciones++;
    }

    /** Cambia el rol de este miembro a {@code "admin"}. */
    public void ascenderAAdmin() {
        this.rol = "admin";
    }

    /** Cambia el rol de este miembro a {@code "miembro"}. */
    public void degradarAMiembro() {
        this.rol = "miembro";
    }

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