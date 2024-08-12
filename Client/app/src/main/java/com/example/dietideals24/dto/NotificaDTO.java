package com.example.dietideals24.dto;

import java.io.Serializable;

public class NotificaDTO implements Serializable {

    private int id;
    private int idUtente;
    private String testo;
    private String data;
    private int idAsta;
    private String nomeAsta;
    private boolean letta;

    // Getters and Setters

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
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

    public String getNomeAsta() {
        return nomeAsta;
    }

    public void setNomeAsta(String nome_asta) {
        this.nomeAsta = nome_asta;
    }
}
