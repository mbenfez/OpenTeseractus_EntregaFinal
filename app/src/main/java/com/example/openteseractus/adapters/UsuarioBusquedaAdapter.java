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
import com.example.openteseractus.modelos.Usuario;

import java.util.List;

public class UsuarioBusquedaAdapter extends RecyclerView.Adapter<UsuarioBusquedaAdapter.ViewHolder> {

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
    }

    private List<Usuario> usuarios;
    private OnUsuarioClickListener listener;

    public UsuarioBusquedaAdapter(List<Usuario> usuarios, OnUsuarioClickListener listener) {
        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_busqueda, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);

        holder.tvUsername.setText(usuario.getUsername());

        Glide.with(holder.itemView.getContext())
                .load(usuario.getFotoPerfilUrl())
                .placeholder(R.drawable.bg_rounded_border)
                .error(R.drawable.bg_rounded_border)
                .into(holder.pfp);

        if (usuario.isActivo()) {
            holder.viewEstado.setBackgroundResource(R.drawable.circle_blue);
        } else {
            holder.viewEstado.setBackgroundResource(R.drawable.circle_gray);
        }

        holder.itemView.setOnClickListener(v -> listener.onUsuarioClick(usuario));
    }

    @Override
    public int getItemCount() { return usuarios.size(); }

    public void update(List<Usuario> nuevos) {
        usuarios.clear();
        usuarios.addAll(nuevos);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View viewEstado;

        ImageView pfp;

        TextView tvUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            viewEstado = itemView.findViewById(R.id.viewEstado);
            pfp = itemView.findViewById(R.id.pfp);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }

    }

}