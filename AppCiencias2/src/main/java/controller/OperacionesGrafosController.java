package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import utilities.Arista;
import utilities.Grafo;
import utilities.GrafoVisual;
import utilities.Vertice;

public class OperacionesGrafosController {

    @FXML private ComboBox operacion;
    @FXML private TextField verticesG1;
    @FXML private TextField aristasG1;
    @FXML private TextField verticesG2;
    @FXML private TextField aristasG2;
    @FXML private Pane paneG1;
    @FXML private Pane paneG2;
    @FXML private Pane paneG3;
    @FXML private GrafoVisual dibujar;
    @FXML private Grafo g1;
    @FXML private Grafo g2;
    @FXML private Grafo g3;

    @FXML
    private LayoutController layoutController;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("inicio.fxml"));

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void initialize(){
        operacion.getItems().addAll("Unión", "Intersección");
        operacion.setValue("Unión"); // Opción por defecto
    }


    /// calculado posición para cada vertice en su propido panel
    private void parsearVertices(String texto, Grafo grafo, Pane panel) {
        // 1. Limpiamos lo que haya en el grafo y en el panel
        grafo.getVertices().clear();

        // 2. Rompemos el texto por las comas
        String[] nombres = texto.split(",");
        int n = nombres.length;

        // 3. Definimos el centro del panel
        double centroX = panel.getWidth() / 2;
        double centroY = panel.getHeight() / 2;
        double radio = Math.min(centroX, centroY) - 30; // 30 de margen

        // 4. Por cada nombre, calculamos su lugar y lo creamos
        for (int i = 0; i < n; i++) {
            double angulo = 2 * Math.PI * i / n; // En radianes
            double x = centroX + radio * Math.cos(angulo);
            double y = centroY + radio * Math.sin(angulo);

            String nombreLimpio = nombres[i].trim(); // Quitamos espacios
            Vertice v = new Vertice(nombreLimpio, x, y);

            grafo.agregarVertice(v);
        }
    }

    private void parsearAristas(String texto, Grafo grafo) {
        String[] conexiones = texto.split(",");

        for (String con : conexiones) {
            String[] partes = con.trim().split("-"); // Separa "A" de "B"
            String nombreOrigen = partes[0];
            String nombreDestino = partes[1];

            // ¿Cómo le pedimos al grafo los objetos Vertice usando el nombre?
            Vertice v1 = grafo.getVertices().get(nombreOrigen);
            Vertice v2 = grafo.getVertices().get(nombreDestino);

            if (v1 != null && v2 != null) {
                // Creamos la arista (el nombre puede ser "A-B")
                Arista nueva = new Arista(nombreOrigen + "-" + nombreDestino, v1, v2);
                grafo.agregarArista(nueva);
            } else {
                mostrarAlerta("Vértice no encontrado",
                        "No se pudo crear la arista " + nombreOrigen + "-" + nombreDestino +
                                " porque uno de los vértices no existe en la lista.");
                }
        }
    }

    // dibujo g1
    @FXML
    private void dibujarG1() {
        try {
            // 1. Creamos el objeto Grafo
            g1 = new Grafo("Grafo 1");

            // 2. Procesamos Vértices (Llamamos a la lógica circular que vimos antes)
            parsearVertices(verticesG1.getText(), g1, paneG1);

            // 3. Procesamos Aristas
            parsearAristas(aristasG1.getText(), g1);

            // 4. ¡A dibujar!
            GrafoVisual.dibujar(g1, paneG1);

        } catch (Exception e) {
            // Aquí es donde va la OPCIÓN C que elegiste: La Alerta
            mostrarAlerta("Error de datos", "Verifica que los vértices existan antes de unirlos.");
        }
    }

    @FXML
    private void dibujarG2() {
        try {
            g2 = new Grafo("Grafo 2");
            parsearVertices(verticesG1.getText(), g2, paneG2);
            parsearAristas(aristasG1.getText(), g2);
            GrafoVisual.dibujar(g2, paneG2);

        } catch (Exception e) {
            // Aquí es donde va la OPCIÓN C que elegiste: La Alerta
            mostrarAlerta("Error de datos", "Verifica que los vértices existan antes de unirlos.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /// grafo resultante g3
    @FXML
    private void hacerOperacion() {
        // 1. Validación de seguridad
        if (g1 == null || g2 == null) {
            mostrarAlerta("Error", "Crea primero G1 y G2");
            return;
        }

        // 2. Obtener la opción del ComboBox
        String operacionSeleccionada = operacion.getValue().toString();

        if (operacion == null) {
            mostrarAlerta("Error", "Selecciona una operación");
            return;
        }

        // 3. Decidir qué hacer
        switch (operacionSeleccionada) {
            case "Unión":
                g3 = Grafo.union(g1, g2);
                break;
            case "Intersección":
                // g3 = Grafo.intersección(g1, g2); // Próximamente...
                break;
            default:
                mostrarAlerta("Info", "Operación no implementada aún");
                return;
        }

        // 4. Dibujar el resultado
        GrafoVisual.dibujar(g3, paneG3);
    }
}
