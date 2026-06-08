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

/**
 * Adapter de RecyclerView para el chat de un teseracto.
 * Maneja tres tipos de vista: separador de fecha ({@code TIPO_FECHA}),
 * mensaje propio enviado ({@code TIPO_ENVIADO}) y mensaje recibido ({@code TIPO_RECIBIDO}).
 * Los separadores de fecha se insertan automáticamente al llamar a {@link #update(java.util.List)}.
 */
public class MensajeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_FECHA    = 0;
    private static final int TIPO_ENVIADO  = 1;
    private static final int TIPO_RECIBIDO = 2;

    private final String uidActual;
    private final Map<String, String> nombresUsuarios;

    private List<Object> items = new ArrayList<>();

    private final SimpleDateFormat sdfHora  = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat sdfFecha = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    private static final String HOY   = "Hoy";
    private static final String AYER  = "Ayer";

    public MensajeAdapter(String uidActual, Map<String, String> nombresUsuarios) {
        this.uidActual = uidActual;
        this.nombresUsuarios = nombresUsuarios;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) return TIPO_FECHA;
        Mensaje m = (Mensaje) item;
        return m.esAutor(uidActual) ? TIPO_ENVIADO : TIPO_RECIBIDO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TIPO_FECHA) {
            View v = inflater.inflate(R.layout.item_chat_fecha, parent, false);
            return new ViewHolderFecha(v);
        } else if (viewType == TIPO_ENVIADO) {
            View v = inflater.inflate(R.layout.item_mensaje_enviado, parent, false);
            return new ViewHolderEnviado(v);
        } else {
            View v = inflater.inflate(R.layout.item_mensaje_recibido, parent, false);
            return new ViewHolderRecibido(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof ViewHolderFecha) {
            ((ViewHolderFecha) holder).tvFecha.setText((String) item);
            return;
        }

        Mensaje m = (Mensaje) item;
        Date fecha = m.getFechaEnvio() > 0 ? new Date(m.getFechaEnvio()) : new Date();
        String hora = sdfHora.format(fecha);

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
        return items.size();
    }

    /**
     * Reemplaza los mensajes mostrados e inserta automáticamente separadores de fecha
     * (etiquetas "Hoy", "Ayer" o la fecha completa) entre grupos de mensajes de días distintos.
     *
     * @param nuevos lista actualizada de mensajes ordenados cronológicamente
     */
    public void update(List<Mensaje> nuevos) {
        items.clear();

        String ultimaFechaKey = null;
        long hoyMs  = startOfDay(System.currentTimeMillis());
        long ayerMs = hoyMs - 86_400_000L;

        for (Mensaje m : nuevos) {
            Date d = m.getFechaEnvio() > 0 ? new Date(m.getFechaEnvio()) : new Date();
            String key = sdfFecha.format(d);

            if (!key.equals(ultimaFechaKey)) {
                long t = startOfDay(d.getTime());
                String label;
                if (t == hoyMs) {
                    label = HOY;
                } else if (t == ayerMs) {
                    label = AYER;
                } else {
                    label = key;
                }
                items.add(label);
                ultimaFechaKey = key;
            }
            items.add(m);
        }

        notifyDataSetChanged();
    }

    private long startOfDay(long ms) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(ms);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    static class ViewHolderFecha extends RecyclerView.ViewHolder {
        TextView tvFecha;
        ViewHolderFecha(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }

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