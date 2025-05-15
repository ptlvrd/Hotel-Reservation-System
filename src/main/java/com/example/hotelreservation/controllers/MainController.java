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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML
    private void openKiosk() {
        loadScreen("/views/kiosk.fxml", "Guest Kiosk");
    }

    @FXML
    private void openAdminLogin() {
        loadScreen("/views/login.fxml", "Admin Login");
    }

    @FXML
    private void openFeedback() {
        // Create a dialog to ask for the bill number
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Feedback");
        dialog.setHeaderText("Please enter your Bill Number");
        dialog.setContentText("Bill Number:");

        // Get the response (bill number)
        Optional<String> result = dialog.showAndWait();

        // Process only if the user entered something and clicked OK
        result.ifPresent(billNumber -> {
            if (!billNumber.trim().isEmpty()) {
                // If valid bill number, open the feedback form
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/feedback.fxml"));
                    Parent root = loader.load();

                    // Pass the bill number to the feedback controller
                    FeedbackController controller = loader.getController();
                    controller.setBillNumber(billNumber);

                    Stage stage = new Stage();
                    stage.setTitle("Leave Feedback");
                    stage.setScene(new Scene(root));
                    stage.setResizable(false);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Could not open feedback form.");
                }
            } else {
                showAlert("Invalid Input", "Please enter a valid Bill Number.");
            }
        });
    }

    private void loadScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            // Close the current selection screen
            Stage currentStage = (Stage) loader.getController().getClass().getDeclaredMethod("getStage").invoke(loader.getController());
            currentStage.close();

        } catch (IOException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}