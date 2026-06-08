package com.example.openteseractus.callbacks;

/**
 * Callback genérico para operaciones asíncronas contra Firestore.
 *
 * @param <T> tipo del resultado devuelto en caso de éxito
 */
public interface FirestoreCallback<T> {

    /**
     * Se invoca cuando la operación ha finalizado con éxito.
     *
     * @param result resultado de la operación
     */
    void onSuccess(T result);

    /**
     * Se invoca cuando la operación ha fallado.
     *
     * @param error mensaje descriptivo del error
     */
    void onFailure(String error);

}