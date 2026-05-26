package com.example.openteseractus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openteseractus.R;
import com.example.openteseractus.modelos.Mensaje;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MensajeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_ENVIADO  = 0;
    private static final int TIPO_RECIBIDO = 1;

    private final String uidActual;
    private List<Mensaje> mensajes = new ArrayList<>();
    // Mapa uid -> username para mostrar el autor en mensajes recibidos
    private Map<String, String> nombresUsuarios;

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MensajeAdapter(String uidActual, Map<String, String> nombresUsuarios) {
        this.uidActual = uidActual;
        this.nombresUsuarios = nombresUsuarios;
    }

    @Override
    public int getItemViewType(int position) {
        Mensaje m = mensajes.get(position);
        android.util.Log.d("CHAT_DEBUG", "uidActual=" + uidActual + "  uidAutor=" + m.getUidAutor());
        return m.esAutor(uidActual) ? TIPO_ENVIADO : TIPO_RECIBIDO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TIPO_ENVIADO) {
            View v = inflater.inflate(R.layout.item_mensaje_enviado, parent, false);
            return new ViewHolderEnviado(v);
        } else {
            View v = inflater.inflate(R.layout.item_mensaje_recibido, parent, false);
            return new ViewHolderRecibido(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje m = mensajes.get(position);
        String hora = sdf.format(new Date(m.getFechaEnvio()));

        if (holder instanceof ViewHolderEnviado) {
            ViewHolderEnviado vh = (ViewHolderEnviado) holder;
            vh.tvContenido.setText(m.getContenido());
            vh.tvHora.setText(hora);
        } else {
            ViewHolderRecibido vh = (ViewHolderRecibido) holder;
            vh.tvContenido.setText(m.getContenido());
            vh.tvHora.setText(hora);
            String nombre = nombresUsuarios.get(m.getUidAutor());
            vh.tvAutor.setText(nombre != null ? nombre : m.getUidAutor());
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    public void update(List<Mensaje> nuevos) {
        mensajes.clear();
        mensajes.addAll(nuevos);
        notifyDataSetChanged();
    }

    // ==================== VIEW HOLDERS ====================

    static class ViewHolderEnviado extends RecyclerView.ViewHolder {
        TextView tvContenido, tvHora;

        ViewHolderEnviado(@NonNull View itemView) {
            super(itemView);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvHora      = itemView.findViewById(R.id.tvHora);
        }
    }

    static class ViewHolderRecibido extends RecyclerView.ViewHolder {
        TextView tvContenido, tvHora, tvAutor;

        ViewHolderRecibido(@NonNull View itemView) {
            super(itemView);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvHora      = itemView.findViewById(R.id.tvHora);
            tvAutor     = itemView.findViewById(R.id.tvAutor);
        }
    }
}
