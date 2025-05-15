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
import javafx.scene.control.*;
import com.example.hotelreservation.database.Database;
import com.example.hotelreservation.models.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class CheckOutController {

    @FXML private TextField searchGuestField;
    @FXML private TextField discountField;
    @FXML private TableView<Reservation> bookingDetailsTable;
    @FXML private TableColumn<Reservation, String> guestNameColumn;
    @FXML private TableColumn<Reservation, String> roomTypeColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> checkOutColumn;
    @FXML private TableColumn<Reservation, Double> totalAmountColumn;
    @FXML private Button confirmCheckOutButton;

    private final ObservableList<Reservation> reservationsList = FXCollections.observableArrayList();
    // Map to store grouped reservation IDs by guest
    private final Map<Integer, List<Integer>> guestReservationsMap = new HashMap<>();

    @FXML
    public void initialize() {
        guestNameColumn.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        roomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        checkInColumn.setCellValueFactory(cellData -> cellData.getValue().checkInDateProperty().asString());
        checkOutColumn.setCellValueFactory(cellData -> cellData.getValue().checkOutDateProperty().asString());
        totalAmountColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());

        // Initialize discountField with default value
        discountField.setText("0");

        loadPendingCheckouts();
    }

    private void loadPendingCheckouts() {
        reservationsList.clear();
        guestReservationsMap.clear();

        // First, gather all reservations and group them by guest
        Map<Integer, GroupedReservation> groupedReservations = new HashMap<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.reservation_id, g.name AS guest_name, g.guest_id, rm.room_id, rm.room_type, " +
                             "r.checkin_date, r.checkout_date, r.num_guests, r.status, b.total_amount " +
                             "FROM reservations r " +
                             "JOIN guests g ON r.guest_id = g.guest_id " +
                             "JOIN rooms rm ON r.room_id = rm.room_id " +
                             "JOIN billing b ON r.reservation_id = b.reservation_id " +
                             "WHERE r.status = 'CONFIRMED'")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int guestId = rs.getInt("guest_id");
                int reservationId = rs.getInt("reservation_id");

                // Add reservation ID to the guest's list
                if (!guestReservationsMap.containsKey(guestId)) {
                    guestReservationsMap.put(guestId, new ArrayList<>());
                }
                guestReservationsMap.get(guestId).add(reservationId);

                // Group reservations by guest
                if (!groupedReservations.containsKey(guestId)) {
                    groupedReservations.put(guestId, new GroupedReservation(
                            guestId,
                            rs.getString("guest_name"),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            LocalDate.MIN,
                            LocalDate.MAX,
                            0.0
                    ));
                }

                GroupedReservation group = groupedReservations.get(guestId);
                group.roomIds.add(rs.getInt("room_id"));
                group.reservationIds.add(reservationId);

                // Update earliest check-in and latest check-out
                LocalDate checkIn = rs.getDate("checkin_date").toLocalDate();
                LocalDate checkOut = rs.getDate("checkout_date").toLocalDate();

                if (checkIn.isAfter(group.earliestCheckIn)) {
                    group.earliestCheckIn = checkIn;
                }

                if (checkOut.isBefore(group.latestCheckOut)) {
                    group.latestCheckOut = checkOut;
                }

                // Add total amount
                group.totalAmount += rs.getDouble("total_amount");

                // Update room type description
                group.roomTypeDescription = updateRoomTypeDescription(group);
            }

            // Now create consolidated reservations for the view
            for (GroupedReservation group : groupedReservations.values()) {
                // First reservation ID will be the primary one for display purposes
                Integer primaryReservationId = group.reservationIds.get(0);
                Integer primaryRoomId = group.roomIds.get(0);

                reservationsList.add(new Reservation(
                        primaryReservationId,
                        group.guestName,
                        group.guestId,
                        primaryRoomId, // Use the first room ID for the reservation object
                        group.roomTypeDescription, // Custom description of all room types
                        group.earliestCheckIn,
                        group.latestCheckOut,
                        0, // Not relevant for display
                        "CONFIRMED",
                        group.totalAmount
                ));
            }

        } catch (SQLException e) {
            showAlert("Error loading pending checkouts: " + e.getMessage());
        }

        bookingDetailsTable.setItems(reservationsList);
    }

    private String updateRoomTypeDescription(GroupedReservation group) {
        if (group.roomIds.size() == 1) {
            return "SINGLE"; // Default room type for single room
        } else {
            return "MULTIPLE (" + group.roomIds.size() + " rooms)";
        }
    }

    @FXML
    private void handleSearchGuest() {
        String keyword = searchGuestField.getText().trim();
        if (keyword.isEmpty()) {
            bookingDetailsTable.setItems(reservationsList);
            return;
        }

        ObservableList<Reservation> filteredList = FXCollections.observableArrayList();
        for (Reservation res : reservationsList) {
            if (res.getGuestName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(res);
            }
        }

        bookingDetailsTable.setItems(filteredList);
    }

    @FXML
    private void handleGenerateBill() {
        Reservation selected = bookingDetailsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a reservation to generate a bill.");
            return;
        }

        try {
            double discount = 0;
            try {
                discount = Double.parseDouble(discountField.getText().trim());
                if (discount < 0 || discount > 100) {
                    throw new NumberFormatException("Discount must be between 0 and 100");
                }
            } catch (NumberFormatException e) {
                showAlert("Please enter a valid discount percentage (0-100).");
                return;
            }

            // Calculate final amount after discount
            double originalAmount = selected.getTotalAmount();
            double discountAmount = originalAmount * (discount / 100);
            double finalAmount = originalAmount - discountAmount;

            // Get list of rooms for this guest
            List<Integer> reservationIds = guestReservationsMap.get(selected.getGuestId());
            StringBuilder roomDetails = new StringBuilder();

            try (Connection conn = Database.getConnection()) {
                for (Integer resId : reservationIds) {
                    PreparedStatement roomStmt = conn.prepareStatement(
                            "SELECT rm.room_type, r.reservation_id " +
                                    "FROM reservations r " +
                                    "JOIN rooms rm ON r.room_id = rm.room_id " +
                                    "WHERE r.reservation_id = ?");
                    roomStmt.setInt(1, resId);
                    ResultSet rs = roomStmt.executeQuery();
                    if (rs.next()) {
                        roomDetails.append("- Room ").append(rs.getString("room_type"))
                                .append(" (Reservation #").append(rs.getInt("reservation_id"))
                                .append(")\n");
                    }
                }
            } catch (SQLException e) {
                showAlert("Error retrieving room details: " + e.getMessage());
            }

            // Show the bill details
            Alert billAlert = new Alert(Alert.AlertType.INFORMATION);
            billAlert.setTitle("Bill Details");
            billAlert.setHeaderText("Guest: " + selected.getGuestName());

            String billDetails = String.format(
                    "Rooms:\n%s\n" +
                            "Check-in Date: %s\n" +
                            "Check-out Date: %s\n" +
                            "Original Amount: $%.2f\n" +
                            "Discount (%.2f%%): $%.2f\n" +
                            "Final Amount: $%.2f",
                    roomDetails.toString(),
                    selected.getCheckInDate(),
                    selected.getCheckOutDate(),
                    originalAmount,
                    discount,
                    discountAmount,
                    finalAmount
            );

            billAlert.setContentText(billDetails);
            billAlert.showAndWait();

            // Update billing information in database for all reservations of this guest
            try (Connection conn = Database.getConnection()) {
                for (Integer resId : reservationIds) {
                    // Fixed: Changed 'discount_percentage' to 'discount'
                    PreparedStatement updateBilling = conn.prepareStatement(
                            "UPDATE billing SET discount = ?, total_amount = (amount + tax) - ? WHERE reservation_id = ?");
                    updateBilling.setDouble(1, discountAmount); // Saving actual discount amount
                    updateBilling.setDouble(2, discountAmount);
                    updateBilling.setInt(3, resId);
                    updateBilling.executeUpdate();
                }
            } catch (SQLException e) {
                showAlert("Error updating billing information: " + e.getMessage());
            }

        } catch (Exception e) {
            showAlert("Error generating bill: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfirmCheckOut() {
        Reservation selected = bookingDetailsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a guest to check out.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            // Get all reservations for this guest
            List<Integer> reservationIds = guestReservationsMap.get(selected.getGuestId());

            // Update all reservations for this guest
            for (Integer resId : reservationIds) {
                // Get room ID for this reservation
                PreparedStatement roomStmt = conn.prepareStatement(
                        "SELECT room_id FROM reservations WHERE reservation_id = ?");
                roomStmt.setInt(1, resId);
                ResultSet rs = roomStmt.executeQuery();

                if (rs.next()) {
                    int roomId = rs.getInt("room_id");

                    // Update reservation status
                    PreparedStatement updateReservation = conn.prepareStatement(
                            "UPDATE reservations SET status = 'CHECKED_OUT' WHERE reservation_id = ?");
                    updateReservation.setInt(1, resId);
                    updateReservation.executeUpdate();

                    // Update room status
                    PreparedStatement updateRoom = conn.prepareStatement(
                            "UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = ?");
                    updateRoom.setInt(1, roomId);
                    updateRoom.executeUpdate();
                }
            }

            // Remove from displayed list
            reservationsList.remove(selected);
            bookingDetailsTable.refresh();
            showAlert("Check-out successful! All rooms for this guest are now available.");
        } catch (SQLException e) {
            showAlert("Error processing check-out: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class to group reservations by guest
    private static class GroupedReservation {
        int guestId;
        String guestName;
        List<Integer> roomIds;
        List<Integer> reservationIds;
        LocalDate earliestCheckIn;
        LocalDate latestCheckOut;
        double totalAmount;
        String roomTypeDescription;

        public GroupedReservation(int guestId, String guestName, List<Integer> roomIds,
                                  List<Integer> reservationIds, LocalDate earliestCheckIn,
                                  LocalDate latestCheckOut, double totalAmount) {
            this.guestId = guestId;
            this.guestName = guestName;
            this.roomIds = roomIds;
            this.reservationIds = reservationIds;
            this.earliestCheckIn = earliestCheckIn;
            this.latestCheckOut = latestCheckOut;
            this.totalAmount = totalAmount;
            this.roomTypeDescription = "SINGLE"; // Default
        }
    }
}