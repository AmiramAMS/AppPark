package com.example.parkmate.data.repository;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createUser(User user, Callback<Void> callback) {
        db.collection("users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getUser(String uid, Callback<User> callback) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError(new IllegalStateException("User document not found"));
                        return;
                    }
                    User user = doc.toObject(User.class);
                    if (user == null) {
                        callback.onError(new IllegalStateException("Failed to parse user document"));
                        return;
                    }
                    if (user.getUid() == null || user.getUid().isEmpty()) {
                        user.setUid(uid);
                    }
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateRadiusPreference(String uid, int radiusPreference, Callback<Void> callback) {
        db.collection("users")
                .document(uid)
                .update("radiusPreference", radiusPreference)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void updateFullName(String uid, String fullName, Callback<Void> callback) {
        db.collection("users")
                .document(uid)
                .update("fullName", fullName)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }
}

