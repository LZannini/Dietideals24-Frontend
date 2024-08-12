package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.NotificaDTO;
import com.example.dietideals24.models.Utente;

import com.example.dietideals24.retrofit.RetrofitService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class HomeActivity extends AppCompatActivity {

    private Utente utente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        utente = (Utente) getIntent().getSerializableExtra("utente");

        LinearLayout buttonCrea = findViewById(R.id.button_crea);
        LinearLayout buttonCerca = findViewById(R.id.button_cerca);
        LinearLayout buttonProfilo = findViewById(R.id.button_profilo);
        Button buttonNotifica = findViewById(R.id.button_notifica);
        LinearLayout buttonDisconnetti = findViewById(R.id.button_disconnetti);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);
        checkNotifiche(apiService);

        buttonCrea.setOnClickListener(v -> openActivityCreaAsta(utente));

        buttonCerca.setOnClickListener(view -> openActivityCercaAsta(utente));

        buttonProfilo.setOnClickListener(view -> openActivityProfilo(utente));

        buttonNotifica.setOnClickListener(view -> recuperaNotifiche(apiService));

        buttonDisconnetti.setOnClickListener(view -> disconnect());

    }

    private void setVisibilityRedNot(boolean pallino){
        View redNot = findViewById(R.id.pallino_notifiche);

        if(pallino) redNot.setVisibility(View.VISIBLE);
        else redNot.setVisibility(View.GONE);
    }

    private void checkNotifiche(ApiService apiService) {
        Call<List<NotificaDTO>> call;

        call = apiService.mostraNotifiche(utente.getId());

        call.enqueue(new Callback<List<NotificaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificaDTO>> call, @NonNull Response<List<NotificaDTO>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<NotificaDTO> notifiche = response.body();
                    boolean nonLetta = false;

                    for(NotificaDTO notifica: notifiche){
                        if(!notifica.isLetta()) {
                            nonLetta = true;
                            break;
                        }
                    }
                    setVisibilityRedNot(!notifiche.isEmpty() && nonLetta);
                } else
                    Logger.getLogger(HomeActivity.class.getName()).log(Level.WARNING, "Notifiche vuote");
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificaDTO>> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Errore durante il caricamento delle notifiche, riprova!", Toast.LENGTH_SHORT).show();
                Logger.getLogger(HomeActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }


    private void recuperaNotifiche(ApiService apiService) {
        Call<List<NotificaDTO>> call;

        call = apiService.mostraNotifiche(utente.getId());

        call.enqueue(new Callback<List<NotificaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificaDTO>> call, @NonNull Response<List<NotificaDTO>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<NotificaDTO> notifiche = response.body();
                    Intent intent = new Intent(HomeActivity.this, NotificaActivity.class);
                    intent.putExtra("listaNotifiche", (Serializable) notifiche);
                    intent.putExtra("utente", utente);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(HomeActivity.this, NotificaActivity.class);
                    intent.putExtra("listaNotifiche", new ArrayList<NotificaDTO>());
                    intent.putExtra("utente", utente);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificaDTO>> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Errore durante il caricamento delle notifiche, riprova!", Toast.LENGTH_SHORT).show();
                Logger.getLogger(HomeActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }


    @Override
    public void onBackPressed() {
        //
    }

    private void disconnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setMessage("Sei sicuro di voler uscire?")
                .setCancelable(true)
                .setPositiveButton("Si", (dialogInterface, i) -> {
                    openActivityLogin();
                    finish();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    // Nessuna azione da eseguire
                })
                .show();
    }

    private void openActivityCreaAsta(Utente u) {
        Intent intentR = new Intent(this, CreaAstaActivity.class);
        intentR.putExtra("utente", u);
        intentR.putExtra("fromHome", true);
        startActivity(intentR);
    }

    private void openActivityCercaAsta(Utente utente) {
        Intent intentR = new Intent(this, CercaAstaActivity.class);
        intentR.putExtra("utente", utente);
        intentR.putExtra("fromHome", true);
        startActivity(intentR);
    }

    private void openActivityLogin() {
        Intent intentL = new Intent(this, LoginActivity.class);
        startActivity(intentL);
    }

    private void openActivityProfilo(Utente utente) {
        Intent intentR = new Intent(this, ProfiloActivity.class);
        intentR.putExtra("utente", utente);
        intentR.putExtra("fromDettagli", false);
        startActivity(intentR);
    }

}
