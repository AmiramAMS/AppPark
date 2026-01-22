package com.example.parkmate;

public class ParkingReport {
    public String id;
    public String address;
    public String status; // "AVAILABLE" / "TAKEN"

    public ParkingReport(String id, String address, String status) {
        this.id = id;
        this.address = address;
        this.status = status;
    }
}
