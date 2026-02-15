package com.example.parkmate.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.parkmate.data.callback.Callback;
import com.example.parkmate.data.model.ParkingReport;
import com.example.parkmate.data.model.UserStats;
import com.example.parkmate.data.util.GeoUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ParkingReportRepository {

    private static final String COLLECTION_REPORTS = "reports";
    private static final String SUBCOLLECTION_RATINGS = "ratings";
    private static final String SUBCOLLECTION_LIKES = "likes";
    private static final int DEFAULT_NEARBY_LIMIT = 200;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public ParkingReportRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void addReport(double latitude, double longitude, String address, String placeType, String status, Callback<String> callback) {
        String uid = auth.getCurrentUser() == null ? null : auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onError(new IllegalStateException("User must be authenticated"));
            return;
        }
        if (!isValidLatitude(latitude) || !isValidLongitude(longitude)) {
            callback.onError(new IllegalArgumentException("Invalid coordinates"));
            return;
        }
        if (address == null || address.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Address is required"));
            return;
        }
        if (!isValidPlaceType(placeType)) {
            callback.onError(new IllegalArgumentException("Invalid place type"));
            return;
        }
        if (!isValidContractStatus(status)) {
            callback.onError(new IllegalArgumentException("Invalid status"));
            return;
        }

        DocumentReference ref = db.collection(COLLECTION_REPORTS).document();
        ParkingReport report = new ParkingReport(
                ref.getId(),
                uid,
                latitude,
                longitude,
                address.trim(),
                placeType.trim(),
                status,
                Timestamp.now(),
                0,
                0,
                0.0,
                0.0
        );

        ref.set(report)
                .addOnSuccessListener(unused -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(callback::onError);
    }

    public void getAvailableReportsNear(double userLat, double userLng, int radiusKm, String placeType, Callback<ArrayList<ParkingReport>> callback) {
        if (!isValidLatitude(userLat) || !isValidLongitude(userLng)) {
            callback.onError(new IllegalArgumentException("Invalid user coordinates"));
            return;
        }
        if (radiusKm < 0) {
            callback.onError(new IllegalArgumentException("Invalid radius"));
            return;
        }
        if (!isValidPlaceType(placeType)) {
            callback.onError(new IllegalArgumentException("Invalid place type"));
            return;
        }

        db.collection(COLLECTION_REPORTS)
                .whereEqualTo("status", "available")
                .whereEqualTo("placeType", placeType.trim())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(DEFAULT_NEARBY_LIMIT)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<ParkingReport> results = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ParkingReport r = doc.toObject(ParkingReport.class);
                        if (r == null) continue;
                        double d = GeoUtils.distanceKm(userLat, userLng, r.getLatitude(), r.getLongitude());
                        if (d <= radiusKm) {
                            results.add(r);
                        }
                    }
                    results.sort(Comparator.comparingDouble(r -> GeoUtils.distanceKm(userLat, userLng, r.getLatitude(), r.getLongitude())));
                    callback.onSuccess(results);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getReports(Callback<ArrayList<ParkingReport>> callback) {
        db.collection(COLLECTION_REPORTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<ParkingReport> reports = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> {
                        ParkingReport r = doc.toObject(ParkingReport.class);
                        if (r != null) {
                            reports.add(r);
                        }
                    });
                    callback.onSuccess(reports);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateReportStatus(String reportId, String status, Callback<Void> callback) {
        if (!isValidContractStatus(status)) {
            callback.onError(new IllegalArgumentException("Invalid status"));
            return;
        }
        db.collection(COLLECTION_REPORTS).document(reportId)
                .update("status", status.trim())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void deleteReport(String reportId, Callback<Void> callback) {
        db.collection(COLLECTION_REPORTS).document(reportId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getReportById(String reportId, Callback<ParkingReport> callback) {
        db.collection(COLLECTION_REPORTS)
                .document(reportId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError(new IllegalStateException("Report not found"));
                        return;
                    }
                    ParkingReport r = doc.toObject(ParkingReport.class);
                    if (r == null) {
                        callback.onError(new IllegalStateException("Failed to parse report"));
                        return;
                    }
                    if (r.getId() == null || r.getId().isEmpty()) {
                        r.setId(doc.getId());
                    }
                    callback.onSuccess(r);
                })
                .addOnFailureListener(callback::onError);
    }

    public void rateReport(String reportId, int value, Callback<Void> callback) {
        String uid = auth.getCurrentUser() == null ? null : auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onError(new IllegalStateException("User must be authenticated"));
            return;
        }
        if (value < 1 || value > 5) {
            callback.onError(new IllegalArgumentException("Rating must be between 1 and 5"));
            return;
        }

        DocumentReference reportRef = db.collection(COLLECTION_REPORTS).document(reportId);
        DocumentReference ratingRef = reportRef.collection(SUBCOLLECTION_RATINGS).document(uid);
        Timestamp now = Timestamp.now();

        db.runTransaction(transaction -> {
                    DocumentSnapshot reportSnap = transaction.get(reportRef);
                    if (!reportSnap.exists()) {
                        throw new IllegalStateException("Report not found");
                    }

                    DocumentSnapshot ratingSnap = transaction.get(ratingRef);

                    double ratingsSum = getDouble(reportSnap, "ratingsSum");
                    int ratingsCount = getInt(reportSnap, "ratingsCount");

                    Long oldValue = ratingSnap.exists() ? ratingSnap.getLong("value") : null;
                    if (oldValue == null) {
                        ratingsSum += value;
                        ratingsCount += 1;
                    } else {
                        ratingsSum = ratingsSum - oldValue + value;
                    }

                    double average = (ratingsCount <= 0) ? 0.0 : (ratingsSum / ratingsCount);

                    Map<String, Object> ratingDoc = new HashMap<>();
                    ratingDoc.put("value", value);
                    ratingDoc.put("createdAt", now);
                    transaction.set(ratingRef, ratingDoc, SetOptions.merge());

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("ratingsSum", ratingsSum);
                    updates.put("ratingsCount", ratingsCount);
                    updates.put("ratingAverage", average);
                    transaction.update(reportRef, updates);

                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void toggleLike(String reportId, Callback<Integer> callback) {
        String uid = auth.getCurrentUser() == null ? null : auth.getCurrentUser().getUid();
        if (uid == null) {
            callback.onError(new IllegalStateException("User must be authenticated"));
            return;
        }

        DocumentReference reportRef = db.collection(COLLECTION_REPORTS).document(reportId);
        DocumentReference likeRef = reportRef.collection(SUBCOLLECTION_LIKES).document(uid);
        Timestamp now = Timestamp.now();

        db.runTransaction(transaction -> {
                    DocumentSnapshot reportSnap = transaction.get(reportRef);
                    if (!reportSnap.exists()) {
                        throw new IllegalStateException("Report not found");
                    }

                    DocumentSnapshot likeSnap = transaction.get(likeRef);
                    int likesCount = getInt(reportSnap, "likesCount");

                    if (likeSnap.exists()) {
                        transaction.delete(likeRef);
                        likesCount = Math.max(0, likesCount - 1);
                    } else {
                        Map<String, Object> likeDoc = new HashMap<>();
                        likeDoc.put("createdAt", now);
                        transaction.set(likeRef, likeDoc, SetOptions.merge());
                        likesCount += 1;
                    }

                    transaction.update(reportRef, "likesCount", likesCount);
                    return likesCount;
                })
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    public void getUserStats(String ownerId, Callback<UserStats> callback) {
        db.collection(COLLECTION_REPORTS)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int reportsCount = snapshot.size();
                    int totalLikes = 0;
                    double totalRatingsSum = 0.0;
                    int totalRatingsCount = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long likes = doc.getLong("likesCount");
                        if (likes != null) {
                            totalLikes += likes.intValue();
                        }

                        totalRatingsSum += getDouble(doc, "ratingsSum");
                        totalRatingsCount += getInt(doc, "ratingsCount");
                    }

                    double ratingAverageReceived = totalRatingsCount <= 0 ? 0.0 : (totalRatingsSum / totalRatingsCount);
                    callback.onSuccess(new UserStats(reportsCount, totalLikes, ratingAverageReceived));
                })
                .addOnFailureListener(callback::onError);
    }

    private static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    private static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }

    private static boolean isValidContractStatus(@Nullable String status) {
        if (status == null) return false;
        String s = status.trim();
        return s.equals("available") || s.equals("occupied") || s.equals("not_relevant");
    }

    private static boolean isValidPlaceType(@Nullable String placeType) {
        if (placeType == null) return false;
        String t = placeType.trim();
        return t.equals("parking_lot") || t.equals("entertainment");
    }

    private static int getInt(@NonNull DocumentSnapshot snap, @NonNull String field) {
        Long v = snap.getLong(field);
        return v == null ? 0 : v.intValue();
    }

    private static double getDouble(@NonNull DocumentSnapshot snap, @NonNull String field) {
        Double d = snap.getDouble(field);
        if (d != null) return d;
        Long l = snap.getLong(field);
        return l == null ? 0.0 : l.doubleValue();
    }
}

