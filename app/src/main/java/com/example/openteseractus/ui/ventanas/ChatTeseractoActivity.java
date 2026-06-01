package com.example.openteseractus.ui.ventanas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import com.example.openteseractus.servicios.MensajeNotificacionService;
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

    private boolean notificacionesSilenciadas = false;
    private Menu menuActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_teseracto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom)
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
        cargarEstadoSilencio();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menuActual = menu;
        actualizarMenuSilencio();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_silenciar) {
            toggleSilencio();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MensajeNotificacionService.teseractoActivo = teseractoId;
        usuarioRepository.marcarLeido(uidActual, teseractoId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MensajeNotificacionService.teseractoActivo = null;
        usuarioRepository.marcarLeido(uidActual, teseractoId);
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

    private void cargarEstadoSilencio() {
        usuarioRepository.obtenerEstadoSilencio(uidActual, teseractoId,
                new FirestoreCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean silenciado) {
                        notificacionesSilenciadas = silenciado;
                        actualizarMenuSilencio();
                    }
                    @Override
                    public void onFailure(String error) { }
                });
    }

    private void actualizarMenuSilencio() {
        if (menuActual == null) return;
        MenuItem item = menuActual.findItem(R.id.action_silenciar);
        if (item != null) {
            item.setTitle(notificacionesSilenciadas
                    ? "Activar notificaciones"
                    : "Silenciar notificaciones");
        }
    }

    private void toggleSilencio() {
        if (notificacionesSilenciadas) {
            usuarioRepository.activarNotificacionesTeseracto(uidActual, teseractoId,
                    new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            notificacionesSilenciadas = false;
                            actualizarMenuSilencio();
                            Toast.makeText(ChatTeseractoActivity.this,
                                    "Notificaciones activadas", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(ChatTeseractoActivity.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            usuarioRepository.silenciarTeseracto(uidActual, teseractoId,
                    new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            notificacionesSilenciadas = true;
                            actualizarMenuSilencio();
                            Toast.makeText(ChatTeseractoActivity.this,
                                    "Notificaciones silenciadas", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(ChatTeseractoActivity.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
