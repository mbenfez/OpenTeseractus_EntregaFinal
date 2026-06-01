package com.example.openteseractus.ui.crear;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openteseractus.R;
import com.example.openteseractus.adapters.TMDBMediaAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Teseracto;
import com.example.openteseractus.repositorios.TeseractoRepository;
import com.example.openteseractus.tmdb.TMDBRepository;
import com.example.openteseractus.tmdb.TMDBMedia;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrearTeseractoActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerResultados;
    private TMDBMediaAdapter adapter;
    private List<TMDBMedia> resultados;

    private TMDBRepository tmdbRepository;
    private TeseractoRepository teseractoRepository;

    private String idGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_teseracto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        idGrupo = getIntent().getStringExtra("GRUPO_ID");

        resultados = new ArrayList<>();
        tmdbRepository = new TMDBRepository();
        teseractoRepository = new TeseractoRepository();

        searchView = findViewById(R.id.searchView);
        recyclerResultados = findViewById(R.id.recyclerResultados);

        adapter = new TMDBMediaAdapter(resultados, media -> comprobarTeseracto(media));

        recyclerResultados.setLayoutManager(new LinearLayoutManager(this));
        recyclerResultados.setAdapter(adapter);

        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscar(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    resultados.clear();
                    adapter.notifyDataSetChanged();
                    recyclerResultados.setVisibility(View.GONE);
                } else if (newText.trim().length() >= 2) {
                    buscar(newText);
                }
                return true;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void buscar(String query) {
        tmdbRepository.buscar(query, new FirestoreCallback<List<TMDBMedia>>() {
            @Override
            public void onSuccess(List<TMDBMedia> resultado) {
                resultados.clear();
                resultados.addAll(resultado);
                adapter.notifyDataSetChanged();
                recyclerResultados.setVisibility(resultados.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CrearTeseractoActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void comprobarTeseracto(TMDBMedia media) {
        teseractoRepository.existeTeseracto(
                idGrupo, media.getId(), media.getMediaType(), new FirestoreCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean existe) {
                        if (existe) {
                            Toast.makeText(
                                    CrearTeseractoActivity.this,
                                    "Ese teseracto ya existe en este grupo",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                        crearTeseracto(media);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(CrearTeseractoActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void crearTeseracto(TMDBMedia media) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.create_teseract)
                .setMessage(getString(R.string.create_teseract_from) + media.getTitulo() + "?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Teseracto teseracto = new Teseracto();
                    teseracto.setId(UUID.randomUUID().toString());
                    teseracto.setIdGrupo(idGrupo);
                    teseracto.setUidAbiertoPor(uid);
                    teseracto.setTmdbId(media.getId());
                    teseracto.setTitulo(media.getTitulo());
                    teseracto.setMediaType(media.getMediaType());
                    teseracto.setPosterUrl(media.getPosterUrl());
                    teseracto.setNotaMedia(0);
                    teseracto.setNumValoraciones(0);
                    teseracto.setFechaApertura(System.currentTimeMillis());
                    teseracto.setUltimaActividad(System.currentTimeMillis());

                    teseractoRepository.crearTeseracto(teseracto, new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void resultado) {
                            Toast.makeText(
                                    CrearTeseractoActivity.this,
                                    "Teseracto creado",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(CrearTeseractoActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
