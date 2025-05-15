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

import com.example.hotelreservation.database.Database;
import com.example.hotelreservation.models.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ReservationController {

    @FXML private TextField searchReservationField;
    @FXML private TableView<GroupedReservation> reservationsTable;
    @FXML private TableColumn<GroupedReservation, Number> reservationIdColumn;
    @FXML private TableColumn<GroupedReservation, String> guestNameColumn;
    @FXML private TableColumn<GroupedReservation, String> roomTypeColumn;
    @FXML private TableColumn<GroupedReservation, LocalDate> checkInColumn;
    @FXML private TableColumn<GroupedReservation, LocalDate> checkOutColumn;
    @FXML private TableColumn<GroupedReservation, String> statusColumn;

    // New components for updating bookings
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    private final Map<Integer, ComboBox<String>> roomTypeComboBoxes = new HashMap<>();
    @FXML private VBox roomTypesContainer;
    @FXML private Spinner<Integer> numGuestsSpinner;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private VBox messageBox;
    @FXML private Label messageLabel;
    @FXML private TitledPane updateBookingPane;
    @FXML private HBox multiRoomIndicator;
    @FXML private Label roomCountLabel;
    @FXML private Label roomTypeLabel;
    private final ObservableList<GroupedReservation> groupedReservationsList = FXCollections.observableArrayList();
    private final ObservableList<String> roomTypes = FXCollections.observableArrayList("SINGLE", "DOUBLE", "DELUX", "PENTHOUSE");
    private final ObservableList<String> statusTypes = FXCollections.observableArrayList("CONFIRMED", "CANCELLED", "CHECKED_OUT");

    // Inner class to represent grouped reservations
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
        public int getGuestId() { return guestId; }
        public LocalDate getCheckInDate() { return checkInDate; }
        public LocalDate getCheckOutDate() { return checkOutDate; }
        public String getStatus() { return status; }
        public String getRoomTypesSummary() { return roomTypesSummary; }
        public double getTotalAmount() { return totalAmount; }
        public List<Reservation> getIndividualReservations() { return individualReservations; }

        // JavaFX property methods
        public javafx.beans.property.IntegerProperty groupIdProperty() {
            return new javafx.beans.property.SimpleIntegerProperty(groupId);
        }
        public javafx.beans.property.StringProperty guestNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(guestName);
        }
        public javafx.beans.property.StringProperty roomTypesSummaryProperty() {
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
        // Initialize table columns
        reservationIdColumn.setCellValueFactory(cellData -> cellData.getValue().groupIdProperty());
        guestNameColumn.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        roomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypesSummaryProperty());
        checkInColumn.setCellValueFactory(cellData -> cellData.getValue().checkInDateProperty());
        checkOutColumn.setCellValueFactory(cellData -> cellData.getValue().checkOutDateProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Initialize update controls
        statusComboBox.setItems(statusTypes);

        // Setup date formatters
        StringConverter<LocalDate> dateConverter = new StringConverter<>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        checkInDatePicker.setConverter(dateConverter);
        checkOutDatePicker.setConverter(dateConverter);

        // Add table selection listener
        reservationsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSelectedReservationDetails(newValue));

        // Load initial data
        loadReservations();
    }

    // Add these field declarations at the top of your class with other @FXML fields

    @FXML
    // Then update the showSelectedReservationDetails method
    private void showSelectedReservationDetails(GroupedReservation groupedReservation) {
        if (groupedReservation != null && !groupedReservation.getIndividualReservations().isEmpty()) {
            // Set common details for all reservations in the group
            checkInDatePicker.setValue(groupedReservation.getCheckInDate());
            checkOutDatePicker.setValue(groupedReservation.getCheckOutDate());
            statusComboBox.setValue(groupedReservation.getStatus());

            // Sum up all guests across reservations
            int totalGuests = 0;
            for (Reservation res : groupedReservation.getIndividualReservations()) {
                totalGuests += res.getNumGuests();
            }
            numGuestsSpinner.getValueFactory().setValue(totalGuests);

            // Update room count label
            int roomCount = groupedReservation.getIndividualReservations().size();
            roomCountLabel.setText("Rooms in this booking: " + roomCount);

            // Clear any existing room type comboboxes
            roomTypesContainer.getChildren().clear();
            roomTypeComboBoxes.clear();

            // Create a combobox for each room in the booking
            for (int i = 0; i < groupedReservation.getIndividualReservations().size(); i++) {
                Reservation reservation = groupedReservation.getIndividualReservations().get(i);

                // Create a new HBox for this room's controls
                HBox roomHBox = new HBox(10);
                roomHBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Add room identifier
                Label roomLabel = new Label("Room " + (i + 1) + " (ID: " + reservation.getRoomId() + "):");
                roomLabel.setPrefWidth(150);

                // Create combobox for this room
                ComboBox<String> roomTypeBox = new ComboBox<>();
                roomTypeBox.setItems(roomTypes);
                roomTypeBox.setValue(reservation.getRoomType());
                roomTypeBox.setPrefWidth(200);

                // Store reference to this combobox with room ID as key
                roomTypeComboBoxes.put(reservation.getRoomId(), roomTypeBox);

                // Add controls to the HBox
                roomHBox.getChildren().addAll(roomLabel, roomTypeBox);

                // Add HBox to the container
                roomTypesContainer.getChildren().add(roomHBox);
            }
        }
    }

    private void loadReservations() {
        groupedReservationsList.clear();
        Map<String, GroupedReservation> groupedReservationsMap = new HashMap<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.reservation_id, g.name AS guest_name, g.guest_id, r.room_id, " +
                             "rm.room_type, r.checkin_date, r.checkout_date, r.num_guests, r.status, " +
                             "COALESCE(b.total_amount, 0) as total_amount " +
                             "FROM reservations r " +
                             "JOIN guests g ON r.guest_id = g.guest_id " +
                             "JOIN rooms rm ON r.room_id = rm.room_id " +
                             "LEFT JOIN billing b ON r.reservation_id = b.reservation_id " +
                             "ORDER BY r.checkin_date DESC, g.guest_id")) {

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
                double totalAmount = rs.getDouble("total_amount");

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
            showAlert("Error loading reservations: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Add all grouped reservations to the observable list
        groupedReservationsList.addAll(groupedReservationsMap.values());
        reservationsTable.setItems(groupedReservationsList);
    }

    @FXML
    private void handleSearchReservation() {
        String query = searchReservationField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            reservationsTable.setItems(groupedReservationsList);
            return;
        }

        ObservableList<GroupedReservation> filteredReservations = FXCollections.observableArrayList();
        for (GroupedReservation res : groupedReservationsList) {
            if (res.getGuestName().toLowerCase().contains(query) ||
                    res.getCheckInDate().toString().contains(query) ||
                    res.getCheckOutDate().toString().contains(query) ||
                    String.valueOf(res.getGroupId()).contains(query) ||
                    res.getRoomTypesSummary().toLowerCase().contains(query)) {
                filteredReservations.add(res);
            }
        }
        reservationsTable.setItems(filteredReservations);
    }

    @FXML
    private void handleModifyBooking() {
        GroupedReservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showAlert("Please select a reservation to modify.", Alert.AlertType.WARNING);
            return;
        }

        // Cannot modify cancelled reservations
        if ("CANCELLED".equals(selectedReservation.getStatus())) {
            showAlert("Cannot modify a cancelled reservation.", Alert.AlertType.WARNING);
            return;
        }

        // Cannot modify checked-out reservations
        if ("CHECKED_OUT".equals(selectedReservation.getStatus())) {
            showAlert("Cannot modify a checked-out reservation.", Alert.AlertType.WARNING);
            return;
        }

        // Show booking details for modification
        showSelectedReservationDetails(selectedReservation);

        // Expand the update booking panel
        updateBookingPane.setExpanded(true);
    }

    @FXML
    private void handleApplyChanges() {
        GroupedReservation selectedGroupedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedGroupedReservation == null) {
            showAlert("Please select a reservation first.", Alert.AlertType.WARNING);
            return;
        }

        // Validate input
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        String status = statusComboBox.getValue();
        int numGuests = numGuestsSpinner.getValue();

        if (checkIn == null || checkOut == null || status == null) {
            showAlert("Please fill in all fields.", Alert.AlertType.WARNING);
            return;
        }

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            showAlert("Check-out date must be after check-in date.", Alert.AlertType.WARNING);
            return;
        }

        // Create content for confirmation dialog
        StringBuilder confirmContent = new StringBuilder();
        confirmContent.append("You are updating a booking with ");
        confirmContent.append(selectedGroupedReservation.getIndividualReservations().size());
        confirmContent.append(" room(s):\n\n");

        // Show changes in dates, guests, and status
        confirmContent.append("• Check-in Date: ").append(checkIn)
                .append(" (previously: ").append(selectedGroupedReservation.getCheckInDate()).append(")\n");

        confirmContent.append("• Check-out Date: ").append(checkOut)
                .append(" (previously: ").append(selectedGroupedReservation.getCheckOutDate()).append(")\n");

        confirmContent.append("• Status: ").append(status)
                .append(" (previously: ").append(selectedGroupedReservation.getStatus()).append(")\n");

        // Calculate total number of guests in current reservation
        int currentTotalGuests = 0;
        for (Reservation res : selectedGroupedReservation.getIndividualReservations()) {
            currentTotalGuests += res.getNumGuests();
        }

        confirmContent.append("• Number of Guests: ").append(numGuests)
                .append(" (previously: ").append(currentTotalGuests).append(")\n\n");

        // List all rooms in the booking with their potentially updated room types
        confirmContent.append("Room Details:\n");
        for (Reservation res : selectedGroupedReservation.getIndividualReservations()) {
            int roomId = res.getRoomId();
            String roomType = roomTypeComboBoxes.containsKey(roomId) ?
                    roomTypeComboBoxes.get(roomId).getValue() : res.getRoomType();

            confirmContent.append("• Room ID ").append(roomId).append(": ")
                    .append(roomType)
                    .append(" (previously: ").append(res.getRoomType()).append(")\n");
        }

        // Confirm update
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Update");
        confirmAlert.setHeaderText("Update Reservation");
        confirmAlert.setContentText(confirmContent.toString());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateGroupedReservation(selectedGroupedReservation, checkIn, checkOut, status, numGuests);
        }
    }

    private void updateGroupedReservation(GroupedReservation groupedReservation, LocalDate checkIn, LocalDate checkOut, String status, int numGuests) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // Distribute guests among rooms (simplified approach, distribute evenly)
            int roomCount = groupedReservation.getIndividualReservations().size();
            int guestsPerRoom = numGuests / roomCount;
            int remainingGuests = numGuests % roomCount;

            // Update each individual reservation in the group
            int index = 0;
            for (Reservation reservation : groupedReservation.getIndividualReservations()) {
                int reservationId = reservation.getReservationId();
                int roomId = reservation.getRoomId();

                // Calculate guests for this room
                int roomGuests = guestsPerRoom + (index < remainingGuests ? 1 : 0);
                index++;

                // Get the potentially updated room type from the combobox
                String updatedRoomType = roomTypeComboBoxes.containsKey(roomId) ?
                        roomTypeComboBoxes.get(roomId).getValue() : reservation.getRoomType();

                // Update the reservation
                try (PreparedStatement updateReservation = conn.prepareStatement(
                        "UPDATE reservations SET checkin_date = ?, checkout_date = ?, " +
                                "status = ?, num_guests = ? WHERE reservation_id = ?")) {
                    updateReservation.setDate(1, java.sql.Date.valueOf(checkIn));
                    updateReservation.setDate(2, java.sql.Date.valueOf(checkOut));
                    updateReservation.setString(3, status);
                    updateReservation.setInt(4, roomGuests);
                    updateReservation.setInt(5, reservationId);
                    updateReservation.executeUpdate();
                }

                // If room type has changed, we need to update the room assignment
                if (!updatedRoomType.equals(reservation.getRoomType())) {
                    // First, free up the current room
                    try (PreparedStatement freeRoom = conn.prepareStatement(
                            "UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = ?")) {
                        freeRoom.setInt(1, roomId);
                        freeRoom.executeUpdate();
                    }

                    // Find a new available room of the desired type
                    int newRoomId = -1;
                    try (PreparedStatement findRoom = conn.prepareStatement(
                            "SELECT room_id FROM rooms WHERE room_type = ? AND status = 'AVAILABLE' LIMIT 1")) {
                        findRoom.setString(1, updatedRoomType);
                        ResultSet rs = findRoom.executeQuery();
                        if (rs.next()) {
                            newRoomId = rs.getInt("room_id");
                        } else {
                            throw new SQLException("No available rooms of type " + updatedRoomType);
                        }
                    }

                    // Update the reservation with the new room
                    try (PreparedStatement updateRoom = conn.prepareStatement(
                            "UPDATE reservations SET room_id = ? WHERE reservation_id = ?")) {
                        updateRoom.setInt(1, newRoomId);
                        updateRoom.setInt(2, reservationId);
                        updateRoom.executeUpdate();
                    }

                    // Mark the new room as occupied
                    try (PreparedStatement occupyRoom = conn.prepareStatement(
                            "UPDATE rooms SET status = 'BOOKED' WHERE room_id = ?")) {
                        occupyRoom.setInt(1, newRoomId);
                        occupyRoom.executeUpdate();
                    }

                    // Use the new room ID for billing calculations
                    roomId = newRoomId;
                }

                // Update billing
                // Get room price
                double roomPrice = 0.0;
                try (PreparedStatement getPrice = conn.prepareStatement(
                        "SELECT price FROM rooms WHERE room_id = ?")) {
                    getPrice.setInt(1, roomId);
                    ResultSet rs = getPrice.executeQuery();
                    if (rs.next()) {
                        roomPrice = rs.getDouble("price");
                    }
                }

                // Calculate new billing
                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                double totalAmount = nights * roomPrice;
                double tax = totalAmount * 0.1; // 10% tax

                // Update billing
                try (PreparedStatement updateBilling = conn.prepareStatement(
                        "UPDATE billing SET amount = ?, tax = ?, total_amount = ? " +
                                "WHERE reservation_id = ?")) {
                    updateBilling.setDouble(1, totalAmount);
                    updateBilling.setDouble(2, tax);
                    updateBilling.setDouble(3, totalAmount + tax);
                    updateBilling.setInt(4, reservationId);
                    updateBilling.executeUpdate();
                }
            }

            conn.commit();

            // Refresh data and show success message
            loadReservations();
            showMessage("Booking updated successfully!");

        } catch (SQLException e) {
            showAlert("Error updating reservations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void handleCancelBooking() {
        GroupedReservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showAlert("Please select a reservation to cancel.", Alert.AlertType.WARNING);
            return;
        }

        // Check if already cancelled
        if ("CANCELLED".equals(selectedReservation.getStatus())) {
            showAlert("This reservation is already cancelled.", Alert.AlertType.INFORMATION);
            return;
        }

        // Check if already checked out
        if ("CHECKED_OUT".equals(selectedReservation.getStatus())) {
            showAlert("Cannot cancel a checked-out reservation.", Alert.AlertType.WARNING);
            return;
        }

        // Create content for confirmation dialog
        StringBuilder confirmContent = new StringBuilder();
        confirmContent.append("You are canceling a booking with ");
        confirmContent.append(selectedReservation.getIndividualReservations().size());
        confirmContent.append(" room(s):\n\n");

        // List all rooms in the booking
        for (int i = 0; i < selectedReservation.getIndividualReservations().size(); i++) {
            Reservation res = selectedReservation.getIndividualReservations().get(i);
            confirmContent.append("• Room ").append(i + 1).append(": ")
                    .append(res.getRoomType())
                    .append(" (Room ID: ").append(res.getRoomId()).append(")\n");
        }

        confirmContent.append("\nAll rooms in this booking will be canceled.");

        // Confirm cancellation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Reservation");
        confirmAlert.setContentText(confirmContent.toString());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);

                // Cancel each individual reservation in the group
                for (Reservation reservation : selectedReservation.getIndividualReservations()) {
                    // Update reservation status
                    try (PreparedStatement updateReservation = conn.prepareStatement(
                            "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?")) {
                        updateReservation.setInt(1, reservation.getReservationId());
                        updateReservation.executeUpdate();
                    }

                    // Free up the room
                    try (PreparedStatement updateRoom = conn.prepareStatement(
                            "UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = ?")) {
                        updateRoom.setInt(1, reservation.getRoomId());
                        updateRoom.executeUpdate();
                    }

                    // Update model
                    reservation.cancelReservation();
                }

                conn.commit();

                // Refresh table data
                loadReservations();
                showMessage("Booking cancelled successfully. " + selectedReservation.getIndividualReservations().size() + " room(s) have been freed up.");
            } catch (SQLException e) {
                showAlert("Error canceling reservation: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handlePrintBooking() {
        GroupedReservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showAlert("Please select a reservation to print.", Alert.AlertType.WARNING);
            return;
        }

        // Generate booking information for printing
        String bookingInfo = generateBookingInfo(selectedReservation);

        // Display print preview
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Information");
        alert.setHeaderText("Booking Details");
        alert.setContentText(null);

        TextArea textArea = new TextArea(bookingInfo);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(400);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private String generateBookingInfo(GroupedReservation groupedReservation) {
        StringBuilder sb = new StringBuilder();
        sb.append("HOTEL VRUNECA - BOOKING CONFIRMATION\n\n");
        sb.append("Booking Reference: ").append(groupedReservation.getGroupId()).append("\n");
        sb.append("Guest Name: ").append(groupedReservation.getGuestName()).append("\n\n");

        sb.append("Room Details:\n");
        sb.append("Room Types: ").append(groupedReservation.getRoomTypesSummary()).append("\n");

        int totalGuests = 0;
        for (Reservation res : groupedReservation.getIndividualReservations()) {
            totalGuests += res.getNumGuests();
        }
        sb.append("Number of Guests: ").append(totalGuests).append("\n\n");

        sb.append("Stay Information:\n");
        sb.append("Check-in Date: ").append(groupedReservation.getCheckInDate()).append("\n");
        sb.append("Check-out Date: ").append(groupedReservation.getCheckOutDate()).append("\n");
        sb.append("Status: ").append(groupedReservation.getStatus()).append("\n\n");

        sb.append("Payment Details:\n");
        sb.append("Total Amount: $").append(String.format("%.2f", groupedReservation.getTotalAmount())).append("\n\n");

        // Add individual room details
        sb.append("Individual Room Details:\n");
        for (int i = 0; i < groupedReservation.getIndividualReservations().size(); i++) {
            Reservation res = groupedReservation.getIndividualReservations().get(i);
            sb.append("  Room ").append(i + 1).append(": ")
                    .append(res.getRoomType())
                    .append(" (ID: ").append(res.getRoomId()).append(")")
                    .append(" - $").append(String.format("%.2f", res.getTotalAmount()))
                    .append("\n");
        }
        sb.append("\nThank you for choosing Hotel Vruneca!");

        return sb.toString();
    }

    @FXML
    private void handleBackButton() {
        try {
            // Return to admin panel
            Stage stage = (Stage) reservationsTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Panel");
            stage.show();
        } catch (IOException e) {
            showAlert("Error returning to Admin Panel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleMessageClose() {
        messageBox.setVisible(false);
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageBox.setVisible(true);
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // This method will be called from the AdminController
    public void openManageBookings() {
        // Additional initialization if needed when called from admin panel
        loadReservations();
    }
}