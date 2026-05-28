package com.example.openteseractus.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.openteseractus.R;
import com.example.openteseractus.callbacks.AuthCallback;
import com.example.openteseractus.modelos.Usuario;
import com.example.openteseractus.servicios.AuthService;
import com.example.openteseractus.ui.MainActivity;
import com.example.openteseractus.ui.ventanas.HomeActivity;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPasswordConfirm, etUsername;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authService = new AuthService();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        etUsername = findViewById(R.id.etUsername);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registrar());

        // Volver a login
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registrar() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, getString(R.string.pswd_no_match), Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario usuario = new Usuario(null, username, new Date());
        usuario.setFechaRegistro(System.currentTimeMillis());
        usuario.setActivo(true);

        authService.registrarUsuario(email, password, usuario, new AuthCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Usuario registrado", Toast.LENGTH_SHORT).show();

                    authService.iniciarSesion(email, password, new AuthCallback() {
                        @Override
                        public void onSuccess(Usuario usuario) {
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() ->
                                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show()
                            );
                        }
                    });
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}