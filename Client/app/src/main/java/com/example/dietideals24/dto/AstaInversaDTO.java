package com.example.dietideals24.dto;

import com.example.dietideals24.enums.Categoria;
import com.example.dietideals24.enums.StatoAsta;

public class AstaInversaDTO {

    private int idAsta;
    private int idCreatore;
    private String nome;
    private String descrizione;
    private Categoria categoria;
    private byte[] foto;
    private float prezzo;
    private Float offertaMinore;
    private String scadenza;
    private StatoAsta stato;

    public int getIdAsta() {
        return idAsta;
    }

    public void setIdAsta(int idAsta) {
        this.idAsta = idAsta;
    }

    public int getIdCreatore() {
        return idCreatore;
    }

    public void setIdCreatore(int idCreatore) {
        this.idCreatore = idCreatore;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public float getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(float prezzo) {
        this.prezzo = prezzo;
    }

    public String getScadenza() {
        return scadenza;
    }

    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
    }

    public StatoAsta getStato() {
        return stato;
    }

    public void setStato(StatoAsta stato) {
        this.stato = stato;
    }

    public Float getOffertaMinore() {
        return offertaMinore;
    }

    public void setOffertaMinore(Float offertaMinore) {
        this.offertaMinore = offertaMinore;
    }
}
