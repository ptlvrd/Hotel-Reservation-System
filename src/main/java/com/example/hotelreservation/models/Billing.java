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

public class Billing {
    private final IntegerProperty billId;
    private final IntegerProperty reservationId;
    private final DoubleProperty amount;
    private final DoubleProperty tax;
    private final DoubleProperty totalAmount;
    private final DoubleProperty discount;
    private final StringProperty guestName;
    private final StringProperty roomType;

    public Billing(int billId, int reservationId, String guestName, String roomType, double amount, double tax, double discount) {
        this.billId = new SimpleIntegerProperty(billId);
        this.reservationId = new SimpleIntegerProperty(reservationId);
        this.guestName = new SimpleStringProperty(guestName);
        this.roomType = new SimpleStringProperty(roomType);
        this.amount = new SimpleDoubleProperty(amount);
        this.tax = new SimpleDoubleProperty(tax);
        this.discount = new SimpleDoubleProperty(discount);
        this.totalAmount = new SimpleDoubleProperty(calculateTotal());
    }

    // ✅ Getters
    public int getBillId() { return billId.get(); }
    public int getReservationId() { return reservationId.get(); }
    public String getGuestName() { return guestName.get(); }
    public String getRoomType() { return roomType.get(); }
    public double getAmount() { return amount.get(); }
    public double getTax() { return tax.get(); }
    public double getDiscount() { return discount.get(); }
    public double getTotalAmount() { return totalAmount.get(); }

    // ✅ Property Methods for JavaFX Table Binding
    public IntegerProperty billIdProperty() { return billId; }
    public IntegerProperty reservationIdProperty() { return reservationId; }
    public StringProperty guestNameProperty() { return guestName; }
    public StringProperty roomTypeProperty() { return roomType; }
    public DoubleProperty amountProperty() { return amount; }
    public DoubleProperty taxProperty() { return tax; }
    public DoubleProperty discountProperty() { return discount; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }

    // ✅ Update Discount
    public void setDiscount(double discount) {
        this.discount.set(discount);
        this.totalAmount.set(calculateTotal());
    }

    // ✅ Calculate Total Amount
    private double calculateTotal() {
        return (amount.get() + tax.get()) - discount.get();
    }
}
