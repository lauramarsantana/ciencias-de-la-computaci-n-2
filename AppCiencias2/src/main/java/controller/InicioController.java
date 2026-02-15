package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class InicioController {

    @FXML
    private StackPane contentPane;

    @FXML
    private void mostrarBusquedaLineal() {
        System.out.println("Abriendo busquedaLineal.fxml");
        loadPanel("busquedaLineal.fxml");
    }
    
    @FXML
    private void mostrarBusquedaBinario() {
        loadPanel("busquedaBinaria.fxml");
    }


    @FXML
    private void mostrarBusquedaHash() {
        System.out.println("Clic en Búsqueda Hash");
    }

    @FXML
    private void mostrarEstructuraEstatica() {
        System.out.println("Clic en Estructura Estática");
    }

    @FXML
    private void mostrarEstructuraDinamica() {
        System.out.println("Clic en Estructura Dinámica");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
