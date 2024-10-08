package com.example.dietideals24;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.AstaRibassoDTO;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class CreaAstaRibassoActivity extends AppCompatActivity {

    private Utente utente;
    private Asta asta;
    private boolean fromHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_asta_ribasso);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        fromHome = getIntent().getBooleanExtra("fromHome", true);
        asta = (Asta) getIntent().getSerializableExtra("asta");
        utente = (Utente) getIntent().getSerializableExtra("utente");

        EditText prezzoIniziale = findViewById(R.id.prezzo_iniziale);
        ImageButton decrPrezzoIniziale = findViewById(R.id.decr_prezzo_iniziale);
        ImageButton incrPrezzoIniziale = findViewById(R.id.incr_prezzo_iniziale);
        EditText prezzoMinimo = findViewById(R.id.prezzo_minimo);
        ImageButton decrPrezzoMinimo = findViewById(R.id.decr_prezzo_minimo);
        ImageButton incrPrezzoMinimo = findViewById(R.id.incr_prezzo_minimo);
        EditText decrementoPrezzo = findViewById(R.id.decremento);
        ImageButton decrDecremento = findViewById(R.id.decr_decremento);
        ImageButton incrDecremento = findViewById(R.id.incr_decremento);

        TextView oreTextView = findViewById(R.id.ore_text_view);
        ImageButton decrOre = findViewById(R.id.decr_ore);
        ImageButton incrOre = findViewById(R.id.incr_ore);
        TextView minutiTextView = findViewById(R.id.minuti_text_view);
        ImageButton decrMinuti = findViewById(R.id.decr_minuti);
        ImageButton incrMinuti = findViewById(R.id.incr_minuti);
        TextView secondiTextView = findViewById(R.id.secondi_text_view);
        ImageButton decrSecondi = findViewById(R.id.decr_secondi);
        ImageButton incrSecondi = findViewById(R.id.incr_secondi);

        ImageButton backButton = findViewById(R.id.back_button);
        Button creaButton = findViewById(R.id.crea_button);
        ImageButton homeButton = findViewById(R.id.home_button);

        valoreInizialeFormattato(prezzoIniziale, 1.00f);
        valoreInizialeFormattato(prezzoMinimo, 1.00f);
        valoreInizialeFormattato(decrementoPrezzo, 1.00f);

        incrOre.setOnClickListener(v -> aggiornaTimer(oreTextView, 1));
        decrOre.setOnClickListener(v -> aggiornaTimer(oreTextView, -1));
        incrMinuti.setOnClickListener(v -> aggiornaTimer(minutiTextView, 1));
        decrMinuti.setOnClickListener(v -> aggiornaTimer(minutiTextView, -1));
        incrSecondi.setOnClickListener(v -> aggiornaTimer(secondiTextView, 1));
        decrSecondi.setOnClickListener(v -> aggiornaTimer(secondiTextView, -1));

        backButton.setOnClickListener(v -> {
            openActivityTipoAsta(utente, asta);
            finish();
        });

        homeButton.setOnClickListener(v -> {
            openActivityHome(utente);
            finish();
        });

        decrPrezzoIniziale.setOnClickListener(view -> {
            float prezzoStandardIniziale = getValueFromEditText(prezzoIniziale);
            if (prezzoStandardIniziale > 1.00f) {
                prezzoStandardIniziale -= 1.00f;
                valoreInizialeFormattato(prezzoIniziale, prezzoStandardIniziale);
            }
        });

        incrPrezzoIniziale.setOnClickListener(view -> {
            float prezzoStandardIniziale = getValueFromEditText(prezzoIniziale);
            prezzoStandardIniziale += 1.00f;
            valoreInizialeFormattato(prezzoIniziale, prezzoStandardIniziale);
        });

        decrPrezzoMinimo.setOnClickListener(view -> {
            float prezzoStandardMinimo = getValueFromEditText(prezzoMinimo);
            if (prezzoStandardMinimo > 1.00f) {
                prezzoStandardMinimo -= 1.00f;
                valoreInizialeFormattato(prezzoMinimo, prezzoStandardMinimo);
            }
        });

        incrPrezzoMinimo.setOnClickListener(view -> {
            float prezzoStandardMinimo = getValueFromEditText(prezzoMinimo);
            prezzoStandardMinimo += 1.00f;
            valoreInizialeFormattato(prezzoMinimo, prezzoStandardMinimo);
        });

        decrDecremento.setOnClickListener(view -> {
            float decrementoStandard = getValueFromEditText(decrementoPrezzo);
            if (decrementoStandard > 1.00f) {
                decrementoStandard -= 1.00f;
                valoreInizialeFormattato(decrementoPrezzo, decrementoStandard);
            }
        });

        incrDecremento.setOnClickListener(view -> {
            float decrementoStandard = getValueFromEditText(decrementoPrezzo);
            decrementoStandard += 1.00f;
            valoreInizialeFormattato(decrementoPrezzo, decrementoStandard);
        });

        creaButton.setOnClickListener(view -> {
            String time = String.format("%02d:%02d:%02d",
                    Integer.parseInt(oreTextView.getText().toString()),
                    Integer.parseInt(minutiTextView.getText().toString()),
                    Integer.parseInt(secondiTextView.getText().toString()));
            float valIniziale = getValueFromEditText(prezzoIniziale);
            float valMinimo = getValueFromEditText(prezzoMinimo);
            float valDecremento = getValueFromEditText(decrementoPrezzo);

            if (valDecremento < valIniziale && valMinimo < valIniziale) {
                AstaRibassoDTO astaR = new AstaRibassoDTO();
                astaR.setIdCreatore(asta.getIdCreatore());
                astaR.setNome(asta.getNome());
                astaR.setDescrizione(asta.getDescrizione());
                astaR.setFoto(asta.getFoto());
                astaR.setPrezzo(valIniziale);
                astaR.setCategoria(asta.getCategoria());
                astaR.setDecremento(valDecremento);
                astaR.setMinimo(valMinimo);
                astaR.setTimer(time);

                ApiService apiService = RetrofitService.getRetrofit(CreaAstaRibassoActivity.this).create(ApiService.class);

                apiService.creaAstaAlRibasso(astaR)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(CreaAstaRibassoActivity.this, "Asta creata con successo!", Toast.LENGTH_SHORT).show();
                                    openActivityHome(utente);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(CreaAstaRibassoActivity.this, "Errore durante la creazione dell'asta!", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                new AlertDialog.Builder(CreaAstaRibassoActivity.this)
                        .setMessage("Errore, importi non validi.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {})
                        .create()
                        .show();
            }
        });
    }

    private float getValueFromEditText(EditText editText) {
        String cleanString = editText.getText().toString().replaceAll("[€,.\\s]", "").trim();
        float value;
        try {
            value = Float.parseFloat(cleanString);
        } catch (NumberFormatException e) {
            value = 0.00f;
        }
        return value / 100;
    }

    private void valoreInizialeFormattato(EditText editText, float val) {
        String valFormattato = NumberFormat.getCurrencyInstance(Locale.ITALY).format(val);
        editText.setText(valFormattato);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openActivityTipoAsta(utente, asta);
        finish();
    }

    private void openActivityTipoAsta(Utente utente, Asta asta) {
        Intent intent = new Intent(this, TipoAstaActivity.class);
        intent.putExtra("utente", utente);
        intent.putExtra("tipoUtente", utente.getTipo());
        intent.putExtra("asta", asta);
        intent.putExtra("fromHome", fromHome);
        startActivity(intent);
    }

    private void aggiornaTimer(TextView textView, int val) {
        int value = Integer.parseInt(textView.getText().toString()) + val;
        textView.setText(String.format("%02d", Math.max(0, value)));
    }

    private void openActivityHome(Utente utente) {
        Intent intentH = new Intent(this, HomeActivity.class);
        intentH.putExtra("utente", utente);
        startActivity(intentH);
    }
}
