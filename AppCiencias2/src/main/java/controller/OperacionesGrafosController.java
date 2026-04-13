package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import utilities.Arista;
import utilities.Grafo;
import utilities.GrafoVisual;
import utilities.Vertice;
import java.util.ArrayList;
import java.util.List;

public class OperacionesGrafosController {

    @FXML private ComboBox<String> operacion;
    @FXML private TextField verticesG1;
    @FXML private TextField aristasG1;
    @FXML private TextField verticesG2;
    @FXML private TextField aristasG2;
    @FXML private Pane paneG1;
    @FXML private Pane paneG2;
    @FXML private Pane paneG3;

    private Grafo g1;
    private Grafo g2;
    private Grafo g3;

    @FXML private LayoutController layoutController;

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void initialize() {
        operacion.getItems().addAll("Unión", "Intersección");
        operacion.getSelectionModel().selectFirst();
        operacion.getItems().add("Suma Anular");
        operacion.getItems().add("Complemento");
    }

    private void parsearVertices(String texto, Grafo grafo, Pane panel) {
        grafo.getVertices().clear();
        if (texto == null || texto.trim().isEmpty()) return;

        String[] nombres = texto.split(",");
        int n = nombres.length;

        double centroX = panel.getWidth() / 2;
        double centroY = panel.getHeight() / 2;
        double radio = Math.min(centroX, centroY) - 30;

        for (int i = 0; i < n; i++) {
            double angulo = 2 * Math.PI * i / n;
            double x = centroX + radio * Math.cos(angulo);
            double y = centroY + radio * Math.sin(angulo);

            String nombreLimpio = nombres[i].trim();
            if (!nombreLimpio.isEmpty()) {
                Vertice v = new Vertice(nombreLimpio, x, y);
                grafo.agregarVertice(v);
            }
        }
    }

    private void parsearAristas(String texto, Grafo grafo) {
        if (texto == null || texto.trim().isEmpty()) return;
        String[] conexiones = texto.split(",");

        for (String con : conexiones) {
            String[] partes = con.trim().split("-");
            if (partes.length == 2) {
                String nombreOrigen = partes[0].trim();
                String nombreDestino = partes[1].trim();

                Vertice v1 = grafo.getVertices().get(nombreOrigen);
                Vertice v2 = grafo.getVertices().get(nombreDestino);

                if (v1 != null && v2 != null) {
                    Arista nueva = new Arista(nombreOrigen + "-" + nombreDestino, v1, v2);
                    grafo.agregarArista(nueva);
                } else {
                    mostrarAlerta("Error en Arista",
                            "La arista " + nombreOrigen + "-" + nombreDestino +
                                    " no se puede crear porque los vértices no existen.");
                }
            }
        }
    }

    @FXML
    private void dibujarG1() {
        try {
            g1 = new Grafo("Grafo 1");
            parsearVertices(verticesG1.getText(), g1, paneG1);
            parsearAristas(aristasG1.getText(), g1);
            GrafoVisual.dibujar(g1, paneG1);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar el Grafo 1");
        }
    }

    @FXML
    private void dibujarG2() {
        try {
            g2 = new Grafo("Grafo 2");
            parsearVertices(verticesG2.getText(), g2, paneG2);
            parsearAristas(aristasG2.getText(), g2);
            GrafoVisual.dibujar(g2, paneG2);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar el Grafo 2");
        }
    }

    @FXML
    private void hacerOperacion() {
        if (g1 == null || g2 == null) {
            mostrarAlerta("Error", "Debes dibujar ambos grafos antes de operar.");
            return;
        }

        String op = operacion.getValue(); // Si definiste ComboBox<String>, no necesitas .toString()

        // Usamos el switch para decidir qué operación hacer
        switch (op) {
            case "Unión":
                g3 = Grafo.union(g1, g2);
                break;
            case "Intersección":
                g3 = Grafo.interseccion(g1, g2);
                break;
            case "Suma Anular":
                g3 = Grafo.sumaAnular(g1, g2);
                break;
            case "Complemento":
                g3 = Grafo.complemento(g1);
                break;
            default:
                return;
        }

        if (g3 != null) {
            // IMPORTANTE: Primero calculamos posiciones, luego dibujamos
            List<Vertice> lista = new ArrayList<>(g3.getVertices().values());

            // Usamos el panel paneG3 para el cálculo
            GrafoVisual.reacomodarCircular(paneG3, lista);

            // Ahora que los objetos Vertice dentro de g3 tienen las nuevas X e Y, dibujamos
            GrafoVisual.dibujar(g3, paneG3);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}