package controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class MenuRepresentacionMetricasController {

    private LayoutController layoutController;

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void abrirMatricesGrafos(MouseEvent event) {
        layoutController.loadPanel("/representacionGrafos.fxml");
    }

    @FXML
    private void abrirDistanciasMetricas(MouseEvent event) {
        layoutController.loadPanel("/distanciaVertices.fxml");
    }
}
