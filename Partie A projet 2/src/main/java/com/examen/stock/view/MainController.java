package com.examen.stock.view;

import javafx.application.Platform;
import javafx.fxml.FXML;

public class MainController {
    @FXML
    public void handleExit() {
        Platform.exit();
        System.exit(0);
    }
}
