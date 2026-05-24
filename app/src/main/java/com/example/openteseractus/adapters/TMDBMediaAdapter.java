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
import com.example.openteseractus.tmdb.TMDBMedia;

import java.util.List;

public class TMDBMediaAdapter extends RecyclerView.Adapter<TMDBMediaAdapter.ViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(TMDBMedia media);
    }

    private List<TMDBMedia> resultados;
    private OnMediaClickListener listener;

    public TMDBMediaAdapter(
            List<TMDBMedia> resultados,
            OnMediaClickListener listener
    ) {

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

        Glide.with(holder.itemView.getContext())
                .load(media.getPosterUrl())
                .placeholder(R.drawable.bg_rounded_border)
                .centerCrop()
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            listener.onMediaClick(media);
        });
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
        TextView tvTitulo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
        }
    }
}