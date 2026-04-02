package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class InicioController {

    @FXML private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("inicio.fxml"));

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    public void openBusquedas(MouseEvent event){
        layoutController.loadPanel("/busquedas.fxml");
        System.out.println("abiriendo busquedas.fx...");
    }

    @FXML
    public void openGrafos(MouseEvent event){
        layoutController.loadPanel("/grafos.fxml");
    }
}
