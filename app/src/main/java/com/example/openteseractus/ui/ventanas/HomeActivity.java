package com.example.openteseractus.ui.ventanas;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.openteseractus.R;
import com.example.openteseractus.adapters.GrupoAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Grupo;
import com.example.openteseractus.modelos.Invitacion;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.example.openteseractus.repositorios.InvitacionRepository;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.example.openteseractus.servicios.AuthService;
import com.example.openteseractus.servicios.MensajeNotificacionService;
import com.example.openteseractus.ui.MainActivity;
import com.example.openteseractus.ui.crear.CrearGrupoActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topBar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNav;
    private ShapeableImageView imgPerfil;
    private ProgressBar progressBar;

    private static final String CHANNEL_ID = "invitaciones";
    private static final int NOTIF_ID = 1001;

    private UsuarioRepository usuarioRepository;
    private GrupoRepository grupoRepository;
    private InvitacionRepository invitacionRepository;
    private ListenerRegistration invitacionListener;
    private int ultimoConteo = -1;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private GrupoAdapter grupoAdapter;
    private List<Grupo> grupos;
    private List<Grupo> gruposCompletos;
    private AuthService authService;
    private Usuario usuarioActual;

    private ActivityResultLauncher<Intent> crearGrupoLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

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
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finishAffinity();
                }
            }
        });

        inicializarVistas();
        inicializarFirebase();
        setupRecyclerView();
        setupListeners();
        setupLaunchers();
        cargarDatosUsuario();
        crearCanalNotificacion();
        pedirPermisoNotificaciones();
        iniciarEscuchaInvitaciones();
        iniciarServicioMensajes();
    }

    private void inicializarVistas() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topBar = findViewById(R.id.topBar);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        imgPerfil = findViewById(R.id.imgPerfil);
        progressBar = findViewById(R.id.progressBar);
    }

    private void inicializarFirebase() {
        usuarioRepository = new UsuarioRepository();
        grupoRepository = new GrupoRepository();
        invitacionRepository = new InvitacionRepository();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        grupos = new ArrayList<>();
        gruposCompletos = new ArrayList<>();
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
            if (item.getItemId() == R.id.nav_invites) {
                startActivity(new Intent(HomeActivity.this, InvitacionesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            return true;
        });

        // Pfp opens the drawer
        imgPerfil.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Group search filter
        SearchView searchView = topBar.findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filtrarGrupos(newText);
                    return true;
                }
            });
        }

        // Drawer navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.nav_change_pfp) {
                pickImageLauncher.launch("image/*");
            } else if (id == R.id.nav_change_username) {
                mostrarDialogoCambiarUsername();
            } else if (id == R.id.nav_reset_password) {
                restablecerContrasena();
            } else if (id == R.id.nav_delete_account) {
                confirmarEliminarCuenta();
            } else if (id == R.id.nav_logout) {
                cerrarSesion();
            }
            return true;
        });
    }

    private void setupLaunchers() {
        crearGrupoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarGrupos();
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) subirFotoPerfil(uri);
                }
        );
    }

    private void cargarGrupos() {
        if (auth.getCurrentUser() == null) return;
        progressBar.setVisibility(View.VISIBLE);
        String userId = auth.getCurrentUser().getUid();

        grupoRepository.obtenerGruposDeUsuario(userId, new FirestoreCallback<List<Grupo>>() {
            @Override
            public void onSuccess(List<Grupo> gruposObtenidos) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    gruposCompletos.clear();
                    gruposCompletos.addAll(gruposObtenidos);
                    grupos.clear();
                    grupos.addAll(gruposObtenidos);
                    grupoAdapter.updateGrupos(grupos);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(HomeActivity.this,
                            "Error al cargar grupos: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void cargarDatosUsuario() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        usuarioRepository.obtenerUsuario(uid, new FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioActual = usuario;

                // Toolbar pfp
                Glide.with(HomeActivity.this)
                        .load(usuario.getFotoPerfilUrl())
                        .placeholder(R.drawable.pfp_placeholder)
                        .error(R.drawable.pfp_placeholder)
                        .circleCrop()
                        .into(imgPerfil);

                // Drawer header
                View header = navigationView.getHeaderView(0);
                ShapeableImageView navImg = header.findViewById(R.id.navImgPerfil);
                TextView navUsername = header.findViewById(R.id.navTvUsername);
                TextView navEmail = header.findViewById(R.id.navTvEmail);

                navUsername.setText(usuario.getUsername() != null ? usuario.getUsername() : "");
                navEmail.setText(usuario.getEmail() != null ? usuario.getEmail() : "");

                Glide.with(HomeActivity.this)
                        .load(usuario.getFotoPerfilUrl())
                        .placeholder(R.drawable.pfp_placeholder)
                        .error(R.drawable.pfp_placeholder)
                        .circleCrop()
                        .into(navImg);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarGrupos(String query) {
        grupos.clear();
        if (TextUtils.isEmpty(query)) {
            grupos.addAll(gruposCompletos);
        } else {
            String q = query.toLowerCase();
            for (Grupo g : gruposCompletos) {
                if (g.getNomGrupo() != null && g.getNomGrupo().toLowerCase().contains(q)) {
                    grupos.add(g);
                }
            }
        }
        grupoAdapter.updateGrupos(grupos);
    }

    private void subirFotoPerfil(Uri uri) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        StorageReference ref = storage.getReference().child("perfiles").child(uid + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            if (usuarioActual != null) {
                                usuarioActual.setFotoPerfilUrl(url);
                                usuarioRepository.actualizarUsuario(usuarioActual,
                                        new FirestoreCallback<Usuario>() {
                                            @Override
                                            public void onSuccess(Usuario u) {
                                                usuarioActual = u;
                                                cargarDatosUsuario();
                                                Toast.makeText(HomeActivity.this,
                                                        "Foto actualizada", Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onFailure(String error) {
                                                Toast.makeText(HomeActivity.this,
                                                        "Error: " + error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error al subir foto: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarDialogoCambiarUsername() {
        FrameLayout container = new FrameLayout(this);
        EditText et = new EditText(this);
        et.setHint(getString(R.string.new_username_hint));
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        int dp16 = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(dp16, dp16 / 2, dp16, 0);
        et.setLayoutParams(lp);
        container.addView(et);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.drawer_change_username))
                .setView(container)
                .setPositiveButton("Confirmar", (dialog, which) ->
                        cambiarUsername(et.getText().toString().trim()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cambiarUsername(String nuevoUsername) {
        if (usuarioActual == null) return;

        // Reuse model validation
        Usuario temp = new Usuario();
        temp.setUsername(nuevoUsername);
        if (!temp.validarUsername()) {
            Toast.makeText(this,
                    "Nombre no válido (3-20 chars, empieza por letra, solo letras/números/_)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        usuarioRepository.existeUsername(nuevoUsername, new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean existe) {
                if (existe) {
                    Toast.makeText(HomeActivity.this,
                            "Ese nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show();
                    return;
                }
                usuarioActual.setUsername(nuevoUsername);
                usuarioRepository.actualizarUsuario(usuarioActual, new FirestoreCallback<Usuario>() {
                    @Override
                    public void onSuccess(Usuario u) {
                        usuarioActual = u;
                        View header = navigationView.getHeaderView(0);
                        TextView navUsername = header.findViewById(R.id.navTvUsername);
                        navUsername.setText(nuevoUsername);
                        Toast.makeText(HomeActivity.this,
                                "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(HomeActivity.this,
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(HomeActivity.this,
                        "Error al verificar: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restablecerContrasena() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) return;

        auth.sendPasswordResetEmail(firebaseUser.getEmail())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                getString(R.string.drawer_reset_email_sent),
                                Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void confirmarEliminarCuenta() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_account))
                .setMessage(getString(R.string.confirm_delete_msg))
                .setPositiveButton("Eliminar", (d, w) -> eliminarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCuenta() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        usuarioRepository.eliminarUsuario(uid, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                auth.getCurrentUser().delete()
                        .addOnSuccessListener(aVoid -> {
                            stopService(new Intent(HomeActivity.this, MensajeNotificacionService.class));
                            Toast.makeText(HomeActivity.this,
                                    "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(HomeActivity.this, MainActivity.class));
                            finishAffinity();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this,
                                        "Error Auth: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(HomeActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarServicioMensajes() {
        ContextCompat.startForegroundService(this,
                new Intent(this, MensajeNotificacionService.class));
    }

    private void cerrarSesion() {
        stopService(new Intent(this, MensajeNotificacionService.class));
        authService.cerrarSesion();
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        finishAffinity();
    }

    private void abrirGrupo(Grupo grupo) {
        // Update last-entry timestamp so groups sort WhatsApp-style on next load
        if (auth.getCurrentUser() != null) {
            grupoRepository.actualizarUltimaActividad(
                    auth.getCurrentUser().getUid(), grupo.getId());
        }
        Intent intent = new Intent(HomeActivity.this, GrupoActivity.class);
        intent.putExtra("GRUPO_ID", grupo.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarGrupos();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (invitacionListener != null) invitacionListener.remove();
    }

    private void crearCanalNotificacion() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Invitaciones", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Nuevas invitaciones a grupos");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    private void iniciarEscuchaInvitaciones() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        invitacionListener = invitacionRepository.escucharInvitaciones(uid,
                new FirestoreCallback<List<Invitacion>>() {
                    @Override
                    public void onSuccess(List<Invitacion> invitaciones) {
                        int count = invitaciones.size();
                        actualizarBadge(count);
                        if (ultimoConteo >= 0 && count > ultimoConteo) {
                            mostrarNotificacionInvitacion(count);
                        }
                        ultimoConteo = count;
                    }
                    @Override
                    public void onFailure(String error) { }
                });
    }

    private void actualizarBadge(int count) {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_invites);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }

    private void mostrarNotificacionInvitacion(int count) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) return;

        Intent intent = new Intent(this, InvitacionesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String texto = count == 1
                ? "Tienes 1 invitación pendiente"
                : "Tienes " + count + " invitaciones pendientes";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mail)
                .setContentTitle("Nueva invitación")
                .setContentText(texto)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        getSystemService(NotificationManager.class).notify(NOTIF_ID, builder.build());
    }
}
