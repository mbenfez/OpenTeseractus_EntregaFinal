package com.example.openteseractus.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openteseractus.R;
import com.example.openteseractus.adapters.TeseractoAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Teseracto;
import com.example.openteseractus.repositorios.TeseractoRepository;
import com.example.openteseractus.ui.crear.CrearTeseractoActivity;

import java.util.ArrayList;
import java.util.List;

public class CarteleraFragment extends Fragment {

    private String idGrupo;

    private RecyclerView recyclerPeliculas, recyclerSeries;

    private TeseractoAdapter peliculasAdapter;
    private TeseractoAdapter seriesAdapter;

    private ImageButton btnCrear;

    private List<Teseracto> peliculas;
    private List<Teseracto> series;

    private TeseractoRepository teseractoRepository;

    public CarteleraFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        peliculas = new ArrayList<>();
        series = new ArrayList<>();

        teseractoRepository = new TeseractoRepository();

        if (getArguments() != null) {
            idGrupo = getArguments().getString("GRUPO_ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_cartelera,
                container,
                false
        );

        recyclerPeliculas = view.findViewById(R.id.recyclerPeliculas);
        recyclerSeries = view.findViewById(R.id.recyclerSeries);

        // evitar que la funcionalidad de fragment no se superponga al slide lateral
        recyclerPeliculas.addOnItemTouchListener(
                new RecyclerView.SimpleOnItemTouchListener() {

                    @Override
                    public boolean onInterceptTouchEvent(
                            @NonNull RecyclerView rv,
                            @NonNull android.view.MotionEvent e
                    ) {
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
        recyclerSeries.addOnItemTouchListener(
                new RecyclerView.SimpleOnItemTouchListener() {

                    @Override
                    public boolean onInterceptTouchEvent(
                            @NonNull RecyclerView rv,
                            @NonNull android.view.MotionEvent e
                    ) {
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });

        btnCrear = view.findViewById(R.id.btnCrearTeseracto);

        peliculasAdapter = new TeseractoAdapter(peliculas);
        seriesAdapter = new TeseractoAdapter(series);

        recyclerPeliculas.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerSeries.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerPeliculas.setAdapter(peliculasAdapter);
        recyclerSeries.setAdapter(seriesAdapter);

        cargarTeseractos();
        crearTeseractos();

        return view;
    }

    private void cargarTeseractos() {

        teseractoRepository.obtenerPorGrupo(
                idGrupo, new FirestoreCallback<List<Teseracto>>() {

                    @Override
                    public void onSuccess(List<Teseracto> resultado) {

                        peliculas.clear();
                        series.clear();

                        for (Teseracto t : resultado) {
                            if ("movie".equals(t.getMediaType())) {
                                peliculas.add(t);
                            } else if ("tv".equals(t.getMediaType())) {
                                series.add(t);
                            }
                        }

                        peliculasAdapter.notifyDataSetChanged();
                        seriesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String error) {

                        Toast.makeText(
                                getContext(),
                                "Error: " + error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void crearTeseractos() {

        btnCrear.setOnClickListener(v -> {
            Intent intent = new Intent(
                    requireActivity(),
                    CrearTeseractoActivity.class
            );
            intent.putExtra("GRUPO_ID", idGrupo);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {

        super.onResume();
        cargarTeseractos();
    }
}