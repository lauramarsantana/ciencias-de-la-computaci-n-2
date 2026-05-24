package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class GrafosController {
    @FXML private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("busquedas.fxml"));
    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }


    @FXML
    private void openOperaciones(MouseEvent event){
        System.out.println("abriendo operaciones con grafos");
        layoutController.loadPanel("/operaciones.fxml");

    }
    
    @FXML
    private void openArboles(MouseEvent event) {
        layoutController.loadPanel("/arbolesMenu.fxml");
    }
    
    @FXML
    private void openAlgoritmosGrafos() {
        layoutController.loadPanel("/algoritmosGrafos.fxml");
    }
    
    @FXML
    private void openRepresentacionMetricas(MouseEvent event) {
        layoutController.loadPanel("/menuRepresentacionMetricas.fxml");
    }

    @FXML
    private void openTeoriaColoracion(MouseEvent event){
        layoutController.loadPanel("/teoriaColor.fxml");
    }
}
