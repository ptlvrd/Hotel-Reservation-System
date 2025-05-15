/** Final Project
 Course:APD545 - Semester5
 Last Name:
 First Name:
 ID:
 Section:
 This assignment represents my own work in accordance with Seneca Academic Policy.
 Signature-  
 Date:12th April 2025 **/
package com.example.hotelreservation.controllers;

import com.example.hotelreservation.models.Guest;
import com.example.hotelreservation.database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import java.sql.*;

public class GuestController {

    @FXML private TableView<Guest> guestsTable;
    @FXML private TableColumn<Guest, String> guestNameColumn;
    @FXML private TableColumn<Guest, String> phoneColumn;
    @FXML private TableColumn<Guest, String> emailColumn;
    @FXML private TableColumn<Guest, String> addressColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private ObservableList<Guest> guestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        guestNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        phoneColumn.setCellValueFactory(data -> data.getValue().phoneProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        addressColumn.setCellValueFactory(data -> data.getValue().addressProperty());

        guestsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        fetchGuestData();
    }

    private void fetchGuestData() {
        guestList.clear();
        statusLabel.setText("Loading guests...");
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM guests");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Guest g = new Guest(
                        rs.getInt("guest_id"),
                        rs.getString("name"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address")
                );
                String feedback = rs.getString("feedback");
                if (feedback != null) g.setFeedback(feedback);
                guestList.add(g);
            }
            guestsTable.setItems(guestList);
            statusLabel.setText(guestList.size() + " guests loaded");
        } catch (SQLException e) {
            statusLabel.setText("Failed to load guests.");
            alert("Database Error", e.getMessage());
        }
    }

    @FXML
    private void executeSearch() {
        String input = searchField.getText().toLowerCase().trim();
        if (input.isEmpty()) {
            guestsTable.setItems(guestList);
            statusLabel.setText("Showing all " + guestList.size() + " guests");
            return;
        }
        ObservableList<Guest> results = FXCollections.observableArrayList();
        for (Guest g : guestList) {
            if (g.getName().toLowerCase().contains(input) ||
                    g.getPhoneNumber().contains(input) ||
                    g.getEmail().toLowerCase().contains(input)) {
                results.add(g);
            }
        }
        guestsTable.setItems(results);
        statusLabel.setText(results.size() + " results found");
    }

    @FXML
    private void modifySelectedGuest() {
        Guest guest = guestsTable.getSelectionModel().getSelectedItem();
        if (guest == null) {
            alert("Selection Required", "Choose a guest to modify.");
            return;
        }
        showEditPopup(guest);
    }

    @FXML
    private void removeSelectedGuest() {
        Guest guest = guestsTable.getSelectionModel().getSelectedItem();
        if (guest == null) {
            alert("Selection Required", "Choose a guest to delete.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Guest Record");
        confirm.setContentText("Are you sure you want to delete guest: " + guest.getName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) deleteGuest(guest.getGuestId());
        });
    }

    @FXML
    private void reloadGuests() {
        fetchGuestData();
    }

    private void showEditPopup(Guest guest) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Guest");
        dialog.setHeaderText("Update info for " + guest.getName());

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField nameField = new TextField(guest.getName());
        TextField phoneField = new TextField(guest.getPhoneNumber());
        TextField emailField = new TextField(guest.getEmail());
        TextField addressField = new TextField(guest.getAddress());

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Phone:"), phoneField);
        grid.addRow(2, new Label("Email:"), emailField);
        grid.addRow(3, new Label("Address:"), addressField);

        dialog.getDialogPane().setContent(grid);
        nameField.requestFocus();

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                updateGuestRecord(guest.getGuestId(), nameField.getText(), phoneField.getText(), emailField.getText(), addressField.getText());
            }
        });
    }

    private void updateGuestRecord(int id, String name, String phone, String email, String address) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE guests SET name=?, phone_number=?, email=?, address=? WHERE guest_id=?")) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setInt(5, id);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                alert("Success", "Guest updated.");
                fetchGuestData();
            } else {
                alert("Update Failed", "No rows affected.");
            }
        } catch (SQLException e) {
            alert("Database Error", e.getMessage());
        }
    }

    private void deleteGuest(int guestId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM guests WHERE guest_id = ?")) {
            stmt.setInt(1, guestId);
            int removed = stmt.executeUpdate();
            if (removed > 0) {
                alert("Deleted", "Guest removed successfully.");
                fetchGuestData();
            } else {
                alert("Failed", "Could not remove guest.");
            }
        } catch (SQLException e) {
            alert("Database Error", e.getMessage());
        }
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
