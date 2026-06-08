package com.example.openteseractus.callbacks;

import com.example.openteseractus.modelos.Usuario;

/**
 * Callback para operaciones de autenticación (registro e inicio de sesión).
 */
public interface AuthCallback {

    /**
     * Se invoca cuando la autenticación ha finalizado con éxito.
     *
     * @param usuario datos del usuario autenticado
     */
    void onSuccess(Usuario usuario);

    /**
     * Se invoca cuando la autenticación ha fallado.
     *
     * @param error mensaje descriptivo del error
     */
    void onFailure(String error);

}