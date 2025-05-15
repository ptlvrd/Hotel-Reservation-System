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
 **********************************************/package com.example.hotelreservation.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.example.hotelreservation.models.Room;
import com.example.hotelreservation.database.Database;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomController {

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Number> roomNumberColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, String> statusColumn;
    @FXML private TableColumn<Room, Number> priceColumn;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button selectRoomButton; // For kiosk mode

    private final ObservableList<Room> roomsList = FXCollections.observableArrayList();
    private final ObservableList<Room> allRoomsList = FXCollections.observableArrayList(); // Keep a copy of all rooms
    private boolean isKioskMode = false; // Flag to check if it's used in Kiosk mode

    @FXML
    public void initialize() {
        // Set up cell value factories
        roomNumberColumn.setCellValueFactory(cellData -> cellData.getValue().roomIdProperty());
        roomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());

        // Set up the status filter
        statusFilter.setItems(FXCollections.observableArrayList("All", "AVAILABLE", "BOOKED"));
        statusFilter.setValue("All");

        // Load rooms based on mode
        if (isKioskMode) {
            loadAvailableRooms();
            // Handle kiosk-specific UI elements
            if (selectRoomButton != null) {
                selectRoomButton.setVisible(true);
            }
        } else {
            loadAllRooms();
            // Hide kiosk-specific elements when in admin mode
            if (selectRoomButton != null) {
                selectRoomButton.setVisible(false);
            }
        }
    }

    // Load all rooms (Admin mode)
    private void loadAllRooms() {
        allRoomsList.clear();
        roomsList.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM rooms")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rs.getInt("num_beds"),
                        rs.getDouble("price"),
                        rs.getString("status")
                );
                allRoomsList.add(room);
                roomsList.add(room);
            }
            roomsTable.setItems(roomsList);
        } catch (SQLException e) {
            showAlert("Error loading rooms: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Load only available rooms (Kiosk mode)
    private void loadAvailableRooms() {
        roomsList.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM rooms WHERE status = 'AVAILABLE'")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomsList.add(new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rs.getInt("num_beds"),
                        rs.getDouble("price"),
                        rs.getString("status")
                ));
            }
            roomsTable.setItems(roomsList);
        } catch (SQLException e) {
            showAlert("Error loading available rooms: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdatePrice() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("Please select a room to update.", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (priceField.getText().isEmpty()) {
                showAlert("Please enter a price.", Alert.AlertType.WARNING);
                return;
            }

            double newPrice = Double.parseDouble(priceField.getText());
            if (newPrice <= 0) {
                showAlert("Price must be greater than zero.", Alert.AlertType.WARNING);
                return;
            }

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE rooms SET price = ? WHERE room_id = ?")) {

                stmt.setDouble(1, newPrice);
                stmt.setInt(2, selectedRoom.getRoomId());
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    selectedRoom.setPrice(newPrice);
                    roomsTable.refresh();
                    showAlert("Price updated successfully!", Alert.AlertType.INFORMATION);
                    priceField.clear();
                } else {
                    showAlert("Failed to update price. Room not found.", Alert.AlertType.ERROR);
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Please enter a valid price.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            showAlert("Error updating price: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFilter() {
        String filter = statusFilter.getValue();
        ObservableList<Room> filteredList = FXCollections.observableArrayList();

        if (filter.equals("All")) {
            filteredList.addAll(allRoomsList);
        } else {
            for (Room room : allRoomsList) {
                if (room.getStatus().equals(filter)) {
                    filteredList.add(room);
                }
            }
        }

        roomsTable.setItems(filteredList);
    }

    @FXML
    private void handleResetFilter() {
        statusFilter.setValue("All");
        roomsTable.setItems(allRoomsList);
    }

    // Room selection for Kiosk mode
    @FXML
    private void handleSelectRoom() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("Please select a room.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/guestInfo.fxml"));
            Parent guestInfoRoot = loader.load();

            // You may want to pass the selected room to the guest info controller
            // GuestInfoController controller = loader.getController();
            // controller.setSelectedRoom(selectedRoom);

            Stage stage = (Stage) roomsTable.getScene().getWindow();
            stage.setScene(new Scene(guestInfoRoot));
            stage.setTitle("Guest Information");

        } catch (IOException e) {
            showAlert("Error loading Guest Information page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Method to change room status
    @FXML
    private void handleChangeStatus() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("Please select a room to update status.", Alert.AlertType.WARNING);
            return;
        }

        String newStatus = selectedRoom.getStatus().equals("AVAILABLE") ? "BOOKED" : "AVAILABLE";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE rooms SET status = ? WHERE room_id = ?")) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, selectedRoom.getRoomId());
            int result = stmt.executeUpdate();

            if (result > 0) {
                selectedRoom.setStatus(newStatus);
                roomsTable.refresh();
                showAlert("Room status updated to " + newStatus, Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Error updating room status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Set mode dynamically
    public void setKioskMode(boolean kioskMode) {
        this.isKioskMode = kioskMode;
        if (this.roomsTable != null) {
            if (kioskMode) {
                loadAvailableRooms();
            } else {
                loadAllRooms();
            }
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType == Alert.AlertType.ERROR ? "Error" : "Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}