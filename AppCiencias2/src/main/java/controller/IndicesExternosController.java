package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class IndicesExternosController {
    @FXML private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("busquedasExternas.fxml"));
    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void openIndicePrimario(MouseEvent event) {
        layoutController.loadPanel("/IndicePrimario.fxml");
    }

    @FXML
    private void openIndiceSecundario(MouseEvent event) {
        layoutController.loadPanel("/IndiceSecundario.fxml");
    }

    @FXML
    private void openIndiceMultinivel(MouseEvent event) {
        layoutController.loadPanel("/IndiceMultinivel.fxml");
    }
}