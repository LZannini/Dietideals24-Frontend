package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.UtenteDTO;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class ModificaPasswordActivity extends AppCompatActivity {

    private EditText vecchiaPassword;
    private EditText nuovaPassword;
    private EditText confermaPassword;
    private Utente utente;
    private int idUtente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifica_password);

        utente = (Utente) getIntent().getSerializableExtra("utente");
        idUtente = utente.getId();
        String passUtente = utente.getPassword();

        vecchiaPassword = findViewById(R.id.vecchia_password);
        nuovaPassword = findViewById(R.id.nuova_password);
        confermaPassword = findViewById(R.id.conferma_password);
        Button salvaButton = findViewById(R.id.salva_button);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        backButton.setOnClickListener(v -> {
            openActivityProfilo(utente);
            finish();
        });

        homeButton.setOnClickListener(v -> {
            openActivityHome(utente);
            finish();
        });

        salvaButton.setOnClickListener(v -> {
            String vecchiaPass = vecchiaPassword.getText().toString();
            String nuovaPass = nuovaPassword.getText().toString();
            String confermaPass = confermaPassword.getText().toString();

            if (!nuovaPass.equals(confermaPass)) {
                showAlert("Le password non corrispondono, riprova.");
                resetPasswordFields();
            } else if (!vecchiaPass.equals(passUtente)) {
                showAlert("Le password corrente inserita non è corretta, riprova.");
                resetPasswordFields();
            } else {
                updatePassword(nuovaPass);
            }
        });
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(ModificaPasswordActivity.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show();
    }

    private void resetPasswordFields() {
        vecchiaPassword.setText("");
        nuovaPassword.setText("");
        confermaPassword.setText("");
    }

    private void updatePassword(String nuovaPass) {
        try {
            UtenteDTO utente = new UtenteDTO();
            utente.setId(idUtente);
            utente.setPassword(nuovaPass);

            ApiService apiService = RetrofitService.getRetrofit(ModificaPasswordActivity.this).create(ApiService.class);

            apiService.modificaPassword(utente).enqueue(new Callback<UtenteDTO>() {
                @Override
                public void onResponse(@NonNull Call<UtenteDTO> call, @NonNull Response<UtenteDTO> response) {
                    if (response.isSuccessful()) {
                        UtenteDTO utenteAggiornato = response.body();
                        Utente utenteIntent = creaUtente(utenteAggiornato);
                        Toast.makeText(ModificaPasswordActivity.this, "Password modificata con successo!", Toast.LENGTH_SHORT).show();
                        openActivityHome(utenteIntent);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UtenteDTO> call, @NonNull Throwable t) {
                    Toast.makeText(ModificaPasswordActivity.this, "Errore durante la modifica della password, riprova!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Utente creaUtente(UtenteDTO utenteDTO) {
        Utente utente = new Utente();
        utente.setId(utenteDTO.getId());
        utente.setUsername(utenteDTO.getUsername());
        utente.setEmail(utenteDTO.getEmail());
        utente.setPassword(utenteDTO.getPassword());
        utente.setBiografia(utenteDTO.getBiografia());
        utente.setSitoweb(utenteDTO.getSitoweb());
        utente.setPaese(utenteDTO.getPaese());
        utente.setTipo(utenteDTO.getTipo());
        utente.setAvatar(utenteDTO.getAvatar());
        return utente;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openActivityProfilo(utente);
        finish();
    }

    public void openActivityHome(Utente utente) {
        Intent intentR = new Intent(this, HomeActivity.class);
        intentR.putExtra("utente", utente);
        startActivity(intentR);
    }

    public void openActivityProfilo(Utente utente) {
        Intent intentP = new Intent(this, ProfiloActivity.class);
        intentP.putExtra("utente", utente);
        startActivity(intentP);
    }
}