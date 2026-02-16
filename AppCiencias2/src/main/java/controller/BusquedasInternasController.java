package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class BusquedasInternasController {

    @FXML
    private AnchorPane internasPane;

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo inicio.fxml...");
        loadPanel("inicio.fxml");
    }
    @FXML
    private void openLineal(javafx.scene.input.MouseEvent event) {
        System.out.println("abriendo busquedaILineal.fxml...");
        loadPanel("busquedaLineal.fxml");
    }

    @FXML
    private void openBinario(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaIBinario.fxml...");
    }

    @FXML
    private void openFuncionHash(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaIFuncionHash.fxml...");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            internasPane.getChildren().clear();
            internasPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
