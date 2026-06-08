package com.example.openteseractus.adapters;

import android.content.Intent;
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
import com.example.openteseractus.modelos.Teseracto;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.MensajeRepository;
import com.example.openteseractus.repositorios.UsuarioRepository;
import com.example.openteseractus.ui.ventanas.TeseractoActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter de RecyclerView para la lista de teseractos de la cartelera de un grupo.
 * Muestra el póster, título, nota media y badge de mensajes no leídos.
 * Los nombres de usuario y el conteo de no leídos se cargan asincrónicamente
 * y se almacenan en cachés internas para evitar peticiones redundantes.
 */
public class TeseractoAdapter extends RecyclerView.Adapter<TeseractoAdapter.ViewHolder> {

    private List<Teseracto> teseractos;
    private List<Teseracto> todosLosTeseractos = new ArrayList<>();
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final MensajeRepository mensajeRepository = new MensajeRepository();
    private final Map<String, String> nombresCache = new HashMap<>();
    private final Map<String, Integer> noLeidosCache = new HashMap<>();
    private final String uidActual;

    public TeseractoAdapter(List<Teseracto> teseractos, String uidActual) {
        this.teseractos = teseractos;
        this.uidActual = uidActual;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teseracto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Teseracto t = teseractos.get(position);

        holder.tvBadgeNoLeidos.setVisibility(View.GONE);
        holder.tvTitulo.setText(t.getTitulo());

        if (t.getNotaMedia() != 0) {
            double nota = t.getNotaMedia();
            String notaStr = nota == Math.floor(nota)
                    ? String.valueOf((int) nota)
                    : String.format("%.1f", nota);
            holder.tvNota.setText(notaStr + "/10");
        } else {
            holder.tvNota.setText("· /10");
        }

        String uid = t.getUidAbiertoPor();
        if (uid != null && !uid.isEmpty()) {
            if (nombresCache.containsKey(uid)) {
                holder.tvAbiertosPor.setText("↪ " + nombresCache.get(uid));
                holder.tvAbiertosPor.setVisibility(View.VISIBLE);
            } else {
                holder.tvAbiertosPor.setVisibility(View.GONE);
                usuarioRepository.obtenerUsuario(uid, new FirestoreCallback<Usuario>() {
                    @Override
                    public void onSuccess(Usuario usuario) {
                        nombresCache.put(uid, usuario.getUsername());
                        holder.tvAbiertosPor.post(() -> {
                            holder.tvAbiertosPor.setText("↪ " + usuario.getUsername());
                            holder.tvAbiertosPor.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onFailure(String error) {}
                });
            }
        } else {
            holder.tvAbiertosPor.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(t.getPosterUrl())
                .placeholder(R.drawable.placeholder_poster)
                .error(R.drawable.placeholder_poster)
                .centerCrop()
                .into(holder.imgPoster);

        if (uidActual != null && t.getId() != null) {
            String tid = t.getId();
            if (noLeidosCache.containsKey(tid)) {
                mostrarBadge(holder.tvBadgeNoLeidos, noLeidosCache.get(tid));
            } else {
                mensajeRepository.contarNoLeidos(uidActual, tid, new FirestoreCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer count) {
                        noLeidosCache.put(tid, count);
                        holder.tvBadgeNoLeidos.post(() -> mostrarBadge(holder.tvBadgeNoLeidos, count));
                    }
                    @Override
                    public void onFailure(String error) {}
                });
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), TeseractoActivity.class);
            intent.putExtra("TESERACTO_ID", t.getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    private void mostrarBadge(TextView badge, int count) {
        if (count > 0) {
            badge.setText(count > 99 ? "99+" : String.valueOf(count));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return teseractos.size();
    }

    public void update(List<Teseracto> nuevos) {
        todosLosTeseractos.clear();
        todosLosTeseractos.addAll(nuevos);
        teseractos.clear();
        teseractos.addAll(nuevos);
        noLeidosCache.clear();
        notifyDataSetChanged();
    }

    public void filtrar(String query) {
        teseractos.clear();
        if (query == null || query.trim().isEmpty()) {
            teseractos.addAll(todosLosTeseractos);
        } else {
            String lower = query.toLowerCase();
            for (Teseracto t : todosLosTeseractos) {
                if (t.getTitulo() != null && t.getTitulo().toLowerCase().contains(lower)) {
                    teseractos.add(t);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo, tvNota, tvAbiertosPor, tvBadgeNoLeidos;
        ImageView imgPoster;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo        = itemView.findViewById(R.id.tvTitulo);
            tvNota          = itemView.findViewById(R.id.tvNota);
            imgPoster       = itemView.findViewById(R.id.imgPoster);
            tvAbiertosPor   = itemView.findViewById(R.id.tvAbiertosPor);
            tvBadgeNoLeidos = itemView.findViewById(R.id.tvBadgeNoLeidos);
        }
    }
}