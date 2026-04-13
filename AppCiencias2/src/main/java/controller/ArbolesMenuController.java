package controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class ArbolesMenuController {

    private LayoutController layoutController;

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void abrirArbolSimple(MouseEvent event) {
        layoutController.loadPanel("/arbolSimple.fxml");
    }
    @FXML
    private void abrirOpcion2(MouseEvent event) {
        System.out.println("abriendo árbol generador");
        layoutController.loadPanel("/arbolGenerador.fxml");
    }

    @FXML
    private void abrirOpcion3(MouseEvent event) {
        System.out.println("opción 3 próximamente");
    }
}
