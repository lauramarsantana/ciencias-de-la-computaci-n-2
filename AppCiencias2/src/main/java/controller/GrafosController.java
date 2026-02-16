package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class GrafosController {

    @FXML
    private AnchorPane grafosPane;
    
    @FXML
    private void openInicio(MouseEvent event){
        System.out.println("abriendo inicio.fxml");
        loadPanel("inicio.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            grafosPane.getChildren().clear();
            grafosPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
