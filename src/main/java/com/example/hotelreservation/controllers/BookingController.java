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
 **********************************************/
package com.example.hotelreservation.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.hotelreservation.models.Room;
import com.example.hotelreservation.database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingController {

    @FXML private Label guestNameLabel;
    @FXML private Label contactDetailsLabel;
    @FXML private Label adultsLabel;
    @FXML private Label childrenLabel;
    @FXML private Label checkinLabel;
    @FXML private Label checkoutLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label priceLabel;
    @FXML private Label totalCostLabel;
    @FXML private VBox completionPanel;

    // Room rates
    private static final double SINGLE_ROOM_RATE = 100.0;
    private static final double DOUBLE_ROOM_RATE = 150.0;
    private static final double DELUX_ROOM_RATE = 250.0;
    private static final double PENTHOUSE_RATE = 400.0;

    private int numAdults;
    private int numChildren;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String guestName;
    private String phoneNumber;
    private String email;
    private String address;

    // Multiple room types
    private int singleRooms;
    private int doubleRooms;
    private int deluxRooms;
    private int penthouseRooms;

    // For backward compatibility
    private Room selectedRoom;

    /**
     * Initialize data from previous screens (single room selection version)
     */
    public void initData(int adults, int children, LocalDate checkin, LocalDate checkout,
                         Room room, String name, String phone, String guestEmail, String guestAddress) {
        this.numAdults = adults;
        this.numChildren = children;
        this.checkIn = checkin;
        this.checkOut = checkout;
        this.selectedRoom = room;
        this.guestName = name;
        this.phoneNumber = phone;
        this.email = guestEmail;
        this.address = guestAddress;

        // Set single room based on type
        this.singleRooms = 0;
        this.doubleRooms = 0;
        this.deluxRooms = 0;
        this.penthouseRooms = 0;

        if (room != null) {
            String roomType = room.getRoomType();
            if (roomType.equalsIgnoreCase("SINGLE")) this.singleRooms = 1;
            else if (roomType.equalsIgnoreCase("DOUBLE")) this.doubleRooms = 1;
            else if (roomType.equalsIgnoreCase("DELUX")) this.deluxRooms = 1;
            else if (roomType.equalsIgnoreCase("PENTHOUSE")) this.penthouseRooms = 1;
        }

        displaySummary();
    }

    /**
     * Initialize data from previous screens (multiple room types version)
     */
    public void initData(int adults, int children, LocalDate checkin, LocalDate checkout,
                         int singleRooms, int doubleRooms, int deluxRooms, int penthouseRooms,
                         String name, String phone, String guestEmail, String guestAddress) {
        this.numAdults = adults;
        this.numChildren = children;
        this.checkIn = checkin;
        this.checkOut = checkout;
        this.singleRooms = singleRooms;
        this.doubleRooms = doubleRooms;
        this.deluxRooms = deluxRooms;
        this.penthouseRooms = penthouseRooms;
        this.guestName = name;
        this.phoneNumber = phone;
        this.email = guestEmail;
        this.address = guestAddress;

        // Set selectedRoom to null as we're using multiple rooms
        this.selectedRoom = null;

        displaySummary();
    }

    private void displaySummary() {
        guestNameLabel.setText(guestName);
        contactDetailsLabel.setText(phoneNumber + " / " + email);
        adultsLabel.setText(String.valueOf(numAdults));
        childrenLabel.setText(String.valueOf(numChildren));
        checkinLabel.setText(checkIn.toString());
        checkoutLabel.setText(checkOut.toString());

        // Calculate total cost based on number of nights
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalCost = 0.0;

        // Build room type description
        StringBuilder roomDescription = new StringBuilder();

        if (selectedRoom != null) {
            // Single room mode
            roomTypeLabel.setText(selectedRoom.getRoomType());
            priceLabel.setText("$" + selectedRoom.getPrice());
            totalCost = nights * selectedRoom.getPrice();
        } else {
            // Multiple room mode
            if (singleRooms > 0) {
                roomDescription.append(singleRooms).append(" Single");
                totalCost += nights * singleRooms * SINGLE_ROOM_RATE;

                if (doubleRooms > 0 || deluxRooms > 0 || penthouseRooms > 0)
                    roomDescription.append(", ");
            }

            if (doubleRooms > 0) {
                roomDescription.append(doubleRooms).append(" Double");
                totalCost += nights * doubleRooms * DOUBLE_ROOM_RATE;

                if (deluxRooms > 0 || penthouseRooms > 0)
                    roomDescription.append(", ");
            }

            if (deluxRooms > 0) {
                roomDescription.append(deluxRooms).append(" Delux");
                totalCost += nights * deluxRooms * DELUX_ROOM_RATE;

                if (penthouseRooms > 0)
                    roomDescription.append(", ");
            }

            if (penthouseRooms > 0) {
                roomDescription.append(penthouseRooms).append(" Penthouse");
                totalCost += nights * penthouseRooms * PENTHOUSE_RATE;
            }

            roomTypeLabel.setText(roomDescription.toString());

            // Create price breakdown
            StringBuilder priceBreakdown = new StringBuilder();
            if (singleRooms > 0)
                priceBreakdown.append("Single: $").append(SINGLE_ROOM_RATE).append("/night");
            if (doubleRooms > 0) {
                if (priceBreakdown.length() > 0) priceBreakdown.append(", ");
                priceBreakdown.append("Double: $").append(DOUBLE_ROOM_RATE).append("/night");
            }
            if (deluxRooms > 0) {
                if (priceBreakdown.length() > 0) priceBreakdown.append(", ");
                priceBreakdown.append("Delux: $").append(DELUX_ROOM_RATE).append("/night");
            }
            if (penthouseRooms > 0) {
                if (priceBreakdown.length() > 0) priceBreakdown.append(", ");
                priceBreakdown.append("Penthouse: $").append(PENTHOUSE_RATE).append("/night");
            }

            priceLabel.setText(priceBreakdown.toString());
        }

        totalCostLabel.setText("$" + totalCost);
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/guestdetails.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) guestNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Guest Details");
        } catch (IOException e) {
            showAlert("Error returning to Guest Details page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleConfirmBooking() {
        // Save the booking to database
        try (Connection conn = Database.getConnection()) {
            // First check if guest exists, if not create a new guest record
            int guestId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT guest_id FROM guests WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    guestId = rs.getInt("guest_id");
                } else {
                    // Create new guest
                    try (PreparedStatement insertGuest = conn.prepareStatement(
                            "INSERT INTO guests (name, phone_number, email, address) VALUES (?, ?, ?, ?)",
                            PreparedStatement.RETURN_GENERATED_KEYS)) {
                        insertGuest.setString(1, guestName);
                        insertGuest.setString(2, phoneNumber);
                        insertGuest.setString(3, email);
                        insertGuest.setString(4, address);
                        insertGuest.executeUpdate();

                        ResultSet keys = insertGuest.getGeneratedKeys();
                        if (keys.next()) {
                            guestId = keys.getInt(1);
                        } else {
                            throw new SQLException("Creating guest failed, no ID obtained.");
                        }
                    }
                }
            }

            // Create reservations for each room type
            if (selectedRoom != null) {
                // Single room mode - use existing logic
                createReservation(conn, guestId, selectedRoom.getRoomId());
            } else {
                // Multiple room mode - create reservations for each room type
                if (singleRooms > 0) {
                    createMultipleReservations(conn, guestId, "SINGLE", singleRooms);
                }

                if (doubleRooms > 0) {
                    createMultipleReservations(conn, guestId, "DOUBLE", doubleRooms);
                }

                if (deluxRooms > 0) {
                    createMultipleReservations(conn, guestId, "DELUX", deluxRooms);
                }

                if (penthouseRooms > 0) {
                    createMultipleReservations(conn, guestId, "PENTHOUSE", penthouseRooms);
                }
            }

            // Show completion message
            completionPanel.setVisible(true);

        } catch (SQLException e) {
            showAlert("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Create a reservation for a specific room ID
     */
    private void createReservation(Connection conn, int guestId, int roomId) throws SQLException {
        // Create the reservation
        int reservationId;
        try (PreparedStatement insertReservation = conn.prepareStatement(
                "INSERT INTO reservations (guest_id, room_id, checkin_date, checkout_date, num_guests, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'CONFIRMED')",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertReservation.setInt(1, guestId);
            insertReservation.setInt(2, roomId);
            insertReservation.setDate(3, java.sql.Date.valueOf(checkIn));
            insertReservation.setDate(4, java.sql.Date.valueOf(checkOut));
            insertReservation.setInt(5, numAdults + numChildren);
            insertReservation.executeUpdate();

            ResultSet keys = insertReservation.getGeneratedKeys();
            if (keys.next()) {
                reservationId = keys.getInt(1);
            } else {
                throw new SQLException("Creating reservation failed, no ID obtained.");
            }
        }

        // Update room status
        try (PreparedStatement updateRoom = conn.prepareStatement(
                "UPDATE rooms SET status = 'BOOKED' WHERE room_id = ?")) {
            updateRoom.setInt(1, roomId);
            updateRoom.executeUpdate();
        }

        // Create billing entry for this reservation
        createBillingEntry(conn, reservationId, roomId);
    }

    /**
     * Create multiple reservations for a specific room type
     */
    private void createMultipleReservations(Connection conn, int guestId, String roomType, int count) throws SQLException {
        // Find available rooms of this type
        try (PreparedStatement findRooms = conn.prepareStatement(
                "SELECT room_id FROM rooms WHERE room_type = ? AND status = 'AVAILABLE' LIMIT ?")) {
            findRooms.setString(1, roomType);
            findRooms.setInt(2, count);
            ResultSet rs = findRooms.executeQuery();

            int roomsBooked = 0;
            while (rs.next() && roomsBooked < count) {
                int roomId = rs.getInt("room_id");
                createReservation(conn, guestId, roomId);
                roomsBooked++;
            }

            if (roomsBooked < count) {
                showAlert("Warning: Only " + roomsBooked + " out of " + count +
                        " requested " + roomType + " rooms were available.", Alert.AlertType.WARNING);
            }
        }
    }

    /**
     * Create a billing entry for a reservation
     */
    private void createBillingEntry(Connection conn, int reservationId, int roomId) throws SQLException {
        // Get room price
        double roomPrice = 0.0;

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT price, room_type FROM rooms WHERE room_id = ?")) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                roomPrice = rs.getDouble("price");
            }
        }

        // Calculate billing
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalAmount = nights * roomPrice;
        double tax = totalAmount * 0.1; // 10% tax

        try (PreparedStatement insertBilling = conn.prepareStatement(
                "INSERT INTO billing (reservation_id, amount, tax, discount, total_amount) " +
                        "VALUES (?, ?, ?, ?, ?)")) {
            insertBilling.setInt(1, reservationId);
            insertBilling.setDouble(2, totalAmount);
            insertBilling.setDouble(3, tax);
            insertBilling.setDouble(4, 0.0); // No discount initially
            insertBilling.setDouble(5, totalAmount + tax); // Total with tax
            insertBilling.executeUpdate();
        }
    }

    @FXML
    private void handleExit() {
        try {
            // Navigate back to the main/start screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Booking.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) completionPanel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Hotel Reservation System");
        } catch (IOException e) {
            showAlert("Error returning to main screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}