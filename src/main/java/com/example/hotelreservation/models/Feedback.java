/**********************************************
 Final Project
 Course:APD545 - Semester5
 Last Name: Patel
 First Name:Vrundaben
 ID: 158605220
 Section: NCC
 This assignment represents my own work in accordance with Seneca Academic Policy.
 Signature- Vrunda Patel
 Date:13th April 2025
 **********************************************/package com.example.hotelreservation.models;

public class Feedback {
    private int feedbackId;
    private int guestId;
    private int reservationId;
    private String comments;
    private int rating;

    public Feedback(int feedbackId, int guestId, int reservationId, String comments, int rating) {
        this.feedbackId = feedbackId;
        this.guestId = guestId;
        this.reservationId = reservationId;
        this.comments = comments;
        this.rating = rating;
    }

    public int getFeedbackId() { return feedbackId; }
    public int getGuestId() { return guestId; }
    public int getReservationId() { return reservationId; }
    public String getComments() { return comments; }
    public int getRating() { return rating; }

    public void setComments(String comments) { this.comments = comments; }
    public void setRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
