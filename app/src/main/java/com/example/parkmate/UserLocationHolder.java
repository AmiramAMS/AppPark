package com.example.parkmate;

/**
 * Holds last known user location in memory (singleton).
 */
public final class UserLocationHolder {
    private static double lat;
    private static double lng;
    private static boolean hasLocation;

    public static void set(double latitude, double longitude) {
        lat = latitude;
        lng = longitude;
        hasLocation = true;
    }

    public static double getLat() { return lat; }
    public static double getLng() { return lng; }
    public static boolean hasLocation() { return hasLocation; }
}
