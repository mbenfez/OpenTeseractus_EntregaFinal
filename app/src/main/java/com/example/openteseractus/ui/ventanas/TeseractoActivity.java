package com.example.openteseractus.ui.ventanas;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.example.openteseractus.modelos.Valoracion;
import com.example.openteseractus.repositorios.TeseractoRepository;
import com.example.openteseractus.repositorios.ValoracionRepository;
import com.example.openteseractus.tmdb.TMDBDetalle;
import com.example.openteseractus.tmdb.TMDBRepository;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class TeseractoActivity extends AppCompatActivity {

    private ImageView imgPoster, imgBackdrop;
    private TextView tvTitulo, tvInfo, tvSinopsis, tvHeaderSinopsis;
    private Chip chipNotaGrupo, chipNotaTMDB, chipNotaUser;
    private MaterialButton btnChat;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ValoracionRepository valoracionRepository;
    private Teseracto teseractoActual;
    private String teseractoId;

    private TeseractoRepository teseractoRepository;
    private TMDBRepository tmdbRepository;

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

        inicializarVistas();
        cargarTeseracto();
    }

    private void inicializarVistas() {

        imgPoster = findViewById(R.id.imgPoster);
        imgBackdrop = findViewById(R.id.imgBackdrop);

        tvTitulo = findViewById(R.id.tvTitulo);
        tvInfo = findViewById(R.id.tvInfo);
        tvSinopsis = findViewById(R.id.tvSinopsis);
        tvHeaderSinopsis = findViewById(R.id.tvHeaderSinopsis);

        chipNotaGrupo = findViewById(R.id.chipNotaGrupo);
        chipNotaTMDB = findViewById(R.id.chipTmdb);
        chipNotaUser = findViewById(R.id.chipUser);
        btnChat = findViewById(R.id.btnChat);

        toolbar = findViewById(R.id.toolbar);
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

                        btnChat.setOnClickListener(v -> {
                            // Intent intent = new Intent(
                            //         TeseractoActivity.this,
                            //         ChatTeseractoActivity.class
                            // );
                            //
                            // intent.putExtra(
                            //         "TESERACTO_ID",
                            //         teseractoId
                            // );
                            //
                            // startActivity(intent);
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
                            if (!detalle.getSinopsis().isEmpty())
                                tvSinopsis.setText(detalle.getSinopsis());
                            else
                                tvHeaderSinopsis.setText("");

                            Glide.with(TeseractoActivity.this)
                                    .load(detalle.getPosterUrl())
                                    .into(imgPoster);
                            Glide.with(TeseractoActivity.this)
                                    .load(detalle.getBackdropUrl())
                                    .into(imgBackdrop);

                            chipNotaTMDB.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "%.1f",
                                            detalle.getVoteAverage()
                                    )
                            );

                            if ("movie".equals(detalle.getMediaType())) {
                                tvInfo.setText(detalle.getFechaSalida()
                                        + " • "
                                        + detalle.getRuntime()
                                        + " min"
                                );
                            } else {
                                tvInfo.setText(detalle.getFechaSalida()
                                        + " • "
                                        + detalle.getTemporadas()
                                        + getString(R.string.detail_seasons)
                                        + detalle.getEpisodios()
                                        + getString(R.string.detail_episodes)
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
                                chipNotaGrupo.setText(
                                        String.format(
                                                Locale.getDefault(),
                                                "%.1f", media
                                        )
                                );
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

                            chipNotaUser.setText(
                                    String.format(
                                            Locale.getDefault(), "%.1f",
                                            valoracion.getPuntuacion()
                                    )
                            );
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
                        tvGeneral.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%.1f",
                                        nota
                                )
                        );
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
                                                                chipNotaUser.setText(
                                                                        String.format(
                                                                                Locale.getDefault(),
                                                                                "%.1f",
                                                                                finalNotaGeneral
                                                                        )
                                                                );

                                                                chipNotaGrupo.setText(
                                                                        String.format(
                                                                                Locale.getDefault(),
                                                                                "%.1f",
                                                                                media
                                                                        )
                                                                );

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
                        textView.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%.1f",
                                        nota
                                )
                        );

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
        tvGeneral.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f", media
                )
        );
    }
}