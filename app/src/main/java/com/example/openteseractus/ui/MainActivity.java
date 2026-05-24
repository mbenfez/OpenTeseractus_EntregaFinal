package com.example.openteseractus.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openteseractus.servicios.AuthService;
import com.example.openteseractus.ui.auth.LoginActivity;
import com.example.openteseractus.ui.ventanas.HomeActivity;

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