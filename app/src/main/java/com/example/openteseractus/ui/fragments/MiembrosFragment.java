package com.example.openteseractus.ui.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MiembrosFragment extends Fragment {

    private String idGrupo;
    private Grupo grupoMf;
    private boolean esAdmin = false;
    private final String uidActual = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

    private RecyclerView recyclerView;
    private MiembroAdapter adapter;
    private List<MiembroGrupo> miembros = new ArrayList<>();

    private ImageView imgGrupo;
    private ImageView btnEditarFoto;
    private TextView tvNombreGrupo;
    private ImageButton btnEditarNombre;
    private ImageButton btnAgregarMiembro;
    private TextInputEditText etBuscarMiembro;

    private GrupoRepository grupoRepository;

    private ActivityResultLauncher<String> fotoLauncher;

    public MiembrosFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        grupoRepository = new GrupoRepository();
        if (getArguments() != null) {
            idGrupo = getArguments().getString("GRUPO_ID");
        }

        fotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) subirFotoGrupo(uri);
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_miembros, container, false);

        imgGrupo        = view.findViewById(R.id.imgGrupo);
        tvNombreGrupo   = view.findViewById(R.id.tvNombreGrupo);
        btnEditarFoto   = view.findViewById(R.id.btnEditarFoto);
        btnEditarNombre = view.findViewById(R.id.btnEditarNombre);
        btnAgregarMiembro = view.findViewById(R.id.btnAgregarMiembro);
        etBuscarMiembro = view.findViewById(R.id.etBuscarMiembro);
        recyclerView    = view.findViewById(R.id.recyclerMiembros);

        adapter = new MiembroAdapter(miembros, uidActual);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnAgregarMiembro.setOnClickListener(v -> mostrarOpcionesGrupo());
        btnEditarNombre.setOnClickListener(v -> mostrarDialogoEditarNombre());
        btnEditarFoto.setOnClickListener(v -> fotoLauncher.launch("image/*"));

        etBuscarMiembro.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
        });

        cargarGrupo();
        cargarMiembros();

        return view;
    }

    private void cargarGrupo() {
        grupoRepository.obtenerGrupo(idGrupo, new FirestoreCallback<Grupo>() {
            @Override
            public void onSuccess(Grupo grupo) {
                grupoMf = grupo;
                tvNombreGrupo.setText(grupo.getNomGrupo());

                Glide.with(requireContext())
                        .load(grupo.getFotoGrupoUrl())
                        .placeholder(R.drawable.bg_rounded_border)
                        .error(R.drawable.bg_rounded_border)
                        .into(imgGrupo);

                verificarAdmin();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarAdmin() {
        grupoRepository.obtenerMiembrosGrupo(idGrupo, new FirestoreCallback<List<MiembroGrupo>>() {
            @Override
            public void onSuccess(List<MiembroGrupo> resultado) {
                for (MiembroGrupo m : resultado) {
                    if (uidActual.equals(m.getUidMiembro()) && m.esAdmin()) {
                        esAdmin = true;
                        break;
                    }
                }
                mostrarControlesAdmin();
            }

            @Override
            public void onFailure(String error) {}
        });
    }

    private void mostrarControlesAdmin() {
        if (esAdmin) {
            btnEditarFoto.setVisibility(View.VISIBLE);
            btnEditarNombre.setVisibility(View.VISIBLE);
            adapter.setOnMiembroLongClickListener((miembro, username) ->
                    mostrarOpcionesAdminMiembro(miembro, username));
        }
    }

    private void cargarMiembros() {
        grupoRepository.obtenerMiembrosGrupo(idGrupo, new FirestoreCallback<List<MiembroGrupo>>() {
            @Override
            public void onSuccess(List<MiembroGrupo> resultado) {
                // Mover usuario actual al principio
                List<MiembroGrupo> ordenados = new ArrayList<>();
                MiembroGrupo miActual = null;
                for (MiembroGrupo m : resultado) {
                    if (uidActual.equals(m.getUidMiembro())) {
                        miActual = m;
                    } else {
                        ordenados.add(m);
                    }
                }
                if (miActual != null) ordenados.add(0, miActual);
                adapter.update(ordenados);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarOpcionesAdminMiembro(MiembroGrupo miembro, String username) {
        boolean puedeAscender = !"admin".equalsIgnoreCase(miembro.getRol());

        String[] opciones = puedeAscender
                ? new String[]{"Ascender a admin", "Expulsar del grupo"}
                : new String[]{"Expulsar del grupo"};

        new AlertDialog.Builder(requireContext())
                .setTitle(username)
                .setItems(opciones, (dialog, which) -> {
                    if (puedeAscender) {
                        if (which == 0) ascenderMiembro(miembro, username);
                        else expulsarMiembro(miembro, username);
                    } else {
                        expulsarMiembro(miembro, username);
                    }
                })
                .show();
    }

    private void ascenderMiembro(MiembroGrupo miembro, String username) {
        grupoRepository.ascenderAAdmin(idGrupo, miembro.getUidMiembro(), new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void v) {
                Toast.makeText(requireContext(), username + " es ahora admin", Toast.LENGTH_SHORT).show();
                cargarMiembros();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void expulsarMiembro(MiembroGrupo miembro, String username) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Expulsar a " + username)
                .setMessage("¿Seguro que quieres expulsar a " + username + " del grupo?")
                .setPositiveButton("Expulsar", (d, w) ->
                        grupoRepository.eliminarMiembro(idGrupo, miembro.getUidMiembro(),
                                new FirestoreCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void v) {
                                        Toast.makeText(requireContext(), username + " expulsado", Toast.LENGTH_SHORT).show();
                                        cargarMiembros();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                                    }
                                }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarNombre() {
        EditText input = new EditText(requireContext());
        input.setText(grupoMf != null ? grupoMf.getNomGrupo() : "");
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar nombre del grupo")
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevo = input.getText().toString().trim();
                    if (nuevo.length() < 2) {
                        Toast.makeText(requireContext(), "El nombre debe tener al menos 2 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    grupoRepository.actualizarNombreGrupo(idGrupo, nuevo, new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            tvNombreGrupo.setText(nuevo);
                            if (grupoMf != null) grupoMf.setNomGrupo(nuevo);
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void subirFotoGrupo(Uri uri) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("grupos/" + idGrupo + "/" + UUID.randomUUID() + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(snap -> ref.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            grupoRepository.actualizarFotoGrupo(idGrupo, url, new FirestoreCallback<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    if (grupoMf != null) grupoMf.setFotoGrupoUrl(url);
                                    Glide.with(requireContext()).load(url).into(imgGrupo);
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error al subir foto", Toast.LENGTH_SHORT).show());
    }

    private void mostrarOpcionesGrupo() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottomsheet_grupo_opciones, null);
        dialog.setContentView(view);
        if (view != null) {
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.surface));
        }

        LinearLayout btnInvitarUsuario = view.findViewById(R.id.btnInvitarUsuario);
        LinearLayout btnSalirGrupo     = view.findViewById(R.id.btnSalirGrupo);
        TextView tvCodigoGrupo         = view.findViewById(R.id.tvCodigoGrupo);
        ImageButton btnMostrarCodigo   = view.findViewById(R.id.btnMostrarCodigo);
        ImageButton btnCopiarCodigo    = view.findViewById(R.id.btnCopiarCodigo);
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
            tvCodigoGrupo.setText(visible[0] ? grupoMf.getCodInvitacion() : "••••••");
        });

        btnCopiarCodigo.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("codigoGrupo", grupoMf.getCodInvitacion()));
            Toast.makeText(requireContext(), "Código copiado", Toast.LENGTH_SHORT).show();
        });

        btnRegenerarCodigo.setOnClickListener(v ->
                grupoRepository.regenerarCodigoInvitacion(grupoMf.getId(), new FirestoreCallback<String>() {
                    @Override
                    public void onSuccess(String nuevoCodigo) {
                        grupoMf.setCodInvitacion(nuevoCodigo);
                        tvCodigoGrupo.setText(nuevoCodigo);
                        Toast.makeText(requireContext(), "Código regenerado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }));

        btnSalirGrupo.setOnClickListener(v -> {
            dialog.dismiss();
            confirmarSalirDelGrupo();
        });

        dialog.show();
    }

    private void confirmarSalirDelGrupo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Salir del grupo")
                .setMessage("¿Seguro que quieres salir de \"" + grupoMf.getNomGrupo() + "\"?")
                .setPositiveButton("Salir", (d, w) -> {
                    grupoRepository.salirDelGrupo(idGrupo, uidActual, new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            requireActivity().finish();
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMiembros();
    }
}
