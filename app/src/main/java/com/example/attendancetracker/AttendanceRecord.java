package com.example.attendancetracker;

public class AttendanceRecord {
    private String name;
    private String status;
    private String date; // Add date field

    // Constructor
    public AttendanceRecord(String name, String status, String date) {
        this.name = name;
        this.status = status;
        this.date = date; // Corrected this line
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Getter for date
    public String getDate() {
        return date; // Return actual date
    }
}
