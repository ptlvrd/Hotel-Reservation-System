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
import com.example.hotelreservation.models.Billing;
import com.example.hotelreservation.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class BillingController {

    @FXML private TextField searchBillingField;
    @FXML private TableView<Billing> billingTable;
    @FXML private TableColumn<Billing, String> guestColumn;
    @FXML private TableColumn<Billing, String> roomColumn;
    @FXML private TableColumn<Billing, Number> totalAmountColumn;
    @FXML private TextField discountField;

    private final ObservableList<Billing> billingList = FXCollections.observableArrayList();
    private final Map<Integer, List<Integer>> guestBillingsMap = new HashMap<>();

    @FXML
    public void initialize() {
        guestColumn.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        roomColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        totalAmountColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty());

        loadPendingBills();
    }

    private void loadPendingBills() {
        billingList.clear();
        guestBillingsMap.clear();

        // Map to group billings by guest
        Map<Integer, GroupedBilling> groupedBillings = new HashMap<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.bill_id, r.reservation_id, g.guest_id, g.name AS guest_name, rm.room_id, rm.room_type, " +
                             "b.amount, b.tax, b.discount, b.total_amount " +
                             "FROM billing b " +
                             "JOIN reservations r ON b.reservation_id = r.reservation_id " +
                             "JOIN guests g ON r.guest_id = g.guest_id " +
                             "JOIN rooms rm ON r.room_id = rm.room_id " +
                             "WHERE r.status = 'CONFIRMED'")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int guestId = rs.getInt("guest_id");
                int billId = rs.getInt("bill_id");
                int reservationId = rs.getInt("reservation_id");

                // Add bill ID to the guest's list
                if (!guestBillingsMap.containsKey(guestId)) {
                    guestBillingsMap.put(guestId, new ArrayList<>());
                }
                guestBillingsMap.get(guestId).add(billId);

                // Group billings by guest
                if (!groupedBillings.containsKey(guestId)) {
                    groupedBillings.put(guestId, new GroupedBilling(
                            guestId,
                            rs.getString("guest_name"),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            0.0,
                            0.0,
                            0.0
                    ));
                }

                GroupedBilling group = groupedBillings.get(guestId);
                group.billIds.add(billId);
                group.reservationIds.add(reservationId);
                group.roomIds.add(rs.getInt("room_id"));
                group.amount += rs.getDouble("amount");
                group.tax += rs.getDouble("tax");
                group.discount += rs.getDouble("discount");

                // Update room type description
                group.roomTypeDescription = updateRoomTypeDescription(group);
            }

            for (GroupedBilling group : groupedBillings.values()) {
                // First bill ID will be the primary one for display purposes
                Integer primaryBillId = group.billIds.get(0);

                billingList.add(new Billing(
                        primaryBillId,
                        group.reservationIds.get(0), // Using first reservation ID for reference
                        group.guestName,
                        group.roomTypeDescription,
                        group.amount,
                        group.tax,
                        group.discount
                ));
            }

        } catch (SQLException e) {
            showAlert("Error loading pending bills: " + e.getMessage());
        }
        billingTable.setItems(billingList);
    }

    private String updateRoomTypeDescription(GroupedBilling group) {
        if (group.roomIds.size() == 1) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT room_type FROM rooms WHERE room_id = ?")) {
                stmt.setInt(1, group.roomIds.get(0));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("room_type");
                }
            } catch (SQLException e) {
                // Fall back to default
            }
            return "SINGLE"; // Default if query fails
        } else {
            return "MULTIPLE (" + group.roomIds.size() + " rooms)";
        }
    }

    @FXML
    private void handleSearchBilling() {
        String keyword = searchBillingField.getText().trim();
        if (keyword.isEmpty()) {
            billingTable.setItems(billingList);
            return;
        }

        ObservableList<Billing> filteredList = FXCollections.observableArrayList();
        for (Billing bill : billingList) {
            if (bill.getGuestName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(bill);
            }
        }

        billingTable.setItems(filteredList);
    }

    @FXML
    private void applyDiscount() {
        Billing selectedBill = billingTable.getSelectionModel().getSelectedItem();
        if (selectedBill == null) {
            showAlert("Please select a bill to apply a discount.");
            return;
        }

        double discount;
        try {
            discount = Double.parseDouble(discountField.getText());
            if (discount < 0) {
                showAlert("Discount cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid discount amount.");
            return;
        }

        // Get guest ID from the selected bill
        int guestId = getGuestIdFromBill(selectedBill.getBillId());
        if (guestId == -1) {
            showAlert("Error retrieving guest information.");
            return;
        }

        // Apply discount to all bills for this guest
        List<Integer> billIds = guestBillingsMap.get(guestId);
        if (billIds != null) {
            try (Connection conn = Database.getConnection()) {
                // Split discount proportionally across all bills
                double discountPerBill = discount / billIds.size();

                for (Integer billId : billIds) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE billing SET discount = ? WHERE bill_id = ?");
                    stmt.setDouble(1, discountPerBill);
                    stmt.setInt(2, billId);
                    stmt.executeUpdate();
                }

                // Update the selected bill's display
                selectedBill.setDiscount(discount);
                billingTable.refresh();
                showAlert("Discount applied successfully to all rooms for this guest.");

                // Reload to get accurate totals
                loadPendingBills();
            } catch (SQLException e) {
                showAlert("Error applying discount: " + e.getMessage());
            }
        }
    }

    private int getGuestIdFromBill(int billId) {
        for (Map.Entry<Integer, List<Integer>> entry : guestBillingsMap.entrySet()) {
            if (entry.getValue().contains(billId)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    @FXML
    private void handlePrintInvoice() {
        Billing selectedBill = billingTable.getSelectionModel().getSelectedItem();
        if (selectedBill == null) {
            showAlert("Please select a bill to print the invoice.");
            return;
        }

        // Get guest ID from the selected bill
        int guestId = getGuestIdFromBill(selectedBill.getBillId());
        if (guestId == -1) {
            showAlert("Error retrieving guest information.");
            return;
        }

        // Retrieve all rooms and details for this guest
        StringBuilder roomDetails = new StringBuilder();
        try (Connection conn = Database.getConnection()) {
            List<Integer> billIds = guestBillingsMap.get(guestId);

            if (billIds != null) {
                for (Integer billId : billIds) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT b.bill_id, r.reservation_id, rm.room_type, b.amount, b.tax, b.discount " +
                                    "FROM billing b " +
                                    "JOIN reservations r ON b.reservation_id = r.reservation_id " +
                                    "JOIN rooms rm ON r.room_id = rm.room_id " +
                                    "WHERE b.bill_id = ?");
                    stmt.setInt(1, billId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        roomDetails.append("Room: ").append(rs.getString("room_type"))
                                .append(" (Reservation #").append(rs.getInt("reservation_id"))
                                .append(")\n")
                                .append("  Amount: $").append(rs.getDouble("amount"))
                                .append(", Tax: $").append(rs.getDouble("tax"))
                                .append(", Discount: $").append(rs.getDouble("discount"))
                                .append("\n");
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error retrieving room details: " + e.getMessage());
            return;
        }

        String invoice = "===== Hotel ABC Invoice =====\n" +
                "Guest: " + selectedBill.getGuestName() + "\n\n" +
                "Room Details:\n" + roomDetails.toString() + "\n" +
                "Total Amount: $" + selectedBill.getAmount() + "\n" +
                "Total Tax: $" + selectedBill.getTax() + "\n" +
                "Total Discount: $" + selectedBill.getDiscount() + "\n" +
                "Final Amount: $" + selectedBill.getTotalAmount() + "\n" +
                "=============================";

        showAlert(invoice);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class to group billings by guest
    private static class GroupedBilling {
        int guestId;
        String guestName;
        List<Integer> billIds;
        List<Integer> reservationIds;
        List<Integer> roomIds;
        double amount;
        double tax;
        double discount;
        String roomTypeDescription;

        public GroupedBilling(int guestId, String guestName, List<Integer> billIds,
                              List<Integer> reservationIds, List<Integer> roomIds,
                              double amount, double tax, double discount) {
            this.guestId = guestId;
            this.guestName = guestName;
            this.billIds = billIds;
            this.reservationIds = reservationIds;
            this.roomIds = roomIds;
            this.amount = amount;
            this.tax = tax;
            this.discount = discount;
            this.roomTypeDescription = "SINGLE"; // Default
        }
    }
}