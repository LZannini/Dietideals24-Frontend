package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.AstaDTO;
import com.example.dietideals24.dto.UtenteDTO;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.AstaInversa;
import com.example.dietideals24.models.AstaRibasso;
import com.example.dietideals24.models.AstaSilenziosa;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textview.MaterialTextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class ProfiloActivity extends AppCompatActivity {

    private ImageView avatarSelector;
    private EditText emailEditText;
    private EditText bioEditText;
    private EditText webSiteEditText;
    private EditText countryEditText;
    private MaterialTextView textUsername;
    private LinearLayout pulsantiAste;
    private Button buttonSalva;
    private Utente utenteOriginale;
    private Utente utenteModificato;
    private Boolean infoMod = false;
    private byte[] imageBytes;
    private boolean fromDettagli;
    private boolean fromHome;
    private boolean modificaAvvenuta;

    @SuppressLint({"SuspiciousIndentation", "WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        fromDettagli = getIntent().getBooleanExtra("fromDettagli", false);
        fromHome = getIntent().getBooleanExtra("fromHome", true);

        ImageButton menuButton = findViewById(R.id.icona_menu);
        avatarSelector = findViewById(R.id.foto_profilo);
        emailEditText = findViewById(R.id.email);
        bioEditText = findViewById(R.id.shortBio);
        webSiteEditText = findViewById(R.id.sito);
        countryEditText = findViewById(R.id.paese);
        pulsantiAste = findViewById(R.id.pulsanti_aste);
        buttonSalva = findViewById(R.id.salva_button);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        Button buttonAsteCreate = findViewById(R.id.asteCreate_button);
        Button buttonOfferteFatte = findViewById(R.id.leTueOfferte_button);
        textUsername = findViewById((R.id.text_nomeProfilo));

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        if(fromDettagli) {
            utenteModificato = (Utente) getIntent().getSerializableExtra("utente_home");
            utenteOriginale = (Utente) getIntent().getSerializableExtra("utente");
            menuButton.setVisibility(View.INVISIBLE);
            buttonOfferteFatte.setVisibility(View.GONE);
            homeButton.setVisibility(View.VISIBLE);
        } else {
            modificaAvvenuta = getIntent().getBooleanExtra("modificaAvvenuta", modificaAvvenuta);
            if (modificaAvvenuta) {
                utenteModificato = (Utente) getIntent().getSerializableExtra("utente");
                utenteOriginale = utenteModificato;
            } else {
                utenteOriginale = (Utente) getIntent().getSerializableExtra("utente");
                utenteModificato = utenteOriginale;
            }
        }

        if(utenteOriginale.getAvatar() != null) {
            Bitmap avatarBitmap = BitmapFactory.decodeByteArray(utenteOriginale.getAvatar(), 0, utenteOriginale.getAvatar().length);
            avatarSelector.setImageBitmap(avatarBitmap);
        }

        textUsername.setText(utenteOriginale.getUsername());
        emailEditText.setText(utenteOriginale.getEmail());
        bioEditText.setText(utenteOriginale.getBiografia());
        webSiteEditText.setText(utenteOriginale.getSitoweb());
        countryEditText.setText(utenteOriginale.getPaese());

        buttonAsteCreate.setOnClickListener(v -> trovaAsteCreate(getApiService()));

        backButton.setOnClickListener(v -> {
            if (!fromDettagli) {
                openActivityHome();
            }
            finish();
        });

        homeButton.setOnClickListener(v -> openActivityHome());

        if (!fromDettagli) {
            menuButton.setOnClickListener(v -> openMenuProfilo(v, utenteOriginale));
        }

        buttonOfferteFatte.setOnClickListener(v -> trovaOfferteFatte(getApiService()));

        buttonSalva.setOnClickListener(v -> salvaModificheUtente());
    }

    private ApiService getApiService() {
        return RetrofitService.getRetrofit(ProfiloActivity.this).create(ApiService.class);
    }

    private void salvaModificheUtente() {
        utenteModificato = new Utente();
        utenteModificato.setId(utenteOriginale.getId());
        utenteModificato.setPassword(utenteOriginale.getPassword());
        utenteModificato.setTipo(utenteOriginale.getTipo());

        if (imageBytes != null) {
            utenteModificato.setAvatar(imageBytes);
            infoMod = true;
        } else if (utenteOriginale.getAvatar() != null) {
            utenteModificato.setAvatar(utenteOriginale.getAvatar());
        }

        utenteModificato.setUsername(getUpdatedField(textUsername.getText().toString(), utenteOriginale.getUsername()));
        utenteModificato.setEmail(getUpdatedField(emailEditText.getText().toString(), utenteOriginale.getEmail()));
        utenteModificato.setBiografia(getUpdatedField(bioEditText.getText().toString(), utenteOriginale.getBiografia()));
        utenteModificato.setSitoweb(getUpdatedField(webSiteEditText.getText().toString(), utenteOriginale.getSitoweb()));
        utenteModificato.setPaese(getUpdatedField(countryEditText.getText().toString(), utenteOriginale.getPaese()));

        ApiService apiService = getApiService();
        UtenteDTO utenteModificatoDTO = creaUtenteDTO(utenteModificato);
        apiService.aggiornaUtente(utenteModificatoDTO)
                .enqueue(new Callback<UtenteDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UtenteDTO> call, @NonNull Response<UtenteDTO> response) {
                        if (response.isSuccessful()) {
                            UtenteDTO utenteRicevuto = response.body();
                            if (utenteRicevuto != null && utenteRicevuto.getAvatar() != null) {
                                Bitmap avatarBitmap = BitmapFactory.decodeByteArray(utenteRicevuto.getAvatar(), 0, utenteRicevuto.getAvatar().length);
                                avatarSelector.setImageBitmap(avatarBitmap);
                            }

                            aggiornaUIConUtente(utenteModificato);
                            Toast.makeText(ProfiloActivity.this, "Modifica effettuata con successo!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfiloActivity.this, "Errore durante la modifica dei dati, riprova.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UtenteDTO> call, @NonNull Throwable t) {
                        Toast.makeText(ProfiloActivity.this, "Errore di connessione", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getUpdatedField(String nuovoValore, String valoreOriginale) {
        return (!nuovoValore.equals(valoreOriginale) && !nuovoValore.isEmpty()) ? nuovoValore : valoreOriginale;
    }

    private void aggiornaUIConUtente(Utente utente) {
        textUsername.setText(utente.getUsername());
        emailEditText.setText(utente.getEmail());
        bioEditText.setText(utente.getBiografia());
        webSiteEditText.setText(utente.getSitoweb());
        countryEditText.setText(utente.getPaese());

        textUsername.setEnabled(false);
        emailEditText.setEnabled(false);
        bioEditText.setEnabled(false);
        webSiteEditText.setEnabled(false);
        countryEditText.setEnabled(false);
        buttonSalva.setVisibility(View.INVISIBLE);
        pulsantiAste.setVisibility(View.VISIBLE);

        utenteOriginale = utente;
    }

    private void openMenuProfilo(View view, Utente utente) {
        PopupMenu popup = new PopupMenu(this, view);

        popup.getMenuInflater().inflate(R.menu.menu_profilo, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit_profile:
                    textUsername.setEnabled(true);
                    emailEditText.setEnabled(true);
                    bioEditText.setEnabled(true);
                    webSiteEditText.setEnabled(true);
                    countryEditText.setEnabled(true);
                    avatarSelector.setOnClickListener(v ->
                            ImagePicker.with(ProfiloActivity.this)
                                    .crop()                    // Crop image (Optional), Check Customization for more option
                                    .compress(1024)            // Final image size will be less than 1 MB (Optional)
                                    .maxResultSize(1080, 1080) // Final image resolution will be less than 1080 x 1080 (Optional)
                                    .start()
                    );
                    pulsantiAste.setVisibility(View.GONE);
                    buttonSalva.setVisibility(View.VISIBLE);
                    return true;
                case R.id.action_change_password:
                    openActivityModificaPassword(utente);
                    return true;
                case R.id.action_switch_account:
                    openActivitySceltaAccount(utente);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!fromDettagli)
            openActivityHome();
        finish();
    }

    private UtenteDTO creaUtenteDTO(Utente u) {
        UtenteDTO utente = new UtenteDTO();
        utente.setId(u.getId());
        utente.setUsername(u.getUsername());
        utente.setEmail(u.getEmail());
        utente.setPassword(u.getPassword());
        utente.setBiografia(u.getBiografia());
        utente.setSitoweb(u.getSitoweb());
        utente.setPaese(u.getPaese());
        utente.setTipo(u.getTipo());
        utente.setAvatar(u.getAvatar());
        return utente;
    }

    private void openActivityHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("utente", utenteModificato);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            avatarSelector.setImageURI(uri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageBytes = convertBitmapToByteArray(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private void openActivityModificaPassword(Utente utente) {
        Intent intentR = new Intent(this, ModificaPasswordActivity.class);
        intentR.putExtra("utente", utente);
        startActivity(intentR);
    }

    private void openActivitySceltaAccount(Utente utente) {
        Intent intentR = new Intent(this, SceltaAccountActivity.class);
        intentR.putExtra("utente", utente);
        intentR.putExtra("fromLogin", false);
        intentR.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentR);
    }

    private void trovaAsteCreate(ApiService apiService) {
        Call<List<AstaDTO>> call;

        call = apiService.cercaPerUtente(utenteOriginale.getId());
        call.enqueue(new Callback<List<AstaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<AstaDTO>> call, @NonNull Response<List<AstaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AstaDTO> asteResponse = response.body();
                    List<Asta> asteList = creaListaModelloAsta(asteResponse);
                    List<Asta> aste = new ArrayList<>();
                    for (Asta a : asteList) {
                        aste.add(a);
                    }
                    Intent intent = new Intent(ProfiloActivity.this, AsteCreateActivity.class);
                    intent.putExtra("listaAste", (Serializable) aste);
                    intent.putExtra("utente_home", utenteModificato);
                    intent.putExtra("utente", utenteOriginale);
                    intent.putExtra("fromDettagli", fromDettagli);
                    intent.putExtra("modificaAvvenuta", infoMod);
                    intent.putExtra("fromHome", fromHome);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(ProfiloActivity.this, AsteCreateActivity.class);
                    intent.putExtra("listaAste", new ArrayList<Asta>());
                    intent.putExtra("utente_home", utenteModificato);
                    intent.putExtra("utente", utenteOriginale);
                    intent.putExtra("fromDettagli", fromDettagli);
                    intent.putExtra("fromHome", fromHome);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AstaDTO>> call, @NonNull Throwable t) {
                Toast.makeText(ProfiloActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                Logger.getLogger(ProfiloActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }

    public void trovaOfferteFatte(ApiService apiService) {
        Call<List<AstaDTO>> call;
        call = apiService.cercaPerOfferteUtente(utenteOriginale.getId());

        call.enqueue(new Callback<List<AstaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<AstaDTO>> call, @NonNull Response<List<AstaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AstaDTO> asteResponse = response.body();
                    List<Asta> asteList = creaListaModelloAsta(asteResponse);
                    List<Asta> aste = new ArrayList<>();
                    for (Asta a : asteList) {
                        aste.add(a);
                    }
                    Intent intent = new Intent(ProfiloActivity.this, OfferteFatteActivity.class);
                    intent.putExtra("listaAste", (Serializable) aste);
                    intent.putExtra("utente_home", utenteModificato);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(ProfiloActivity.this, OfferteFatteActivity.class);
                    intent.putExtra("listaAste", new ArrayList<Asta>());
                    intent.putExtra("utente_home", utenteModificato);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AstaDTO>> call, @NonNull Throwable t) {
                Toast.makeText(ProfiloActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                Logger.getLogger(ProfiloActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }

    public AstaRibasso creaModelloAstaR(AstaDTO dto) {
        AstaRibasso asta = new AstaRibasso();
        asta.setId(dto.getID());
        asta.setIdCreatore(dto.getIdCreatore());
        asta.setCategoria(dto.getCategoria());
        asta.setFoto(dto.getFoto());
        asta.setNome(dto.getNome());
        asta.setDescrizione(dto.getDescrizione());
        asta.setStato(dto.getStato());
        asta.setVincitore(dto.getVincitore());

        return asta;
    }

    public AstaSilenziosa creaModelloAstaS(AstaDTO dto) {
        AstaSilenziosa asta = new AstaSilenziosa();
        asta.setId(dto.getID());
        asta.setIdCreatore(dto.getIdCreatore());
        asta.setCategoria(dto.getCategoria());
        asta.setFoto(dto.getFoto());
        asta.setNome(dto.getNome());
        asta.setDescrizione(dto.getDescrizione());
        asta.setStato((dto.getStato()));
        asta.setVincitore(dto.getVincitore());

        return asta;
    }

    public AstaInversa creaModelloAstaI(AstaDTO dto) {
        AstaInversa asta = new AstaInversa();
        asta.setId(dto.getID());
        asta.setIdCreatore(dto.getIdCreatore());
        asta.setCategoria(dto.getCategoria());
        asta.setFoto(dto.getFoto());
        asta.setNome(dto.getNome());
        asta.setDescrizione(dto.getDescrizione());
        asta.setStato(dto.getStato());
        asta.setVincitore(dto.getVincitore());

        return asta;
    }

    public List<Asta> creaListaModelloAsta(List<AstaDTO> listaDto) {
        List<Asta> asteList = new ArrayList<>();
        for (AstaDTO dto : listaDto) {
            switch (dto.getTipo()) {
                case "RIBASSO":
                    asteList.add(creaModelloAstaR(dto));
                    break;
                case "SILENZIOSA":
                    asteList.add(creaModelloAstaS(dto));
                    break;
                case "INVERSA":
                    asteList.add(creaModelloAstaI(dto));
                    break;
                default:
                    return new ArrayList<>();
            }
        }
        return asteList;
    }
}