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

    private final Color[] PALETA = {
            Color.web("#FF6B6B"), Color.web("#4ECDC4"), Color.web("#FFE66D"),
            Color.web("#1A535C"), Color.web("#FF9F1C"), Color.web("#706fd3"),
            Color.web("#A3CB38"), Color.web("#1289A7"), Color.web("#D980FA")
    };

    @FXML
    private void initialize() {
        operacion.getItems().addAll(
                "Número Cromático (Vértices)",
                "Clases Cromáticas",
                "Indice Cromático",
                "Polinomio Cromático"
        );
        operacion.getSelectionModel().selectFirst();
        infoG3.setText("Esperando análisis...");

        operacion.setOnAction(e -> {
            if (g1 != null) dibujarG1();
        });
    }

    @FXML
    private void dibujarG1() {
        try {
            paneG1.getChildren().clear();
            g1 = new Grafo("Grafo de Análisis");
            parsearVertices(verticesG1.getText(), g1);
            parsearAristas(aristasG1.getText(), g1);

            List<Vertice> lista = new ArrayList<>(g1.getVertices().values());
            GrafoVisual.reacomodarCircular(paneG1, lista);
            GrafoVisual.dibujar(g1, paneG1);

            infoG3.setText("Grafo cargado y listo.");
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar el grafo.");
        }
    }

    @FXML
    private void hacerOperacion() {
        if (g1 == null || g1.getVertices().isEmpty()) {
            mostrarAlerta("Error", "Dibuja un grafo primero.");
            return;
        }

        String op = operacion.getValue();

        if (op.equals("Número Cromático (Vértices)")) {
            mapaColores = g1.colorearGreedy();
            actualizarVisualColoracion();
            int numC = (mapaColores.isEmpty()) ? 0 : Collections.max(mapaColores.values()) + 1;
            infoG3.setText("ANÁLISIS ESTRUCTURAL:\n\nNúmero Cromático \u03c7(G) = " + numC);
        }

        if (op.equals("Clases Cromáticas")) {
            mapaColores = g1.colorearGreedy();
            actualizarVisualColoracion();
            Map<Integer, List<String>> clases = obtenerMapaClases();

            int numC = clases.size();
            int alfaG = 0;

            StringBuilder sb = new StringBuilder("--- REPORTE ESTRUCTURAL ---\n\n");
            sb.append("1. NÚMERO CROMÁTICO \u03c7(G) = ").append(numC).append("\n\n");

            sb.append("2. CLASES CROMÁTICAS (S_i):\n");
            for (Map.Entry<Integer, List<String>> entry : clases.entrySet()) {
                List<String> nodos = entry.getValue();
                sb.append(" \u2022 S").append(entry.getKey() + 1).append(": ").append(nodos).append("\n");
                if (nodos.size() > alfaG) alfaG = nodos.size();
            }

            sb.append("\n3. CONJUNTOS MAX. INDEPENDIENTES:\n");
            sb.append(" \u2022 Cantidad: ").append(clases.size()).append("\n");
            sb.append(" \u2022 No. Independencia \u03b1(G) = ").append(alfaG).append("\n");

            sb.append("\n4. CONJUNTOS MÁXIMOS:\n");
            for (List<String> nodos : clases.values()) {
                if (nodos.size() == alfaG) sb.append(" \u2192 ").append(nodos).append("\n");
            }

            infoG3.setText(sb.toString());
        }

        if (op.equals("Indice Cromático")) { // Corregido nombre para match exacto
            Map<Arista, Integer> coloresAristas = g1.colorearAristasGreedy();
            actualizarVisualAristas(coloresAristas);
            int maxCol = (coloresAristas.isEmpty()) ? 0 : Collections.max(coloresAristas.values()) + 1;
            infoG3.setText("RESULTADO:\n\nÍndice Cromático \u03c7'(G) = " + maxCol);
        }

        if (op.equals("Polinomio Cromático")) {
            int n = g1.getVertices().size();
            int m = g1.getAristas().size();
            int numC = g1.colorearGreedy().values().stream().max(Integer::compare).orElse(-1) + 1;

            StringBuilder sb = new StringBuilder("ANÁLISIS DEL POLINOMIO:\n\n");

            // Si es un Árbol (n vértices y n-1 aristas)
            if (m == n - 1 && n > 0) {
                sb.append("TIPO: Árbol detectado.\n");
                sb.append("Fórmula: P(\u03bb) = \u03bb(\u03bb-1)^{").append(n-1).append("}\n");

                // Evaluamos para lambda = 2 (mínimo para árboles)
                int lambda = 2;
                long resultado = (long) (lambda * Math.pow(lambda - 1, n - 1));

                sb.append("Resultado para \u03bb=").append(lambda).append(": ").append(resultado);
            } else {
                // Caso General (Fórmula Binomial)
                sb.append("TIPO: Grafo General.\n");
                sb.append("P(\u03bb) = \u2211 c_i \u00b7 C(\u03bb, i)\n");
                long formas = calcularP(numC, n, numC);
                sb.append("Resultado para \u03bb=").append(numC).append(": ").append(formas);
            }

            infoG3.setText(sb.toString());
        }
    }

    private long calcularP(int k, int n, int numC) {
        if (k < numC) return 0;
        // Basado en tus apuntes para el cálculo directo de P(k)
        long res = 1;
        for (int i = 0; i < n; i++) res *= (k - (i % numC));
        return Math.abs(res);
    }

    private Map<Integer, List<String>> obtenerMapaClases() {
        Map<Integer, List<String>> clases = new TreeMap<>();
        mapaColores.forEach((nodo, colorIdx) -> {
            clases.computeIfAbsent(colorIdx, k -> new ArrayList<>()).add(nodo);
        });
        return clases;
    }

    private void actualizarVisualAristas(Map<Arista, Integer> coloresAristas) {
        paneG1.getChildren().clear();
        for (Vertice v : g1.getVertices().values()) {
            GrafoVisual.dibujarNodoColoreado(paneG1, v, Color.LIGHTGRAY);
        }
        coloresAristas.forEach((arista, colorIdx) -> {
            Color colorAsignado = PALETA[colorIdx % PALETA.length];
            GrafoVisual.dibujarAristaColoreada(paneG1, arista, colorAsignado);
        });
    }

    private void actualizarVisualColoracion() {
        paneG1.getChildren().clear();
        GrafoVisual.dibujar(g1, paneG1);
        for (Vertice v : g1.getVertices().values()) {
            int idx = mapaColores.getOrDefault(v.getName(), 0);
            GrafoVisual.dibujarNodoColoreado(paneG1, v, PALETA[idx % PALETA.length]);
        }
    }

    @FXML
    private void handleGuardar() {
        if (g1 == null || g1.getVertices().isEmpty()) return;
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Analisis_Color.gra");
        File file = fc.showSaveDialog(paneG1.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("TIPO=COLORACION");
                pw.println("VERTICES=" + String.join(",", g1.getVertices().keySet()));
                pw.println("ARISTAS");
                for (Arista a : g1.getAristas()) pw.println(a.getVerticeOrigen().getName() + "|" + a.getVerticeDestino().getName() + "|1");
                pw.println("END");
            } catch (Exception e) { mostrarAlerta("Error", "No se pudo guardar."); }
        }
    }

    @FXML
    private void handleCargar() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(paneG1.getScene().getWindow());
        if (file == null) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line, vT = "";
            List<String> aL = new ArrayList<>();
            boolean ar = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("VERTICES=")) vT = line.substring(9);
                else if (line.equals("ARISTAS")) ar = true;
                else if (ar && !line.equals("END")) {
                    String[] p = line.split("\\|");
                    if (p.length >= 2) aL.add(p[0] + "-" + p[1]);
                }
            }
            verticesG1.setText(vT);
            aristasG1.setText(String.join(",", aL));
            dibujarG1();
        } catch (Exception e) { mostrarAlerta("Error", "Error al cargar."); }
    }

    @FXML private void handleLimpiar() {
        g1 = null; verticesG1.clear(); aristasG1.clear();
        paneG1.getChildren().clear(); infoG3.setText("Esperando análisis...");
    }

    private void parsearVertices(String texto, Grafo grafo) {
        if (texto == null || texto.isEmpty()) return;
        for (String n : texto.split(",")) grafo.agregarVertice(new Vertice(n.trim(), 0, 0));
    }

    private void parsearAristas(String texto, Grafo grafo) {
        if (texto == null || texto.isEmpty()) return;
        for (String con : texto.split(",")) {
            String[] p = con.trim().split("-");
            if (p.length == 2) {
                Vertice v1 = grafo.getVertices().get(p[0].trim());
                Vertice v2 = grafo.getVertices().get(p[1].trim());
                if (v1 != null && v2 != null) grafo.agregarArista(new Arista(v1.getName()+"-"+v2.getName(), v1, v2));
            }
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}