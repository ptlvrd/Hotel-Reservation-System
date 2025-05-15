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
import javafx.stage.Stage;
import com.example.hotelreservation.models.Room;
import com.example.hotelreservation.database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomSelectionController {

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, Integer> maxGuestsColumn;
    @FXML private TableColumn<Room, Double> priceColumn;
    @FXML private Button selectRoomButton;

    private final ObservableList<Room> availableRooms = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        roomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        maxGuestsColumn.setCellValueFactory(cellData -> cellData.getValue().numBedsProperty().asObject());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        loadAvailableRooms();
    }

    private void loadAvailableRooms() {
        availableRooms.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM rooms WHERE status = 'AVAILABLE'")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                availableRooms.add(new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rs.getInt("num_beds"),
                        rs.getDouble("price"),
                        rs.getString("status")
                ));
            }
            roomsTable.setItems(availableRooms);
        } catch (SQLException e) {
            showAlert("Error loading rooms: " + e.getMessage());
        }
    }


    @FXML
    private void handleSelectRoom() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("Please select a room.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/guestDetails.fxml"));
            Parent guestInfoRoot = loader.load();

            Stage stage = (Stage) selectRoomButton.getScene().getWindow();
            stage.setScene(new Scene(guestInfoRoot));
            stage.setTitle("Guest Information");
        } catch (IOException e) {
            showAlert("Error loading Guest Information page.");
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
