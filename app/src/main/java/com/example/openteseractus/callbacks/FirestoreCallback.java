package com.example.openteseractus.callbacks;

public interface FirestoreCallback<T> {

    void onSuccess(T result);
    void onFailure(String error);

}
