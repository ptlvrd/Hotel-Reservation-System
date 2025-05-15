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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.hotelreservation.models.Room;
import com.example.hotelreservation.models.RoomType;
import com.example.hotelreservation.database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GuestDetailsController {

    // Screen 1: Number of Guests
    @FXML private VBox guestCountScreen;
    @FXML private TextField adultsField;
    @FXML private TextField childrenField;
    @FXML private Button nextToDateButton;

    // Screen 2: Check-in/Check-out Dates
    @FXML private VBox datesScreen;
    @FXML private DatePicker checkinDate;
    @FXML private DatePicker checkoutDate;
    @FXML private Button backToGuestCountButton;
    @FXML private Button nextToRoomSelectionButton;

    // Screen 3: Room Selection
    @FXML private VBox roomSelectionScreen;
    @FXML private Spinner<Integer> singleRoomSpinner;
    @FXML private Spinner<Integer> doubleRoomSpinner;
    @FXML private Spinner<Integer> deluxRoomSpinner;
    @FXML private Spinner<Integer> penthouseSpinner;
    @FXML private Label totalGuestsLabel;
    @FXML private Label roomCapacityLabel;
    @FXML private Button backToDatesButton;
    @FXML private Button nextToGuestInfoButton;

    // Screen 4: Guest Information
    @FXML private VBox guestInfoScreen;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private Button backToRoomSelectionButton;
    @FXML private Button confirmButton;

    // Booking data
    private int totalAdults = 0;
    private int totalChildren = 0;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int singleRooms = 0;
    private int doubleRooms = 0;
    private int deluxRooms = 0;
    private int penthouseRooms = 0;

    @FXML
    public void initialize() {
        // Initialize with only the first screen visible
        showGuestCountScreen();

        // Set up spinners (will be initialized when room selection screen is shown)
        setupRoomCapacityValidation();
    }

    /**
     * Sets up validation to ensure enough rooms for all guests
     */
    private void setupRoomCapacityValidation() {
        // Update room capacity whenever spinners change
        if (singleRoomSpinner != null && doubleRoomSpinner != null &&
                deluxRoomSpinner != null && penthouseSpinner != null) {

            singleRoomSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateRoomCapacity());
            doubleRoomSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateRoomCapacity());
            deluxRoomSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateRoomCapacity());
            penthouseSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateRoomCapacity());
        }
    }

    /**
     * Updates the room capacity label and validates if enough rooms are selected
     */
    private void updateRoomCapacity() {
        int capacity = (singleRoomSpinner.getValue() * 1) +
                (doubleRoomSpinner.getValue() * 2) +
                (deluxRoomSpinner.getValue() * 3) +
                (penthouseSpinner.getValue() * 4);

        int totalGuests = totalAdults + totalChildren;

        roomCapacityLabel.setText("Room Capacity: " + capacity + " guests");

        if (capacity < totalGuests) {
            roomCapacityLabel.setStyle("-fx-text-fill: red;");
            nextToGuestInfoButton.setDisable(true);
        } else {
            roomCapacityLabel.setStyle("-fx-text-fill: green;");
            nextToGuestInfoButton.setDisable(false);
        }
    }

    // Screen visibility management methods
    private void showGuestCountScreen() {
        guestCountScreen.setVisible(true);
        datesScreen.setVisible(false);
        roomSelectionScreen.setVisible(false);
        guestInfoScreen.setVisible(false);
    }

    private void showDatesScreen() {
        guestCountScreen.setVisible(false);
        datesScreen.setVisible(true);
        roomSelectionScreen.setVisible(false);
        guestInfoScreen.setVisible(false);
    }

    private void showRoomSelectionScreen() {
        guestCountScreen.setVisible(false);
        datesScreen.setVisible(false);
        roomSelectionScreen.setVisible(true);
        guestInfoScreen.setVisible(false);

        // Update total guests label
        totalGuestsLabel.setText("Total Guests: " + (totalAdults + totalChildren) +
                " (" + totalAdults + " adults, " + totalChildren + " children)");

        // Initialize spinners if they haven't been already
        if (singleRoomSpinner.getValueFactory() == null) {
            // Create value factories with initial value 0
            SpinnerValueFactory<Integer> singleFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
            SpinnerValueFactory<Integer> doubleFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
            SpinnerValueFactory<Integer> deluxFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0);
            SpinnerValueFactory<Integer> penthouseFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2, 0);

            // Set the factories to the spinners
            singleRoomSpinner.setValueFactory(singleFactory);
            doubleRoomSpinner.setValueFactory(doubleFactory);
            deluxRoomSpinner.setValueFactory(deluxFactory);
            penthouseSpinner.setValueFactory(penthouseFactory);
        }

        updateRoomCapacity();
    }

    private void showGuestInfoScreen() {
        guestCountScreen.setVisible(false);
        datesScreen.setVisible(false);
        roomSelectionScreen.setVisible(false);
        guestInfoScreen.setVisible(true);
    }

    // Navigation handlers

    /**
     * Validates and moves to date selection screen
     */
    @FXML
    private void handleNextToDate() {
        if (!validateGuestCount()) return;

        totalAdults = Integer.parseInt(adultsField.getText());
        totalChildren = Integer.parseInt(childrenField.getText());

        showDatesScreen();
    }

    /**
     * Goes back to guest count screen
     */
    @FXML
    private void handleBackToGuestCount() {
        showGuestCountScreen();
    }

    /**
     * Validates and moves to room selection screen
     */
    @FXML
    private void handleNextToRoomSelection() {
        if (!validateDates()) return;

        checkIn = checkinDate.getValue();
        checkOut = checkoutDate.getValue();

        showRoomSelectionScreen();
    }

    /**
     * Goes back to date selection screen
     */
    @FXML
    private void handleBackToDates() {
        showDatesScreen();
    }

    /**
     * Validates and moves to guest info screen
     */
    @FXML
    private void handleNextToGuestInfo() {
        singleRooms = singleRoomSpinner.getValue();
        doubleRooms = doubleRoomSpinner.getValue();
        deluxRooms = deluxRoomSpinner.getValue();
        penthouseRooms = penthouseSpinner.getValue();
        // Validate room capacity
        int capacity = (singleRooms * 1) + (doubleRooms * 2) +
                (deluxRooms * 3) + (penthouseRooms * 4);
        int totalGuests = totalAdults + totalChildren;

        if (capacity < totalGuests) {
            showAlert("Warning", "Not enough rooms for all guests. Please add more rooms.");
            return;
        }

        showGuestInfoScreen();
    }

    /**
     * Goes back to room selection screen
     */
    @FXML
    private void handleBackToRoomSelection() {
        showRoomSelectionScreen();
    }

    /**
     * Validates guest information and proceeds to booking confirmation
     */
    @FXML
    private void handleConfirmBooking() {
        if (!validateGuestInfo()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/booking.fxml"));
            Parent summaryRoot = loader.load();

            // Pass the data to the BookingController
            BookingController bookingController = loader.getController();
            bookingController.initData(
                    totalAdults,
                    totalChildren,
                    checkIn,
                    checkOut,
                    singleRooms,
                    doubleRooms,
                    deluxRooms,
                    penthouseRooms,
                    nameField.getText(),
                    phoneField.getText(),
                    emailField.getText(),
                    addressField.getText()
            );

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(summaryRoot));
            stage.setTitle("Booking Summary");

        } catch (IOException e) {
            showAlert("Error", "Error loading Booking Summary page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Validation methods

    /**
     * Validates the Guest Count Form
     */
    private boolean validateGuestCount() {
        if (adultsField.getText().isEmpty() || childrenField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields before proceeding.");
            return false;
        }

        try {
            int adults = Integer.parseInt(adultsField.getText());
            int children = Integer.parseInt(childrenField.getText());

            if (adults < 1) {
                showAlert("Error", "At least one adult is required.");
                return false;
            }

            if (adults < 0 || children < 0) {
                showAlert("Error", "Number of guests cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid number format for guests.");
            return false;
        }

        return true;
    }

    /**
     * Validates the Date Selection Form
     */
    private boolean validateDates() {
        if (checkinDate.getValue() == null || checkoutDate.getValue() == null) {
            showAlert("Error", "Please select check-in and check-out dates.");
            return false;
        }

        LocalDate today = LocalDate.now();
        if (checkinDate.getValue().isBefore(today)) {
            showAlert("Error", "Check-in date cannot be in the past.");
            return false;
        }

        if (checkoutDate.getValue().isBefore(checkinDate.getValue())) {
            showAlert("Error", "Check-out date cannot be before check-in date.");
            return false;
        }

        if (checkoutDate.getValue().isEqual(checkinDate.getValue())) {
            showAlert("Error", "Check-out date must be at least one day after check-in date.");
            return false;
        }

        return true;
    }

    /**
     * Validates Guest Information Form
     */
    private boolean validateGuestInfo() {
        if (nameField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                emailField.getText().isEmpty() || addressField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all guest information fields.");
            return false;
        }

        // Additional validation could be added here (email format, phone number format, etc.)

        return true;
    }

    /**
     * Displays messages in an alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}