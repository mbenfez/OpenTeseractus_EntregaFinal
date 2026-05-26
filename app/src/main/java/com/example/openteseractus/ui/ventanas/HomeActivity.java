package com.example.openteseractus.ui.ventanas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.openteseractus.R;
import com.example.openteseractus.adapters.GrupoAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Grupo;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.example.openteseractus.servicios.AuthService;
import com.example.openteseractus.ui.MainActivity;
import com.example.openteseractus.ui.crear.CrearGrupoActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar topBar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNav;
    private ImageView imgPerfil;

    private UsuarioRepository usuarioRepository;
    private GrupoRepository grupoRepository;
    private FirebaseAuth auth;
    private GrupoAdapter grupoAdapter;
    private List<Grupo> grupos;
    private AuthService authService;

    private ActivityResultLauncher<Intent> crearGrupoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });

        inicializarVistas();
        inicializarFirebase();
        setupRecyclerView();
        setupListeners();
        setupLauncher();
        cargarGrupos();
        cargarFotoPerfil();
    }

    private void inicializarVistas() {
        topBar = findViewById(R.id.topBar);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        imgPerfil = findViewById(R.id.imgPerfil);
    }

    private void inicializarFirebase() {
        usuarioRepository = new UsuarioRepository();
        grupoRepository = new GrupoRepository();
        auth = FirebaseAuth.getInstance();
        grupos = new ArrayList<>();
        authService = new AuthService();
    }

    private void setupRecyclerView() {
        grupoAdapter = new GrupoAdapter(grupos, this::abrirGrupo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(grupoAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearGrupoActivity.class);
            crearGrupoLauncher.launch(intent);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_invites) {
                startActivity(new Intent(HomeActivity.this, InvitacionesActivity.class));
            }

            return true;
        });

        imgPerfil.setOnClickListener(v -> {
            authService.cerrarSesion();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });
    }
    private void setupLauncher() {
        crearGrupoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarGrupos();
                    }
                }
        );
    }
    private void cargarGrupos() {
        String userId = auth.getCurrentUser().getUid();

        grupoRepository.obtenerGruposDeUsuario(userId,
                new FirestoreCallback<List<Grupo>>() {

                    @Override
                    public void onSuccess(List<Grupo> gruposObtenidos) {
                        grupos.clear();
                        grupos.addAll(gruposObtenidos); // Ya vienen completos
                        grupoAdapter.updateGrupos(grupos);

                        if (grupos.isEmpty()) {
                            Toast.makeText(HomeActivity.this,
                                    "No tienes grupos aún. ¡Crea uno!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(HomeActivity.this,
                                "Error al cargar grupos: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarFotoPerfil() {

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        usuarioRepository.obtenerUsuario(uid,
                new FirestoreCallback<Usuario>() {

                    @Override
                    public void onSuccess(Usuario usuario) {
                        Glide.with(HomeActivity.this)
                                .load(usuario.getFotoPerfilUrl())
                                .placeholder(R.drawable.pfp_placeholder)
                                .error(R.drawable.pfp_placeholder)
                                .circleCrop()
                                .into(imgPerfil);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(
                                HomeActivity.this,
                                error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void abrirGrupo(Grupo grupo) {
        Intent intent = new Intent(HomeActivity.this, GrupoActivity.class);
        intent.putExtra("GRUPO_ID", grupo.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarGrupos();
    }
}