package com.example.dietideals24;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dietideals24.adapters.AuctionAdapter;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.Utente;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
public class AsteCreateActivity extends AppCompatActivity implements AuctionAdapter.OnAstaListener {

    private Utente utente;
    private Utente utenteHome;
    private List<Asta> listaAste;
    private List<Asta> asteAttive;
    private MaterialButton btnAttive;
    private Asta astaSelezionata;
    private Boolean fromDettagli;
    private Boolean modificaAvvenuta;
    private boolean fromHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aste_create);

        modificaAvvenuta = getIntent().getBooleanExtra("modificaAvvenuta", false);
        fromHome = getIntent().getBooleanExtra("fromHome", true);
        fromDettagli = getIntent().getBooleanExtra("fromDettagli", false);
        utente = (Utente) getIntent().getSerializableExtra("utente");
        listaAste = (List<Asta>) getIntent().getSerializableExtra("listaAste");
        utenteHome = (Utente) getIntent().getSerializableExtra("utenteHome");

        asteAttive = new ArrayList<>();
        List<Asta> asteConcluse = new ArrayList<>();

        for(Asta a : listaAste) {
            if(a.getStato().toString().equals("ATTIVA")) {
                asteAttive.add(a);
            } else {
                asteConcluse.add(a);
            }
        }

        TextView noAuctionsText = findViewById(R.id.no_auctions_text);
        ImageButton backButton = findViewById(R.id.back_button);
        btnAttive = findViewById(R.id.btn_aste_attive);
        MaterialButton btnConcluse = findViewById(R.id.btn_aste_concluse);
        Button btnCrea = findViewById(R.id.crea_button);
        LinearLayout layoutAttributi = findViewById(R.id.layout_attributi);
        RecyclerView recyclerView = findViewById(R.id.risultati_recycler_view);
        ImageButton homeButton = findViewById(R.id.home_button);

        AtomicBoolean attiva = new AtomicBoolean(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AuctionAdapter adapter = new AuctionAdapter(asteAttive,this, true, true);
        recyclerView.setAdapter(adapter);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        btnAttive.setOnClickListener(view -> {
            attiva.set(true);
            btnAttive.setBackgroundColor(Color.parseColor("#FF0000"));
            btnConcluse.setBackgroundColor(Color.parseColor("#0E4273"));
            adapter.setAste(asteAttive, attiva.get());
            adapter.notifyDataSetChanged();
            if (asteAttive == null || asteAttive.isEmpty()) {
                noAuctionsText.setVisibility(View.VISIBLE);
                if (utenteHome.getId() == utente.getId()) {
                    btnCrea.setVisibility(View.VISIBLE);
                }
                int childCount = layoutAttributi.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = layoutAttributi.getChildAt(i);
                    child.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnConcluse.setOnClickListener(view -> {
            attiva.set(false);
            noAuctionsText.setVisibility(View.GONE);
            btnCrea.setVisibility(View.GONE);
            int childCount = layoutAttributi.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = layoutAttributi.getChildAt(i);
                child.setVisibility(View.VISIBLE);
            }
            btnConcluse.setBackgroundColor(Color.parseColor("#FF0000"));
            btnAttive.setBackgroundColor(Color.parseColor("#0E4273"));
            adapter.setAste(asteConcluse, attiva.get());
            adapter.notifyDataSetChanged();
        });

        backButton.setOnClickListener(v -> {
            openActivityProfilo();
            finish();
        });

        homeButton.setOnClickListener(v -> {
            openActivityHome();
            finish();
        });

        btnCrea.setOnClickListener(view -> openActivityCreaAsta());

        if(attiva.get() && (asteAttive == null || asteAttive.isEmpty())) {
            noAuctionsText.setVisibility(View.VISIBLE);
            if(utenteHome.getId() == utente.getId()) {
                btnCrea.setVisibility(View.VISIBLE);
            }
            int childCount = layoutAttributi.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = layoutAttributi.getChildAt(i);
                child.setVisibility(View.INVISIBLE);
            }
        }else {
            noAuctionsText.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openActivityProfilo();
        finish();
    }

    public void openActivityHome() {
        Intent intentR = new Intent(this, HomeActivity.class);
        intentR.putExtra("utente", utenteHome);
        startActivity(intentR);
    }

    private void openActivityProfilo() {
        Intent intentP = new Intent(this, ProfiloActivity.class);
        intentP.putExtra("utente", utente);
        intentP.putExtra("utenteHome", utenteHome);
        intentP.putExtra("fromDettagli", fromDettagli);
        intentP.putExtra("modificaAvvenuta", modificaAvvenuta);
        intentP.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intentP);
    }

    private void openActivityDettagliAsta() {
        Intent intent = new Intent(this, DettagliAstaActivity.class);
        intent.putExtra("listaAste", (Serializable) listaAste);
        intent.putExtra("utenteCreatore", utente);
        intent.putExtra("utente", utenteHome);
        intent.putExtra("utenteProfilo", utenteHome);
        intent.putExtra("asta", astaSelezionata);
        intent.putExtra("fromAsteCreate", true);
        intent.putExtra("fromDettagli", fromDettagli);
        intent.putExtra("modificaAvvenuta", modificaAvvenuta);
        intent.putExtra("fromHome", fromHome);
        startActivity(intent);
    }

    private void openActivityCreaAsta() {
        Intent intentR = new Intent(this, CreaAstaActivity.class);
        intentR.putExtra("utente", utenteHome);
        intentR.putExtra("fromHome", false);
        intentR.putExtra("listaAste", (Serializable) listaAste);
        intentR.putExtra("modificaAvvenuta", modificaAvvenuta);
        startActivity(intentR);
    }


    @Override
    public void onAstaClick(int position, boolean isAttive) {
        if (isAttive) {
            astaSelezionata = asteAttive.get(position);
            openActivityDettagliAsta();
        }
    }
}

