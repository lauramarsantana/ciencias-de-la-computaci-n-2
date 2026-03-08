package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class BusquedasDinamicasController {

    @FXML private AnchorPane dinamicasPane;
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

    // ===== Navegación menú lateral =====
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

    // ===== Opciones principales de búsquedas dinámicas =====
    @FXML
    private void openExpTotales(javafx.scene.input.MouseEvent event){
        loadPanel("busquedaExpTotales.fxml");
    }

    @FXML
    private void openExpParciales(javafx.scene.input.MouseEvent event){
        loadPanel("busquedaExpParciales.fxml");
    }

    // opcional: volver a la pantalla anterior de externas
    @FXML
    private void openExternas(javafx.scene.input.MouseEvent event){
        loadPanel("busquedasExternas.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            dinamicasPane.getChildren().clear();
            dinamicasPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
