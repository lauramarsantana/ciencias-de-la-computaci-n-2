package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class BusquedasDinamicasController {
    @FXML
    private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("busquedaDinamicas.fxml"));
    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void openExpTotales(MouseEvent event){
        System.out.println("abriendo expansion total");
        layoutController.loadPanel("/busquedaExpTotales.fxml");
    }

    @FXML
    private void openExpParciales(MouseEvent event){
        System.out.println("abriendo expasion parcial...");
        layoutController.loadPanel("/busquedaExpParciales.fxml");
    }
}
