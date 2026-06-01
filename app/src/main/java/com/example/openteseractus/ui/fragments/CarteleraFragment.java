package com.example.openteseractus.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CarteleraFragment extends Fragment {

    private String idGrupo;

    private RecyclerView recyclerPeliculas, recyclerSeries;
    private TeseractoAdapter peliculasAdapter;
    private TeseractoAdapter seriesAdapter;
    private ImageButton btnCrear;
    private TextInputEditText etBuscar;

    private List<Teseracto> peliculas = new ArrayList<>();
    private List<Teseracto> series = new ArrayList<>();

    private TeseractoRepository teseractoRepository;

    public CarteleraFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teseractoRepository = new TeseractoRepository();
        if (getArguments() != null) {
            idGrupo = getArguments().getString("GRUPO_ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cartelera, container, false);

        recyclerPeliculas = view.findViewById(R.id.recyclerPeliculas);
        recyclerSeries    = view.findViewById(R.id.recyclerSeries);
        btnCrear          = view.findViewById(R.id.btnCrearTeseracto);
        etBuscar          = view.findViewById(R.id.etBuscar);

        habilitarScrollHorizontal(recyclerPeliculas);
        habilitarScrollHorizontal(recyclerSeries);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        peliculasAdapter = new TeseractoAdapter(peliculas, uid);
        seriesAdapter    = new TeseractoAdapter(series,    uid);

        recyclerPeliculas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerSeries.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerPeliculas.setAdapter(peliculasAdapter);
        recyclerSeries.setAdapter(seriesAdapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                peliculasAdapter.filtrar(s.toString());
                seriesAdapter.filtrar(s.toString());
            }
        });

        cargarTeseractos();

        btnCrear.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CrearTeseractoActivity.class);
            intent.putExtra("GRUPO_ID", idGrupo);
            startActivity(intent);
        });

        return view;
    }

    private void cargarTeseractos() {
        teseractoRepository.obtenerPorGrupo(idGrupo, new FirestoreCallback<List<Teseracto>>() {
            @Override
            public void onSuccess(List<Teseracto> resultado) {
                List<Teseracto> nuevasPeliculas = new ArrayList<>();
                List<Teseracto> nuevasSeries = new ArrayList<>();

                for (Teseracto t : resultado) {
                    if ("movie".equals(t.getMediaType())) nuevasPeliculas.add(t);
                    else if ("tv".equals(t.getMediaType())) nuevasSeries.add(t);
                }

                peliculasAdapter.update(nuevasPeliculas);
                seriesAdapter.update(nuevasSeries);

                // Reaplicar filtro activo si hay texto
                if (etBuscar != null && etBuscar.getText() != null) {
                    String q = etBuscar.getText().toString();
                    if (!q.isEmpty()) {
                        peliculasAdapter.filtrar(q);
                        seriesAdapter.filtrar(q);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarTeseractos();
    }

    private void habilitarScrollHorizontal(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private float startX, startY;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = e.getX();
                        startY = e.getY();
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(e.getX() - startX);
                        float dy = Math.abs(e.getY() - startY);
                        rv.getParent().requestDisallowInterceptTouchEvent(dx >= dy);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        rv.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }

            @Override public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}
            @Override public void onRequestDisallowInterceptTouchEvent(boolean b) {}
        });
    }
}
