package com.example.dietideals24.models;

import com.example.dietideals24.enums.StatoOfferta;

public class Offerta {
    private int id;
    private int idUtente;
    private int idAsta;
    private float valore;
    private String data;
    private String offerente;
    private StatoOfferta stato;

    public Offerta(int idUtente, int idAsta, float valore, String data) {
        this.idUtente = idUtente;
        this.idAsta = idAsta;
        this.valore = valore;
        this.data = data;
    }

    public Offerta() {

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

    public int getIdAsta() {
        return idAsta;
    }

    public void setIdAsta(int idAsta) {
        this.idAsta = idAsta;
    }

    public float getValore() {
        return valore;
    }

    public void setValore(float valore) {
        this.valore = valore;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOfferente() {
        return offerente;
    }

    public void setOfferente(String offerente) {
        this.offerente = offerente;
    }

    public StatoOfferta getStato() {
        return stato;
    }

    public void setStato(StatoOfferta stato) {
        this.stato = stato;
    }

}
