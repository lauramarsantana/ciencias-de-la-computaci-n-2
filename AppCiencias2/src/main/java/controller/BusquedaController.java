package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class BusquedaController {

    @FXML
    private AnchorPane busquedasPane;

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo inicio.fxml...");
        loadPanel("inicio.fxml");
    }
    @FXML
    private void openInternas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedasInternas.fxml...");
        loadPanel("busquedasInternas.fxml");
    }

    @FXML
    private void openExternas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedasExternas.fxml...");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            busquedasPane.getChildren().clear();
            busquedasPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
