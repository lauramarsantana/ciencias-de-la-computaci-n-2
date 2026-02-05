package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class InicioController {

    @FXML
    private BorderPane mainContent; // el panel central

    // Métodos de cambio de vista
    @FXML
    private void mostrarBusquedaLineal() {
        loadPanel("busquedaLineal.fxml");
    }

    @FXML
    private void mostrarBusquedaBinario() {
        System.out.println("Clic en Búsqueda Binaria");
    }

    @FXML
    private void mostrarBusquedaHash(){
        System.out.println("busqueda por funcion hash");
    }

    @FXML
    private void mostrarEstructuraEstatica() {
        System.out.println("Clic en estrutura estatica");
    }

    @FXML
    private void mostrarEstructuraDinamica() {
        System.out.println("Clic en estrutura dinamica");
    }

    @FXML
    private StackPane contentPane; // contenedor dinámico

    // Método genérico para cargar FXML
    private void loadUI(String ui) {
        try {
            Pane pane = FXMLLoader.load(getClass().getResource(ui));
            mainContent.setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            // Reemplaza el contenido del contenedor
            contentPane.getChildren().clear();
            contentPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


