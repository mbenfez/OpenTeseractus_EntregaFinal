package com.example.openteseractus.modelos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Representa a un usuario registrado en la aplicación.
 * Se persiste en la colección {@code usuarios} de Firestore y se autentica
 * mediante Firebase Authentication.
 */
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

    /**
     * Crea un usuario activo con los datos mínimos y registra la fecha de alta.
     *
     * @param uid             UID de Firebase Authentication
     * @param username        nombre de usuario público
     * @param fechaNacimiento fecha de nacimiento para calcular la edad
     */
    public Usuario(String uid, String username, Date fechaNacimiento) {
        this.uid = uid;
        this.username = username;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaRegistro = System.currentTimeMillis();
        this.activo = true;
    }

    /**
     * Calcula la edad actual del usuario en años completos.
     *
     * @return edad en años, o {@code 0} si la fecha de nacimiento es nula
     */
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

    /**
     * Indica si el usuario tiene 18 años o más.
     *
     * @return {@code true} si la edad calculada es &ge; 18
     */
    public boolean esMayorDeEdad() {
        return getEdad() >= 18;
    }

    /**
     * Valida el formato del nombre de usuario:
     * entre 3 y 20 caracteres, debe empezar por letra y solo puede contener
     * letras, dígitos y guiones bajos.
     *
     * @return {@code true} si el nombre de usuario es válido
     */
    public boolean validarUsername() {
        if (username == null || username.trim().isEmpty()) return false;
        if (username.length() < 3 || username.length() > 20) return false;
        if (Character.isDigit(username.charAt(0))) return false;
        return username.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

    /** Marca al usuario como inactivo (p.ej. al cerrar sesión o eliminar la cuenta). */
    public void desactivar() {
        this.activo = false;
    }

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