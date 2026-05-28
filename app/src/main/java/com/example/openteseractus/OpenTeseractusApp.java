package com.example.openteseractus;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class OpenTeseractusApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Forzar siempre modo oscuro para que los colores definidos
        // en colors.xml se apliquen en todos los dispositivos,
        // evitando que Material3 DayNight sobreescriba la paleta
        // con colores dinámicos o con el tema claro del sistema.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
