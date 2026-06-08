package com.example.openteseractus.ui.ventanas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openteseractus.R;
import com.example.openteseractus.adapters.InvitacionAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Invitacion;
import com.example.openteseractus.repositorios.InvitacionRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de invitaciones pendientes del usuario.
 * Carga la lista al abrirse y permite aceptar, rechazar o borrar todas las invitaciones.
 * La navegación "atrás" aplica una animación de deslizamiento hacia la izquierda
 * para mantener coherencia con la transición de entrada desde HomeActivity.
 */
public class InvitacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerInvitaciones;
    private ImageButton btnBack;
    private ImageButton btnBorrarTodas;
    private BottomNavigationView bottomNav;

    private InvitacionAdapter adapter;
    private List<Invitacion> invitaciones;
    private InvitacionRepository invitacionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invitaciones);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAHome();
            }
        });

        invitaciones = new ArrayList<>();
        invitacionRepository = new InvitacionRepository();

        inicializarVistas();
        configurarBotones();
        cargarInvitaciones();
    }

    private void inicializarVistas() {
        recyclerInvitaciones = findViewById(R.id.recyclerInvitaciones);
        btnBack = findViewById(R.id.btnBack);
        btnBorrarTodas = findViewById(R.id.btnBorrarTodas);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_invites);

        adapter = new InvitacionAdapter(invitaciones, invitacionRepository);
        recyclerInvitaciones.setLayoutManager(new LinearLayoutManager(this));
        recyclerInvitaciones.setAdapter(adapter);
    }

    private void configurarBotones() {
        btnBack.setOnClickListener(v -> volverAHome());

        btnBorrarTodas.setOnClickListener(v -> {
            adapter.borrarTodas();
            Toast.makeText(this, "Invitaciones borradas", Toast.LENGTH_SHORT).show();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                volverAHome();
            }
            return true;
        });
    }

    private void volverAHome() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void cargarInvitaciones() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        invitacionRepository.obtenerInvitaciones(uid, new FirestoreCallback<List<Invitacion>>() {
            @Override
            public void onSuccess(List<Invitacion> resultado) {
                invitaciones.clear();
                invitaciones.addAll(resultado);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(InvitacionesActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}