package com.example.openteseractus.modelos;

import java.util.ArrayList;
import java.util.List;

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

    // ==================== MÉTODOS DE NEGOCIO ====================

    public void actualizarNotaMedia(double nuevaPuntuacion) {
        double total = this.notaMedia * this.numValoraciones;
        total += nuevaPuntuacion;

        this.numValoraciones++;
        this.notaMedia = total / this.numValoraciones;
    }

    public void actualizarActividad() {
        this.ultimaActividad = System.currentTimeMillis();
    }

    public boolean perteneceAGrupo(String idGrupo) {
        return this.idGrupo != null && this.idGrupo.equals(idGrupo);
    }

    // ==================== GETTERS Y SETTERS ====================

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