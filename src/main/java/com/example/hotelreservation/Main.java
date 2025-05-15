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
 **********************************************/package com.example.hotelreservation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        showSelectionScreen(primaryStage); // ✅ Show selection screen first
    }

    private void showSelectionScreen(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Set the title of the window
            stage.setTitle("Welcome - Hotel Reservation System");

            // Set the scene to the stage
            stage.setScene(scene);

            // Enable window resizing
            stage.setResizable(true);

            // Set minimum window size to ensure content is always visible
            stage.setMinWidth(800);  // Minimum width
            stage.setMinHeight(600); // Minimum height

            // Center the window on the screen
            stage.centerOnScreen();

            // Show the stage
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading main.fxml");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}