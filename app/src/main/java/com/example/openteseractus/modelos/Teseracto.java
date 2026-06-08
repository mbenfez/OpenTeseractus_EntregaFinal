package com.example.openteseractus.modelos;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un teseracto: la entrada de una película o serie dentro de un grupo.
 * Contiene la referencia a TMDB, la nota media calculada a partir de las valoraciones
 * de los miembros y la lista de participantes que han enviado mensajes en su chat.
 */
public class Teseracto {

    private String id;
    private String idGrupo;
    private String uidAbiertoPor;

    private int tmdbId;
    private String titulo;
    private String mediaType;
    private String posterUrl;

    private int numValoraciones;
    private double notaMedia;

    private long fechaApertura;
    private long ultimaActividad;
    private List<String> participantes = new ArrayList<>();

    public Teseracto() {}

    /**
     * Crea un teseracto con sus datos de identificación y lo inicializa sin valoraciones.
     *
     * @param id           identificador único del teseracto
     * @param idGrupo      identificador del grupo al que pertenece
     * @param uidAbiertoPor UID del usuario que crea el teseracto
     * @param tmdbId       identificador de la película o serie en TMDB
     * @param titulo       título de la película o serie
     */
    public Teseracto(String id, String idGrupo, String uidAbiertoPor, int tmdbId, String titulo) {
        this.id = id;
        this.idGrupo = idGrupo;
        this.uidAbiertoPor = uidAbiertoPor;
        this.tmdbId = tmdbId;
        this.titulo = titulo;

        this.fechaApertura = System.currentTimeMillis();
        this.ultimaActividad = this.fechaApertura;

        this.numValoraciones = 0;
        this.notaMedia = 0.0;
    }

    /**
     * Incorpora una nueva puntuación a la nota media del teseracto usando
     * la fórmula de media incremental: {@code (mediaActual * n + nueva) / (n + 1)}.
     *
     * @param nuevaPuntuacion puntuación a añadir (0–10)
     */
    public void actualizarNotaMedia(double nuevaPuntuacion) {
        double total = this.notaMedia * this.numValoraciones;
        total += nuevaPuntuacion;

        this.numValoraciones++;
        this.notaMedia = total / this.numValoraciones;
    }

    /** Actualiza el timestamp de última actividad al momento actual. */
    public void actualizarActividad() {
        this.ultimaActividad = System.currentTimeMillis();
    }

    /**
     * Comprueba si este teseracto pertenece al grupo indicado.
     *
     * @param idGrupo identificador del grupo a verificar
     * @return {@code true} si el teseracto es de ese grupo
     */
    public boolean perteneceAGrupo(String idGrupo) {
        return this.idGrupo != null && this.idGrupo.equals(idGrupo);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdGrupo() { return idGrupo; }
    public void setIdGrupo(String idGrupo) { this.idGrupo = idGrupo; }

    public String getUidAbiertoPor() { return uidAbiertoPor; }
    public void setUidAbiertoPor(String uidAbiertoPor) { this.uidAbiertoPor = uidAbiertoPor; }

    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public int getNumValoraciones() { return numValoraciones; }
    public void setNumValoraciones(int numValoraciones) { this.numValoraciones = numValoraciones; }

    public double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(double notaMedia) { this.notaMedia = notaMedia; }

    public long getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(long fechaApertura) { this.fechaApertura = fechaApertura; }

    public long getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(long ultimaActividad) { this.ultimaActividad = ultimaActividad; }

    public List<String> getParticipantes() { return participantes; }
    public void setParticipantes(List<String> participantes) { this.participantes = participantes; }
}