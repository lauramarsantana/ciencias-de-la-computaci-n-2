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
        layoutController.loadPanel("/busquedaIndicePrimario.fxml");
    }

    @FXML
    private void openIndiceSecundario(MouseEvent event) {
        layoutController.loadPanel("/busquedaIndiceSecundario.fxml");
    }

    @FXML
    private void openIndiceAgrupamiento(MouseEvent event) {
        System.out.println("abriendo indice...");
    }

    @FXML
    private void openIndiceMultinivel(MouseEvent event) {
        System.out.println("abriendo multinivel");
    }
}