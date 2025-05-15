module com.example.hotelreservation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    opens com.example.hotelreservation to javafx.fxml;
    exports com.example.hotelreservation;

    opens com.example.hotelreservation.controllers to javafx.fxml;
    exports com.example.hotelreservation.controllers;

    opens com.example.hotelreservation.models to javafx.base;
    exports com.example.hotelreservation.models;
}
