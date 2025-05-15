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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Guest {
    private final SimpleIntegerProperty guestId;
    private final SimpleStringProperty name;
    private final SimpleStringProperty phoneNumber;
    private final SimpleStringProperty email;
    private final SimpleStringProperty address;
    private final SimpleStringProperty feedback;

    public Guest(int guestId, String name, String phoneNumber, String email, String address) {
        this.guestId = new SimpleIntegerProperty(guestId);
        this.name = new SimpleStringProperty(name);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.email = new SimpleStringProperty(email);
        this.address = new SimpleStringProperty(address);
        this.feedback = new SimpleStringProperty("");
    }

    // ✅ Getters
    public int getGuestId() { return guestId.get(); }
    public String getName() { return name.get(); }
    public String getPhoneNumber() { return phoneNumber.get(); }
    public String getEmail() { return email.get(); }
    public String getAddress() { return address.get(); }
    public String getFeedback() { return feedback.get(); }

    // ✅ Property Methods for JavaFX Binding
    public SimpleIntegerProperty guestIdProperty() { return guestId; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty phoneProperty() { return phoneNumber; }
    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty addressProperty() { return address; }
    public SimpleStringProperty feedbackProperty() { return feedback; }

    // ✅ Setters
    public void setName(String name) { this.name.set(name); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber.set(phoneNumber); }
    public void setEmail(String email) { this.email.set(email); }
    public void setAddress(String address) { this.address.set(address); }
    public void setFeedback(String feedback) { this.feedback.set(feedback); }
}
