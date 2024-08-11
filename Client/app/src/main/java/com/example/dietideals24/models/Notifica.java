package com.example.dietideals24.models;

import java.time.LocalDateTime;

public class Notifica {

    private int id;
    private int idUtente;
    private String testo;
    private LocalDateTime data;
    private int idAsta;
    private boolean letta;

    public Notifica(int id, int idUtente, String testo, LocalDateTime data, int idAsta, boolean letta) {
        this.id = id;
        this.idUtente = idUtente;
        this.testo = testo;
        this.data = data;
        this.idAsta = idAsta;
        this.letta = letta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public boolean isLetta() {
        return letta;
    }

    public void setLetta(boolean letta) {
        this.letta = letta;
    }

    public int getIdAsta() {
        return idAsta;
    }

    public void setIdAsta(int idAsta) {
        this.idAsta = idAsta;
    }
}