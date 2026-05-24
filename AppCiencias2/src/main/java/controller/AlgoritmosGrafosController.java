package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;

public class AlgoritmosGrafosController {

    @FXML
    private LayoutController layoutController;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("algoritmosGrafos.fxml"));

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void abrirFuncionOrdinal(MouseEvent event) {

        System.out.println("abriendo funcion ordinal");

        layoutController.loadPanel("/funcionOrdinal.fxml");
    }

    @FXML
    private void abrirCaminosMinimos(MouseEvent event) {

        System.out.println("abriendo caminos mínimos");

        layoutController.loadPanel("/caminosMinimos.fxml");
    }
}
