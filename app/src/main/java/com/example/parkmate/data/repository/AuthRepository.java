package com.example.parkmate.data.repository;

import androidx.annotation.Nullable;

import com.example.parkmate.data.callback.Callback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private final FirebaseAuth auth;

    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void signUp(String email, String password, Callback<String> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        callback.onError(new IllegalStateException("Sign up succeeded but user is null"));
                        return;
                    }
                    callback.onSuccess(user.getUid());
                })
                .addOnFailureListener(callback::onError);
    }

    public void signIn(String email, String password, Callback<String> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        callback.onError(new IllegalStateException("Sign in succeeded but user is null"));
                        return;
                    }
                    callback.onSuccess(user.getUid());
                })
                .addOnFailureListener(callback::onError);
    }

    public void signOut() {
        auth.signOut();
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    @Nullable
    public String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return (user == null) ? null : user.getUid();
    }
}

