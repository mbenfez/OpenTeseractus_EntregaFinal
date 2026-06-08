package com.example.openteseractus.adapters;

import static com.example.openteseractus.R.string.invite_msg;

import android.widget.ImageButton;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openteseractus.R;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.modelos.Invitacion;
import com.example.openteseractus.repositorios.InvitacionRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Adapter de RecyclerView para la lista de invitaciones pendientes.
 * Gestiona los botones de aceptar y rechazar invitación con actualización optimista
 * de la lista (elimina el ítem antes de confirmar la operación en servidor).
 */
public class InvitacionAdapter extends RecyclerView.Adapter<InvitacionAdapter.ViewHolder> {

    private List<Invitacion> invitaciones;

    private InvitacionRepository invitacionRepository;

    public InvitacionAdapter(List<Invitacion> invitaciones, InvitacionRepository invitacionRepository) {
        this.invitaciones = invitaciones;
        this.invitacionRepository = invitacionRepository;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitacion, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Invitacion invitacion = invitaciones.get(position);

        holder.tvGrupo.setText(invitacion.getNombreGrupo());
        holder.tvMensaje.setText(invitacion.getNombreUsuario() + " " +
                holder.itemView.getContext().getString(invite_msg));

        holder.btnAceptar.setOnClickListener(v -> {

            int pos = holder.getAbsoluteAdapterPosition();

            invitacionRepository.aceptarInvitacion(
                    invitacion, new FirestoreCallback<Void>() {

                        @Override
                        public void onSuccess(Void resultado) {
                            invitaciones.remove(pos);
                            notifyItemRemoved(pos);
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    "Te has unido al grupo",
                                    Toast.LENGTH_SHORT
                            ).show();

                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        holder.btnRechazar.setOnClickListener(v -> {

            int pos = holder.getAbsoluteAdapterPosition();

            invitacionRepository.rechazarInvitacion(
                    invitacion.getId(), invitacion.getUidInvitado(),
                    new FirestoreCallback<Void>() {

                        @Override
                        public void onSuccess(Void resultado) {
                            invitaciones.remove(pos);
                            notifyItemRemoved(pos);
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return invitaciones.size();
    }

    public void borrarTodas() {
        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        invitacionRepository.borrarInvitacionesDeUsuario(uid,
                new FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void resultado) {
                        invitaciones.clear();
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String error) {
                    }
                });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvGrupo;
        TextView tvMensaje;

        ImageButton btnAceptar;
        ImageButton btnRechazar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGrupo = itemView.findViewById(R.id.tvGrupo);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}