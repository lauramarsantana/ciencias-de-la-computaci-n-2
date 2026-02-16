package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class InicioController {

    @FXML
    private AnchorPane contentPane;
    @FXML
    private AnchorPane busquedasPane;
    @FXML
    private AnchorPane grafosPane;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private VBox subMenuBusquedas;
    @FXML
    private VBox subMenuInternas;

    @FXML
    private void openBusquedas(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo busquedas.fxml...");
        loadPanel("busquedas.fxml");
    }
    @FXML
    private void openGrafos(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo grafos.fxml...");
        loadPanel("grafos.fxml");
    }

// configurando cada boton del menu desplegable
    @FXML
    public void initialize() {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);
    }
    @FXML
    private void openMenu(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo menu...");
        menuPane.setVisible(true); // para que el panel del menu se vea
        menuPane.setManaged(true);// posiciona de primeras al panel
    }
    @FXML
    private void closeMenu(javafx.scene.input.MouseEvent event){
        System.out.println("cerrando menu...");
        menuPane.setVisible(false);// para que el panel del menu se oculte
        menuPane.setManaged(false);// lo quita de la primera capa, para liberar el espacio
    }
    @FXML
    private void openMenuBusquedas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo submenu de busquedas...");
        boolean isVisible = subMenuBusquedas.isVisible();
        subMenuBusquedas.setVisible(!isVisible);
        subMenuBusquedas.setManaged(!isVisible);
    }
    @FXML
    private void openMenuInternas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo submenu de busquedas internas...");
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);
    }
    @FXML
    private void mostrarBusquedaLineal(javafx.scene.input.MouseEvent event) {
        System.out.println("Abriendo busquedaLineal.fxml");
        loadPanel("busquedaLineal.fxml");
    }
    @FXML
    private void openBinario(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaBinaria.fxml");
        loadPanel("busquedaBinaria.fxml");
    }
    @FXML
    private void openFuncionHash(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaHash.fxml");
        loadPanel("busquedaHash.fxml");
    }

    @FXML
    private void mostrarBusquedaBinario() {
        loadPanel("busquedaBinaria.fxml");
    }


    @FXML
    private void mostrarBusquedaHash() {
        loadPanel("busquedaHash.fxml");
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
