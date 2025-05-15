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

import com.example.hotelreservation.database.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class FeedbackController {

    @FXML private Label billNumberLabel;
    @FXML private RadioButton rating1, rating2, rating3, rating4, rating5;
    @FXML private TextArea commentsArea;
    @FXML private ToggleGroup ratingGroup;

    private String billNumber;
    private int guestId;
    private int reservationId;

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
        billNumberLabel.setText(billNumber);
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    @FXML
    public void initialize() {
        // Make sure the radio buttons are in the toggle group
        if (ratingGroup == null) {
            ratingGroup = new ToggleGroup();
        }
        rating1.setToggleGroup(ratingGroup);
        rating2.setToggleGroup(ratingGroup);
        rating3.setToggleGroup(ratingGroup);
        rating4.setToggleGroup(ratingGroup);
        rating5.setToggleGroup(ratingGroup);
    }

    @FXML
    private void submitFeedback() {
        // Get the selected rating
        RadioButton selectedRating = (RadioButton) ratingGroup.getSelectedToggle();
        int ratingValue = 0;

        if (selectedRating != null) {
            ratingValue = Integer.parseInt(selectedRating.getText());
        }

        // Get comments
        String comments = commentsArea.getText();

        if (ratingValue == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Rating Required");
            alert.setHeaderText(null);
            alert.setContentText("Please select a rating (1-5) before submitting.");
            alert.showAndWait();
            return;
        }

        // Save the feedback to the database
        boolean saveSuccess = saveFeedbackToDatabase(guestId, reservationId, comments, ratingValue);

        if (saveSuccess) {
            // Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Feedback Submitted");
            alert.setHeaderText(null);
            alert.setContentText("Thank you for your feedback!\nBill #: " + billNumber +
                    "\nRating: " + ratingValue +
                    "\nComments: " + comments +
                    "\nYour feedback has been recorded.");
            alert.showAndWait();

            // Close the feedback window
            ((Stage) billNumberLabel.getScene().getWindow()).close();
        }
    }

    private boolean saveFeedbackToDatabase(int guestId, int reservationId, String comments, int rating) {
        try (Connection conn = Database.getConnection()) {
            // Check if feedback for this reservation already exists
            String feedbackExistsQuery = "SELECT feedback_id FROM feedbacks WHERE reservation_id = ?";
            PreparedStatement feedbackExistsStmt = conn.prepareStatement(feedbackExistsQuery);
            feedbackExistsStmt.setInt(1, reservationId);

            if (feedbackExistsStmt.executeQuery().next()) {
                String updateQuery = "UPDATE feedbacks SET comments = ?, rating = ? WHERE reservation_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, comments);
                updateStmt.setInt(2, rating);
                updateStmt.setInt(3, reservationId);
                updateStmt.executeUpdate();
            } else {
                try {
                    // Try direct insert first
                    String feedbackQuery = "INSERT IGNORE INTO feedbacks (guest_id, reservation_id, comments, rating) VALUES (?, ?, ?, ?)";
                    PreparedStatement feedbackStmt = conn.prepareStatement(feedbackQuery);
                    feedbackStmt.setInt(1, guestId);
                    feedbackStmt.setInt(2, reservationId);
                    feedbackStmt.setString(3, comments);
                    feedbackStmt.setInt(4, rating);
                    int result = feedbackStmt.executeUpdate();

                    if (result == 0) {
                        // If direct insert fails, try setting foreign key checks off temporarily
                        conn.setAutoCommit(false);
                        Statement stmt = conn.createStatement();
                        stmt.execute("SET FOREIGN_KEY_CHECKS=0");

                        feedbackQuery = "INSERT INTO feedbacks (guest_id, reservation_id, comments, rating) VALUES (?, ?, ?, ?)";
                        feedbackStmt = conn.prepareStatement(feedbackQuery);
                        feedbackStmt.setInt(1, guestId);
                        feedbackStmt.setInt(2, reservationId);
                        feedbackStmt.setString(3, comments);
                        feedbackStmt.setInt(4, rating);
                        feedbackStmt.executeUpdate();

                        stmt.execute("SET FOREIGN_KEY_CHECKS=1");
                        conn.commit();
                        conn.setAutoCommit(true);
                    }
                } catch (SQLException ex) {
                    String feedbackQuery = "INSERT INTO feedbacks (guest_id, reservation_id, comments, rating) VALUES (0, 0, ?, ?)";
                    PreparedStatement feedbackStmt = conn.prepareStatement(feedbackQuery);
                    feedbackStmt.setString(1, comments);
                    feedbackStmt.setInt(2, rating);
                    feedbackStmt.executeUpdate();
                }
            }

            try {
                String guestQuery = "UPDATE guests SET feedback = ? WHERE guest_id = ?";
                PreparedStatement guestStmt = conn.prepareStatement(guestQuery);
                String feedbackSummary = "Rating: " + rating + "/5, Comments: " + comments;
                guestStmt.setString(1, feedbackSummary);
                guestStmt.setInt(2, guestId);
                guestStmt.executeUpdate();
            } catch (SQLException ex) {
            }

            return true;
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("Error saving feedback: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
            return false;
        }
    }
    @FXML
    private void cancel() {
        ((Stage) billNumberLabel.getScene().getWindow()).close();
    }

    public Stage getStage() {
        return (Stage) billNumberLabel.getScene().getWindow();
    }
}