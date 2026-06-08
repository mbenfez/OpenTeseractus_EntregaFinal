package com.example.openteseractus.adapters;

import android.graphics.Color;
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
import com.example.openteseractus.modelos.MiembroGrupo;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.UsuarioRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter de RecyclerView para la lista de miembros de un grupo.
 * Carga asincrónicamente el nombre y la foto de perfil de cada miembro desde Firestore,
 * mantiene una caché interna de usernames para filtrado local sin llamadas adicionales
 * y resalta al usuario actual en el color primario.
 */
public class MiembroAdapter extends RecyclerView.Adapter<MiembroAdapter.ViewHolder> {

    public interface OnMiembroLongClickListener {
        void onLongClick(MiembroGrupo miembro, String username);
    }

    private List<MiembroGrupo> miembros;
    private List<MiembroGrupo> todosMiembros = new ArrayList<>();
    private final Map<String, String> usernameCache = new HashMap<>();
    private final String uidActual;
    private OnMiembroLongClickListener longClickListener;
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    public MiembroAdapter(List<MiembroGrupo> miembros, String uidActual) {
        this.miembros   = miembros;
        this.uidActual  = uidActual;
    }

    public void setOnMiembroLongClickListener(OnMiembroLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_miembro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MiembroGrupo miembro = miembros.get(position);
        boolean esMiActual = miembro.getUidMiembro() != null
                && miembro.getUidMiembro().equals(uidActual);

        holder.tvRol.setText(miembro.getRol());
        holder.tvNombre.setText("Cargando…");
        holder.viewEstado.setBackgroundResource(R.drawable.circle_gray);

        if (esMiActual) {
            holder.tvNombre.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.primary, null));
        } else {
            holder.tvNombre.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.onSurface, null));
        }

        usuarioRepository.obtenerUsuario(miembro.getUidMiembro(), new FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                usernameCache.put(miembro.getUidMiembro(), usuario.getUsername());
                holder.tvNombre.setText(usuario.getUsername());

                Glide.with(holder.itemView.getContext())
                        .load(usuario.getFotoPerfilUrl())
                        .placeholder(R.drawable.pfp_placeholder)
                        .error(R.drawable.pfp_placeholder)
                        .circleCrop()
                        .into(holder.fotoPerf);

                holder.viewEstado.setBackgroundResource(
                        usuario.isActivo() ? R.drawable.circle_blue : R.drawable.circle_gray);

                if (longClickListener != null && !esMiActual) {
                    holder.itemView.setOnLongClickListener(v -> {
                        longClickListener.onLongClick(miembro, usuario.getUsername());
                        return true;
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                holder.tvNombre.setText("Error");
            }
        });
    }

    @Override
    public int getItemCount() {
        return miembros.size();
    }

    public void update(List<MiembroGrupo> nuevos) {
        todosMiembros.clear();
        todosMiembros.addAll(nuevos);
        miembros.clear();
        miembros.addAll(nuevos);
        notifyDataSetChanged();
        
        for (MiembroGrupo m : nuevos) {
            if (!usernameCache.containsKey(m.getUidMiembro())) {
                usuarioRepository.obtenerUsuario(m.getUidMiembro(), new FirestoreCallback<Usuario>() {
                    @Override
                    public void onSuccess(Usuario usuario) {
                        usernameCache.put(m.getUidMiembro(), usuario.getUsername());
                    }
                    @Override
                    public void onFailure(String error) {}
                });
            }
        }
    }

    /**
     * Filtra los miembros visibles por username usando la caché local.
     * Si la consulta está vacía, restaura la lista completa.
     *
     * @param query texto de búsqueda (insensible a mayúsculas)
     */
    public void filtrar(String query) {
        miembros.clear();
        if (query == null || query.trim().isEmpty()) {
            miembros.addAll(todosMiembros);
        } else {
            String q = query.trim().toLowerCase();
            for (MiembroGrupo m : todosMiembros) {
                String username = usernameCache.get(m.getUidMiembro());
                if (username != null && username.toLowerCase().contains(q)) {
                    miembros.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvRol;
        ImageView fotoPerf;
        View viewEstado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre   = itemView.findViewById(R.id.tvNombre);
            tvRol      = itemView.findViewById(R.id.tvRol);
            fotoPerf   = itemView.findViewById(R.id.imgUsuario);
            viewEstado = itemView.findViewById(R.id.viewEstado);
        }
    }
}