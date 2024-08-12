package com.example.dietideals24;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.AstaSilenziosaDTO;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
@SuppressWarnings("deprecation")
public class CreaAstaSilenziosaActivity extends AppCompatActivity {

    private Utente utente;
    private Asta asta;
    private boolean fromHome;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_asta_silenziosa);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        fromHome = getIntent().getBooleanExtra("fromHome", true);
        asta = (Asta) getIntent().getSerializableExtra("asta");
        utente = (Utente) getIntent().getSerializableExtra("utente");

        DatePicker datePicker = findViewById(R.id.datePicker);
        TimePicker timePicker = findViewById(R.id.timePicker);
        Button createButton = findViewById(R.id.crea_button);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);

        timePicker.setIs24HourView(true);

        backButton.setOnClickListener(v -> {
            openActivityTipoAsta(utente, asta);
            finish();
        });

        homeButton.setOnClickListener(v -> {
            openActivityHome(utente);
            finish();
        });

        createButton.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;  // Aggiunta +1 per il mese
            int year = datePicker.getYear();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String scadenza = String.format("%d-%02d-%02d %02d:%02d:00", year, month, day, hour, minute);

            Calendar currentTime = Calendar.getInstance();
            Calendar scadenzaTime = Calendar.getInstance();
            scadenzaTime.set(year, month - 1, day, hour, minute);  // Mese corretto per Calendar

            if (currentTime.after(scadenzaTime)) {
                new AlertDialog.Builder(CreaAstaSilenziosaActivity.this)
                        .setMessage("Errore, la data di scadenza non è valida.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {})
                        .create()
                        .show();
                return;
            } else {
                AstaSilenziosaDTO astaS = new AstaSilenziosaDTO();
                astaS.setIdCreatore(asta.getIdCreatore());
                astaS.setNome(asta.getNome());
                astaS.setDescrizione(asta.getDescrizione());
                astaS.setFoto(asta.getFoto());
                astaS.setCategoria(asta.getCategoria());
                astaS.setScadenza(scadenza);

                ApiService apiService = RetrofitService.getRetrofit(CreaAstaSilenziosaActivity.this).create(ApiService.class);

                apiService.creaAstaSilenziosa(astaS).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreaAstaSilenziosaActivity.this, "Asta creata con successo!", Toast.LENGTH_SHORT).show();
                            openActivityHome(utente);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(CreaAstaSilenziosaActivity.this, "Errore durante la creazione dell'asta!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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

    private void openActivityHome(Utente utente) {
        Intent intentH = new Intent(this, HomeActivity.class);
        intentH.putExtra("utente", utente);
        startActivity(intentH);
    }
}
