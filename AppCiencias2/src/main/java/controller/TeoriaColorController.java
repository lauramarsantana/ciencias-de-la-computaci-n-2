package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import utilities.Arista;
import utilities.Grafo;
import utilities.GrafoVisual;
import utilities.Vertice;

import java.io.*;
import java.util.*;

public class TeoriaColorController {

    @FXML private ComboBox<String> operacion;
    @FXML private TextField verticesG1, aristasG1;
    @FXML private Pane paneG1;
    @FXML private Label infoG3;

    private Grafo g1;
    private Map<String, Integer> mapaColores;

    // Paleta profesional para coloreado de grafos
    private final Color[] PALETA = {
            Color.web("#FF6B6B"), Color.web("#4ECDC4"), Color.web("#FFE66D"),
            Color.web("#1A535C"), Color.web("#FF9F1C"), Color.web("#706fd3"),
            Color.web("#A3CB38"), Color.web("#1289A7"), Color.web("#D980FA")
    };

    @FXML
    private void initialize() {
        operacion.getItems().addAll(
                "Número Cromático (Vértices)",
                "Clases Cromáticas"
        );
        operacion.getSelectionModel().selectFirst();
        infoG3.setText("Esperando análisis...");

        // Listener para que no se borren las líneas al cambiar de operación
        operacion.setOnAction(e -> {
            if (g1 != null) dibujarG1();
        });
    }

    @FXML
    private void dibujarG1() {
        paneG1.getChildren().clear(); // Limpiar el dibujo anterior
        g1 = new Grafo("Grafo 1");    // Crear instancia nueva desde cero
        parsearVertices(verticesG1.getText(), g1);
        parsearAristas(aristasG1.getText(), g1);
        try {
            g1 = new Grafo("Grafo de Análisis");
            parsearVertices(verticesG1.getText(), g1);
            parsearAristas(aristasG1.getText(), g1);

            // Reacomodamos y dibujamos en el panel principal
            List<Vertice> lista = new ArrayList<>(g1.getVertices().values());
            GrafoVisual.reacomodarCircular(paneG1, lista);
            GrafoVisual.dibujar(g1, paneG1);

            infoG3.setText("Grafo dibujado correctamente.");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo procesar el grafo. Revisa el formato.");
        }
    }

    @FXML
    private void hacerOperacion() {
        if (g1 == null || g1.getVertices().isEmpty()) {
            mostrarAlerta("Error", "Debes cargar o dibujar un grafo primero.");
            return;
        }

        String op = operacion.getValue();
        // Llamada al motor Greedy en la clase Grafo
        mapaColores = g1.colorearGreedy();

        if (op.equals("Número Cromático (Vértices)")) {
            actualizarVisualColoracion();
            int numCromatico = (mapaColores.isEmpty()) ? 0 : Collections.max(mapaColores.values()) + 1;
            infoG3.setText("ANÁLISIS:\n\nNúmero Cromático \u03c7(G) = " + numCromatico);
        }
        else if (op.equals("Clases Cromáticas")) {
            actualizarVisualColoracion();
            mostrarClasesCromaticas();
        }
    }

    private void actualizarVisualColoracion() {
        paneG1.getChildren().clear();
        // 1. Dibujar aristas primero (capa inferior)
        GrafoVisual.dibujar(g1, paneG1);

        // 2. Repintar nodos con sus colores asignados
        for (Vertice v : g1.getVertices().values()) {
            int idx = mapaColores.getOrDefault(v.getName(), 0);
            Color colorAsignado = PALETA[idx % PALETA.length];

            // Usamos tu método de utilidad para dibujar el nodo con color
            GrafoVisual.dibujarNodoColoreado(paneG1, v, colorAsignado);
        }
    }

    private void mostrarClasesCromaticas() {
        Map<Integer, List<String>> clases = new TreeMap<>();
        mapaColores.forEach((nodo, colorIdx) -> {
            // CORRECCIÓN: El .add(nodo) debe ir fuera del constructor de la lista
            clases.computeIfAbsent(colorIdx, k -> new ArrayList<>()).add(nodo);
        });

        StringBuilder sb = new StringBuilder("CLASES CROMÁTICAS:\n\n");
        clases.forEach((idx, nodos) -> {
            sb.append("Color ").append(idx + 1).append(": {")
                    .append(String.join(", ", nodos)).append("}\n");
        });
        infoG3.setText(sb.toString());
    }

    // --- MÉTODOS DE PERSISTENCIA (LIMPIOS Y UNIFICADOS) ---

    @FXML
    private void handleGuardar() {
        if (g1 == null || g1.getVertices().isEmpty()) return;

        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Analisis_Color.gra");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos .gra", "*.gra"));
        File file = fc.showSaveDialog(paneG1.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                bw.write("TIPO=COLORACION"); bw.newLine();
                bw.write("VERTICES=" + String.join(",", g1.getVertices().keySet())); bw.newLine();
                bw.write("ARISTAS"); bw.newLine();
                for (Arista a : g1.getAristas()) {
                    bw.write(a.getVerticeOrigen().getName() + "|" + a.getVerticeDestino().getName() + "|1");
                    bw.newLine();
                }
                bw.write("END");
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo guardar el archivo.");
            }
        }
    }

    @FXML
    private void handleCargar() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos .gra", "*.gra"));
        File file = fc.showOpenDialog(paneG1.getScene().getWindow());

        if (file == null) return;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            String vTexto = "";
            List<String> aList = new ArrayList<>();
            boolean leyendoAristas = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("VERTICES=")) vTexto = line.substring(9);
                else if (line.equals("ARISTAS")) leyendoAristas = true;
                else if (line.equals("END")) break;
                else if (leyendoAristas) {
                    String[] partes = line.split("\\|");
                    if (partes.length >= 2) aList.add(partes[0].trim() + "-" + partes[1].trim());
                }
            }

            // Sincronización de campos para evitar pérdida de datos al operar
            verticesG1.setText(vTexto);
            aristasG1.setText(String.join(",", aList));
            dibujarG1();

        } catch (Exception e) {
            mostrarAlerta("Error", "Archivo dañado o formato incompatible.");
        }
    }

    @FXML
    private void handleLimpiar() {
        g1 = null;
        verticesG1.clear();
        aristasG1.clear();
        paneG1.getChildren().clear();
        infoG3.setText("Esperando análisis...");
    }

    // --- MÉTODOS DE PARSEO ---

    private void parsearVertices(String texto, Grafo grafo) {
        grafo.getVertices().clear();
        if (texto == null || texto.trim().isEmpty()) return;
        for (String n : texto.split(",")) {
            String nombre = n.trim();
            if (!nombre.isEmpty()) grafo.agregarVertice(new Vertice(nombre, 0, 0));
        }
    }

    private void parsearAristas(String texto, Grafo grafo) {
        if (texto == null || texto.trim().isEmpty()) return;
        for (String con : texto.split(",")) {
            String[] partes = con.trim().split("-");
            if (partes.length == 2) {
                Vertice v1 = grafo.getVertices().get(partes[0].trim());
                Vertice v2 = grafo.getVertices().get(partes[1].trim());
                if (v1 != null && v2 != null) {
                    grafo.agregarArista(new Arista(v1.getName() + "-" + v2.getName(), v1, v2));
                }
            }
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