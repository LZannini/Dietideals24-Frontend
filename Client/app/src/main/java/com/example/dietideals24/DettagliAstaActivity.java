package com.example.dietideals24;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dietideals24.adapters.OfferAdapter;
import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.AstaInversaDTO;
import com.example.dietideals24.dto.AstaRibassoDTO;
import com.example.dietideals24.dto.AstaSilenziosaDTO;
import com.example.dietideals24.dto.NotificaDTO;
import com.example.dietideals24.dto.OffertaDTO;
import com.example.dietideals24.enums.StatoAsta;
import com.example.dietideals24.enums.StatoOfferta;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.AstaInversa;
import com.example.dietideals24.models.AstaRibasso;
import com.example.dietideals24.models.AstaSilenziosa;
import com.example.dietideals24.models.Offerta;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class DettagliAstaActivity extends AppCompatActivity implements OfferAdapter.OnOffertaListener {

    private LinearLayout userSection;
    private TextView tvPriceValue;
    private TextView tvTimerValue;
    private TextView tvDecrementValue;
    private Asta asta;
    private List<NotificaDTO> notifiche;
    private List<Asta> listaAste;
    private String criterioRicerca;
    private Utente utente;
    private boolean fromAsteCreate;
    private boolean fromNotifica;
    private boolean fromDettagli;
    private boolean modificaAvvenuta;
    private Utente utenteCreatore;
    List<Offerta> offerte = new ArrayList<>();

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final int POLLING_INTERVAL = 1000;
    private static final int TIMER_INTERVAL = 1000;
    private Runnable pollingRunnable;
    private Runnable timerRunnable;
    private long timerValue;
    private boolean fromHome;
    private OfferAdapter adapter;


    @SuppressLint({"MissingInflatedId","NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagli_asta);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        boolean isRibasso = false;
        modificaAvvenuta = getIntent().getBooleanExtra("modificaAvvenuta", false);
        fromDettagli = getIntent().getBooleanExtra("fromDettagli", false);
        fromHome = getIntent().getBooleanExtra("fromHome", true);
        asta = (Asta) getIntent().getSerializableExtra("asta");
        listaAste = (List<Asta>) getIntent().getSerializableExtra("listaAste");
        if (getIntent().getStringExtra("criterioRicerca") != null)
            criterioRicerca = getIntent().getStringExtra("criterioRicerca");
        utente = (Utente) getIntent().getSerializableExtra("utente");
        Utente utenteProfilo = (Utente) getIntent().getSerializableExtra("utenteProfilo");
        fromAsteCreate = getIntent().getBooleanExtra("fromAsteCreate", false);
        utenteCreatore = (Utente) getIntent().getSerializableExtra("utenteCreatore");
        fromNotifica = getIntent().getBooleanExtra("fromNotifica", false);
        notifiche = (List<NotificaDTO>) getIntent().getSerializableExtra("listaNotifiche");

        userSection = findViewById(R.id.userSection);
        LinearLayout creatorSection = findViewById(R.id.creatorSection);
        ImageView ivFoto = findViewById(R.id.imageView_Foto);
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDescription = findViewById(R.id.etDescription);
        TextView tvCategoryValue = findViewById(R.id.tvCategoryValue);
        ImageView ivTypeValue = findViewById(R.id.ivTypeValue);
        TextView tvCreatorValue = findViewById(R.id.tvCreatorValue);
        tvPriceValue = findViewById(R.id.tvPriceValue);
        tvDecrementValue = findViewById(R.id.tvDecrementValue);
        tvTimerValue = findViewById(R.id.tvTimerValue);
        TextView tvLowestOffer = findViewById(R.id.tvLowestOffer);
        TextView tvLowestOfferValue = findViewById(R.id.tvLowestOfferValue);
        EditText etOffer = findViewById(R.id.etOffer);
        Button btnSubmitOffer = findViewById(R.id.btnSubmitOffer);
        ImageButton btnBack = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);

        if (asta.getIdCreatore() == utente.getId()) {
            creatorSection.setVisibility(View.VISIBLE);
            userSection.setVisibility(View.GONE);
        } else {
            userSection.setVisibility(View.VISIBLE);
            creatorSection.setVisibility(View.GONE);
        }

        ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);

        byte[] fotoBytes = asta.getFoto();
        if (fotoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
            ivFoto.setImageBitmap(bitmap);
        }

        etTitle.setText(asta.getNome());
        etDescription.setText(asta.getDescrizione());
        tvCategoryValue.setText(asta.getCategoria().toString());
        if (fromAsteCreate && !fromDettagli)
            tvCreatorValue.setText(utenteProfilo.getUsername());
        else
            tvCreatorValue.setText(utenteCreatore.getUsername());

        if (asta instanceof AstaRibasso) {
            ivTypeValue.setImageResource(R.drawable.ribasso);
            isRibasso = true;
            apiService.recuperaDettagliAstaRibasso(asta.getId())
                    .enqueue(new Callback<AstaRibassoDTO>() {
                        @Override
                        public void onResponse(@NonNull Call<AstaRibassoDTO> call, @NonNull Response<AstaRibassoDTO> response) {
                            AstaRibassoDTO astaRicevuta = response.body();

                            tvLowestOffer.setVisibility(View.GONE);
                            tvLowestOfferValue.setVisibility(View.GONE);
                            tvPriceValue.setText(NumberFormat.getCurrencyInstance(Locale.ITALY).format(astaRicevuta.getPrezzo()));
                            tvDecrementValue.setText(NumberFormat.getCurrencyInstance(Locale.ITALY).format(astaRicevuta.getDecremento()));
                            tvTimerValue.setText(astaRicevuta.getTimer());

                            setupPolling();
                            handler.post(pollingRunnable);
                            handler.post(timerRunnable);

                            etOffer.setEnabled(false);
                            etOffer.setText(String.valueOf(astaRicevuta.getPrezzo()));
                        }

                        @Override
                        public void onFailure(@NonNull Call<AstaRibassoDTO> call, @NonNull Throwable t) {
                            Logger.getLogger(DettagliAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                        }
                    });
        } else if (asta instanceof AstaSilenziosa) {
            ivTypeValue.setImageResource(R.drawable.silenziosa);
            apiService.recuperaDettagliAstaSilenziosa(asta.getId())
                    .enqueue(new Callback<AstaSilenziosaDTO>() {
                        @Override
                        public void onResponse(@NonNull Call<AstaSilenziosaDTO> call, @NonNull Response<AstaSilenziosaDTO> response) {
                            AstaSilenziosaDTO astaRicevuta = response.body();

                            tvLowestOffer.setVisibility(View.GONE);
                            tvLowestOfferValue.setVisibility(View.GONE);

                            LocalDateTime scadenzaAsta = LocalDateTime.parse(astaRicevuta.getScadenza(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                            tvTimerValue.setText(calcolaTempoRimanente(scadenzaAsta));
                        }

                        @Override
                        public void onFailure(@NonNull Call<AstaSilenziosaDTO> call, @NonNull Throwable t) {
                            Logger.getLogger(DettagliAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);

                        }
                    });
        } else if (asta instanceof AstaInversa) {
            ivTypeValue.setImageResource(R.drawable.inversa);
            apiService.recuperaDettagliAstaInversa(asta.getId())
                    .enqueue(new Callback<AstaInversaDTO>() {
                        @Override
                        public void onResponse(@NonNull Call<AstaInversaDTO> call, @NonNull Response<AstaInversaDTO> response) {
                            AstaInversaDTO astaRicevuta = response.body();

                            if (astaRicevuta.getOffertaMinore() != null) {
                                tvLowestOfferValue.setText(NumberFormat.getCurrencyInstance(Locale.ITALY).format(astaRicevuta.getOffertaMinore()));
                            } else {
                                tvLowestOfferValue.setText("Ancora nessuna offerta per questa asta.");
                            }
                            tvPriceValue.setText(NumberFormat.getCurrencyInstance(Locale.ITALY).format(astaRicevuta.getPrezzo()));

                            LocalDateTime scadenzaAsta = LocalDateTime.parse(astaRicevuta.getScadenza(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            tvTimerValue.setText(calcolaTempoRimanente(scadenzaAsta));
                        }

                        @Override
                        public void onFailure(@NonNull Call<AstaInversaDTO> call, @NonNull Throwable t) {
                            Logger.getLogger(DettagliAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                        }
                    });
        }

        if (asta.getIdCreatore() == utente.getId() && !isRibasso) {
            RecyclerView recyclerView = findViewById(R.id.recyclerViewOfferte);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new OfferAdapter(offerte, this);
            recyclerView.setAdapter(adapter);
            apiService.recuperaOffertePerId(asta.getId())
                    .enqueue(new Callback<List<OffertaDTO>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<OffertaDTO>> call, @NonNull Response<List<OffertaDTO>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<OffertaDTO> offerteResponse = response.body();
                                List<Offerta> offerteList = creaListModelloOfferta(offerteResponse);
                                for (Offerta o : offerteList) {
                                    offerte.add(o);
                                    adapter.notifyDataSetChanged();
                                }

                            } else if (response.body() != null) {
                                userSection.setVisibility(View.GONE);
                            }

                        }

                        @Override
                        public void onFailure(@NonNull Call<List<OffertaDTO>> call, @NonNull Throwable t) {
                            Logger.getLogger(DettagliAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                        }
                    });
        }

        adjustEditTextWidth(etTitle);


        btnBack.setOnClickListener(v -> {
            if (fromAsteCreate)
                openActivityAsteCreate(listaAste, false);
            else if (fromNotifica)
                openActivityNotifica();
            else
                openActivityRisultatiRicerca();
            finish();
        });

        homeButton.setOnClickListener(v -> {
            openActivityHome();
            finish();
        });

        if (asta.getIdCreatore() != utente.getId() && !fromAsteCreate) {
            tvCreatorValue.setTypeface(tvCreatorValue.getTypeface(), Typeface.BOLD);
            tvCreatorValue.setOnClickListener(view -> openActivityProfilo());
        }

        btnSubmitOffer.setOnClickListener(v -> {
            if (!etOffer.getText().toString().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DettagliAstaActivity.this);
                builder.setMessage("Sei sicuro di voler presentare l'offerta?")
                        .setCancelable(true)
                        .setPositiveButton("Si", (dialogInterface, i) -> {
                            OffertaDTO offerta = new OffertaDTO();
                            offerta.setIdAsta(asta.getId());
                            offerta.setIdUtente(utente.getId());
                            offerta.setValore(Float.parseFloat(etOffer.getText().toString()));
                            offerta.setStato(StatoOfferta.ATTESA);

                            LocalDateTime currentDateTime = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            String dateTimeString = currentDateTime.format(formatter);

                            offerta.setData(dateTimeString);

                            apiService.creaOfferta(offerta)
                                    .enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                            Toast.makeText(DettagliAstaActivity.this, "Offerta presentata con successo!", Toast.LENGTH_SHORT).show();
                                            if (fromAsteCreate)
                                                openActivityAsteCreate(listaAste, false);
                                            else if (fromNotifica)
                                                openActivityNotifica();
                                            else
                                                openActivityRisultatiRicerca();
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                            Toast.makeText(DettagliAstaActivity.this, "Errore durante la creazione dell'offerta!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            finish();
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> {
                        })
                        .show();
            } else {
                Toast.makeText(DettagliAstaActivity.this, "Bisogna inserire un importo valido!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (fromAsteCreate)
            openActivityAsteCreate(listaAste, false);
        else if(fromNotifica)
            openActivityNotifica();
        else
            openActivityRisultatiRicerca();
        finish();
    }

    private void openActivityRisultatiRicerca() {
        Intent intent = new Intent(this, RisultatiRicercaActivity.class);
        intent.putExtra("listaAste", (Serializable) listaAste);
        intent.putExtra("criterioRicerca", criterioRicerca);
        intent.putExtra("utente", utente);
        intent.putExtra("fromHome", fromHome);
        startActivity(intent);
    }

    private void openActivityNotifica(){
        Intent intent = new Intent(DettagliAstaActivity.this,NotificaActivity.class);
        intent.putExtra("listaNotifiche", (Serializable) notifiche);
        intent.putExtra("utente",utente);
        intent.putExtra("astaRicevuta",asta);
        startActivity(intent);

    }

    public void openActivityHome() {
        Intent intentR = new Intent(this, HomeActivity.class);
        intentR.putExtra("utente", utente);
        startActivity(intentR);
    }

    private void openActivityAsteCreate(List<Asta> listaAste, boolean offertaAccettata) {
        Intent intent = new Intent(this, AsteCreateActivity.class);
        intent.putExtra("listaAste", (Serializable) listaAste);
        intent.putExtra("utenteHome", utente);
        intent.putExtra("utente", utenteCreatore);
        intent.putExtra("utenteCreatore", utenteCreatore);
        intent.putExtra("fromDettagli", fromDettagli);
        intent.putExtra("modificaAvvenuta", modificaAvvenuta);
        intent.putExtra("fromHome", fromHome);
        if (offertaAccettata) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
        finish();
    }

    private void openActivityProfilo() {
        Intent intentR = new Intent(this, ProfiloActivity.class);
        intentR.putExtra("utenteHome", utente);
        intentR.putExtra("utente", utenteCreatore);
        intentR.putExtra("fromDettagli", true);
        intentR.putExtra("fromHome", fromHome);
        startActivity(intentR);
    }

    private void adjustEditTextWidth(EditText editText) {
        String text = editText.getText().toString();
        Paint paint = editText.getPaint();
        float textWidth = paint.measureText(text);

        int padding = editText.getPaddingLeft() + editText.getPaddingRight();
        editText.setWidth((int) (textWidth + padding));
    }

    private void setupPolling() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                aggiornaDettagliAsta();
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                handler.postDelayed(this, TIMER_INTERVAL);
            }
        };
    }

    private void aggiornaDettagliAsta() {
        ApiService apiService = RetrofitService.getRetrofit(this).create(ApiService.class);


        apiService.recuperaDettagliAstaRibasso(asta.getId())
                .enqueue(new Callback<AstaRibassoDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<AstaRibassoDTO> call, @NonNull Response<AstaRibassoDTO> response) {
                        AstaRibassoDTO astaR = response.body();
                        updateUI(astaR);
                    }

                    @Override
                    public void onFailure(@NonNull Call<AstaRibassoDTO> call, @NonNull Throwable t) {
                        Logger.getLogger(DettagliAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                    }
                });
    }

    private void updateUI(AstaRibassoDTO asta) {
        if (asta != null) {
            tvPriceValue.setText(NumberFormat.getCurrencyInstance(Locale.ITALY).format(asta.getPrezzo()));
            tvDecrementValue.setText(NumberFormat.getCurrencyInstance(Locale.ITALY).format(asta.getDecremento()));
            timerValue = convertToMilliseconds(asta.getTimer());
        }
    }

    private long convertToMilliseconds(String timer) {
        String[] parts = timer.split(":");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);
        return (hours * 3600 + minutes * 60 + seconds) * 1000;
    }

    private void updateTimer() {
        if (timerValue > 0) {
            timerValue -= 1000;
            tvTimerValue.setText(formatMilliseconds(timerValue));
        } else {
            aggiornaDettagliAsta();
        }
    }

    private String formatMilliseconds(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(pollingRunnable);
        handler.removeCallbacks(timerRunnable);
    }

    @SuppressLint("NewApi")
    private String calcolaTempoRimanente(LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDateTime)) {
            return "Scaduta";
        }

        Period period = Period.between(now.toLocalDate(), endDateTime.toLocalDate());
        Duration duration = Duration.between(now.toLocalTime(), endDateTime.toLocalTime());

        if (period.getYears() > 0) {
            return period.getYears() + " anni, " + period.getMonths() + " mesi";
        } else if (period.getMonths() > 0) {
            return period.getMonths() + " mesi, " + period.getDays() + " giorni";
        } else if (period.getDays() > 0) {
            return period.getDays() + " giorni";
        } else if (duration.toHours() > 0) {
            return duration.toHours() + " ore";
        } else if (duration.toMinutes() > 0) {
            return duration.toMinutes() + " minuti";
        } else {
            return "meno di un minuto";
        }
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
            offerteList.add(offerta);
        }
        return offerteList;
    }

    @Override
    public void onAcceptClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sei sicuro di voler accettare questa offerta?")
                .setCancelable(true)
                .setPositiveButton("Si", (dialogInterface, i) -> {
                    ApiService apiService = RetrofitService.getRetrofit(DettagliAstaActivity.this).create(ApiService.class);

                    apiService.accettaOfferta(offerte.get(position).getId())
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                    Toast.makeText(DettagliAstaActivity.this, "Hai accettato l'offerta con successo!", Toast.LENGTH_SHORT).show();
                                    if (listaAste != null) {
                                        for (Asta a : listaAste) {
                                            if (a.getId() == asta.getId())
                                                a.setStato(StatoAsta.VENDUTA);
                                        }
                                        openActivityAsteCreate(listaAste, true);
                                    } else {
                                        asta.setStato(StatoAsta.VENDUTA);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                    Logger.getLogger(DettagliAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                                }
                            });
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    @Override
    public void onRejectClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sei sicuro di voler rifiutare questa offerta?")
                .setCancelable(true)
                .setPositiveButton("Si", (dialogInterface, i) -> {
                    ApiService apiService = RetrofitService.getRetrofit(DettagliAstaActivity.this).create(ApiService.class);

                    Offerta offerta = offerte.get(position);
                    apiService.rifiutaOfferta(offerta.getId())
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                    offerte.remove(offerta);
                                    adapter.notifyItemRemoved(position);
                                    Toast.makeText(DettagliAstaActivity.this, "Hai rifiutato l'offerta con successo!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                    Logger.getLogger(DettagliAstaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                                }
                            });
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

}

