package com.example.openteseractus.ui.crear;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CrearGrupoActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivGrupoFoto;
    private FloatingActionButton fabCambiarFoto;
    private TextInputLayout tilNombreGrupo;
    private TextInputEditText etNombreGrupo;
    private MaterialButton btnCrearGrupo;
    private ProgressBar progressBar;

    private GrupoRepository grupoRepository;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private Uri fotoUri = null;

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
        setupListeners();
        setupImagePicker();
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        ivGrupoFoto = findViewById(R.id.ivGrupoFoto);
        fabCambiarFoto = findViewById(R.id.fabCambiarFoto);
        tilNombreGrupo = findViewById(R.id.tilNombreGrupo);
        etNombreGrupo = findViewById(R.id.etNombreGrupo);
        btnCrearGrupo = findViewById(R.id.btnCrearGrupo);
        progressBar = findViewById(R.id.progressBar);
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
            toolbar.setTitle(R.string.create_group);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .into(ivGrupoFoto);
                    }
                }
        );
    }

    private void setupListeners() {
        fabCambiarFoto.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );

        btnCrearGrupo.setOnClickListener(v -> crearGrupo());
    }

    private void crearGrupo() {
        String nombre = etNombreGrupo.getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            tilNombreGrupo.setError("Ingresa un nombre para el grupo");
            return;
        }

        if (nombre.length() > 50) {
            tilNombreGrupo.setError("El nombre no puede superar 50 caracteres");
            return;
        }

        tilNombreGrupo.setError(null);
        mostrarLoading(true);

        String userId = auth.getCurrentUser().getUid();

        // Si hay foto, subirla primero
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
                    mostrarLoading(false);
                    Toast.makeText(this,
                            "Error al subir la foto: " + e.getMessage(),
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
                mostrarLoading(false);
                Toast.makeText(CrearGrupoActivity.this,
                        "Grupo creado exitosamente",
                        Toast.LENGTH_SHORT).show();

                // Volver al Home con resultado exitoso
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                mostrarLoading(false);
                Toast.makeText(CrearGrupoActivity.this,
                        "Error al crear grupo: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCrearGrupo.setEnabled(!show);
        fabCambiarFoto.setEnabled(!show);
        etNombreGrupo.setEnabled(!show);
    }
}