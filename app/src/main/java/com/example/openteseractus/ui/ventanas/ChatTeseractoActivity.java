package com.example.openteseractus.ui.ventanas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openteseractus.R;
import com.example.openteseractus.adapters.MensajeAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Mensaje;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.MensajeRepository;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatTeseractoActivity extends AppCompatActivity {

    private RecyclerView rvMensajes;
    private TextInputEditText etMensaje;
    private MaterialButton btnEnviar;
    private Toolbar toolbar;

    private MensajeAdapter adapter;
    private MensajeRepository mensajeRepository;
    private UsuarioRepository usuarioRepository;

    private String teseractoId;
    private String tituloTeseracto;
    private String uidActual;

    // Mapa uid -> username para mostrar el nombre del autor
    private final Map<String, String> nombresUsuarios = new HashMap<>();

    // Listener de Firestore; se cancela al salir de la pantalla
    private ListenerRegistration listenerMensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_teseracto);

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

        teseractoId     = getIntent().getStringExtra("TESERACTO_ID");
        tituloTeseracto = getIntent().getStringExtra("TESERACTO_TITULO");
        uidActual       = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mensajeRepository = new MensajeRepository();
        usuarioRepository = new UsuarioRepository();

        inicializarVistas();
        cargarNombreUsuarioActual();
        iniciarEscucha();
    }

    private void inicializarVistas() {

        toolbar    = findViewById(R.id.toolbar);
        rvMensajes = findViewById(R.id.rvMensajes);
        etMensaje  = findViewById(R.id.etMensaje);
        btnEnviar  = findViewById(R.id.btnEnviar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(
                    tituloTeseracto != null ? tituloTeseracto : "Chat"
            );
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMensajes.setLayoutManager(layoutManager);

        adapter = new MensajeAdapter(uidActual, nombresUsuarios);
        rvMensajes.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> enviarMensaje());

        // También enviar al pulsar la tecla "enviar" del teclado
        etMensaje.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                enviarMensaje();
                return true;
            }
            return false;
        });
    }

    // Precarga el username del usuario actual en el mapa
    private void cargarNombreUsuarioActual() {
        usuarioRepository.obtenerUsuario(uidActual, new FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                nombresUsuarios.put(uidActual, usuario.getUsername());
            }

            @Override
            public void onFailure(String error) {
                // Sin nombre no es crítico, se mostrará el uid
            }
        });
    }

    private void iniciarEscucha() {
        listenerMensajes = mensajeRepository.escucharMensajes(
                teseractoId,
                new FirestoreCallback<List<Mensaje>>() {

                    @Override
                    public void onSuccess(List<Mensaje> mensajes) {
                        // Resuelve nombres de autores aún desconocidos
                        for (Mensaje m : mensajes) {
                            if (!nombresUsuarios.containsKey(m.getUidAutor())) {
                                resolverNombreAutor(m.getUidAutor());
                            }
                        }

                        runOnUiThread(() -> {
                            adapter.update(mensajes);
                            if (!mensajes.isEmpty()) {
                                rvMensajes.scrollToPosition(mensajes.size() - 1);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        ChatTeseractoActivity.this,
                                        "Error al cargar mensajes",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    private void resolverNombreAutor(String uid) {
        // Marcamos con el uid para evitar peticiones duplicadas mientras resuelve
        nombresUsuarios.put(uid, uid);
        usuarioRepository.obtenerUsuario(uid, new FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                nombresUsuarios.put(uid, usuario.getUsername());
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

            @Override
            public void onFailure(String error) {
                // Queda el uid como fallback
            }
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText() != null
                ? etMensaje.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(texto)) return;

        Mensaje mensaje = new Mensaje(null, uidActual, teseractoId, texto);

        etMensaje.setText("");

        mensajeRepository.enviarMensaje(mensaje, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void resultado) {
                // El listener en tiempo real ya actualizará la lista
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(
                                ChatTeseractoActivity.this,
                                "Error al enviar el mensaje",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancelar el listener para evitar fugas de memoria
        if (listenerMensajes != null) {
            listenerMensajes.remove();
        }
    }
}
