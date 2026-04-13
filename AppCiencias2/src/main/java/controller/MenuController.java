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
    @FXML private ImageView arrowBusqueda1;
    @FXML private ImageView arrowBusqueda2;
    @FXML private ImageView arrowBusqueda3;
    @FXML private ImageView arrowBusqueda4;
    @FXML private ImageView arrowBusqueda5;
    @FXML private VBox subMenuResiduos;
    @FXML private VBox subMenuExternas;
    @FXML private VBox subMenuDinamicas;
    @FXML private VBox subMenuIndices;

    private LayoutController layoutController;

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    private final Image arrowRight = new Image(getClass().getResource("/arrow-right.png").toExternalForm());
    private final Image arrowDown = new Image(getClass().getResource("/arrow-down.png").toExternalForm());

    // el menu y los submenus inician siendo invisibles
    @FXML
    public void initialize() {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false); //no ocupa espacio
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);
        subMenuResiduos.setVisible(false);
        subMenuResiduos.setManaged(false);
        subMenuExternas.setVisible(false);
        subMenuExternas.setManaged(false);
        subMenuDinamicas.setVisible(false);
        subMenuDinamicas.setManaged(false);
        subMenuIndices.setVisible(false);
        subMenuIndices.setManaged(false);
    }

    @FXML
    private void closeMenu(MouseEvent event) {
        System.out.println("Cerrando menú desde el botón interno...");

        // 1. Ocultamos el contenido del menú
        menuPane.setVisible(false);
        menuPane.setManaged(false);

        // 2. Accedemos al contenedor padre (menuSpace)
        if (menuPane.getParent() != null) {
            // Lo hacemos invisible
            menuPane.getParent().setVisible(false);

            // ¡ESTO ES LO MÁS IMPORTANTE!
            // Lo hacemos transparente al ratón para que puedas dar clic a lo que hay atrás
            menuPane.getParent().setMouseTransparent(true);
        }
    }

    @FXML
    private void openInicio(MouseEvent event){
        layoutController.loadPanel("/inicio.fxml");
        System.out.println("abrinedo inicio desde menu Late...");
    }
    @FXML
    private void openBusquedas(MouseEvent event){
        layoutController.loadPanel("/busquedas.fxml");
        System.out.println("abireindo busquedas desde el menu later...");
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
    private void openInternas(MouseEvent event){
        System.out.println("abriendo internas desde el menu lateral");
        layoutController.loadPanel("/busquedasInternas.fxml");
    }
    @FXML
    private void openSubMenuInternas(MouseEvent event){
        System.out.println("abriendo submenu de busquedas internas...");
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);

        if (subMenuInternas.isVisible()) {
            arrowBusqueda1.setImage(arrowDown); // abierto → flecha abajo
        } else {
            arrowBusqueda1.setImage(arrowRight); // cerrado → flecha derecha
        }
    }

    @FXML
    private void openLineal(MouseEvent event) {
        layoutController.loadPanel("/busquedaLineal.fxml");
        System.out.println("abriendo lineal desde el menu late...");
    }
    @FXML
    private void openBinario(MouseEvent event){
        layoutController.loadPanel("/busquedaBinaria.fxml");
        System.out.println("abriendo binario desde el menu late...");
    }

    @FXML
    private void openFuncionHash(MouseEvent event){
        layoutController.loadPanel("/busquedaHash.fxml");
        System.out.println("abriendo hash desde el menu...");
    }

    @FXML
    private void openResiduos(MouseEvent event){
        System.out.println("abriendo residuos desde el menu...");
        layoutController.loadPanel("/busquedaPorResiduos.fxml");
    }

    @FXML
    private void openSubMenuResiduos(MouseEvent event){
        System.out.println("abriendo subMenuResiduos...");
        boolean isVisible = subMenuResiduos.isVisible();
        subMenuResiduos.setVisible(!isVisible);
        subMenuResiduos.setManaged(!isVisible);

        if (subMenuResiduos.isVisible()) {
            arrowBusqueda2.setImage(arrowDown); // abierto → flecha abajo
        } else {
            arrowBusqueda2.setImage(arrowRight); // cerrado → flecha derecha
        }
    }

    @FXML
    private void openArbolDigital(MouseEvent event){
        System.out.println("Abriendo arbolDigital desde el menu...");
        layoutController.loadPanel("/arbolDigital.fxml");
    }

    @FXML
    private void openTries(MouseEvent event){
        System.out.println("Abriendo Tries desde el menu...");
        layoutController.loadPanel("/tresResiduos.fxml");
    }

    @FXML
    private void openMultiple(MouseEvent event){
        System.out.println("abriendo residuos multiples desde el menu...");
        layoutController.loadPanel("/residuosMultiples.fxml");
    }

    @FXML
    private void openHuffman(MouseEvent event){
        layoutController.loadPanel("/huffman.fxml");
    }

    @FXML
    private void openExternas(MouseEvent event){
        System.out.println("abriendo externas desde el menu...");
        layoutController.loadPanel("/busquedsExternas");
    }

    @FXML
    private void openSubMenuExternas(MouseEvent event){
        System.out.println("abriendo submenu externas desde el menu...");
        boolean isVisible = subMenuExternas.isVisible();
        subMenuExternas.setVisible(!isVisible);
        subMenuExternas.setManaged(!isVisible);

        if (subMenuExternas.isVisible()) {
            arrowBusqueda3.setImage(arrowDown); // abierto → flecha abajo
        } else {
            arrowBusqueda3.setImage(arrowRight); // cerrado → flecha derecha
        }
    }

    @FXML
    private void openDinamicas(MouseEvent event){
        System.out.println("dinamicas desde el menu...");
        layoutController.loadPanel("/busquedasDinamicas.fxml");
    }

    @FXML
    private void openSubMenuDinamicas(MouseEvent event){
        System.out.println("Abriendo submenu Dinámicas");
        boolean isVisible = subMenuDinamicas.isVisible();
        subMenuDinamicas.setVisible(!isVisible);
        subMenuDinamicas.setManaged(!isVisible);

        if (subMenuDinamicas.isVisible()) {
            arrowBusqueda4.setImage(arrowDown); // abierto → flecha abajo
        } else {
            arrowBusqueda4.setImage(arrowRight); // cerrado → flecha derecha
        }
    }

    @FXML
    private void openExpTotal(MouseEvent event){
        System.out.println("Abriendo exp total desde menu...");
        layoutController.loadPanel("/busquedaExpTotales.fxml");
    }

    @FXML
    private void openExpParcial(MouseEvent event){
        System.out.println("abriendo exp parcial desde menu...");
        layoutController.loadPanel("/busquedaExpParciales.fxml");
    }

    @FXML
    private void openHashExterno(MouseEvent event){
        System.out.println("abriendo hasH ext en menu...");
        layoutController.loadPanel("/busquedaHashExterna.fxml");
    }

    @FXML
    private void openIndices(MouseEvent event){
        System.out.println("abriendo indices en men...");
        layoutController.loadPanel("/indicesExternos.fxml");
    }

    @FXML
    private void openSubMenuIndices(MouseEvent event){
        System.out.println("abriendo submenu indices");
        boolean isVisible = subMenuIndices.isVisible();
        subMenuIndices.setVisible(!isVisible);
        subMenuIndices.setManaged(!isVisible);

        if (subMenuIndices.isVisible()) {
            arrowBusqueda5.setImage(arrowDown); // abierto → flecha abajo
        } else {
            arrowBusqueda5.setImage(arrowRight); // cerrado → flecha derecha
        }
    }

    @FXML
    private void openPrimario(MouseEvent event){
        System.out.println("primario desde menu...");
        layoutController.loadPanel("/busquedaIndicePrimario.fxml");
    }

    @FXML
    private void openSecundario(MouseEvent event){
        System.out.println("secundario desde menu...");
        layoutController.loadPanel("/busquedaIndiceSecundario.fxml");
    }

    @FXML
    private void openAgrupamiento(MouseEvent event){
        System.out.println("agrupamiento desde menu...");
        System.out.println("proximamente...");
    }

    @FXML
    private void openMultinivel(MouseEvent event){
        System.out.println("multinivel desde menu...");
        System.out.println("proximamente...");
    }

    @FXML
    private void openGrafos(MouseEvent event){
        layoutController.loadPanel("/grafos.fxml");
        System.out.println("abriendo grafos desde le menu...");
    }

    public void showMenu() {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
        menuPane.toFront();
    }

}
