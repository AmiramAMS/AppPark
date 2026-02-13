package com.example.parkmate.data.callback;

public interface Callback<T> {
    void onSuccess(T data);
    void onError(Exception e);
}

