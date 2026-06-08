package com.example.openteseractus.ui.ventanas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.openteseractus.R;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Teseracto;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.modelos.Valoracion;
import com.example.openteseractus.modelos.MiembroGrupo;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.example.openteseractus.repositorios.TeseractoRepository;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.example.openteseractus.repositorios.ValoracionRepository;
import com.example.openteseractus.tmdb.TMDBDetalle;
import com.example.openteseractus.tmdb.TMDBRepository;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

/**
 * Pantalla de detalle de un teseracto.
 * Muestra el póster, backdrop, sinopsis, información técnica y notas (grupo, TMDB y usuario)
 * obtenidas de TMDB y de las valoraciones de Firestore.
 * Un BottomSheet permite al usuario introducir su valoración con puntuaciones opcionales
 * de dirección, guion y actores; la nota general se recalcula automáticamente a partir
 * de los detalles introducidos.
 * El botón de eliminar solo se muestra al creador del teseracto o a un administrador del grupo.
 */
public class TeseractoActivity extends AppCompatActivity {

    private static String formatNota(double nota) {
        if (nota == Math.floor(nota) && nota >= 0 && nota <= 10) {
            return String.valueOf((int) nota);
        }
        return String.format(Locale.getDefault(), "%.1f", nota);
    }

    private ImageView imgPoster, imgBackdrop;
    private TextView tvTitulo, tvInfo, tvAnio, tvDirector, tvSinopsis, tvHeaderSinopsis, tvAbiertosPor;
    private Chip chipNotaGrupo, chipNotaTMDB, chipNotaUser;
    private MaterialButton btnChat;
    private ImageButton btnEliminar;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ValoracionRepository valoracionRepository;
    private Teseracto teseractoActual;
    private String teseractoId;

    private TeseractoRepository teseractoRepository;
    private TMDBRepository tmdbRepository;
    private GrupoRepository grupoRepository;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teseracto);
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

        teseractoId = getIntent().getStringExtra("TESERACTO_ID");
        Log.d("VALOR ID", "ID de Teseracto = " + teseractoId);

        teseractoRepository = new TeseractoRepository();
        valoracionRepository = new ValoracionRepository();
        tmdbRepository = new TMDBRepository();
        grupoRepository = new GrupoRepository();
        usuarioRepository = new UsuarioRepository();

        inicializarVistas();
        cargarTeseracto();
    }

    private void inicializarVistas() {

        imgPoster    = findViewById(R.id.imgPoster);
        imgBackdrop  = findViewById(R.id.imgBackdrop);

        tvTitulo         = findViewById(R.id.tvTitulo);
        tvAnio           = findViewById(R.id.tvAnio);
        tvInfo           = findViewById(R.id.tvInfo);
        tvDirector       = findViewById(R.id.tvDirector);
        tvSinopsis       = findViewById(R.id.tvSinopsis);
        tvHeaderSinopsis = findViewById(R.id.tvHeaderSinopsis);
        tvAbiertosPor    = findViewById(R.id.tvAbiertosPor);

        chipNotaGrupo = findViewById(R.id.chipNotaGrupo);
        chipNotaTMDB  = findViewById(R.id.chipTmdb);
        chipNotaUser  = findViewById(R.id.chipUser);
        btnChat       = findViewById(R.id.btnChat);
        btnEliminar   = findViewById(R.id.btnEliminar);

        toolbar           = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);

        chipNotaUser.setOnClickListener(v -> mostrarBottomSheetValoracion());
    }

    private void cargarTeseracto() {

        teseractoRepository.obtenerTeseracto(
                teseractoId, new FirestoreCallback<Teseracto>() {

                    @Override
                    public void onSuccess(Teseracto teseracto) {
                        teseractoActual = teseracto;

                        cargarNotaGrupo();
                        cargarNotaUsuario();
                        cargarDetalleTMDB(teseracto);
                        cargarInfoApertura(teseracto);
                        verificarPuedeEliminar(teseracto);

                        btnChat.setOnClickListener(v -> {
                            Intent intent = new Intent(
                                    TeseractoActivity.this,
                                    ChatTeseractoActivity.class
                            );
                            intent.putExtra("TESERACTO_ID", teseractoId);
                            intent.putExtra("TESERACTO_TITULO", teseracto.getTitulo());
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(
                                TeseractoActivity.this,
                                error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void cargarDetalleTMDB(Teseracto teseracto) {

        tmdbRepository.obtenerDetalle(
                teseracto.getTmdbId(), teseracto.getMediaType(),
                new FirestoreCallback<TMDBDetalle>() {

                    @Override
                    public void onSuccess(TMDBDetalle detalle) {
                        runOnUiThread(() -> {
                            collapsingToolbar.setTitle(detalle.getTitulo());
                            tvTitulo.setText(detalle.getTitulo());
                            if (detalle.getSinopsis() != null && !detalle.getSinopsis().isEmpty())
                                tvSinopsis.setText(detalle.getSinopsis());
                            else
                                tvHeaderSinopsis.setText("");

                            Glide.with(TeseractoActivity.this)
                                    .load(detalle.getPosterUrl())
                                    .placeholder(R.drawable.placeholder_poster)
                                    .error(R.drawable.placeholder_poster)
                                    .into(imgPoster);
                            Glide.with(TeseractoActivity.this)
                                    .load(detalle.getBackdropUrl())
                                    .placeholder(R.drawable.placeholder_backdrop)
                                    .error(R.drawable.placeholder_backdrop)
                                    .into(imgBackdrop);

                            chipNotaTMDB.setText(formatNota(detalle.getVoteAverage()));

                            String anio = detalle.getFechaSalida();
                            tvAnio.setText(anio != null && !anio.isEmpty() ? anio : "");
                            tvAnio.setVisibility(anio != null && !anio.isEmpty() ? View.VISIBLE : View.GONE);

                            if ("movie".equals(detalle.getMediaType())) {
                                tvInfo.setText(detalle.getRuntime() > 0 ? detalle.getRuntime() + " min" : "");
                            } else {
                                tvInfo.setText(detalle.getTemporadas()
                                        + getString(R.string.detail_seasons)
                                        + detalle.getEpisodios()
                                        + getString(R.string.detail_episodes));
                            }

                            String director = detalle.getDirectorOCreador();
                            if (director != null && !director.isEmpty()) {
                                tvDirector.setText("movie".equals(detalle.getMediaType())
                                        ? "Dir. " + director : "Crea. " + director);
                                tvDirector.setVisibility(View.VISIBLE);
                            } else {
                                tvDirector.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> Toast.makeText(
                                TeseractoActivity.this,
                                error,
                                Toast.LENGTH_SHORT
                        ).show());
                    }
                });
    }
    private void cargarNotaGrupo() {

        valoracionRepository.obtenerMediaValoraciones(
                teseractoId, new FirestoreCallback<Double>() {

                    @Override
                    public void onSuccess(Double media) {

                        runOnUiThread(() -> {
                            if (media <= 0) {
                                chipNotaGrupo.setText("-");
                            } else {
                                chipNotaGrupo.setText(formatNota(media));
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {

                        runOnUiThread(() -> chipNotaGrupo.setText("-"));
                    }
                });
    }

    private void cargarNotaUsuario() {

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        valoracionRepository.obtenerValoracionUsuario(
                teseractoId, uid, new FirestoreCallback<Valoracion>() {

                    @Override
                    public void onSuccess(Valoracion valoracion) {

                        runOnUiThread(() -> {
                            if (valoracion == null || valoracion.getPuntuacion() <= 0) {
                                chipNotaUser.setText("-");
                                return;
                            }
                            chipNotaUser.setText(formatNota(valoracion.getPuntuacion()));
                        });
                    }

                    @Override
                    public void onFailure(String error) {

                        runOnUiThread(() -> chipNotaUser.setText("-"));
                    }
                });
    }

    private void mostrarBottomSheetValoracion() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.getBehavior().setDraggable(true);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottomsheet_valoracion, null);
        dialog.setContentView(view);
        if (view != null) {
            view.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.surface)
            );
        }

        TextView tvTituloValoracion = view.findViewById(R.id.tvTituloValoracion);
        if (teseractoActual != null && teseractoActual.getTitulo() != null) {
            tvTituloValoracion.setText(getString(R.string.title_rating) + teseractoActual.getTitulo());
        }

        SeekBar seekGeneral = view.findViewById(R.id.seekGeneral);
        SeekBar seekDireccion = view.findViewById(R.id.seekDireccion);
        SeekBar seekGuion = view.findViewById(R.id.seekGuion);
        SeekBar seekActores = view.findViewById(R.id.seekActores);

        TextView tvGeneral = view.findViewById(R.id.tvGeneral);
        TextView tvDireccion = view.findViewById(R.id.tvDireccion);
        TextView tvGuion = view.findViewById(R.id.tvGuion);
        TextView tvActores = view.findViewById(R.id.tvActores);

        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardarValoracion);
        LinearLayout layoutExpandir = view.findViewById(R.id.layoutExpandir);
        LinearLayout layoutDetalles = view.findViewById(R.id.layoutDetalles);
        ImageView imgExpand = view.findViewById(R.id.imgExpand);

        final boolean[] expandido = { false };
        final boolean[] direccionTocada = { false };
        final boolean[] guionTocado = { false };
        final boolean[] actoresTocados = { false };

        layoutExpandir.setOnClickListener(v -> {
            expandido[0] = !expandido[0];
            layoutDetalles.setVisibility(expandido[0] ? View.VISIBLE : View.GONE);
            imgExpand.setRotation(expandido[0] ? 180 : 0);
        });

        configurarSeekBar(
                seekDireccion,
                tvDireccion,
                direccionTocada,
                seekGeneral,
                tvGeneral,
                seekDireccion,
                seekGuion,
                seekActores,
                direccionTocada,
                guionTocado,
                actoresTocados
        );

        configurarSeekBar(
                seekGuion,
                tvGuion,
                guionTocado,
                seekGeneral,
                tvGeneral,
                seekDireccion,
                seekGuion,
                seekActores,
                direccionTocada,
                guionTocado,
                actoresTocados
        );

        configurarSeekBar(
                seekActores,
                tvActores,
                actoresTocados,
                seekGeneral,
                tvGeneral,
                seekDireccion,
                seekGuion,
                seekActores,
                direccionTocada,
                guionTocado,
                actoresTocados
        );

        seekGeneral.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {
                        double nota = progress / 10.0;
                        tvGeneral.setText(formatNota(nota));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

        btnGuardar.setOnClickListener(v -> {
            double notaGeneral = seekGeneral.getProgress() / 10.0;
            double notaDireccion = seekDireccion.getProgress() / 10.0;
            double notaGuion = seekGuion.getProgress() / 10.0;
            double notaActores = seekActores.getProgress() / 10.0;

            int total = 0;
            double suma = 0;

            if (direccionTocada[0]) {
                suma += notaDireccion;
                total++;
            }

            if (guionTocado[0]) {
                suma += notaGuion;
                total++;
            }

            if (actoresTocados[0]) {
                suma += notaActores;
                total++;
            }

            if (total > 0) {
                notaGeneral = suma / total;
            }

            if (notaGeneral <= 0) {
                Toast.makeText(
                        this,
                        "Debes introducir una valoración",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Valoracion valoracion = new Valoracion();
            valoracion.setIdTeseracto(teseractoId);
            valoracion.setUidValoradoPor(
                    FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getUid()
            );
            valoracion.setPuntuacion(notaGeneral);
            valoracion.setPuntuacionDireccion(
                    direccionTocada[0] ? notaDireccion : 0
            );
            valoracion.setPuntuacionGuion(
                    guionTocado[0] ? notaGuion : 0
            );
            valoracion.setPuntuacionActores(
                    actoresTocados[0] ? notaActores : 0
            );

            double finalNotaGeneral = notaGeneral;
            valoracionRepository.valorar(
                    teseractoId, valoracion, new FirestoreCallback<Void>() {

                        @Override
                        public void onSuccess(Void resultado) {

                            valoracionRepository.obtenerMediaValoraciones(
                                    teseractoId, new FirestoreCallback<Double>() {

                                        @Override
                                        public void onSuccess(Double media) {

                                            teseractoRepository.actualizarNotaMedia(
                                                    teseractoId, media,
                                                    new FirestoreCallback<Void>() {

                                                        @Override
                                                        public void onSuccess(Void resultado) {

                                                            runOnUiThread(() -> {
                                                                chipNotaUser.setText(formatNota(finalNotaGeneral));
                                                                chipNotaGrupo.setText(formatNota(media));

                                                                Toast.makeText(
                                                                        TeseractoActivity.this,
                                                                        "Valoración guardada",
                                                                        Toast.LENGTH_SHORT
                                                                ).show();

                                                                dialog.dismiss();
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(String error) {

                                                            runOnUiThread(() ->
                                                                    Toast.makeText(
                                                                            TeseractoActivity.this,
                                                                            error,
                                                                            Toast.LENGTH_SHORT
                                                                    ).show()
                                                            );
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onFailure(String error) {

                                            runOnUiThread(() ->
                                                    Toast.makeText(
                                                            TeseractoActivity.this,
                                                            error,
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> Toast.makeText(
                                    TeseractoActivity.this,
                                    error,
                                    Toast.LENGTH_SHORT
                            ).show());
                        }
                    });
        });

        dialog.show();
    }

    private void cargarInfoApertura(Teseracto teseracto) {
        String uid = teseracto.getUidAbiertoPor();
        if (uid == null || uid.isEmpty()) return;

        usuarioRepository.obtenerUsuario(uid, new FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                runOnUiThread(() -> {
                    String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy",
                            java.util.Locale.getDefault()).format(
                            new java.util.Date(teseracto.getFechaApertura()));
                    tvAbiertosPor.setText(usuario.getUsername() + "\n" + fecha);
                    tvAbiertosPor.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(String error) {}
        });
    }

    private void verificarPuedeEliminar(Teseracto teseracto) {
        String uidActual = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        if (uidActual.equals(teseracto.getUidAbiertoPor())) {
            mostrarBotonEliminar(teseracto);
            return;
        }

        grupoRepository.obtenerMiembrosGrupo(teseracto.getIdGrupo(),
                new FirestoreCallback<java.util.List<com.example.openteseractus.modelos.MiembroGrupo>>() {
                    @Override
                    public void onSuccess(java.util.List<MiembroGrupo> miembros) {
                        for (MiembroGrupo m : miembros) {
                            if (uidActual.equals(m.getUidMiembro()) && m.esAdmin()) {
                                runOnUiThread(() -> mostrarBotonEliminar(teseracto));
                                return;
                            }
                        }
                    }

                    @Override
                    public void onFailure(String error) {}
                });
    }

    private void mostrarBotonEliminar(Teseracto teseracto) {
        btnEliminar.setVisibility(View.VISIBLE);
        btnEliminar.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Eliminar teseracto")
                        .setMessage("¿Seguro que quieres eliminar \"" + teseracto.getTitulo() + "\"?")
                        .setPositiveButton("Eliminar", (d, w) ->
                                teseractoRepository.eliminarTeseracto(teseractoId,
                                        new FirestoreCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                runOnUiThread(() -> Toast.makeText(
                                                        TeseractoActivity.this, error, Toast.LENGTH_SHORT).show());
                                            }
                                        }))
                        .setNegativeButton("Cancelar", null)
                        .show());
    }

    private void configurarSeekBar(
            SeekBar seekBar,
            TextView textView,
            boolean[] tocado,
            SeekBar seekGeneral,
            TextView tvGeneral,
            SeekBar seekDireccion,
            SeekBar seekGuion,
            SeekBar seekActores,
            boolean[] direccionTocada,
            boolean[] guionTocado,
            boolean[] actoresTocados
    ) {

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {
                        if (fromUser) {
                            tocado[0] = true;
                        }

                        double nota = progress / 10.0;
                        textView.setText(formatNota(nota));

                        actualizarNotaGeneral(
                                seekGeneral,
                                tvGeneral,
                                seekDireccion,
                                seekGuion,
                                seekActores,
                                direccionTocada,
                                guionTocado,
                                actoresTocados
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
    }

    private void actualizarNotaGeneral(
            SeekBar seekGeneral,
            TextView tvGeneral,
            SeekBar seekDireccion,
            SeekBar seekGuion,
            SeekBar seekActores,
            boolean[] direccionTocada,
            boolean[] guionTocado,
            boolean[] actoresTocados
    ) {

        double suma = 0;
        int total = 0;

        if (direccionTocada[0]) {
            suma += seekDireccion.getProgress() / 10.0;
            total++;
        }

        if (guionTocado[0]) {
            suma += seekGuion.getProgress() / 10.0;
            total++;
        }

        if (actoresTocados[0]) {
            suma += seekActores.getProgress() / 10.0;
            total++;
        }

        if (total == 0) {
            return;
        }

        double media = suma / total;
        seekGeneral.setProgress((int) (media * 10));
        tvGeneral.setText(formatNota(media));
    }
}