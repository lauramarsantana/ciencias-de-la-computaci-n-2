package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class IndicesExternosController {

    @FXML private AnchorPane indicesPane;
    @FXML private AnchorPane menuPane;

    @FXML
    public void initialize() {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
    }

    @FXML
    private void openMenu(javafx.scene.input.MouseEvent event) {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
    }

    @FXML
    private void closeMenu(javafx.scene.input.MouseEvent event) {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
    }

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event) {
        loadPanel("inicio.fxml");
    }

    @FXML
    private void openBusquedasExternas(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedasExternas.fxml");
    }

    @FXML
    private void openIndicePrimario(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaIndicePrimario.fxml");
    }

    @FXML
    private void openIndiceSecundario(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaIndiceSecundario.fxml");
    }

    @FXML
    private void openIndiceAgrupamiento(javafx.scene.input.MouseEvent event) {
        // Mientras lo haces, puedes dejar temporalmente esta misma vista o abrir un placeholder
        loadPanel("indiceAgrupamiento.fxml");
    }

    @FXML
    private void openIndiceMultinivel(javafx.scene.input.MouseEvent event) {
        // Mientras lo haces, puedes dejar temporalmente esta misma vista o abrir un placeholder
        loadPanel("indiceMultinivel.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();
            indicesPane.getChildren().clear();
            indicesPane.getChildren().add(panel);

            AnchorPane.setTopAnchor(panel, 0.0);
            AnchorPane.setBottomAnchor(panel, 0.0);
            AnchorPane.setLeftAnchor(panel, 0.0);
            AnchorPane.setRightAnchor(panel, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}