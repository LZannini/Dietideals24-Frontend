package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.AstaDTO;
import com.example.dietideals24.enums.Categoria;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.AstaInversa;
import com.example.dietideals24.models.AstaRibasso;
import com.example.dietideals24.models.AstaSilenziosa;
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
public class CercaAstaActivity extends AppCompatActivity {

    private final Categoria[] items = Categoria.values();
    private Utente utente;
    private boolean fromHome;
    private boolean modificaAvvenuta;
    private List<Asta> listaAste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerca_asta);

        Button vaiButton = findViewById(R.id.vai_button);

        EditText cercaAstaInput = findViewById(R.id.cerca_asta_input);

        AutoCompleteTextView autoCompleteTxt = findViewById(R.id.auto_complete_txt);

        ArrayAdapter<Categoria> adapterItems = new ArrayAdapter<>(this, R.layout.activity_list_item, items);

        autoCompleteTxt.setAdapter(adapterItems);

        utente = (Utente) getIntent().getSerializableExtra("utente");

        fromHome = getIntent().getBooleanExtra("fromHome", true);

        listaAste = (List<Asta>) getIntent().getSerializableExtra("listaAste");

        modificaAvvenuta = getIntent().getBooleanExtra("modificaAvvenuta", false);

        ImageButton backButton = findViewById(R.id.back_button);

        ImageButton homeButton = findViewById(R.id.home_button);

        if(!fromHome) {
            homeButton.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);

        backButton.setOnClickListener(v -> {
            if (fromHome) {
                openActivityHome(utente);
            } else {
                openActivityOfferteFatte();
            }
            finish();
        });

        homeButton.setOnClickListener(v -> {
            openActivityHome(utente);
            finish();
        });

        autoCompleteTxt.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(getApplicationContext(), "Filtro Selezionato: " + item, Toast.LENGTH_SHORT).show();
        });

        vaiButton.setOnClickListener(v -> {
            String filtro = autoCompleteTxt.getText().toString();
            String query = cercaAstaInput.getText().toString();
            cercaAsta(apiService, filtro, query);
        });


        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(fromHome) {
            openActivityHome(utente);
        } else {
            openActivityOfferteFatte();
        }
        finish();
    }

    private void openActivityHome(Utente utente) {
        Intent intentH = new Intent(this, HomeActivity.class);
        intentH.putExtra("utente", utente);
        startActivity(intentH);
    }

    private void openActivityOfferteFatte() {
        Intent intent = new Intent(this, OfferteFatteActivity.class);
        intent.putExtra("utente", utente);
        intent.putExtra("utenteHome", utente);
        intent.putExtra("modificaAvvenuta", modificaAvvenuta);
        if(listaAste != null) {
            intent.putExtra("listaAste", (Serializable) listaAste);
        } else {
            intent.putExtra("listaAste", new ArrayList<>());
        }
        startActivity(intent);
    }

    private void cercaAsta(ApiService apiService, String filtro, String query) {
        Call<List<AstaDTO>> call;
        String searchCriteria = null;

        if (!query.isEmpty() && !filtro.isEmpty()) {
            call = apiService.cercaPerParolaChiaveAndCategoria(query, filtro.toUpperCase());
            searchCriteria = query + " in " + filtro;
        } else if (!query.isEmpty() && filtro.isEmpty()) {
            call = apiService.cercaPerParolaChiave(query);
            searchCriteria = query;
        } else if (query.isEmpty() && !filtro.isEmpty()) {
            call = apiService.cercaPerCategoria(filtro.toUpperCase());
            searchCriteria = filtro;
        }
        else
            call = apiService.cercaTutte();

        String finalSearchCriteria = searchCriteria;
        call.enqueue(new Callback<List<AstaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<AstaDTO>> call, @NonNull Response<List<AstaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AstaDTO> asteResponse = response.body();
                    List<Asta> asteList = creaListaModelloAsta(asteResponse);
                    List<Asta> aste = new ArrayList<>();
                    for (Asta a : asteList) {
                        if (a.getIdCreatore() != utente.getId())
                            aste.add(a);
                    }
                    Intent intent = new Intent(CercaAstaActivity.this, RisultatiRicercaActivity.class);
                    intent.putExtra("listaAste", (Serializable) aste);
                    intent.putExtra("criterioRicerca", finalSearchCriteria);
                    intent.putExtra("utente", utente);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CercaAstaActivity.this, RisultatiRicercaActivity.class);
                    intent.putExtra("listaAste", new ArrayList<AstaDTO>());
                    intent.putExtra("criterioRicerca", finalSearchCriteria);
                    intent.putExtra("utente", utente);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AstaDTO>> call, @NonNull Throwable t) {
                Toast.makeText(CercaAstaActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                Logger.getLogger(CercaAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
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
        asta.setStato(dto.getStato());

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

        return asta;
    }

    public List<Asta> creaListaModelloAsta(List<AstaDTO> listaDto) {
        List<Asta> asteList = new ArrayList<>();
        for (AstaDTO dto : listaDto) {
            if (dto.getTipo().equals("RIBASSO") && !utente.getTipo().toString().equals("VENDITORE")) {
                asteList.add(creaModelloAstaR(dto));
            } else if (dto.getTipo().equals("SILENZIOSA") && !utente.getTipo().toString().equals("VENDITORE")) {
                asteList.add(creaModelloAstaS(dto));
            } else if (dto.getTipo().equals("INVERSA") && !utente.getTipo().toString().equals("COMPRATORE")) {
                asteList.add(creaModelloAstaI(dto));
            }
        }
        return asteList;
    }

}