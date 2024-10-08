package com.example.dietideals24.models;

import com.example.dietideals24.enums.Categoria;

public class AstaRibasso extends Asta {

    private float prezzo;
    private String timer;
    private float decremento;
    private float minimo;

    public AstaRibasso(int idCreatore, String nome, String descrizione, Categoria categoria, byte[] foto, float prezzo, String timer, float decremento, float minimo) {
        super(idCreatore, nome, descrizione, categoria, foto);
        this.prezzo = prezzo;
        this.timer = timer;
        this.decremento = decremento;
        this.minimo = minimo;
    }

    public AstaRibasso() {}

    public float getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(float prezzo) {
        this.prezzo = prezzo;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public float getDecremento() {
        return decremento;
    }

    public void setDecremento(float decremento) {
        this.decremento = decremento;
    }

    public float getMinimo() {
        return minimo;
    }

    public void setMinimo(float minimo) {
        this.minimo = minimo;
    }

}