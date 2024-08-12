package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.UtenteDTO;
import com.example.dietideals24.enums.TipoUtente;
import com.example.dietideals24.retrofit.RetrofitService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class RegistrazioneActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confPasswordEditText;
    private List<TipoUtente> selezioneAccount = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);

        usernameEditText = findViewById(R.id.username_input);
        emailEditText = findViewById(R.id.email_input);
        passwordEditText = findViewById(R.id.password_input);
        confPasswordEditText = findViewById(R.id.conferma_password_input);
        CheckBox tipoCompratore = findViewById(R.id.checkbox_compratore);
        CheckBox tipoVenditore = findViewById(R.id.checkbox_venditore);
        ImageButton backButton = findViewById(R.id.back_button);
        TextView buttonLogin = (TextView) findViewById(R.id.textView_accedi);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        Button btnR = (Button) findViewById(R.id.registrati_button);

        backButton.setOnClickListener(v -> {
            openActivityLogin();
            finish();
        });

        tipoCompratore.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selezioneAccount.add(TipoUtente.COMPRATORE);
            } else {
                selezioneAccount.remove(TipoUtente.COMPRATORE);
            }
        });

        tipoVenditore.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selezioneAccount.add(TipoUtente.VENDITORE);
            } else {
                selezioneAccount.remove(TipoUtente.VENDITORE);
            }
        });

        buttonLogin.setOnClickListener(v -> openActivityLogin());

        btnR.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confPass = confPasswordEditText.getText().toString().trim();

        if (!validateInputs(username, email, password, confPass)) {
            return;
        }

        if (password.equals(confPass)) {
            UtenteDTO utente = new UtenteDTO();
            utente.setUsername(username);
            utente.setEmail(email);
            utente.setPassword(password);
            utente.setTipo(getTipoUtente());

            ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);
            apiService.registraUtente(utente).enqueue(new Callback<UtenteDTO>() {
                @Override
                public void onResponse(@NonNull Call<UtenteDTO> call, @NonNull Response<UtenteDTO> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegistrazioneActivity.this, "Registrazione effettuata con successo!", Toast.LENGTH_SHORT).show();
                        openActivityLogin();
                    } else if (response.code() == 409) {
                        showAlert("Email o username già registrati, prova ad effettuare il login!");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UtenteDTO> call, @NonNull Throwable t) {
                    showAlert("Errore durante la registrazione!");
                    Logger.getLogger(RegistrazioneActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                }
            });
        } else {
            showAlert("Le password non corrispondono, riprova");
        }
    }

    private boolean validateInputs(String username, String email, String password, String confPass) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confPass.isEmpty() || selezioneAccount.isEmpty()) {
            showAlert("Bisogna riempire tutti i campi!");
            return false;
        }
        return true;
    }

    private TipoUtente getTipoUtente() {
        if (selezioneAccount.size() == 1) {
            return selezioneAccount.iterator().next();
        } else {
            return TipoUtente.COMPLETO;
        }
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                    // Optional: add any action on button click
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openActivityLogin();
        finish();
    }

    public void openActivityLogin(){
        Intent intentR = new Intent(this, LoginActivity.class);
        startActivity(intentR);
    }
}