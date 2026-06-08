package com.example.openteseractus;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Clase {@link android.app.Application} de OpenTeseractus.
 * Se ejecuta antes de cualquier Activity y aplica configuración global:
 * fuerza el modo oscuro en toda la aplicación.
 */
public class OpenTeseractusApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}