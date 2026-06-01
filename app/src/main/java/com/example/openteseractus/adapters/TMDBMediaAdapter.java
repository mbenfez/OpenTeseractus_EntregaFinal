package com.example.openteseractus.adapters;

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
import com.example.openteseractus.tmdb.TMDBDetalle;
import com.example.openteseractus.tmdb.TMDBMedia;
import com.example.openteseractus.tmdb.TMDBRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMDBMediaAdapter extends RecyclerView.Adapter<TMDBMediaAdapter.ViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(TMDBMedia media);
    }

    private List<TMDBMedia> resultados;
    private final OnMediaClickListener listener;
    private final TMDBRepository tmdbRepository = new TMDBRepository();
    // Cache: tmdbId -> director name (null = not yet loaded, "" = no director)
    private final Map<Integer, String> directorCache = new HashMap<>();

    public TMDBMediaAdapter(List<TMDBMedia> resultados, OnMediaClickListener listener) {
        this.resultados = resultados;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tmdb_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TMDBMedia media = resultados.get(position);

        holder.tvTitulo.setText(media.getTitulo());

        String anio = media.getAnio();
        holder.tvAnio.setText(anio.isEmpty() ? "" : anio);
        holder.tvAnio.setVisibility(anio.isEmpty() ? View.GONE : View.VISIBLE);

        Glide.with(holder.itemView.getContext())
                .load(media.getPosterUrl())
                .placeholder(R.drawable.placeholder_poster)
                .error(R.drawable.placeholder_poster)
                .centerCrop()
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> listener.onMediaClick(media));

        // Lazy-load director
        if (directorCache.containsKey(media.getId())) {
            String dir = directorCache.get(media.getId());
            mostrarDirector(holder, dir);
        } else {
            holder.tvDirector.setVisibility(View.GONE);
            directorCache.put(media.getId(), null); // mark as loading
            tmdbRepository.obtenerDetalle(media.getId(), media.getMediaType(),
                    new FirestoreCallback<TMDBDetalle>() {
                        @Override
                        public void onSuccess(TMDBDetalle detalle) {
                            String dir = detalle.getDirectorOCreador();
                            directorCache.put(media.getId(), dir != null ? dir : "");
                            holder.itemView.post(() -> {
                                int pos = holder.getAdapterPosition();
                                if (pos != RecyclerView.NO_ID) {
                                    notifyItemChanged(pos);
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            directorCache.put(media.getId(), "");
                        }
                    });
        }
    }

    private void mostrarDirector(ViewHolder holder, String dir) {
        if (dir != null && !dir.isEmpty()) {
            holder.tvDirector.setText(dir);
            holder.tvDirector.setVisibility(View.VISIBLE);
        } else {
            holder.tvDirector.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return resultados.size();
    }

    public void update(List<TMDBMedia> nuevos) {
        resultados.clear();
        resultados.addAll(nuevos);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitulo, tvAnio, tvDirector;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster  = itemView.findViewById(R.id.imgPoster);
            tvTitulo   = itemView.findViewById(R.id.tvTitulo);
            tvAnio     = itemView.findViewById(R.id.tvAnio);
            tvDirector = itemView.findViewById(R.id.tvDirector);
        }
    }
}
