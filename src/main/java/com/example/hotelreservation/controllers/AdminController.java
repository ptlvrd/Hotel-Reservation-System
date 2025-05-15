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
import javafx.stage.Stage;
import com.example.hotelreservation.models.Reservation;
import com.example.hotelreservation.database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

    @FXML private Label adminNameLabel;
    @FXML private TextField searchField;
    @FXML private Label lastLoginLabel;
    @FXML private TableView<GroupedReservation> adminTable;
    @FXML private TableColumn<GroupedReservation, Number> colID;
    @FXML private TableColumn<GroupedReservation, String> colName;
    @FXML private TableColumn<GroupedReservation, String> colRoom;
    @FXML private TableColumn<GroupedReservation, LocalDate> colCheckIn;
    @FXML private TableColumn<GroupedReservation, LocalDate> colCheckOut;
    @FXML private TableColumn<GroupedReservation, String> colStatus;

    private final ObservableList<GroupedReservation> groupedReservationsList = FXCollections.observableArrayList();

    // Inner class to represent grouped reservations, similar to ReservationController
    public class GroupedReservation {
        private final int groupId; // Using first reservation ID as group ID
        private final String guestName;
        private final int guestId;
        private final LocalDate checkInDate;
        private final LocalDate checkOutDate;
        private final String status;
        private final List<Reservation> individualReservations;
        private String roomTypesSummary;
        private double totalAmount;

        public GroupedReservation(int groupId, String guestName, int guestId, LocalDate checkInDate,
                                  LocalDate checkOutDate, String status) {
            this.groupId = groupId;
            this.guestName = guestName;
            this.guestId = guestId;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.status = status;
            this.individualReservations = new ArrayList<>();
            this.totalAmount = 0.0;
        }

        public void addReservation(Reservation reservation) {
            individualReservations.add(reservation);
            totalAmount += reservation.getTotalAmount();
            updateRoomTypesSummary();
        }

        private void updateRoomTypesSummary() {
            Map<String, Integer> roomCounts = new HashMap<>();

            // Count rooms by type
            for (Reservation res : individualReservations) {
                String roomType = res.getRoomType();
                roomCounts.put(roomType, roomCounts.getOrDefault(roomType, 0) + 1);
            }

            // Build summary string
            StringBuilder summary = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Integer> entry : roomCounts.entrySet()) {
                if (!first) {
                    summary.append(", ");
                }
                summary.append(entry.getValue()).append(" ").append(entry.getKey());
                first = false;
            }

            this.roomTypesSummary = summary.toString();
        }

        // Getters
        public int getGroupId() { return groupId; }
        public String getGuestName() { return guestName; }
        public LocalDate getCheckInDate() { return checkInDate; }
        public LocalDate getCheckOutDate() { return checkOutDate; }
        public String getStatus() { return status; }
        public String getRoomTypesSummary() { return roomTypesSummary; }

        // JavaFX property methods for TableView binding
        public javafx.beans.property.IntegerProperty reservationIdProperty() {
            return new javafx.beans.property.SimpleIntegerProperty(groupId);
        }
        public javafx.beans.property.StringProperty guestNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(guestName);
        }
        public javafx.beans.property.StringProperty roomTypeProperty() {
            return new javafx.beans.property.SimpleStringProperty(roomTypesSummary);
        }
        public javafx.beans.property.ObjectProperty<LocalDate> checkInDateProperty() {
            return new javafx.beans.property.SimpleObjectProperty<>(checkInDate);
        }
        public javafx.beans.property.ObjectProperty<LocalDate> checkOutDateProperty() {
            return new javafx.beans.property.SimpleObjectProperty<>(checkOutDate);
        }
        public javafx.beans.property.StringProperty statusProperty() {
            return new javafx.beans.property.SimpleStringProperty(status);
        }
    }

    @FXML
    public void initialize() {
        colID.setCellValueFactory(cellData -> cellData.getValue().reservationIdProperty());
        colName.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        colRoom.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        colCheckIn.setCellValueFactory(cellData -> cellData.getValue().checkInDateProperty());
        colCheckOut.setCellValueFactory(cellData -> cellData.getValue().checkOutDateProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        loadReservations();
    }

    private void loadReservations() {
        groupedReservationsList.clear();
        Map<String, GroupedReservation> groupedReservationsMap = new HashMap<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.reservation_id, g.name AS guest_name, g.guest_id, r.room_id, rm.room_type, " +
                             "r.checkin_date, r.checkout_date, r.num_guests, r.status, " +
                             "COALESCE(b.total_amount, 0) as totalAmount " +
                             "FROM reservations r " +
                             "JOIN guests g ON r.guest_id = g.guest_id " +
                             "JOIN rooms rm ON r.room_id = rm.room_id " +
                             "LEFT JOIN billing b ON r.reservation_id = b.reservation_id " +
                             "ORDER BY r.checkin_date DESC")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                String guestName = rs.getString("guest_name");
                int guestId = rs.getInt("guest_id");
                int roomId = rs.getInt("room_id");
                String roomType = rs.getString("room_type");
                LocalDate checkInDate = rs.getDate("checkin_date").toLocalDate();
                LocalDate checkOutDate = rs.getDate("checkout_date").toLocalDate();
                int numGuests = rs.getInt("num_guests");
                String status = rs.getString("status");
                double totalAmount = rs.getDouble("totalAmount");

                // Create individual reservation
                Reservation reservation = new Reservation(
                        reservationId, guestName, guestId, roomId, roomType,
                        checkInDate, checkOutDate, numGuests, status, totalAmount
                );

                // Create a key for grouping: guestId_checkInDate_checkOutDate
                String groupKey = guestId + "_" + checkInDate + "_" + checkOutDate;

                // Add to or create grouped reservation
                if (groupedReservationsMap.containsKey(groupKey)) {
                    groupedReservationsMap.get(groupKey).addReservation(reservation);
                } else {
                    GroupedReservation groupedReservation = new GroupedReservation(
                            reservationId, guestName, guestId, checkInDate, checkOutDate, status
                    );
                    groupedReservation.addReservation(reservation);
                    groupedReservationsMap.put(groupKey, groupedReservation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading reservations: " + e.getMessage());
            showAlert("Error loading reservations: " + e.getMessage());
        }

        // Add all grouped reservations to the observable list
        groupedReservationsList.addAll(groupedReservationsMap.values());
        adminTable.setItems(groupedReservationsList);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            adminTable.setItems(groupedReservationsList);
            return;
        }

        ObservableList<GroupedReservation> filteredList = FXCollections.observableArrayList();
        for (GroupedReservation res : groupedReservationsList) {
            if (res.getGuestName().toLowerCase().contains(keyword.toLowerCase()) ||
                    String.valueOf(res.getGroupId()).contains(keyword) ||
                    res.getRoomTypesSummary().toLowerCase().contains(keyword)) {
                filteredList.add(res);
            }
        }

        adminTable.setItems(filteredList);
    }

    @FXML
    private void handleManageBookings() { loadScreen("/views/guestDetails.fxml", "Manage Bookings"); }

    @FXML
    private void handleGuestCheckOut() { loadScreen("/views/checkout.fxml", "Guest Check-out"); }

    @FXML
    private void handleRoomManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/room.fxml"));
            Parent root = loader.load();

            // Get the controller and set it to admin mode
            RoomController roomController = loader.getController();
            roomController.setKioskMode(false); // Set to admin mode

            Stage stage = new Stage();
            stage.setTitle("Room Management");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading Room Management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBilling() { loadScreen("/views/billing.fxml", "Billing and Payments"); }

    @FXML
    private void handleGuestRecords() { loadScreen("/views/guest.fxml", "Guest Records"); }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        Stage stage = (Stage) adminTable.getScene().getWindow();
        stage.close();

        // Redirect to login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Admin Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (IOException e) {
            System.err.println("Error returning to login: " + e.getMessage());
        }
    }

    private void loadScreen(String fxml, String title) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading " + title + ": " + e.getMessage());
            e.printStackTrace(); // Added for debugging
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openManageBookings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservation.fxml"));
            Parent root = loader.load();

            // Get the controller and call openManageBookings to initialize
            ReservationController controller = loader.getController();
            controller.openManageBookings();

            Stage stage = new Stage();
            stage.setTitle("Manage Bookings");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading Manage Bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}