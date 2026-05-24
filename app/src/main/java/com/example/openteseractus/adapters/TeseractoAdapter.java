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
import com.example.openteseractus.modelos.Teseracto;
import com.example.openteseractus.ui.ventanas.TeseractoActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeseractoAdapter extends RecyclerView.Adapter<TeseractoAdapter.ViewHolder> {

    private List<Teseracto> teseractos;

    public TeseractoAdapter(List<Teseracto> teseractos) {
        this.teseractos = teseractos;
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

        holder.tvTitulo.setText(t.getTitulo());

        if (t.getNotaMedia() != 0) {
            holder.tvNota.setText(String.format("%s/10", t.getNotaMedia()));
        } else {
            holder.tvNota.setText("· /10");
        }

        Glide.with(holder.itemView.getContext())
                .load(t.getPosterUrl())
                .placeholder(R.drawable.bg_rounded_border)
                .error(R.drawable.bg_rounded_border)
                .centerCrop()
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    holder.itemView.getContext(),
                    TeseractoActivity.class
            );
            intent.putExtra("TESERACTO_ID", t.getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return teseractos.size();
    }

    public void update(List<Teseracto> nuevos) {
        teseractos.clear();
        teseractos.addAll(nuevos);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo, tvNota, tvFecha;
        ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvNota = itemView.findViewById(R.id.tvNota);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }
    }
}