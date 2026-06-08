package com.example.openteseractus.modelos;

/**
 * Representa un mensaje de texto enviado dentro del chat de un teseracto.
 * Se almacena en la subcolección {@code mensajes} del documento del teseracto en Firestore.
 */
public class Mensaje {

    private String id;
    private String uidAutor;
    private String idTeseracto;
    private String contenido;
    private long fechaEnvio;

    public Mensaje() {}

    /**
     * Crea un mensaje con su contenido y referencias al autor y al teseracto.
     *
     * @param id           identificador único del mensaje
     * @param uidAutor     UID del usuario que envía el mensaje
     * @param idTeseracto  identificador del teseracto al que pertenece el chat
     * @param contenido    texto del mensaje
     */
    public Mensaje(String id, String uidAutor, String idTeseracto, String contenido) {
        this.id = id;
        this.uidAutor = uidAutor;
        this.idTeseracto = idTeseracto;
        this.contenido = contenido;
    }

    /**
     * Comprueba que el mensaje tiene contenido no vacío antes de enviarlo.
     *
     * @return {@code true} si el contenido no es nulo ni está en blanco
     */
    public boolean esValido() {
        return contenido != null && !contenido.trim().isEmpty();
    }

    /**
     * Indica si el UID dado es el autor de este mensaje.
     * Se usa en el adapter para distinguir mensajes enviados de recibidos.
     *
     * @param uid UID del usuario a comparar
     * @return {@code true} si el usuario es el autor
     */
    public boolean esAutor(String uid) {
        return this.uidAutor != null && this.uidAutor.equals(uid);
    }

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