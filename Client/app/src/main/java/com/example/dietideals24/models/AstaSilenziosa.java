package com.example.dietideals24.models;

import com.example.dietideals24.enums.Categoria;

public class AstaSilenziosa extends Asta {

    private String scadenza;

    public AstaSilenziosa(int idCreatore, String nome, String descrizione, Categoria categoria, byte[] foto, String scadenza) {
        super(idCreatore, nome, descrizione, categoria, foto);
        this.scadenza = scadenza;
    }

    public AstaSilenziosa() {}

    public String getScadenza() {
        return scadenza;
    }

    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
    }

}

