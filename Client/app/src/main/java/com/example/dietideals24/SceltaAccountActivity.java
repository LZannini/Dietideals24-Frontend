package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.UtenteDTO;
import com.example.dietideals24.enums.TipoUtente;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;
import retrofit2.Callback;

import retrofit2.Call;
import retrofit2.Response;
@SuppressWarnings("deprecation")
public class SceltaAccountActivity extends AppCompatActivity {

    private Utente utente;
    private boolean fromLogin;
    private UtenteDTO currentUser;
    private LinearLayout venditoreButt;
    private LinearLayout compratoreButt;
    private LinearLayout completoButt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scelta_account);

        utente = (Utente) getIntent().getSerializableExtra("utente");
        fromLogin = getIntent().getBooleanExtra("fromLogin", true);

        venditoreButt = findViewById(R.id.button_vendi);
        compratoreButt = findViewById(R.id.button_compra);
        completoButt = findViewById(R.id.button_completo);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        backButton.setOnClickListener(v -> {
            if (fromLogin) {
                openActivityLogin();
            } else {
                openActivityProfilo(utente);
            }
            finish();
        });

        if (fromLogin) {
            homeButton.setVisibility(View.INVISIBLE);
        } else {
            homeButton.setOnClickListener(v -> {
                openActivityHome(utente);
                finish();
            });
        }

        venditoreButt.setOnClickListener(v -> change(TipoUtente.VENDITORE));
        compratoreButt.setOnClickListener(v -> change(TipoUtente.COMPRATORE));
        completoButt.setOnClickListener(v -> change(TipoUtente.COMPLETO));

        aggiornaButton();
    }

    @Override
    public void onResume(){
        super.onResume();
        aggiornaButton();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fromLogin) {
            openActivityLogin();
        } else {
            openActivityProfilo(utente);
        }
        finish();
    }

    public void openActivityProfilo(Utente utente) {
        Intent intentP = new Intent(this, ProfiloActivity.class);
        intentP.putExtra("utente", utente);
        intentP.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentP);
    }

    public void openActivityHome(Utente utente) {
        Intent intentR = new Intent(this, HomeActivity.class);
        intentR.putExtra("utente", utente);
        intentR.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentR);
    }

    public void openActivityLogin(){
        Intent intentR = new Intent(this, LoginActivity.class);
        startActivity(intentR);
    }

    private void aggiornaButton(){

        venditoreButt = findViewById(R.id.button_vendi);
        compratoreButt = findViewById(R.id.button_compra);
        completoButt = findViewById(R.id.button_completo);

        venditoreButt.setEnabled(true);
        compratoreButt.setEnabled(true);
        completoButt.setEnabled(true);
        venditoreButt.setAlpha(1.0f);
        compratoreButt.setAlpha(1.0f);
        completoButt.setAlpha(1.0f);

        if (utente != null) {
            switch (utente.getTipo()) {
                case VENDITORE:
                    venditoreButt.setEnabled(false);
                    venditoreButt.setAlpha(0.5f);
                    break;
                case COMPRATORE:
                    compratoreButt.setEnabled(false);
                    compratoreButt.setAlpha(0.5f);
                    break;
                case COMPLETO:
                    completoButt.setEnabled(false);
                    completoButt.setAlpha(0.5f);
                    break;
                default:
                    return;
            }
        }
    }

    private void change(TipoUtente nuovoTipo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SceltaAccountActivity.this);
        builder.setMessage("Sei sicuro di voler selezionare il tuo Tipo di Account?")
                .setCancelable(true)
                .setPositiveButton("Si", (dialogInterface, i) -> {
                    utente.setTipo(nuovoTipo);
                    currentUser = converteDTO(utente);
                    aggiornaTipoAccount(currentUser);
                    utente = creaUtente(currentUser);
                    if (fromLogin) {
                        openActivityHome(utente);
                    } else {
                        aggiornaButton();
                    }
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }


    private UtenteDTO converteDTO(Utente ut){
        UtenteDTO utenteDTO = new UtenteDTO();
        utenteDTO.setId(ut.getId());
        utenteDTO.setUsername(ut.getUsername());
        utenteDTO.setEmail(ut.getEmail());
        utenteDTO.setPassword(ut.getPassword());
        utenteDTO.setBiografia(ut.getBiografia());
        utenteDTO.setSitoweb(ut.getSitoweb());
        utenteDTO.setPaese(ut.getPaese());
        utenteDTO.setTipo(ut.getTipo());
        utenteDTO.setAvatar(ut.getAvatar());

        return utenteDTO;
    }

    private Utente creaUtente(UtenteDTO u) {
        Utente user = new Utente();
        user.setId(u.getId());
        user.setUsername(u.getUsername());
        user.setEmail(u.getEmail());
        user.setPassword(u.getPassword());
        user.setBiografia(u.getBiografia());
        user.setSitoweb(u.getSitoweb());
        user.setPaese(u.getPaese());
        user.setTipo(u.getTipo());
        user.setAvatar(u.getAvatar());
        return user;
    }

    private void aggiornaTipoAccount(UtenteDTO utente) {

        ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);

        Call<UtenteDTO> call = apiService.aggiornaUtente(utente);

        call.enqueue(new Callback<UtenteDTO>() {
            @Override
            public void onResponse(@NonNull Call<UtenteDTO> call, @NonNull Response<UtenteDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SceltaAccountActivity.this, "Tipo di account aggiornato con successo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SceltaAccountActivity.this, "Errore nell'aggiornamento del tipo di account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UtenteDTO> call, @NonNull Throwable t) {
                Toast.makeText(SceltaAccountActivity.this, "Errore di connessione", Toast.LENGTH_SHORT).show();
            }
        });
    }
}