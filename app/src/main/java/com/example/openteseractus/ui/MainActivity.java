package com.example.openteseractus.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openteseractus.servicios.AuthService;
import com.example.openteseractus.ui.auth.LoginActivity;
import com.example.openteseractus.ui.ventanas.HomeActivity;

/**
 * Activity de entrada de la aplicación.
 * Comprueba si hay sesión activa y redirige a {@link com.example.openteseractus.ui.ventanas.HomeActivity}
 * o a {@link com.example.openteseractus.ui.auth.LoginActivity} según corresponda.
 * No muestra ningún layout propio.
 */
public class MainActivity extends AppCompatActivity {

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authService = new AuthService();

        if (authService.hayUsuarioAutenticado()) {
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}