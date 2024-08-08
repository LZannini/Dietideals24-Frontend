package com.example.dietideals24.models;

import java.time.LocalDateTime;

public class Notifica {

    private int id;
    private int id_utente;
    private String testo;
    private LocalDateTime data;
    private int id_asta;
    private boolean letta;

    public Notifica(int id, int id_utente, String testo, LocalDateTime data, int idAsta, boolean letta) {
        this.id = id;
        this.id_utente = id_utente;
        this.testo = testo;
        this.data = data;
        id_asta = idAsta;
        this.letta = letta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_utente() {
        return id_utente;
    }

    public void setId_utente(int id_utente) {
        this.id_utente = id_utente;
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

    public int getId_asta() {
        return id_asta;
    }

    public void setId_asta(int id_asta) {
        this.id_asta = id_asta;
    }
}