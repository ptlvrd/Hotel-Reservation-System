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
import java.time.LocalDate;

public class Reservation {
    private final IntegerProperty reservationId;
    private final StringProperty guestName;
    private final IntegerProperty guestId;
    private final IntegerProperty roomId;
    private final StringProperty roomType;
    private final ObjectProperty<LocalDate> checkInDate;
    private final ObjectProperty<LocalDate> checkOutDate;
    private final IntegerProperty numGuests;
    private final StringProperty status;
    private final DoubleProperty totalAmount;

    public Reservation(int reservationId, String guestName, int guestId, int roomId, String roomType, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, String status, double totalAmount) {
        this.reservationId = new SimpleIntegerProperty(reservationId);
        this.guestName = new SimpleStringProperty(guestName);
        this.guestId = new SimpleIntegerProperty(guestId);
        this.roomId = new SimpleIntegerProperty(roomId);
        this.roomType = new SimpleStringProperty(roomType);
        this.checkInDate = new SimpleObjectProperty<>(checkInDate);
        this.checkOutDate = new SimpleObjectProperty<>(checkOutDate);
        this.numGuests = new SimpleIntegerProperty(numGuests);
        this.status = new SimpleStringProperty(status);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
    }

    // ✅ Getters
    public int getReservationId() { return reservationId.get(); }
    public String getGuestName() { return guestName.get(); }
    public int getGuestId() { return guestId.get(); }
    public int getRoomId() { return roomId.get(); }
    public String getRoomType() { return roomType.get(); }
    public LocalDate getCheckInDate() { return checkInDate.get(); }
    public LocalDate getCheckOutDate() { return checkOutDate.get(); }
    public int getNumGuests() { return numGuests.get(); }
    public String getStatus() { return status.get(); }
    public double getTotalAmount() { return totalAmount.get(); }  // ✅ Added for billing

    // ✅ JavaFX Property Methods for TableView Binding
    public IntegerProperty reservationIdProperty() { return reservationId; }
    public StringProperty guestNameProperty() { return guestName; }
    public IntegerProperty guestIdProperty() { return guestId; }
    public IntegerProperty roomIdProperty() { return roomId; }
    public StringProperty roomTypeProperty() { return roomType; }
    public ObjectProperty<LocalDate> checkInDateProperty() { return checkInDate; }
    public ObjectProperty<LocalDate> checkOutDateProperty() { return checkOutDate; }
    public IntegerProperty numGuestsProperty() { return numGuests; }
    public StringProperty statusProperty() { return status; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }  // ✅ Added for billing

    // ✅ Update Status
    public void cancelReservation() { this.status.set("CANCELLED"); }
    public void checkOut() { this.status.set("CHECKED_OUT"); }
}
