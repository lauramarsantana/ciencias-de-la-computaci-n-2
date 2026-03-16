package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class BusquedasExternasController {

    @FXML private AnchorPane externasPane;
    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;

    @FXML
    public void initialize() {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);
    }

    // ===== Menú =====
    @FXML
    private void openMenu(javafx.scene.input.MouseEvent event){
        menuPane.setVisible(true);
        menuPane.setManaged(true);
    }

    @FXML
    private void closeMenu(javafx.scene.input.MouseEvent event){
        menuPane.setVisible(false);
        menuPane.setManaged(false);
    }

    @FXML
    private void openMenuBusquedas(javafx.scene.input.MouseEvent event){
        boolean isVisible = subMenuBusquedas.isVisible();
        subMenuBusquedas.setVisible(!isVisible);
        subMenuBusquedas.setManaged(!isVisible);
    }

    @FXML
    private void openMenuInternas(javafx.scene.input.MouseEvent event){
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);
    }

    // ===== Navegación menú =====
    @FXML
    private void mostrarBusquedaLineal(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaLineal.fxml");
    }

    @FXML
    private void openBinario(javafx.scene.input.MouseEvent event){
        loadPanel("busquedaBinaria.fxml");
    }

    @FXML
    private void openFuncionHash(javafx.scene.input.MouseEvent event){
        loadPanel("busquedaHash.fxml");
    }

    @FXML
    private void openGrafos(javafx.scene.input.MouseEvent event){
        loadPanel("grafos.fxml");
    }

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event){
        loadPanel("inicio.fxml");
    }

    // ===== Opciones principales de externas =====
    @FXML
    private void openBusquedasDinamicas(javafx.scene.input.MouseEvent event){
        loadPanel("busquedasDinamicas.fxml");
    }

    @FXML
    private void openFuncionHashExterna(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaHashExterna.fxml");
    }

    @FXML
    private void openIndices(javafx.scene.input.MouseEvent event) {
        loadPanel("indicesExternos.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();
            externasPane.getChildren().clear();
            externasPane.getChildren().add(panel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}