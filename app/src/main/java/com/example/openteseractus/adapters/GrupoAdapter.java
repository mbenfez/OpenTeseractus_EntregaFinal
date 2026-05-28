package com.example.openteseractus.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.openteseractus.R;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Grupo;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.example.openteseractus.repositorios.UsuarioRepository;

import java.util.List;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    private List<Grupo> grupos;
    private OnGrupoClickListener listener;
    private GrupoRepository grupoRepository;
    private UsuarioRepository usuarioRepository;

    public interface OnGrupoClickListener {
        void onGrupoClick(Grupo grupo);
    }

    public GrupoAdapter(List<Grupo> grupos, OnGrupoClickListener listener) {
        this.grupos = grupos;
        this.listener = listener;
        grupoRepository = new GrupoRepository();
        usuarioRepository = new UsuarioRepository();
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grupo, parent, false);
        return new GrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        holder.bind(grupos.get(position));
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    public void updateGrupos(List<Grupo> nuevosGrupos) {
        this.grupos = nuevosGrupos;
        notifyDataSetChanged();
    }

    class GrupoViewHolder extends RecyclerView.ViewHolder {

        ImageView ivGrupoFoto;
        TextView tvNombreGrupo;
        TextView tvCantidadMiembros;
        TextView tvCreadorGrupo;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGrupoFoto = itemView.findViewById(R.id.ivGrupoFoto);
            tvNombreGrupo = itemView.findViewById(R.id.tvNombreGrupo);
            tvCantidadMiembros = itemView.findViewById(R.id.tvCantidadMiembros);
            tvCreadorGrupo = itemView.findViewById(R.id.tvCreadorGrupo);
        }

        public void bind(Grupo grupo) {
            tvNombreGrupo.setText(grupo.getNomGrupo());
            tvCreadorGrupo.setText("");

            // Member count
            grupoRepository.obtenerCantidadMiembros(grupo.getId(),
                    new FirestoreCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer resultado) {
                            tvCantidadMiembros.setText(
                                    resultado + " miembro" + (resultado != 1 ? "s" : "")
                            );
                        }
                        @Override
                        public void onFailure(String error) {
                            tvCantidadMiembros.setText("0 miembros");
                        }
                    });

            // Creator username
            if (grupo.getUidCreador() != null) {
                usuarioRepository.obtenerUsuario(grupo.getUidCreador(),
                        new FirestoreCallback<Usuario>() {
                            @Override
                            public void onSuccess(Usuario usuario) {
                                if (usuario.getUsername() != null) {
                                    tvCreadorGrupo.setText(
                                            itemView.getContext().getString(R.string.grupo_creado_por)
                                                    + "@" + usuario.getUsername()
                                    );
                                }
                            }
                            @Override
                            public void onFailure(String error) {
                                Log.w("GrupoAdapter", "No se pudo obtener creador: " + error);
                            }
                        });
            }

            // Group image
            String fotoUrl = grupo.getFotoGrupoUrl();
            Log.d("DEBUG_IMAGEN", "Grupo: " + grupo.getNomGrupo());
            Log.d("DEBUG_IMAGEN", "URL: " + (fotoUrl != null ? fotoUrl : "NULL"));

            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(fotoUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivGrupoFoto);
            } else {
                ivGrupoFoto.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onGrupoClick(grupo);
            });
        }
    }
}
