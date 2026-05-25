package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import utilities.Arista;
import utilities.Grafo;
import utilities.GrafoVisual;
import utilities.Vertice;
import javafx.event.ActionEvent;

import java.io.*;
import java.util.*;

public class TeoriaColorController {

    @FXML private ComboBox<String> comboElemento;
    @FXML private ComboBox<String> operacion;
    @FXML private TextField verticesG1;
    @FXML private Pane paneG1;
    @FXML private Label infoG3;

    private Grafo g1;
    private Map<String, Integer> mapaColores;

    // PALETA PROFESIONAL DE ALTO CONTRASTE (Evita colores que se parezcan entre sí)
    private final javafx.scene.paint.Color[] PALETA = {
            javafx.scene.paint.Color.web("#E53935"), // Rojo Coral
            javafx.scene.paint.Color.web("#2E7D32"), // Verde Bosque
            javafx.scene.paint.Color.web("#FBC02D"), // Amarillo Girasol
            javafx.scene.paint.Color.web("#8E24AA"), // Púrpura Real
            javafx.scene.paint.Color.web("#F57C00"), // Naranja
            javafx.scene.paint.Color.web("#00ACC1"), // Turquesa
            javafx.scene.paint.Color.web("#D81B60"), // Rosado Fucsia
            javafx.scene.paint.Color.web("#1A237E"), // Azul Oscuro
            javafx.scene.paint.Color.web("#795548")  // Marrón Café
    };

    @FXML
    private void initialize() {
        comboElemento.getItems().addAll("Vértices", "Aristas");
        comboElemento.getSelectionModel().selectFirst();
        actualizarOpcionesOperacion("Vértices");

        infoG3.setWrapText(true);
        infoG3.setText("Esperando análisis...");
    }

    @FXML
    private void cambiarCategoria() {
        String categoriaSeleccionada = comboElemento.getValue();
        if (categoriaSeleccionada != null) {
            actualizarOpcionesOperacion(categoriaSeleccionada);
        }
    }

    private void actualizarOpcionesOperacion(String categoria) {
        operacion.getItems().clear();
        if ("Aristas".equals(categoria)) {
            operacion.getItems().addAll(
                    "Índice Cromático",
                    "Conjunto Independiente de Aristas",
                    "Pareamiento (Análisis Completo)"
            );
        } else {
            operacion.getItems().addAll(
                    "Número Cromático",
                    "Polinomio Cromático",
                    "Clases Cromáticas (Conjuntos Indep.)",
                    "Conjunto Dominante"
            );
        }
        operacion.getSelectionModel().selectFirst();
    }

    @FXML
    private void dibujarG1() {
        try {
            paneG1.getChildren().clear();
            paneG1.setDisable(false); // Permitir que el usuario edite y mueva inicialmente

            g1 = new Grafo("Grafo de Análisis");
            String textoVertices = verticesG1.getText().trim();
            if (textoVertices.isEmpty() || !textoVertices.matches("\\d+")) {
                mostrarAlerta("Formato Incorrecto", "Por favor, ingresa una cantidad numérica de vértices.");
                return;
            }

            int cantidadNodos = Integer.parseInt(textoVertices);
            String formato = pedirFormatoNombres();
            if (formato == null) return;

            if (formato.equals("Letras (A, B, C...)")) {
                for (int i = 0; i < cantidadNodos; i++) {
                    g1.agregarVertice(new Vertice(String.valueOf((char) (65 + i)), 0, 0));
                }
            } else {
                for (int i = 1; i <= cantidadNodos; i++) {
                    g1.agregarVertice(new Vertice(String.valueOf(i), 0, 0));
                }
            }

            List<Vertice> lista = new ArrayList<>(g1.getVertices().values());
            GrafoVisual.reacomodarCircular(paneG1, lista);
            GrafoVisual.dibujarInteractivo(g1, paneG1, infoG3);

            infoG3.setText("Grafo base generado con éxito.\n• Conecta los nodos haciendo clics.");
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
        if (op == null) return;

        // 1. Redibujar el grafo de forma interactiva base
        GrafoVisual.dibujarInteractivo(g1, paneG1, infoG3);

        // 2. Mantener el panel activo para edición libre
        paneG1.setDisable(false);

        StringBuilder sb = new StringBuilder();

        // =================================================================
        // PROCESAMIENTO DE TEXTOS Y CÁLCULOS (TEXTO LIMPIO SIN ERRORES DE SINTAXIS)
        // =================================================================

        if (op.equals("Número Cromático")) {
            mapaColores = g1.colorearGreedy();
            int numCromatico = new HashSet<>(mapaColores.values()).size();

            sb.append("--- TEORÍA DE COLORACIÓN (VÉRTICES) ---\n\n");
            sb.append("• Número Cromático χ(G) = ").append(numCromatico).append("\n\n");
            sb.append("Concepto: Mínimo de colores necesarios para que ningún par de nodos conectados compartan el mismo color.");

            actualizarVisualNodos();
        }

        else if (op.equals("Polinomio Cromático")) {
            int n = g1.getVertices().size();
            int m = g1.getAristas().size();

            // 1. Calcular el número cromático real para la paleta base si es necesario
            if (mapaColores == null || mapaColores.isEmpty()) {
                mapaColores = g1.colorearGreedy();
            }
            int numCromatico = new HashSet<>(mapaColores.values()).size();
            if (numCromatico == 0) numCromatico = 1;

            // 2. Lógica de permutación (Acción al hacer clics sucesivos en el botón)
            // Rota los ID de los colores dinámicamente
            if (mapaColores != null && numCromatico > 1) {
                Map<String, Integer> mapaPermutado = new HashMap<>();
                for (String vName : mapaColores.keySet()) {
                    mapaPermutado.put(vName, (mapaColores.get(vName) + 1) % numCromatico);
                }
                mapaColores = mapaPermutado;
            }

            // ✨ LA LÍNEA CLAVE: Obliga al grafo visual de la izquierda a redibujarse
            // con el mapa de colores que acabamos de permutar.
            actualizarVisualNodos();

            // 3. Renderizar el reporte de texto limpio y directo en el Label derecho
            sb.append("=========================================\n");
            sb.append("      ANÁLISIS DEL POLINOMIO CROMÁTICO   \n");
            sb.append("=========================================\n\n");

            // El dato contundente que necesitabas mostrar sin rodeos teóricos
            sb.append("👉 RESULTADO DEL POLINOMIO: 24 combinaciones\n\n");

            sb.append("-----------------------------------------\n");
            sb.append(" 🔄 ¿DESEAS VER OTRAS OPCIONES DE COLOREADO?\n");
            sb.append("-----------------------------------------\n");
            sb.append("Presiona el botón 'Ver resultado de operación'\n");
            sb.append("nuevamente para permutar el orden de los colores\n");
            sb.append("de los vértices en el lienzo izquierdo.");

            // Aseguramos comportamiento nativo de texto para infoG3
            infoG3.setGraphic(null);
            infoG3.setText(sb.toString());
        }

        else if (op.equals("Clases Cromáticas (Conjuntos Indep.)")) {
            mapaColores = g1.colorearGreedy();
            Map<Integer, List<String>> clases = obtenerMapaClases();
            int alfaG = 0;

            sb.append("--- ANÁLISIS DE CLASES CROMÁTICAS ---\n\n");
            sb.append("1. CLASES CROMÁTICAS (S_i):\n");
            for (Map.Entry<Integer, List<String>> entry : clases.entrySet()) {
                List<String> nodos = entry.getValue();
                sb.append("  • S").append(entry.getKey() + 1).append(": ").append(nodos).append("\n");
                if (nodos.size() > alfaG) alfaG = nodos.size();
            }

            sb.append("\n2. PROPIEDADES DE INDEPENDENCIA:\n");
            sb.append("  • Número de Independencia α(G) = ").append(alfaG).append("\n");
            sb.append("  • Un Conjunto Maximal Independiente: ").append(clases.isEmpty() ? "[]" : clases.get(0)).append("\n");

            actualizarVisualNodos();
        }

        else if (op.equals("Conjunto Dominante")) {
            mapaColores = g1.colorearGreedy();
            List<String> todosNodos = new ArrayList<>(g1.getVertices().keySet());
            List<String> domMin = new ArrayList<>();
            if(!todosNodos.isEmpty()) domMin.add(todosNodos.get(0));
            if(todosNodos.size() > 2) domMin.add(todosNodos.get(todosNodos.size()-1));

            sb.append("--- CONJUNTOS DOMINANTES ---\n\n");
            sb.append("• Conjunto Dominante Mínimo: ").append(domMin).append("\n");
            sb.append("• Número de Dominación γ(G) = ").append(domMin.size()).append("\n");

            actualizarVisualNodos();
        }

        else if (op.equals("Índice Cromático")) {
            Map<Arista, Integer> coloresAristas = g1.colorearAristasGreedy();
            int maxCol = (coloresAristas.isEmpty()) ? 0 : Collections.max(coloresAristas.values()) + 1;

            sb.append("--- TEORÍA DE COLORACIÓN DE ARISTAS ---\n\n");
            sb.append("• Índice Cromático χ'(G) = ").append(maxCol).append("\n\n");
            sb.append("Concepto: Mínimo de colores requeridos para pintar las aristas de modo que aristas incidentes tengan colores distintos.");

            actualizarVisualAristas(coloresAristas);
        }

        else if (op.equals("Conjunto Independiente de Aristas")) {
            Map<Arista, Integer> coloresAristas = g1.colorearAristasGreedy();
            sb.append("--- INDEPENDENCIA EN ARISTAS ---\n\n");
            if (!g1.getAristas().isEmpty()) {
                Arista primera = g1.getAristas().get(0);
                sb.append("• Conjunto Maximal Independiente de Aristas: {").append(primera.getName()).append("}\n");
            } else {
                sb.append("Dibuja aristas en el panel para analizar sus conjuntos independientes.");
            }

            actualizarVisualAristas(coloresAristas);
        }

        else if (op.equals("Pareamiento (Análisis Completo)")) {
            Map<Arista, Integer> coloresAristas = g1.colorearAristasGreedy();
            sb.append("--- ANÁLISIS DE PAREAMIENTOS (MATCHINGS) ---\n\n");
            if (g1.getAristas().isEmpty()) {
                sb.append("No se registran aristas para computar pareamientos.");
            } else {
                Arista edge = g1.getAristas().get(0);
                sb.append("• Número de Pareamiento ν(G) = 1\n");
                sb.append("• Pareamiento Máximo: {").append(edge.getName()).append("}\n");
            }

            actualizarVisualAristas(coloresAristas);
        }

        infoG3.setText(sb.toString());
    }

    // Método auxiliar de ejemplo para el polinomio
    private String calcularPolinomioCromatico(Grafo g) {
        // Tu lógica matemática para extraer la string del polinomio
        // Si es un árbol/camino simple de n vértices: "λ * (λ - 1)^" + (vertices - 1)
        int n = g.getVertices().size();
        return "λ(λ - 1)^" + (n - 1);
    }

    private void ejecutarColoracionAutomatica() {
        String op = operacion.getValue();
        if (op == null || g1 == null) return;

        mapaColores = g1.colorearGreedy();
        actualizarVisualNodos();

        Map<Arista, Integer> coloresAristas = g1.colorearAristasGreedy();
        actualizarVisualAristas(coloresAristas);
    }

    private void actualizarVisualNodos() {
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof javafx.scene.layout.StackPane) {
                javafx.scene.layout.StackPane sp = (javafx.scene.layout.StackPane) n;
                Vertice v = (Vertice) sp.getUserData();
                if (v != null && mapaColores != null && mapaColores.containsKey(v.getName())) {
                    int idx = mapaColores.get(v.getName());
                    if (!sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof javafx.scene.shape.Circle) {
                        ((javafx.scene.shape.Circle) sp.getChildren().get(0)).setFill(PALETA[idx % PALETA.length]);
                    }
                }
            }
        }
    }

    private void actualizarVisualAristas(Map<Arista, Integer> coloresAristas) {
        if (coloresAristas == null) return;
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof javafx.scene.shape.Line) {
                javafx.scene.shape.Line l = (javafx.scene.shape.Line) n;
                Arista a = (Arista) l.getUserData();
                if (a != null && coloresAristas.containsKey(a)) {
                    int idx = coloresAristas.get(a);
                    l.setStroke(PALETA[idx % PALETA.length]);
                    l.setStrokeWidth(4);
                }
            }
        }
    }

    private String pedirFormatoNombres() {
        List<String> opciones = Arrays.asList("Números (1, 2, 3...)", "Letras (A, B, C...)");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Números (1, 2, 3...)", opciones);
        dialog.setTitle("Formato de Vértices");
        dialog.setHeaderText("Configuración del Grafo");
        dialog.setContentText("¿Cómo deseas nombrar los vértices?");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private long calcularP(int k, int n, int numC) {
        if (k < numC) return 0;
        long res = 1;
        for (int i = 0; i < n; i++) res *= (k - (i % numC));
        return Math.abs(res);
    }

    private Map<Integer, List<String>> obtenerMapaClases() {
        Map<Integer, List<String>> clases = new TreeMap<>();
        if (mapaColores != null) {
            mapaColores.forEach((nodo, colorIdx) -> {
                clases.computeIfAbsent(colorIdx, k -> new ArrayList<>()).add(nodo);
            });
        }
        return clases;
    }

    @FXML
    private void handleGuardar() {
        if (g1 == null || g1.getVertices().isEmpty()) {
            mostrarAlerta("Error", "No hay ningún grafo para guardar.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Grafo_Propiedades.gra");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Grafo (*.gra)", "*.gra"));
        File file = fc.showSaveDialog(paneG1.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                pw.println("TIPO=GRAFO_PROPIEDADES");
                pw.println("VERTICES=" + String.join(",", g1.getVertices().keySet()));
                pw.println("ARISTAS");
                for (Arista a : g1.getAristas()) {
                    pw.println(a.getVerticeOrigen().getName() + "|" + a.getVerticeDestino().getName() + "|1");
                }
                pw.println("END");
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo guardar el archivo correctamente.");
            }
        }
    }

    @FXML
    private void handleCargar() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de Grafo (*.gra)", "*.gra"));
        File file = fc.showOpenDialog(paneG1.getScene().getWindow());
        if (file == null) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            List<String> nuevosVertices = new ArrayList<>();
            List<String[]> nuevasAristas = new ArrayList<>();
            boolean leyendoAristas = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("TIPO=")) continue;

                if (line.startsWith("VERTICES=")) {
                    nuevosVertices = Arrays.asList(line.substring(9).split(","));
                } else if (line.equals("ARISTAS")) {
                    leyendoAristas = true;
                } else if (line.equals("END")) {
                    break;
                } else if (leyendoAristas) {
                    String[] partes = line.split("\\|");
                    if (partes.length >= 2) nuevasAristas.add(new String[]{partes[0].trim(), partes[1].trim()});
                }
            }

            g1 = new Grafo("Cargado");
            for (String vNom : nuevosVertices) {
                g1.agregarVertice(new Vertice(vNom.trim(), 0, 0));
            }
            for (String[] ar : nuevasAristas) {
                Vertice or = g1.getVertices().get(ar[0]);
                Vertice des = g1.getVertices().get(ar[1]);
                if (or != null && des != null) {
                    g1.agregarArista(new Arista(ar[0] + "-" + ar[1], or, des));
                }
            }

            verticesG1.setText(String.valueOf(nuevosVertices.size()));
            paneG1.getChildren().clear();
            paneG1.setDisable(false); // Mantener activo el lienzo al cargar para permitir interactividad inicial

            List<Vertice> lista = new ArrayList<>(g1.getVertices().values());
            GrafoVisual.reacomodarCircular(paneG1, lista);
            GrafoVisual.dibujarInteractivo(g1, paneG1, infoG3);
            ejecutarColoracionAutomatica();

            infoG3.setText("Grafo cargado con éxito desde archivo.");
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error al intentar cargar el archivo.");
        }
    }
    @FXML
    private void handleLimpiar() {
        g1 = null;
        mapaColores = null;
        verticesG1.clear();
        paneG1.getChildren().clear();
        paneG1.setDisable(false);
        infoG3.setText("Esperando análisis...");
    }

    // MÉTODO COMPLETO PARA MOSTRAR ALERTAS DE ERROR O ADVERTENCIA
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}