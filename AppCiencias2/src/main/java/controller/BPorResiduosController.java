package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class BPorResiduosController {

    @FXML private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("busquedaPorResiduos.fxml"));
    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void openArbolDigital(MouseEvent event){
        System.out.println("abriendo arbol digital ...");
        layoutController.loadPanel("/arbolDigital.fxml");
    }
    @FXML
    private void openTries(MouseEvent event){
        System.out.println("abriendo tries...");
        layoutController.loadPanel("/tresResiduos.fxml");
    }
    @FXML
    private void openResiduosMultiples(MouseEvent event){
        System.out.println("abriendo multiples...");
        layoutController.loadPanel("/residuosMultiples.fxml");
    }
    @FXML
    private void openHuffman(MouseEvent event){
        System.out.println("abriendo huffman...");
        layoutController.loadPanel("/huffman.fxml");
    }
}
