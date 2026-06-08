package com.example.openteseractus.ui.agregar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openteseractus.R;
import com.example.openteseractus.adapters.UsuarioBusquedaAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Invitacion;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.InvitacionRepository;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pantalla para invitar a un usuario a un grupo.
 * Muestra un buscador de usuarios por username y, al seleccionar uno,
 * envía una invitación usando el nombre del usuario actual como remitente.
 */
public class AgregarUsuarioActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerUsuarios;

    private UsuarioBusquedaAdapter adapter;

    private List<Usuario> usuarios;

    private UsuarioRepository usuarioRepository;
    private InvitacionRepository invitacionRepository;

    private String idGrupo;
    private String nombreGrupo;

    private Usuario currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        idGrupo = getIntent().getStringExtra("GRUPO_ID");
        nombreGrupo = getIntent().getStringExtra("NOMBRE_GRUPO");
        usuarioRepository = new UsuarioRepository();
        invitacionRepository = new InvitacionRepository();
        usuarios = new ArrayList<>();

        inicializarVistas();
        cargarUsuarioActual();
    }

    private void inicializarVistas() {

        searchView = findViewById(R.id.searchViewUsuarios);

        adapter = new UsuarioBusquedaAdapter(
                usuarios, usuario -> invitarUsuario(usuario));

        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsuarios.setAdapter(adapter);

        searchView.setOnQueryTextListener(

                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        buscar(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        buscar(newText);
                        return true;
                    }
                });
    }

    private void cargarUsuarioActual() {

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        usuarioRepository.obtenerUsuario(uid,
                new FirestoreCallback<Usuario>() {

                    @Override
                    public void onSuccess(Usuario resultado) {
                        currentUser = resultado;
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(
                                AgregarUsuarioActivity.this,
                                error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void buscar(String query) {

        if (query.trim().isEmpty()) {
            usuarios.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        usuarioRepository.buscarUsuarios(query, new FirestoreCallback<List<Usuario>>() {
                    @Override
                    public void onSuccess(List<Usuario> resultado) {
                        adapter.update(resultado);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AgregarUsuarioActivity.this,
                                error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void invitarUsuario(Usuario usuario) {

        Invitacion invitacion = new Invitacion(
                UUID.randomUUID().toString(),
                idGrupo,
                usuario.getUid(),
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid(),
                        nombreGrupo
        );

        invitacionRepository.crearInvitacion(
                invitacion, currentUser.getUsername(),
                new FirestoreCallback<Void>() {

                    @Override
                    public void onSuccess(Void resultado) {
                        Toast.makeText(AgregarUsuarioActivity.this,
                                "Invitación enviada", Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AgregarUsuarioActivity.this,
                                error, Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}