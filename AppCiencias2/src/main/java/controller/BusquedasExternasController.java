package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class BusquedasExternasController {

    @FXML private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("busquedasExternas.fxml"));
    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }
    
    @FXML
    private void openBusquedaLinealExterna() {
        layoutController.loadPanel("/busquedaLinealExterna.fxml");
    }
    
    @FXML
    private void openBusquedaBinariaExterna() {
        layoutController.loadPanel("/busquedaBinariaExterna.fxml");
    }

    @FXML
    private void openBusquedasDinamicas(MouseEvent event){
        System.out.println("abriendo dinamicas");
        layoutController.loadPanel("/busquedasDinamicas.fxml");
    }

    @FXML
    private void openFuncionHashExterna(MouseEvent event) {
        layoutController.loadPanel("/busquedaHashExterna.fxml");
        System.out.println("abriendo hashExter...");
    }

    @FXML
    private void openIndices(javafx.scene.input.MouseEvent event) {
        layoutController.loadPanel("/indicesExternos.fxml");
        System.out.println("abriendo indices...");
    }
}