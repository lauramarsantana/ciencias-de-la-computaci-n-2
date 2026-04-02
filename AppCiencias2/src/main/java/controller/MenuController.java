package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class MenuController {
    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;
    @FXML private ImageView arrowBusqueda;

    @FXML private AnchorPane layoutPane;

    private LayoutController layoutController;

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    public void setLayoutPane(AnchorPane layoutPane) {
        this.layoutPane = layoutPane;
    }

    private final Image arrowRight = new Image(getClass().getResource("/arrow-right.png").toExternalForm());
    private final Image arrowDown = new Image(getClass().getResource("/arrow-down.png").toExternalForm());

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
    private void closeMenu(MouseEvent event){
        System.out.println("cerrando menu...");
        menuPane.setVisible(false);// para que el panel del menu se oculte
        menuPane.setManaged(false);// lo quita de la primera capa, para liberar el espacio
    }
    @FXML
    private void openInicio(MouseEvent event){
        layoutController.loadPanel("/inicio.fxml");
        System.out.println("abrinedo inicio...");
    }
    @FXML
    private void openBusquedas(MouseEvent event){
        layoutController.loadPanel("/busquedas.fxml");
        System.out.println("abireindo busquedas...");
    }

    @FXML
    private void openMenuBusquedas(MouseEvent event){
        System.out.println("abriendo submenu de busquedas...");
        boolean isVisible = subMenuBusquedas.isVisible();
        subMenuBusquedas.setVisible(!isVisible);
        subMenuBusquedas.setManaged(!isVisible);

        if (subMenuBusquedas.isVisible()) {
            arrowBusqueda.setImage(arrowDown); // abierto → flecha abajo
        } else {
            arrowBusqueda.setImage(arrowRight); // cerrado → flecha derecha
        }
    }
    @FXML
    private void openMenuInternas(MouseEvent event){
        System.out.println("abriendo submenu de busquedas internas...");
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);
    }

    @FXML
    private void mostrarBusquedaLineal(MouseEvent event) {
        layoutController.loadPanel("busquedaLineal.fxml");
    }
    @FXML
    private void openBinario(MouseEvent event){
        layoutController.loadPanel("busquedaBinaria.fxml");
    }

    @FXML
    private void openFuncionHash(MouseEvent event){
        layoutController.loadPanel("busquedaHash.fxml");
    }

    @FXML
    private void openGrafos(MouseEvent event){
        layoutController.loadPanel("grafos.fxml");
    }

    public void showMenu() {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
        menuPane.toFront();
    }

}
