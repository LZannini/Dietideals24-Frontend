package com.example.dietideals24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dietideals24.adapters.NotificaAdapter;
import com.example.dietideals24.api.ApiService;
import com.example.dietideals24.dto.AstaDTO;
import com.example.dietideals24.dto.AstaInversaDTO;
import com.example.dietideals24.dto.AstaRibassoDTO;
import com.example.dietideals24.dto.AstaSilenziosaDTO;
import com.example.dietideals24.dto.NotificaDTO;
import com.example.dietideals24.dto.UtenteDTO;
import com.example.dietideals24.models.Asta;
import com.example.dietideals24.models.AstaInversa;
import com.example.dietideals24.models.AstaRibasso;
import com.example.dietideals24.models.AstaSilenziosa;
import com.example.dietideals24.models.Notifica;
import com.example.dietideals24.models.Utente;
import com.example.dietideals24.retrofit.RetrofitService;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class NotificaActivity extends AppCompatActivity implements NotificaAdapter.OnAstaClickListener {

    private ListView listView;
    private NotificaAdapter adapter;
    private List<NotificaDTO> listaNotifiche;
    private Utente utente;
    private Utente utenteCreatore;
    private Asta astaRicevuta;
    private ImageButton backButton;
    private TextView noResultsText;
    private ApiService apiService;
    private Button btnSegnaTutte, btnRimuoviLette, btnRimuoviTutte;

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifica);

        utente = (Utente) getIntent().getSerializableExtra("utente");
        astaRicevuta = (Asta) getIntent().getSerializableExtra("asta_ricevuta");

        noResultsText = findViewById(R.id.no_results_text);

        backButton = findViewById(R.id.back_button);
        btnSegnaTutte = findViewById(R.id.btnSegna);
        btnRimuoviLette = findViewById(R.id.btnRmvRead);
        btnRimuoviTutte = findViewById(R.id.btnRmvAll);

        backButton.setOnClickListener(v -> {
            openActivityHome(utente);
            finish();
        });

        btnSegnaTutte.setOnClickListener(v -> mostraDialogoMarcaTutte());

        btnRimuoviLette.setOnClickListener(v -> mostraDialogoEliminaLette());

        btnRimuoviTutte.setOnClickListener(v -> mostraDialogoEliminaTutte());

        apiService = RetrofitService.getRetrofit(this).create(ApiService.class);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        listView = findViewById(R.id.notifiche_list_view);

        listaNotifiche = (List<NotificaDTO>) getIntent().getSerializableExtra("listaNotifiche");
        if (listaNotifiche == null || listaNotifiche.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
            btnSegnaTutte.setEnabled(false);
            btnRimuoviTutte.setEnabled(false);
            btnRimuoviLette.setEnabled(false);
        }

            adapter = new NotificaAdapter(this, listaNotifiche);
            adapter.setOnAstaClickListener(this);
            listView.setAdapter(adapter);

            for (NotificaDTO notifica : listaNotifiche) {
                if(notifica.getIdAsta()!=0)
                recuperaAsta(notifica, apiService);
            }

    }

    private void mostraDialogoEliminaTutte() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Vuoi davvero eliminare tutte le notifiche?");
        builder.setPositiveButton("Elimina", (dialog, which) -> {
            svuotaNotifiche(apiService);
        });
        builder.setNegativeButton("Annulla", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostraDialogoEliminaLette() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Vuoi davvero eliminare tutte le notifiche lette?");
        builder.setPositiveButton("Elimina", (dialog, which) -> {
            rimuoviNotificheLette(apiService);
        });
        builder.setNegativeButton("Annulla", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostraDialogoMarcaTutte() {
        Call<Void> call = apiService.segnaTutteLeNotifiche(utente.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    for(NotificaDTO notifica : listaNotifiche) {
                        notifica.setLetta(true);
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(NotificaActivity.this, "Tutte le notifiche sono state segnate come lette", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NotificaActivity.this, "Errore durante la marcatura delle notifiche, riprova!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(NotificaActivity.this, "Errore durante la marcatura delle notifiche, riprova!", Toast.LENGTH_SHORT).show();
                Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }

    private void rimuoviNotificheLette(ApiService apiService) {
        Call<Void> call = apiService.rimuoviAllNotificheLette(utente.getId());
        call.enqueue(new Callback<Void>() {
            @SuppressLint("NewApi")
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    listaNotifiche.removeIf(NotificaDTO::isLetta);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(NotificaActivity.this, "Tutte le notifiche lette sono state eliminate", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NotificaActivity.this, "Errore durante l'eliminazione delle notifiche lette, riprova!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(NotificaActivity.this, "Errore durante l'eliminazione delle notifiche lette, riprova!", Toast.LENGTH_SHORT).show();
                Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }

    private void svuotaNotifiche(ApiService apiService) {
        Call<Void> call = apiService.svuotaNotifiche(utente.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    listaNotifiche.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(NotificaActivity.this, "Tutte le notifiche sono state eliminate", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NotificaActivity.this, "Errore durante l'eliminazione delle notifiche", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(NotificaActivity.this, "Errore durante l'eliminazione delle notifiche, riprova!", Toast.LENGTH_SHORT).show();
                Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }

    private void rimuoviSelezionata(NotificaDTO notifica, ApiService apiService) {
        if (notifica != null) {
            Call<Void> call = apiService.rimuoviNotifica(notifica.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        adapter.removeNotifica(notifica);
                        Toast.makeText(NotificaActivity.this, "Notifica rimossa", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NotificaActivity.this, "Errore durante la rimozione della notifica", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(NotificaActivity.this, "Errore durante la rimozione della notifica, riprova!", Toast.LENGTH_SHORT).show();
                    Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                }
            });
        }
    }

    private void segnaComeLetta(NotificaDTO notifica, ApiService apiService) {
        if (notifica != null) {
            Call<Void> call = apiService.segnaNotifica(notifica.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        notifica.setLetta(true);
                        adapter.updateNotifica();
                    } else {
                        Toast.makeText(NotificaActivity.this, "Errore durante la marcatura della notifica, riprova!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(NotificaActivity.this, "Errore durante la marcatura della notifica, riprova!", Toast.LENGTH_SHORT).show();
                    Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                }
            });
        }
    }

    private void recuperaAsta(NotificaDTO notifica, ApiService apiService) {
            int idAsta = notifica.getIdAsta();
            apiService.recuperaAsta(idAsta).enqueue(new Callback<AstaDTO>() {
                @Override
                public void onResponse(@NonNull Call<AstaDTO> call, @NonNull Response<AstaDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AstaDTO astadto = response.body();
                        Asta asta = converteToModel(astadto);
                        astaRicevuta = asta;
                        if (asta instanceof AstaInversa) {
                            apiService.recuperaDettagliAstaInversa(idAsta).enqueue(new Callback<AstaInversaDTO>() {
                                @Override
                                public void onResponse(@NonNull Call<AstaInversaDTO> call, @NonNull Response<AstaInversaDTO> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String nomeAsta = asta.getNome();
                                        notifica.setNomeAsta(nomeAsta);
                                        astaRicevuta = (AstaInversa) asta;
                                        adapter.notifyDataSetChanged();
                                    } else
                                        Toast.makeText(NotificaActivity.this, "Asta Inversa non trovata", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(@NonNull Call<AstaInversaDTO> call, @NonNull Throwable t) {
                                    Toast.makeText(NotificaActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                                    Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                                }
                            });
                        } else if (asta instanceof AstaRibasso) {
                            apiService.recuperaDettagliAstaRibasso(idAsta).enqueue(new Callback<AstaRibassoDTO>() {
                                @Override
                                public void onResponse(@NonNull Call<AstaRibassoDTO> call, @NonNull Response<AstaRibassoDTO> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String nome_asta = asta.getNome();
                                        notifica.setNomeAsta(nome_asta);
                                        astaRicevuta = (AstaRibasso) asta;
                                        adapter.notifyDataSetChanged();
                                    } else
                                        Toast.makeText(NotificaActivity.this, "Asta Ribasso non trovata", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(@NonNull Call<AstaRibassoDTO> call, @NonNull Throwable t) {
                                    Toast.makeText(NotificaActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                                    Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                                }
                            });
                        } else if (asta instanceof AstaSilenziosa) {
                            apiService.recuperaDettagliAstaSilenziosa(idAsta).enqueue(new Callback<AstaSilenziosaDTO>() {
                                @Override
                                public void onResponse(@NonNull Call<AstaSilenziosaDTO> call, @NonNull Response<AstaSilenziosaDTO> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String nome_asta = asta.getNome();
                                        notifica.setNomeAsta(nome_asta);
                                        astaRicevuta = (AstaSilenziosa) asta;
                                        adapter.notifyDataSetChanged();
                                    } else
                                        Toast.makeText(NotificaActivity.this, "Asta Silenziosa non trovata", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(@NonNull Call<AstaSilenziosaDTO> call, @NonNull Throwable t) {
                                    Toast.makeText(NotificaActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                                    Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                                }
                            });
                        }
                    } else
                        Toast.makeText(NotificaActivity.this, "Asta non trovata", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(@NonNull Call<AstaDTO> call, @NonNull Throwable t) {
                    Toast.makeText(NotificaActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                    Logger.getLogger(NotificaActivity.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
                }
            });

    }

    private void recuperaUtenteCreatore(int idCreatore,ApiService apiService) {
        Call<UtenteDTO> call;
        call = apiService.recuperaUtente(idCreatore);
        call.enqueue(new Callback<UtenteDTO>() {
            @Override
            public void onResponse(@NonNull Call<UtenteDTO> call, @NonNull Response<UtenteDTO> response) {
                UtenteDTO user = response.body();
                if (user != null) {
                        utenteCreatore = creaCreatoreAsta(user);
                        openActivityDettagliAsta();

                }else
                    Toast.makeText(NotificaActivity.this, "Utente non trovato", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(@NonNull Call<UtenteDTO> call, @NonNull Throwable t) {
                Toast.makeText(NotificaActivity.this, "Errore di Connessione", Toast.LENGTH_SHORT).show();
                Logger.getLogger(Notifica.class.getName()).log(Level.SEVERE, "Errore rilevato", t);
            }
        });
    }

    public Utente creaCreatoreAsta(UtenteDTO utenteDTO) {
        Utente u = new Utente();
        u.setId(utenteDTO.getId());
        u.setUsername(utenteDTO.getUsername());
        u.setEmail(utenteDTO.getEmail());
        u.setPassword(utenteDTO.getPassword());
        u.setTipo(utenteDTO.getTipo());
        if (utenteDTO.getAvatar() != null) u.setAvatar(utenteDTO.getAvatar());
        if (utenteDTO.getBiografia() != null) u.setBiografia(utenteDTO.getBiografia());
        if (utenteDTO.getPaese() != null) u.setPaese(utenteDTO.getPaese());
        if (utenteDTO.getSitoweb() != null) u.setSitoweb(utenteDTO.getSitoweb());
        return u;
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

    public Asta converteToModel(AstaDTO asta) {
        Asta a = new Asta();

        switch (asta.getTipo()) {
            case "RIBASSO":
                a = creaModelloAstaR(asta);
                break;
            case "SILENZIOSA":
                a = creaModelloAstaS(asta);
                break;
            case "INVERSA":
                a = creaModelloAstaI(asta);
                break;
        }

        return a;
    }

    @Override
    public void onAstaClicked(NotificaDTO notifica) {

        recuperaAsta(notifica, apiService);

        if(!notifica.isLetta())
            segnaComeLetta(notifica,apiService);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (astaRicevuta != null)
                    recuperaUtenteCreatore(astaRicevuta.getIdCreatore(),apiService);
                else
                    handler.postDelayed(this, 100);
            }
        }, 100);

    }

    @Override
    public void onNotificaClicked(NotificaDTO notifica){
        segnaComeLetta(notifica, apiService);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        if(notifica.getNomeAsta() == null || notifica.getIdAsta() == 0)
        builder.setMessage(notifica.getTesto());
        else
            builder.setMessage(notifica.getTesto() + ' ' + notifica.getNomeAsta());

        builder.setPositiveButton("Rimuovi", (dialog, which) -> {
            rimuoviSelezionata(notifica, apiService);
        });

        builder.setNegativeButton("Chiudi", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        if (!notifica.isLetta()) {
            notifica.setLetta(true);
            adapter.updateNotifica();
        }
    }

    private void openActivityDettagliAsta() {
            Intent intent = new Intent(this, DettagliAstaActivity.class);
            intent.putExtra("asta", astaRicevuta);
            intent.putExtra("utenteCreatore",utenteCreatore);
            intent.putExtra("utente", utente);
            intent.putExtra("fromNotifica",true);
            intent.putExtra("listaNotifiche",(Serializable) listaNotifiche);
            startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openActivityHome(utente);
        finish();
    }

    private void openActivityHome(Utente utente) {
        Intent intentH = new Intent(this, HomeActivity.class);
        intentH.putExtra("utente", utente);
        startActivity(intentH);
    }
}
