package com.example.openteseractus.callbacks;

import com.example.openteseractus.modelos.Usuario;

public interface AuthCallback {

    void onSuccess(Usuario usuario);
    void onFailure(String error);

}
