package controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import utilities.Arbol;
import utilities.ArbolVisual;

public class ArbolesController {

    @FXML
    private TextField relacionesField;

    @FXML
    private TextArea infoArea;

    @FXML
    private Pane panelArbol;

    private Arbol arbol;
    

    @FXML
    private void dibujarArbol() {
        try {
            arbol = new Arbol();
            arbol.construirDesdeTexto(relacionesField.getText());

            ArbolVisual.dibujar(arbol, panelArbol);
            mostrarInformacion();

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void mostrarInformacion() {
        StringBuilder sb = new StringBuilder();

        sb.append("Cantidad de nodos: ").append(arbol.contarNodos()).append("\n");
        sb.append("Cantidad de aristas: ").append(arbol.contarAristas()).append("\n\n");

        sb.append("Aristas:\n");
        List<String> aristas = arbol.obtenerAristasComoTexto();
        for (String a : aristas) {
            sb.append(a).append("\n");
        }

        sb.append("\nNiveles:\n");
        List<String> niveles = arbol.obtenerNivelesComoTexto();
        for (String n : niveles) {
            sb.append(n).append("\n");
        }

        sb.append("\nHojas:\n");
        arbol.obtenerHojas().forEach(h -> sb.append(h.getNombre()).append("\n"));

        sb.append("\n").append(arbol.centroOBicentroComoTexto());

        infoArea.setText(sb.toString());
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}