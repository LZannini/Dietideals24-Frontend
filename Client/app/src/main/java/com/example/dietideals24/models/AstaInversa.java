package com.example.dietideals24.models;

import com.example.dietideals24.enums.Categoria;

public class AstaInversa extends Asta {

        private float prezzo;
        private Float offertaMinore;
        private String scadenza;

        public AstaInversa(int idCreatore, String nome, String descrizione, Categoria categoria, byte[] foto, float prezzo, String scadenza) {
            super(idCreatore, nome, descrizione, categoria, foto);
            this.prezzo = prezzo;
            this.scadenza = scadenza;
        }

        public AstaInversa() {

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

        public Float getOffertaMinore() {
            return offertaMinore;
        }

        public void setOffertaMinore(Float offertaMinore) {
            this.offertaMinore = offertaMinore;
        }
}
