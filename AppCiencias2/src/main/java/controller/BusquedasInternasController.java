package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class BusquedasInternasController {

    @FXML private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("busquedasInternas.fxml"));
    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void openLineal(MouseEvent event) {
        System.out.println("abriendo busquedaILineal.fxml...");
        layoutController.loadPanel("/busquedaLineal.fxml");
    }

    @FXML
    private void openBinario(MouseEvent event){
        System.out.println("abriendo binario...");
        layoutController.loadPanel("/busquedaBinaria.fxml");
    }

    @FXML
    private void openFuncionHash(MouseEvent event){
        System.out.println("abriendo funcionHash interno");
        layoutController.loadPanel("/busquedaHash.fxml");
    }

    @FXML
    private void openBResiduos(MouseEvent event){
        System.out.println("abriendo busquedaPorResiduos.fxml...");
        layoutController.loadPanel("/busquedaPorResiduos.fxml");
    }
}
