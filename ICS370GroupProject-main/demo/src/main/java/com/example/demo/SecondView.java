package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class SecondView implements Initializable {

    @FXML
    private Label displayLabel;

    @FXML
    private Button returnButton;


    @FXML
    protected void initialize() {
    }

    public void returnButtonClicked() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SchedulerApplication.class.getResource("first-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        ((Stage)returnButton.getScene().getWindow()).setScene(scene);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
