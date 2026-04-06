package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class BusquedaController {
    @FXML private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("busquedas.fxml"));
    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void openInternas(MouseEvent event){
        System.out.println("abriendo busquedasInternas.fxml...");
        layoutController.loadPanel("/busquedasInternas.fxml");
    }

    @FXML
    private void openExternas(MouseEvent event){
        System.out.println("abriendo busquedasExternas.fxml...");
        layoutController.loadPanel("/busquedasExternas.fxml");
    }
}
