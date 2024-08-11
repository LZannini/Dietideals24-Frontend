package com.example.dietideals24.models;

import com.example.dietideals24.enums.Categoria;

import java.time.LocalDateTime;

public class Asta_Silenziosa extends Asta {

    private String scadenza;

    public Asta_Silenziosa(int idCreatore, String nome, String descrizione, Categoria categoria, byte[] foto, String scadenza) {
        super(idCreatore, nome, descrizione, categoria, foto);;
        this.scadenza = scadenza;
    }

    public Asta_Silenziosa() {}

    public String getScadenza() {
        return scadenza;
    }

    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
    }

}

