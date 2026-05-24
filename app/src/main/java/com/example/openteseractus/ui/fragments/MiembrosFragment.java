package com.example.openteseractus.ui.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.openteseractus.R;
import com.example.openteseractus.adapters.MiembroAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Grupo;
import com.example.openteseractus.modelos.MiembroGrupo;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.example.openteseractus.ui.agregar.AgregarUsuarioActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class MiembrosFragment extends Fragment {

    private String idGrupo;
    private Grupo grupoMf;

    private RecyclerView recyclerView;
    private MiembroAdapter adapter;
    private List<MiembroGrupo> miembros;

    private ImageView imgGrupo;

    private TextView tvNombreGrupo;

    private ImageButton btnAgregarMiembro;


    private GrupoRepository grupoRepository;

    public MiembrosFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        grupoRepository = new GrupoRepository();
        miembros = new ArrayList<>();

        if (getArguments() != null) {
            idGrupo = getArguments().getString("GRUPO_ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_miembros, container, false);

        imgGrupo = view.findViewById(R.id.imgGrupo);
        tvNombreGrupo = view.findViewById(R.id.tvNombreGrupo);
        btnAgregarMiembro = view.findViewById(R.id.btnAgregarMiembro);

        recyclerView = view.findViewById(R.id.recyclerMiembros);

        adapter = new MiembroAdapter(miembros);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnAgregarMiembro.setOnClickListener(v -> {
            mostrarOpcionesGrupo();
        });

        cargarGrupo();
        cargarMiembros();

        return view;
    }

    private void cargarGrupo() {

        grupoRepository.obtenerGrupo(idGrupo,
                new FirestoreCallback<Grupo>() {
                    @Override
                    public void onSuccess(Grupo grupo) {
                        grupoMf = grupo;

                        tvNombreGrupo.setText(grupo.getNomGrupo());

                        Glide.with(requireContext())
                                .load(grupo.getFotoGrupoUrl())
                                .placeholder(R.drawable.bg_rounded_border)
                                .error(R.drawable.bg_rounded_border)
                                .into(imgGrupo);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(
                                requireContext(),
                                error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void cargarMiembros() {

        grupoRepository.obtenerMiembrosGrupo(idGrupo,
                new FirestoreCallback<List<MiembroGrupo>>() {
                    @Override
                    public void onSuccess(List<MiembroGrupo> resultado) {
                        adapter.update(resultado);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(),
                                "Error: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarOpcionesGrupo() {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottomsheet_grupo_opciones, null);
        dialog.setContentView(view);
        if (view != null) {
            view.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.surface)
            );
        }

        LinearLayout btnInvitarUsuario = view.findViewById(R.id.btnInvitarUsuario);

        TextView tvCodigoGrupo = view.findViewById(R.id.tvCodigoGrupo);

        ImageButton btnMostrarCodigo = view.findViewById(R.id.btnMostrarCodigo);
        ImageButton btnCopiarCodigo = view.findViewById(R.id.btnCopiarCodigo);
        ImageButton btnRegenerarCodigo = view.findViewById(R.id.btnRegenerarCodigo);

        btnInvitarUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AgregarUsuarioActivity.class);

            intent.putExtra("GRUPO_ID", grupoMf.getId());
            intent.putExtra("NOMBRE_GRUPO", grupoMf.getNomGrupo());

            startActivity(intent);

            dialog.dismiss();
        });

        final boolean[] visible = {false};

        btnMostrarCodigo.setOnClickListener(v -> {
            visible[0] = !visible[0];

            if (visible[0]) {
                tvCodigoGrupo.setText(grupoMf.getCodInvitacion());
            } else {
                tvCodigoGrupo.setText("••••••");
            }
        });

        btnCopiarCodigo.setOnClickListener(v -> {
            ClipboardManager clipboard =
                    (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText("codigoGrupo", grupoMf.getCodInvitacion());

            clipboard.setPrimaryClip(clip);

            Toast.makeText(
                    requireContext(),
                    "Código copiado",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnRegenerarCodigo.setOnClickListener(v -> {

            grupoRepository.regenerarCodigoInvitacion(
                    grupoMf.getId(), new FirestoreCallback<String>() {

                        @Override
                        public void onSuccess(String nuevoCodigo) {
                            grupoMf.setCodInvitacion(nuevoCodigo);
                            tvCodigoGrupo.setText(nuevoCodigo);

                            Toast.makeText(
                                    requireContext(),
                                    "Código regenerado",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(
                                    requireContext(),
                                    error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        dialog.show();
    }
}