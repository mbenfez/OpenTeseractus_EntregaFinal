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
import com.example.openteseractus.modelos.MiembroGrupo;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.repositorios.UsuarioRepository;

import java.util.List;

public class MiembroAdapter extends RecyclerView.Adapter<MiembroAdapter.ViewHolder> {

    private List<MiembroGrupo> miembros;
    private UsuarioRepository usuarioRepository;

    public MiembroAdapter(List<MiembroGrupo> miembros) {
        this.miembros = miembros;
        this.usuarioRepository = new UsuarioRepository();
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

        holder.tvRol.setText(miembro.getRol());
        holder.tvNombre.setText("Cargando...");

        // Estado por defecto (gris mientras carga)
        holder.viewEstado.setBackgroundResource(R.drawable.circle_gray);

        usuarioRepository.obtenerUsuario(miembro.getUidMiembro(),
                new FirestoreCallback<Usuario>() {
                    @Override
                    public void onSuccess(Usuario usuario) {

                        holder.tvNombre.setText(usuario.getUsername());
                        Glide.with(holder.itemView.getContext())
                                .load(usuario.getFotoPerfilUrl())
                                .placeholder(R.drawable.pfp_placeholder)
                                .error(R.drawable.pfp_placeholder)
                                .circleCrop()
                                .into(holder.fotoPerf);

                        if (usuario.isActivo()) {
                            holder.viewEstado.setBackgroundResource(R.drawable.circle_blue);
                        } else {
                            holder.viewEstado.setBackgroundResource(R.drawable.circle_gray);
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
        miembros.clear();
        miembros.addAll(nuevos);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre;
        TextView tvRol;
        ImageView fotoPerf;
        View viewEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvRol = itemView.findViewById(R.id.tvRol);
            fotoPerf = itemView.findViewById(R.id.imgUsuario);
            viewEstado = itemView.findViewById(R.id.viewEstado);
        }
    }
}