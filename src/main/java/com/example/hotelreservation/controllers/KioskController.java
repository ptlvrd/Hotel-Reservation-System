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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;

public class KioskController {

    @FXML private ImageView welcomeImage;
    @FXML private Button startBookingButton;

    @FXML
    public void initialize() {
        // Load the welcome image
        Image image = new Image(getClass().getResource("/images/welcome_screen.png").toExternalForm());
        welcomeImage.setImage(image);
    }

    @FXML
    private void handleStartBooking() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/guestDetails.fxml"));
            Parent guestRoot = loader.load();

            // Get current stage and switch scene
            Stage stage = (Stage) startBookingButton.getScene().getWindow();
            stage.setScene(new Scene(guestRoot));
            stage.setTitle("Guest Details - Hotel Vruneca");

        } catch (IOException e) {
            showAlert("Error loading Guest Details page.");
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
