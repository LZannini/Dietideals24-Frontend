package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dietideals24.adapters.AuctionAdapter;
import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.OffertaDTO;
import com.example.dietideals24.enums.StatoOfferta;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.Offerta;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferteFatteActivity extends AppCompatActivity implements AuctionAdapter.OnAstaListener {

    private Utente utente;
    private List<Asta> listaAste;
    private List<Asta> asteAttive;
    private List<Asta> asteVinte;
    private List<Asta> asteRifiutate;
    private List<Asta> astePerse;
    private TextView noAuctionsText;
    private RecyclerView recyclerView;
    private ImageButton backButton;
    private MaterialButton btnAttive, btnVinte, btnRifiutate, btnPerse;
    private Button btnCrea;
    private LinearLayout layoutAttributi;
    private Asta astaSelezionata;
    private Boolean fromDettagli;
    private Boolean modificaAvvenuta;
    private boolean attiva, rifiutata;
    List<Offerta> offerte = new ArrayList<>();
    AuctionAdapter adapter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offerte_fatte);

        utente = (Utente) getIntent().getSerializableExtra("utente_home");
        listaAste = (List<Asta>) getIntent().getSerializableExtra("listaAste");
        modificaAvvenuta = getIntent().getBooleanExtra("modificaAvvenuta", false);


        asteAttive = new ArrayList<>();
        asteVinte = new ArrayList<>();
        asteRifiutate = new ArrayList<>();
        astePerse = new ArrayList<>();
        attiva = true;
        rifiutata = false;


        ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);


        apiService.recuperaOffertePerUtente(utente.getId())
                .enqueue(new Callback<List<OffertaDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<OffertaDTO>> call, @NonNull Response<List<OffertaDTO>> response) {
                        if(response.isSuccessful() && response.body() != null) {
                            List<OffertaDTO> offerteResponse = response.body();
                            List<Offerta> offerteList = creaListModelloOfferta(offerteResponse);
                            for(Offerta o : offerteList) {
                                offerte.add(o);
                            }

                            for(Asta a : listaAste) {
                                if(a.getStato().toString().equals("ATTIVA")) {
                                    for(Offerta o : offerte) {
                                        if(o.getIdAsta() == a.getId()) {
                                            if(o.getStato().toString().equals("ATTESA")) {
                                                asteAttive.add(a);
                                            } else if(o.getStato().toString().equals("RIFIUTATA")) {
                                                asteRifiutate.add(a);
                                            } else {
                                                astePerse.add(a);
                                            }
                                            break;
                                        }
                                    }
                                } else if(a.getStato().toString().equals("VENDUTA") && utente.getId() == a.getVincitore()) {
                                    asteVinte.add(a);
                                } else {
                                    astePerse.add(a);
                                }
                            }
                        }
                        if(attiva && (asteAttive == null || asteAttive.isEmpty())) {
                            noAuctionsText.setVisibility(View.VISIBLE);
                            btnCrea.setVisibility(View.VISIBLE);
                            int childCount = layoutAttributi.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                View child = layoutAttributi.getChildAt(i);
                                child.setVisibility(View.INVISIBLE);
                            }
                        }else {
                            noAuctionsText.setVisibility(View.GONE);
                        }
                        recyclerView.setLayoutManager(new LinearLayoutManager(OfferteFatteActivity.this));
                        adapter = new AuctionAdapter(asteAttive,OfferteFatteActivity.this, true, false);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<OffertaDTO>> call, @NonNull Throwable t) {
                        Toast.makeText(OfferteFatteActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                        Logger.getLogger(OfferteFatteActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                    }
                });

        noAuctionsText = findViewById(R.id.no_auctions_text);
        backButton = findViewById(R.id.back_button);
        btnCrea = findViewById(R.id.cerca_button);
        btnAttive = findViewById(R.id.btn_aste_attive);
        btnVinte = findViewById(R.id.btn_aste_vinte);
        btnRifiutate = findViewById(R.id.btn_aste_rifiutate);
        btnPerse = findViewById(R.id.btn_aste_perse);
        layoutAttributi = findViewById(R.id.layout_attributi);
        recyclerView = findViewById(R.id.risultati_recycler_view);
        ImageButton homeButton = findViewById(R.id.home_button);

        btnAttive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attiva = true;
                rifiutata = false;
                btnAttive.setBackgroundColor(Color.parseColor("#FF0000"));
                btnVinte.setBackgroundColor(Color.parseColor("#0E4273"));
                btnRifiutate.setBackgroundColor(Color.parseColor("#0E5273"));
                btnPerse.setBackgroundColor(Color.parseColor("#0E4273"));
                adapter.setAste(asteAttive, attiva);
                adapter.notifyDataSetChanged();
                if(asteAttive == null || asteAttive.isEmpty()) {
                    noAuctionsText.setVisibility(View.VISIBLE);
                    btnCrea.setVisibility(View.VISIBLE);
                    int childCount = layoutAttributi.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = layoutAttributi.getChildAt(i);
                        child.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        btnVinte.setOnClickListener(v -> {
            attiva = false;
            rifiutata = false;
            mostraLayout();
            aggiornaVistaBottoni(btnVinte);
            adapter.setAste(asteVinte, attiva);
            adapter.notifyDataSetChanged();
        });

        btnRifiutate.setOnClickListener(v -> {
            attiva = false;
            rifiutata = true;
            mostraLayout();
            aggiornaVistaBottoni(btnRifiutate);
            adapter.setAste(asteRifiutate, rifiutata);
            adapter.notifyDataSetChanged();
        });

        btnPerse.setOnClickListener(v -> {
            attiva = false;
            rifiutata = false;
            mostraLayout();
            aggiornaVistaBottoni(btnPerse);
            adapter.setAste(astePerse, attiva);
            adapter.notifyDataSetChanged();
        });

        backButton.setOnClickListener(v -> {
            openActivityProfilo();
            finish();
        });

        homeButton.setOnClickListener(v -> {
            openActivityHome(utente);
            finish();
        });

        btnCrea.setOnClickListener(v -> openActivityCercaAsta());

    }

    private void aggiornaVistaBottoni(Button bottoneAttivo) {
        btnVinte.setBackgroundColor(Color.parseColor("#0E5273"));
        btnRifiutate.setBackgroundColor(Color.parseColor("#0E5273"));
        btnPerse.setBackgroundColor(Color.parseColor("#0E5273"));
        btnAttive.setBackgroundColor(Color.parseColor("#0E4273"));

        bottoneAttivo.setBackgroundColor(Color.parseColor("#FF0000"));
    }

    private void mostraLayout() {
        noAuctionsText.setVisibility(View.GONE);
        btnCrea.setVisibility(View.GONE);
        int childCount = layoutAttributi.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = layoutAttributi.getChildAt(i);
            child.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openActivityProfilo();
        finish();
    }

    public void openActivityHome(Utente utente) {
        Intent intentR = new Intent(this, HomeActivity.class);
        intentR.putExtra("utente", utente);
        startActivity(intentR);
    }

    private void openActivityProfilo() {
        Intent intentP = new Intent(this, ProfiloActivity.class);
        intentP.putExtra("utente", utente);
        intentP.putExtra("utenteHome", utente);
        intentP.putExtra("fromDettagli", fromDettagli);
        intentP.putExtra("modificaAvvenuta", modificaAvvenuta);
        intentP.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intentP);
    }

    private void openActivityCercaAsta() {
        Intent intentR = new Intent(this, CercaAstaActivity.class);
        intentR.putExtra("utente", utente);
        intentR.putExtra("fromHome", false);
        intentR.putExtra("listaAste", (Serializable) listaAste);
        intentR.putExtra("modificaAvvenuta", modificaAvvenuta);
        startActivity(intentR);
    }

    public List<Offerta> creaListModelloOfferta(List<OffertaDTO> listaDto) {
        List<Offerta> offerteList = new ArrayList<>();
        for (OffertaDTO dto : listaDto) {
            Offerta offerta = new Offerta();
            offerta.setId(dto.getId());
            offerta.setIdAsta(dto.getIdAsta());
            offerta.setData(dto.getData());
            offerta.setIdUtente(dto.getIdUtente());
            offerta.setValore(dto.getValore());
            offerta.setOfferente(dto.getOfferente());
            offerta.setStato(dto.getStato());
            offerteList.add(offerta);
        }
        return offerteList;
    }

    @Override
    public void onAstaClick(int position, boolean isAttive) {
        if (isAttive) {
            if(rifiutata) {
                astaSelezionata = asteRifiutate.get(position);
                asteRifiutate.remove(position);
            } else {
                astaSelezionata = asteAttive.get(position);
            }
            showNewOfferDialog(astaSelezionata);
        }
    }

    private void showNewOfferDialog(Asta asta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuova_offerta, null);
        builder.setView(dialogView);

        EditText editTextOfferAmount = dialogView.findViewById(R.id.editTextOfferAmount);
        Button buttonSubmitOffer = dialogView.findViewById(R.id.buttonSubmitOffer);
        Button buttonCancelOffer = dialogView.findViewById(R.id.buttonCancelOffer);

        AlertDialog dialog = builder.create();

        buttonSubmitOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String importoStr = editTextOfferAmount.getText().toString();
                if (!importoStr.isEmpty()) {
                    float importo = Float.parseFloat(importoStr);
                    submitNewOffer(asta, importo);
                    dialog.dismiss();
                } else {
                    Toast.makeText(OfferteFatteActivity.this, "Inserisci un importo valido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonCancelOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                asteRifiutate.add(asta);
            }
        });

        dialog.show();
    }

    @SuppressLint("NewApi")
    private void submitNewOffer(Asta asta, float importo) {
        ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);


        OffertaDTO offerta = new OffertaDTO();
        offerta.setIdAsta(asta.getId());
        offerta.setIdUtente(utente.getId());
        offerta.setValore(importo);
        offerta.setStato(StatoOfferta.ATTESA);

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTimeString = currentDateTime.format(formatter);

        offerta.setData(dateTimeString);

        apiService.creaOfferta(offerta)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        Toast.makeText(OfferteFatteActivity.this, "Offerta presentata con successo!", Toast.LENGTH_SHORT).show();
                        if(rifiutata) {
                            asteAttive.add(asta);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(OfferteFatteActivity.this, "Errore durante la creazione dell'offerta!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}