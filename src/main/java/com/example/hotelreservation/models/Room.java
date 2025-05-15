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

import javafx.beans.property.*;

public class Room {
    private final IntegerProperty roomId;
    private final StringProperty roomType;
    private final IntegerProperty numBeds;
    private final DoubleProperty price;
    private final StringProperty status;

    public Room(int roomId, String roomType, int numBeds, double price, String status) {
        this.roomId = new SimpleIntegerProperty(roomId);
        this.roomType = new SimpleStringProperty(roomType);
        this.numBeds = new SimpleIntegerProperty(numBeds);
        this.price = new SimpleDoubleProperty(price);
        this.status = new SimpleStringProperty(status);
    }

    // Getters
    public int getRoomId() { return roomId.get(); }
    public String getRoomType() { return roomType.get(); }
    public int getNumBeds() { return numBeds.get(); }
    public double getPrice() { return price.get(); }
    public String getStatus() { return status.get(); }

    // Setters
    public void setRoomId(int roomId) { this.roomId.set(roomId); }
    public void setRoomType(String roomType) { this.roomType.set(roomType); }
    public void setNumBeds(int numBeds) { this.numBeds.set(numBeds); }
    public void setPrice(double price) { this.price.set(price); }
    public void setStatus(String status) { this.status.set(status); }

    // Property getters
    public IntegerProperty roomIdProperty() { return roomId; }
    public StringProperty roomTypeProperty() { return roomType; }
    public IntegerProperty numBedsProperty() { return numBeds; }
    public DoubleProperty priceProperty() { return price; }
    public StringProperty statusProperty() { return status; }

    @Override
    public String toString() {
        return "Room #" + getRoomId() + " (" + getRoomType() + ")";
    }
}