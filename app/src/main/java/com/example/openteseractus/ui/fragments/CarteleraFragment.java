package com.example.openteseractus.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import android.view.MotionEvent;

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

        // Permitir scroll horizontal dentro del ViewPager2 detectando
        // la dirección del gesto: solo bloquea la intercepción del padre
        // cuando el movimiento es claramente horizontal.
        habilitarScrollHorizontal(recyclerPeliculas);
        habilitarScrollHorizontal(recyclerSeries);

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

    /**
     * Registra un touch listener en el RecyclerView para que el ViewPager2
     * no intercepte los gestos horizontales cuando el usuario está desplazando
     * la lista. Solo cede el control al padre cuando el gesto es claramente
     * vertical (para que el NestedScrollView pueda desplazarse).
     */
    private void habilitarScrollHorizontal(RecyclerView recyclerView) {

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private float startX, startY;

            @Override
            public boolean onInterceptTouchEvent(
                    @NonNull RecyclerView rv,
                    @NonNull MotionEvent e
            ) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = e.getX();
                        startY = e.getY();
                        // En el primer toque bloqueamos la intercepción del padre
                        // para que el siguiente ACTION_MOVE llegue aquí primero.
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(e.getX() - startX);
                        float dy = Math.abs(e.getY() - startY);
                        // Mantenemos el bloqueo solo si el gesto es horizontal;
                        // si es vertical lo liberamos para el NestedScrollView.
                        rv.getParent().requestDisallowInterceptTouchEvent(dx >= dy);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        rv.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });
    }
}