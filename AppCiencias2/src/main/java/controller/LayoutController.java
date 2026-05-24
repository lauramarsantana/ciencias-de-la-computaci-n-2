package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class LayoutController {
    @FXML
    private AnchorPane contentPane;
    @FXML private MenuController menuController;
    @FXML private AnchorPane menuPane;
    @FXML private AnchorPane menuSpace;
    @FXML private HBox goBack;

    // contador de cambios de panel
    private int panelCount = 0;

    // historial de paneles cargados
    private Deque<Node> history = new ArrayDeque<>();

    public void setContent(Node node) {
        contentPane.getChildren().setAll(node);
    }

    @FXML
    public void initialize() {

        try {
            goBack.setVisible(false);
            goBack.setManaged(false);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            menuPane = loader.load();
            menuController = loader.getController();
            menuController.setLayoutController(this);

            // el menú empieza oculto
            menuPane.setVisible(false);
            menuPane.setManaged(false);

            menuSpace.setVisible(false);
            menuSpace.setMouseTransparent(true);

            // lo agregamos encima del BorderPane
            menuSpace.getChildren().add(menuPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openMenu(javafx.scene.input.MouseEvent event) {
        System.out.println("abriendo menu...");
        boolean visible = !menuPane.isVisible(); // Invertimos el estado

        menuPane.setVisible(visible);
        menuPane.setManaged(visible);
        menuSpace.setVisible(visible);

        // ESTA ES LA CLAVE:
        // Si no es visible, el panel deja pasar los clics hacia el fondo.
        menuSpace.setMouseTransparent(!visible);

        if (visible) {
            menuSpace.toFront();
        }
        System.out.println("Menú " + (visible ? "Abierto" : "Cerrado"));
    }

    @FXML
    private void goBack(MouseEvent event){
        if (!history.isEmpty()) {
            Node previous = history.pop();
            setContent(previous);
            System.out.println("Volviendo al panel anterior..." + previous);
        } else {
            System.out.println("No hay panel anterior en el historial.");
        }
    }

    public void loadPanel(String fxml) {

        try {
            // guardar el panel actual en el historial antes de reemplazarlo
            if (!contentPane.getChildren().isEmpty()) {
                history.push(contentPane.getChildren().get(0));
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node panel = loader.load();

            // obtener el controlador del panel
            Object controller = loader.getController();
            if (controller instanceof InicioController inicioController) {
                inicioController.setLayoutController(this);
            }
            if (controller instanceof BusquedaController busquedaController){
                busquedaController.setLayoutController(this);
            }
            if (controller instanceof BusquedasInternasController busquedaInternaController){
                busquedaInternaController.setLayoutController(this);
            }
            if (controller instanceof BusquedasExternasController busquedasExternasController){
                busquedasExternasController.setLayoutController(this);
            }
            if (controller instanceof BPorResiduosController residuosController){
                residuosController.setLayoutController(this);
            }
            if (controller instanceof BusquedasDinamicasController dinamicasController){
                dinamicasController.setLayoutController(this);
            }
            if (controller instanceof IndicesExternosController indicesController){
                indicesController.setLayoutController(this);
            }
            if (controller instanceof GrafosController grafosController){
                grafosController.setLayoutController(this);
            }
            if (controller instanceof ArbolesMenuController arbolesMenuController){
                arbolesMenuController.setLayoutController(this);
            }
            if (controller instanceof AlgoritmosGrafosController) {
                ((AlgoritmosGrafosController) controller).setLayoutController(this);
            }
            if (controller instanceof MenuRepresentacionMetricasController) {
            ((MenuRepresentacionMetricasController) controller).setLayoutController(this);
            }
            
            

            // incrementar contador de cambios
            panelCount++;
            System.out.println("cambios de panel: "+panelCount);

            // mostrar el botón atrás a partir del primer cambio
            if (panelCount == 1) {
                goBack.setVisible(true);
                goBack.setManaged(true);
                goBack.toFront();
            }
            if (panelCount == 0){
                goBack.setVisible(false);
                goBack.setManaged(false);
            }

            setContent(panel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
