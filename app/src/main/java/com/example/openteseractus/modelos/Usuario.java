package com.example.openteseractus.modelos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Usuario {

    private String uid;
    private String email;
    private String username;
    private Date fechaNacimiento;
    private String genero;
    private String fotoPerfilUrl;
    private long fechaRegistro;
    private boolean activo;

    public Usuario() {}

    public Usuario(String uid, String username, Date fechaNacimiento) {
        this.uid = uid;
        this.username = username;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaRegistro = System.currentTimeMillis();
        this.activo = true;
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    public int getEdad() {
        if (fechaNacimiento == null) return 0;

        Calendar hoy = Calendar.getInstance();
        Calendar nacimiento = Calendar.getInstance();
        nacimiento.setTime(fechaNacimiento);

        int edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad;
    }

    public boolean esMayorDeEdad() {
        return getEdad() >= 18;
    }

    public boolean validarUsername() {
        if (username == null || username.trim().isEmpty()) return false;
        if (username.length() < 3 || username.length() > 20) return false;
        if (Character.isDigit(username.charAt(0))) return false;
        return username.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

    public void desactivar() {
        this.activo = false;
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }

    public long getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(long fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

}
