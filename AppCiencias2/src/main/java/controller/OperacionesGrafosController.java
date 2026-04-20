package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import utilities.Arista;
import utilities.Grafo;
import utilities.GrafoVisual;
import utilities.Vertice;
import java.util.*;

public class OperacionesGrafosController {

    @FXML private ComboBox<String> operacion;
    @FXML private TextField verticesG1, aristasG1, verticesG2, aristasG2;
    @FXML private Pane paneG1, paneG2, paneG3;
    @FXML private Label infoG1, infoG2, infoG3;

    private Grafo g1, g2, g3;

    @FXML
    private void initialize() {
        operacion.getItems().addAll("Unión", "Intersección", "Suma Anular", "Complemento", "Suma", "Fusión de Vértices");
        operacion.getSelectionModel().selectFirst();

        // Configuración para que el texto no desplace los paneles
        configurarLabelInfo(infoG1);
        configurarLabelInfo(infoG2);
        configurarLabelInfo(infoG3);
    }

    private void configurarLabelInfo(Label label) {
        label.setWrapText(true);
        label.setMaxWidth(300);
        label.setMinHeight(Label.USE_PREF_SIZE);
    }

    @FXML
    private void dibujarG1() {
        try {
            g1 = new Grafo("Grafo 1");
            parsearVertices(verticesG1.getText(), g1, paneG1);
            parsearAristas(aristasG1.getText(), g1);
            actualizarPanel(g1, paneG1, infoG1);
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
            actualizarPanel(g2, paneG2, infoG2);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar el Grafo 2");
        }
    }

    @FXML
    private void hacerOperacion() {
        String op = operacion.getValue();
        if (op == null) return;

        // --- 1. Operaciones de UN SOLO grafo ---
        if (op.equals("Complemento")) {
            Grafo seleccionado = elegirGrafo();
            if (seleccionado != null) {
                g3 = Grafo.complemento(seleccionado);
                g3.setNombre("Comp. de " + seleccionado.getNombre());
                actualizarPanel(g3, paneG3, infoG3);
            }
            return;
        }

        if (op.equals("Fusión de Vértices")) {
            Grafo seleccionado = elegirGrafo();
            if (seleccionado != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Fusión");
                dialog.setHeaderText("Fusión en " + seleccionado.getNombre());
                dialog.setContentText("Ingrese los vértices a fusionar (ej: 1,4):");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent() && result.get().contains(",")) {
                    String[] v = result.get().split(",");
                    String n1 = v[0].trim();
                    String n2 = v[1].trim();

                    if (seleccionado.getVertices().containsKey(n1) && seleccionado.getVertices().containsKey(n2)) {
                        g3 = Grafo.fusionarVertices(seleccionado, n1, n2);
                        actualizarPanel(g3, paneG3, infoG3);
                    } else {
                        mostrarAlerta("Vértice no encontrado", "Uno o ambos vértices no existen en el grafo.");
                    }
                }
            }
            return;
        }

        // --- 2. Operaciones BINARIAS (G1 + G2) ---
        if (g1 == null || g2 == null) {
            mostrarAlerta("Error", "Debes dibujar ambos grafos (G1 y G2) antes de operar.");
            return;
        }

        switch (op) {
            case "Unión": g3 = Grafo.union(g1, g2); break;
            case "Suma": g3 = Grafo.sumaNormal(g1, g2); break;
            case "Suma Anular": g3 = Grafo.sumaAnular(g1, g2); break;
            case "Intersección": g3 = Grafo.interseccion(g1, g2); break;
            default: return;
        }

        actualizarPanel(g3, paneG3, infoG3);
    }

    private void actualizarPanel(Grafo g, Pane pane, Label infoLabel) {
        if (g == null) return;
        pane.getChildren().clear();
        List<Vertice> lista = new ArrayList<>(g.getVertices().values());

        // Ordenamos para que el círculo sea consistente
        lista.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        GrafoVisual.reacomodarCircular(pane, lista);
        GrafoVisual.dibujar(g, pane);
        infoLabel.setText(generarInfoTexto(g));
    }

    private String generarInfoTexto(Grafo g) {
        if (g == null) return "";

        // Vértices
        Set<String> vNombres = new LinkedHashSet<>();
        for (String key : g.getVertices().keySet()) {
            vNombres.add(formatearNombre(key));
        }
        String vertices = "S = {" + String.join(", ", vNombres) + "}";

        // ARISTAS: Cambiamos Set por List para que NO borre las aristas repetidas
        List<String> aristasLista = new ArrayList<>();
        for (Arista a : g.getAristas()) {
            String v1 = formatearNombre(a.getVerticeOrigen().getName());
            String v2 = formatearNombre(a.getVerticeDestino().getName());

            // Mantener un orden visual (ej: siempre 1-2, nunca 2-1)
            if (v1.compareTo(v2) < 0) aristasLista.add(v1 + "-" + v2);
            else aristasLista.add(v2 + "-" + v1);
        }

        // Ordenamos la lista para que el texto se vea bonito
        Collections.sort(aristasLista);
        String aristas = "A = {" + String.join(", ", aristasLista) + "}";

        return g.getNombre() + ":\n" + vertices + "\n" + aristas;
    }

    private String formatearNombre(String nombre) {
        if (nombre.contains("_")) {
            String[] partes = nombre.split("_");
            if (partes[0].equals("2")) return partes[1] + "\u0305";
            return partes[1];
        }
        if (nombre.contains(",")) {
            return nombre + "\u0305";
        }
        return nombre;
    }

    private void parsearVertices(String texto, Grafo grafo, Pane panel) {
        grafo.getVertices().clear();
        if (texto == null || texto.trim().isEmpty()) return;
        String[] nombres = texto.split(",");
        for (String n : nombres) {
            String nombre = n.trim();
            if (!nombre.isEmpty()) grafo.agregarVertice(new Vertice(nombre, 0, 0));
        }
    }

    private void parsearAristas(String texto, Grafo grafo) {
        if (texto == null || texto.trim().isEmpty()) return;
        String[] conexiones = texto.split(",");
        for (String con : conexiones) {
            String[] partes = con.trim().split("-");
            if (partes.length == 2) {
                String nombreV1 = partes[0].trim();
                String nombreV2 = partes[1].trim();

                Vertice v1 = grafo.getVertices().get(nombreV1);
                Vertice v2 = grafo.getVertices().get(nombreV2);

                if (v1 != null && v2 != null) {
                    grafo.agregarArista(new Arista(v1.getName() + "-" + v2.getName(), v1, v2));
                } else {
                    // AQUÍ es donde lanzamos la alerta si el vértice no existe
                    String faltante = (v1 == null) ? nombreV1 : nombreV2;
                    mostrarAlerta("Error de Arista", "El vértice '" + faltante + "' no existe en el grafo.");
                }
            }
        }
    }

    private Grafo elegirGrafo() {
        if (g1 != null && g2 == null) return g1;
        if (g1 == null && g2 != null) return g2;
        if (g1 == null && g2 == null) return null;

        List<String> opciones = Arrays.asList("Grafo 1", "Grafo 2");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Grafo 1", opciones);
        dialog.setTitle("Seleccionar Grafo");
        dialog.setHeaderText("¿A qué grafo quieres aplicar la operación?");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            return result.get().equals("Grafo 1") ? g1 : g2;
        }
        return null;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}