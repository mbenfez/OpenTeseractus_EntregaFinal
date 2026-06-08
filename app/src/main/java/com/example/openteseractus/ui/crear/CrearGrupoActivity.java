package com.example.openteseractus.ui.crear;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.openteseractus.R;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Grupo;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Pantalla para crear un nuevo grupo o unirse a uno existente.
 * Contiene dos pestañas (TabLayout):
 * <ul>
 *   <li><b>Crear:</b> permite introducir un nombre y una foto opcional para el grupo.</li>
 *   <li><b>Unirse:</b> permite buscar un grupo por su código de 6 caracteres y previsualizar
 *       su nombre antes de confirmar la unión.</li>
 * </ul>
 * Devuelve {@code RESULT_OK} al Activity llamante cuando la operación es exitosa.
 */
public class CrearGrupoActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ScrollView panelCrear;
    private ScrollView panelUnirse;

    private FrameLayout frameGrupoFoto;
    private ImageView ivGrupoFoto;
    private TextInputLayout tilNombreGrupo;
    private TextInputEditText etNombreGrupo;
    private MaterialButton btnCrearGrupo;
    private ProgressBar progressBar;

    private TextInputLayout tilCodigo;
    private TextInputEditText etCodigo;
    private MaterialButton btnBuscarGrupo;
    private MaterialCardView cardPreviewGrupo;
    private ImageView ivGrupoPreview;
    private TextView tvGrupoPreviewNombre;
    private MaterialButton btnUnirseGrupo;
    private ProgressBar progressBarUnirse;

    private GrupoRepository grupoRepository;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private Uri fotoUri = null;
    private Grupo grupoEncontrado = null;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_grupo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        inicializarFirebase();
        setupToolbar();
        setupTabs();
        setupListeners();
        setupImagePicker();
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        panelCrear = findViewById(R.id.panelCrear);
        panelUnirse = findViewById(R.id.panelUnirse);

        frameGrupoFoto = findViewById(R.id.frameGrupoFoto);
        ivGrupoFoto = findViewById(R.id.ivGrupoFoto);
        tilNombreGrupo = findViewById(R.id.tilNombreGrupo);
        etNombreGrupo = findViewById(R.id.etNombreGrupo);
        btnCrearGrupo = findViewById(R.id.btnCrearGrupo);
        progressBar = findViewById(R.id.progressBar);

        tilCodigo = findViewById(R.id.tilCodigo);
        etCodigo = findViewById(R.id.etCodigo);
        btnBuscarGrupo = findViewById(R.id.btnBuscarGrupo);
        cardPreviewGrupo = findViewById(R.id.cardPreviewGrupo);
        ivGrupoPreview = findViewById(R.id.ivGrupoPreview);
        tvGrupoPreviewNombre = findViewById(R.id.tvGrupoPreviewNombre);
        btnUnirseGrupo = findViewById(R.id.btnUnirseGrupo);
        progressBarUnirse = findViewById(R.id.progressBarUnirse);
    }

    private void inicializarFirebase() {
        grupoRepository = new GrupoRepository();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    panelCrear.setVisibility(View.VISIBLE);
                    panelUnirse.setVisibility(View.GONE);
                } else {
                    panelCrear.setVisibility(View.GONE);
                    panelUnirse.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoUri = uri;
                        Glide.with(this).load(uri).centerCrop().into(ivGrupoFoto);
                    }
                }
        );
    }

    private void setupListeners() {
        
        frameGrupoFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnCrearGrupo.setOnClickListener(v -> crearGrupo());

        btnBuscarGrupo.setOnClickListener(v -> buscarGrupoPorCodigo());

        btnUnirseGrupo.setOnClickListener(v -> {
            if (grupoEncontrado != null) {
                mostrarConfirmacionUnirse(grupoEncontrado);
            }
        });
    }

    private void crearGrupo() {
        String nombre = etNombreGrupo.getText().toString().trim();

        if (nombre.isEmpty()) {
            tilNombreGrupo.setError("Ingresa un nombre para el grupo");
            return;
        }
        if (nombre.length() > 50) {
            tilNombreGrupo.setError("El nombre no puede superar 50 caracteres");
            return;
        }

        tilNombreGrupo.setError(null);
        mostrarLoadingCrear(true);

        String userId = auth.getCurrentUser().getUid();

        if (fotoUri != null) {
            subirFotoYCrearGrupo(nombre, userId);
        } else {
            crearGrupoEnFirestore(nombre, null, userId);
        }
    }

    private void subirFotoYCrearGrupo(String nombre, String userId) {
        StorageReference fotoRef = storage.getReference()
                .child("grupos")
                .child(System.currentTimeMillis() + ".jpg");

        fotoRef.putFile(fotoUri)
                .addOnSuccessListener(taskSnapshot ->
                        fotoRef.getDownloadUrl().addOnSuccessListener(downloadUri ->
                                crearGrupoEnFirestore(nombre, downloadUri.toString(), userId)
                        )
                )
                .addOnFailureListener(e -> {
                    mostrarLoadingCrear(false);
                    Toast.makeText(this, "Error al subir la foto: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void crearGrupoEnFirestore(String nombre, String fotoUrl, String userId) {
        Grupo nuevoGrupo = new Grupo();
        nuevoGrupo.setNomGrupo(nombre);
        nuevoGrupo.setFotoGrupoUrl(fotoUrl);

        grupoRepository.crearGrupo(nuevoGrupo, userId, new FirestoreCallback<Grupo>() {
            @Override
            public void onSuccess(Grupo grupo) {
                mostrarLoadingCrear(false);
                Toast.makeText(CrearGrupoActivity.this,
                        "Grupo creado exitosamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                mostrarLoadingCrear(false);
                Toast.makeText(CrearGrupoActivity.this,
                        "Error al crear grupo: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoadingCrear(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCrearGrupo.setEnabled(!show);
        frameGrupoFoto.setEnabled(!show);
        etNombreGrupo.setEnabled(!show);
    }

    private void buscarGrupoPorCodigo() {
        String codigo = etCodigo.getText().toString().trim().toUpperCase();

        if (codigo.isEmpty()) {
            tilCodigo.setError("Introduce un código");
            return;
        }
        if (codigo.length() != 6) {
            tilCodigo.setError("El código debe tener 6 caracteres");
            return;
        }

        tilCodigo.setError(null);
        cardPreviewGrupo.setVisibility(View.GONE);
        btnUnirseGrupo.setVisibility(View.GONE);
        progressBarUnirse.setVisibility(View.VISIBLE);
        grupoEncontrado = null;

        grupoRepository.buscarGrupoPorCodigo(codigo, new FirestoreCallback<Grupo>() {
            @Override
            public void onSuccess(Grupo grupo) {
                runOnUiThread(() -> {
                    progressBarUnirse.setVisibility(View.GONE);
                    grupoEncontrado = grupo;

                    tvGrupoPreviewNombre.setText(grupo.getNomGrupo());
                    if (grupo.getFotoGrupoUrl() != null && !grupo.getFotoGrupoUrl().isEmpty()) {
                        Glide.with(CrearGrupoActivity.this)
                                .load(grupo.getFotoGrupoUrl())
                                .centerCrop()
                                .into(ivGrupoPreview);
                    } else {
                        ivGrupoPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    }

                    cardPreviewGrupo.setVisibility(View.VISIBLE);
                    btnUnirseGrupo.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBarUnirse.setVisibility(View.GONE);
                    tilCodigo.setError("Código inválido o no encontrado");
                    grupoEncontrado = null;
                });
            }
        });
    }

    private void mostrarConfirmacionUnirse(Grupo grupo) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_join_group))
                .setMessage("¿Unirte a \"" + grupo.getNomGrupo() + "\"?")
                .setPositiveButton("Unirse", (d, w) -> unirseAlGrupo(grupo))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void unirseAlGrupo(Grupo grupo) {
        String uid = auth.getCurrentUser().getUid();
        progressBarUnirse.setVisibility(View.VISIBLE);
        btnUnirseGrupo.setEnabled(false);
        btnBuscarGrupo.setEnabled(false);

        grupoRepository.unirseAGrupo(grupo.getId(), uid, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                runOnUiThread(() -> {
                    progressBarUnirse.setVisibility(View.GONE);
                    Toast.makeText(CrearGrupoActivity.this,
                            "Te has unido a " + grupo.getNomGrupo(),
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBarUnirse.setVisibility(View.GONE);
                    btnUnirseGrupo.setEnabled(true);
                    btnBuscarGrupo.setEnabled(true);
                    Toast.makeText(CrearGrupoActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}