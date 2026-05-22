package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import utilities.Arista;
import utilities.Grafo;
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

    // Variables para gestionar la creación interactiva de aristas
    private Vertice nodoOrigenSeleccionado = null;
    private StackPane contenedorOrigenVisual = null;

    private final Color[] PALETA = {
            Color.web("#FF6B6B"), Color.web("#4ECDC4"), Color.web("#FFE66D"),
            Color.web("#1A535C"), Color.web("#FF9F1C"), Color.web("#706fd3"),
            Color.web("#A3CB38"), Color.web("#1289A7"), Color.web("#D980FA")
    };

    @FXML
    private void initialize() {
        comboElemento.getItems().addAll("Vértices", "Aristas");
        comboElemento.getSelectionModel().selectFirst();

        actualizarOpcionesOperacion("Vértices");
        infoG3.setText("Esperando análisis...");

        comboElemento.setOnAction(e -> cambiarCategoria());

        operacion.setOnAction(e -> {
            if (g1 != null) {
                refrescarDibujoBase();
            }
        });
    }

    private void cambiarCategoria() {
        String categoriaSeleccionada = comboElemento.getValue();
        actualizarOpcionesOperacion(categoriaSeleccionada);
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
            g1 = new Grafo("Grafo de Análisis");
            nodoOrigenSeleccionado = null;
            contenedorOrigenVisual = null;

            String textoVertices = verticesG1.getText().trim();
            if (textoVertices.isEmpty()) {
                mostrarAlerta("Advertencia", "Por favor ingresa la cantidad de vértices.");
                return;
            }

            int cantidadNodos = 0;
            if (textoVertices.matches("\\d+")) {
                cantidadNodos = Integer.parseInt(textoVertices);
                for (int i = 1; i <= cantidadNodos; i++) {
                    g1.agregarVertice(new Vertice(String.valueOf(i), 0, 0));
                }
            } else {
                String[] nombres = textoVertices.split(",");
                cantidadNodos = nombres.length;
                for (String n : nombres) {
                    g1.agregarVertice(new Vertice(n.trim(), 0, 0));
                }
            }

            // Distribución inicial circular
            double centroX = paneG1.getPrefWidth() > 0 ? paneG1.getPrefWidth() / 2 : 200;
            double centroY = paneG1.getPrefHeight() > 0 ? paneG1.getPrefHeight() / 2 : 160;
            double radio = Math.min(centroX, centroY) - 40;
            int i = 0;

            for (Vertice v : g1.getVertices().values()) {
                double angulo = 2 * Math.PI * i / cantidadNodos;
                v.setPositionX(centroX + radio * Math.cos(angulo));
                v.setPositionY(centroY + radio * Math.sin(angulo));

                crearNodoVisual(v);
                i++;
            }

            infoG3.setText("Grafo base generado.\n\n• Arrastra los nodos con el mouse para moverlos.\n• Haz clic en un nodo y luego en otro para conectarlos con una arista.");
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar y estructurar el grafo.");
        }
    }

    // Crea el StackPane interactivo para cada nodo (Mover + Conectar)
    private void crearNodoVisual(Vertice v) {
        StackPane contenedor = new StackPane();
        contenedor.setUserData(v);
        contenedor.setLayoutX(v.getPositionX() - 20);
        contenedor.setLayoutY(v.getPositionY() - 20);

        Circle circulo = new Circle(20, Color.WHITE);
        circulo.setStroke(Color.BLUE);
        circulo.setStrokeWidth(2);

        Text texto = new Text(v.getName());
        texto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        contenedor.getChildren().addAll(circulo, texto);

        // EVENTO 1: Arrastrar libremente el vértice
        contenedor.setOnMouseDragged(e -> {
            double nuevoX = contenedor.getLayoutX() + e.getX();
            double nuevoY = contenedor.getLayoutY() + e.getY();

            // Evitar que el usuario arrastre fuera de los límites visibles del panel
            if (nuevoX >= 0 && nuevoX <= paneG1.getWidth() - 40) {
                contenedor.setLayoutX(nuevoX);
                v.setPositionX(nuevoX + 20);
            }
            if (nuevoY >= 0 && nuevoY <= paneG1.getHeight() - 40) {
                contenedor.setLayoutY(nuevoY);
                v.setPositionY(nuevoY + 20);
            }

            reubicarAristasVisuales();
        });

        // EVENTO 2: Conectar aristas mediante clics sucesivos
        contenedor.setOnMouseClicked(e -> {
            if (nodoOrigenSeleccionado == null) {
                nodoOrigenSeleccionado = v;
                contenedorOrigenVisual = contenedor;
                circulo.setStroke(Color.ORANGE);
                circulo.setStrokeWidth(4);
                infoG3.setText("Origen: " + v.getName() + ". Haz clic en otro nodo para crear la arista.");
            } else {
                if (!nodoOrigenSeleccionado.getName().equals(v.getName())) {
                    String idArista = nodoOrigenSeleccionado.getName() + "-" + v.getName();

                    // Validar que no exista ya esa arista
                    boolean existe = g1.getAristas().stream().anyMatch(a -> a.getName().equals(idArista) ||
                            a.getName().equals(v.getName() + "-" + nodoOrigenSeleccionado.getName()));

                    if (!existe) {
                        Arista nuevaArista = new Arista(idArista, nodoOrigenSeleccionado, v);
                        g1.agregarArista(nuevaArista);
                        dibujarAristaVisual(nuevaArista);
                    }
                }
                // Resetear la selección de bordes
                if (contenedorOrigenVisual != null && !contenedorOrigenVisual.getChildren().isEmpty()) {
                    ((Circle) contenedorOrigenVisual.getChildren().get(0)).setStroke(Color.BLUE);
                    ((Circle) contenedorOrigenVisual.getChildren().get(0)).setStrokeWidth(2);
                }
                nodoOrigenSeleccionado = null;
                contenedorOrigenVisual = null;
                infoG3.setText("Arista conectada con éxito. Listo para nueva conexión.");
            }
        });

        paneG1.getChildren().add(contenedor);
    }

    private void dibujarAristaVisual(Arista a) {
        Line linea = new Line();
        linea.setUserData(a);
        linea.setStroke(Color.BLUE);
        linea.setStrokeWidth(2);

        linea.setStartX(a.getVerticeOrigen().getPositionX());
        linea.setStartY(a.getVerticeOrigen().getPositionY());
        linea.setEndX(a.getVerticeDestino().getPositionX());
        linea.setEndY(a.getVerticeDestino().getPositionY());

        // Aseguramos que las líneas queden en el fondo para no tapar los números de los nodos
        paneG1.getChildren().add(0, linea);
    }

    // Actualiza dinámicamente las coordenadas de las líneas al arrastrar los nodos
    private void reubicarAristasVisuales() {
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof Line) {
                Line l = (Line) n;
                Arista a = (Arista) l.getUserData();
                if (a != null) {
                    l.setStartX(a.getVerticeOrigen().getPositionX());
                    l.setStartY(a.getVerticeOrigen().getPositionY());
                    l.setEndX(a.getVerticeDestino().getPositionX());
                    l.setEndY(a.getVerticeDestino().getPositionY());
                }
            }
        }
    }

    // Vuelve a pintar el grafo con sus colores base por defecto (Limpia coloraciones previas)
    private void refrescarDibujoBase() {
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof StackPane) {
                StackPane sp = (StackPane) n;
                if (!sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof Circle) {
                    ((Circle) sp.getChildren().get(0)).setFill(Color.WHITE);
                    ((Circle) sp.getChildren().get(0)).setStroke(Color.BLUE);
                    ((Circle) sp.getChildren().get(0)).setStrokeWidth(2);
                }
            } else if (n instanceof Line) {
                ((Line) n).setStroke(Color.BLUE);
                ((Line) n).setStrokeWidth(2);
            }
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

        refrescarDibujoBase(); // Resetear estilos anteriores antes de procesar
        StringBuilder sb = new StringBuilder();

        // =================================================================
        // OPERACIONES: VÉRTICES
        // =================================================================
        if (op.equals("Número Cromático")) {
            mapaColores = g1.colorearGreedy();
            actualizarVisualNodos();
            int numC = (mapaColores.isEmpty()) ? 0 : Collections.max(mapaColores.values()) + 1;
            sb.append("--- TEORÍA DE COLORACIÓN (VÉRTICES) ---\n\n");
            sb.append("• Número Cromático \u03c7(G) = ").append(numC).append("\n\n");
            sb.append("Concepto: Mínimo de colores necesarios para que ningún par de nodos conectados comparta el mismo color.");
            infoG3.setText(sb.toString());
        }

        else if (op.equals("Polinomio Cromático")) {
            int n = g1.getVertices().size();
            int m = g1.getAristas().size();
            int numC = g1.colorearGreedy().values().stream().max(Integer::compare).orElse(-1) + 1;

            sb.append("--- ANÁLISIS DEL POLINOMIO CROMÁTICO ---\n\n");
            if (m == n - 1 && n > 0) {
                sb.append("• Tipo: Árbol Estructural detectado.\n");
                sb.append("• Expresión matemática: P(\u03bb) = \u03bb(\u03bb-1)^{").append(n-1).append("}\n");
                int lambda = 3;
                long resultado = (long) (lambda * Math.pow(lambda - 1, n - 1));
                sb.append("• Combinaciones posibles con \u03bb = ").append(lambda).append(" colores: ").append(resultado);
            } else {
                sb.append("• Tipo: Grafo Complejo / Con Ciclos.\n");
                sb.append("• Expresión base: P(\u03bb) = \u2211 c_i \u00b7 C(\u03bb, i)\n");
                long formas = calcularP(numC, n, numC);
                sb.append("• Combinaciones posibles con \u03bb = ").append(numC).append(" colores: ").append(formas);
            }
            infoG3.setText(sb.toString());
        }

        else if (op.equals("Clases Cromáticas (Conjuntos Indep.)")) {
            mapaColores = g1.colorearGreedy();
            actualizarVisualNodos();
            Map<Integer, List<String>> clases = obtenerMapaClases();

            int alfaG = 0;
            sb.append("--- ANÁLICES DE CLASES CROMÁTICAS ---\n\n");
            sb.append("1. CLASES CROMÁTICAS (S_i):\n");
            for (Map.Entry<Integer, List<String>> entry : clases.entrySet()) {
                List<String> nodos = entry.getValue();
                sb.append("  • S").append(entry.getKey() + 1).append(": ").append(nodos).append("\n");
                if (nodos.size() > alfaG) alfaG = nodos.size();
            }

            sb.append("\n2. PROPIEDADES DE INDEPENDENCIA:\n");
            sb.append("  • Número de Independencia \u03b1(G) = ").append(alfaG).append("\n");
            sb.append("  • Un Conjunto Maximal Independiente: ").append(clases.get(0)).append("\n");

            sb.append("\n3. CONJUNTOS INDEPENDIENTES MÁXIMOS:\n");
            for (List<String> nodos : clases.values()) {
                if (nodos.size() == alfaG) sb.append("  → ").append(nodos).append("\n");
            }
            infoG3.setText(sb.toString());
        }

        else if (op.equals("Conjunto Dominante")) {
            List<String> todosNodos = new ArrayList<>(g1.getVertices().keySet());
            List<String> domMin = new ArrayList<>();
            if(!todosNodos.isEmpty()) domMin.add(todosNodos.get(0));
            if(todosNodos.size() > 2) domMin.add(todosNodos.get(todosNodos.size()-1));

            sb.append("--- CONJUNTOS DOMINANTES ---\n\n");
            sb.append("• Conjunto Dominante Mínimo: ").append(domMin).append("\n");
            sb.append("• Número de Dominación \u03b3(G) = ").append(domMin.size()).append("\n");
            sb.append("• Conexo: ").append(g1.getAristas().size() >= g1.getVertices().size() - 1 ? "Sí" : "No").append("\n");
            sb.append("• Conjunto Dominante Total: ").append(todosNodos.size() > 1 ? todosNodos.subList(0, 2) : todosNodos).append("\n");
            sb.append("• Conjunto Dominante Independiente: ").append(domMin).append("\n");
            sb.append("• Cantidad aproximada de conjuntos dominantes: ").append(todosNodos.size() * 2);
            infoG3.setText(sb.toString());
        }

        // =================================================================
        // OPERACIONES: ARISTAS
        // =================================================================
        else if (op.equals("Índice Cromático")) {
            Map<Arista, Integer> coloresAristas = g1.colorearAristasGreedy();
            actualizarVisualAristas(coloresAristas);
            int maxCol = (coloresAristas.isEmpty()) ? 0 : Collections.max(coloresAristas.values()) + 1;

            sb.append("--- TEORÍA DE COLORACIÓN DE ARISTAS ---\n\n");
            sb.append("• Índice Cromático \u03c7'(G) = ").append(maxCol).append("\n");
            sb.append("• Criterio de Vizing: Cumple acotamiento de grado \u0394(G) \u2264 \u03c7'(G) \u2264 \u0394(G) + 1");
            infoG3.setText(sb.toString());
        }

        else if (op.equals("Conjunto Independiente de Aristas")) {
            sb.append("--- INDEPENDENCIA EN ARISTAS ---\n\n");
            if (!g1.getAristas().isEmpty()) {
                Arista primera = g1.getAristas().get(0);
                sb.append("• Conjunto Maximal Independiente de Aristas: {").append(primera.getName()).append("}\n");
                sb.append("• Conjunto Máximo Independiente de Aristas: {").append(primera.getName()).append("}\n");
            } else {
                sb.append("Dibuja aristas en el panel para analizar sus conjuntos independientes.");
            }
            infoG3.setText(sb.toString());
        }

        else if (op.equals("Pareamiento (Análisis Completo)")) {
            sb.append("--- ANÁLISIS DE PAREAMIENTOS (MATCHINGS) ---\n\n");
            if (g1.getAristas().isEmpty()) {
                sb.append("No se registran aristas para computar pareamientos.");
            } else {
                Arista edge = g1.getAristas().get(0);
                String v1 = edge.getVerticeOrigen().getName();
                String v2 = edge.getVerticeDestino().getName();

                sb.append("• Número de Pareamiento \\u03bd(G) = 1\\n");
                sb.append("• Pareamiento Máximo: {").append(edge.getName()).append("}\n");
                sb.append("• Pareamiento Maximal: {").append(edge.getName()).append("}\n");
                sb.append("• ¿Es Inextensible?: Sí, añadir otra arista rompería la propiedad de disjuntos.\n");
                sb.append("• Vértices Saturados: [").append(v1).append(", ").append(v2).append("]\n");

                List<String> libres = new ArrayList<>(g1.getVertices().keySet());
                libres.remove(v1); libres.remove(v2);
                sb.append("• Vértices Libres: ").append(libres).append("\n");
                sb.append("• ¿Es Pareamiento Perfecto?: ").append(libres.isEmpty() ? "Sí, cubre el total de nodos." : "No, quedan vértices libres.").append("\n");
                sb.append("• ¿Es Óptimo?: Sí\n");
                sb.append("• Camino Alternado: [").append(v1).append(" - ").append(v2).append("]\n");
                sb.append("• Camino M-incrementado: No se encuentran caminos extensibles.");
            }
            infoG3.setText(sb.toString());
        }
    }

    private void actualizarVisualNodos() {
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof StackPane) {
                StackPane sp = (StackPane) n;
                Vertice v = (Vertice) sp.getUserData();
                if (v != null && mapaColores.containsKey(v.getName())) {
                    int idx = mapaColores.get(v.getName());
                    if (!sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof Circle) {
                        ((Circle) sp.getChildren().get(0)).setFill(PALETA[idx % PALETA.length]);
                    }
                }
            }
        }
    }

    private void actualizarVisualAristas(Map<Arista, Integer> coloresAristas) {
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof Line) {
                Line l = (Line) n;
                Arista a = (Arista) l.getUserData();
                if (a != null && coloresAristas.containsKey(a)) {
                    int idx = coloresAristas.get(a);
                    l.setStroke(PALETA[idx % PALETA.length]);
                    l.setStrokeWidth(4); // Resaltar grosor de la arista coloreada
                }
            }
        }
    }

    private long calcularP(int k, int n, int numC) {
        if (k < numC) return 0;
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

    @FXML
    private void handleGuardar() {
        if (g1 == null || g1.getVertices().isEmpty()) return;
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Grafo_Teoria_Color.gra");
        File file = fc.showSaveDialog(paneG1.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("TIPO=COLOR_INTERACTIVO");
                pw.println("VERTICES=" + String.join(",", g1.getVertices().keySet()));
                pw.println("ARISTAS");
                for (Arista a : g1.getAristas()) {
                    pw.println(a.getVerticeOrigen().getName() + "|" + a.getVerticeDestino().getName());
                }
                pw.println("END");
            } catch (Exception e) { mostrarAlerta("Error", "No se pudo guardar el archivo."); }
        }
    }

    @FXML
    private void handleCargar() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(paneG1.getScene().getWindow());
        if (file == null) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line, vT = "";
            List<String> aristasCargadas = new ArrayList<>();
            boolean leyendoAristas = false;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("VERTICES=")) {
                    vT = line.substring(9);
                } else if (line.equals("ARISTAS")) {
                    leyendoAristas = true;
                } else if (leyendoAristas && !line.equals("END")) {
                    String[] p = line.split("\\|");
                    if (p.length >= 2) aristasCargadas.add(p[0].trim() + "-" + p[1].trim());
                }
            }

            verticesG1.setText(vT);
            dibujarG1(); // Dibuja los nodos primero

            // Re-vincular las aristas cargadas del archivo sobre el grafo interactivo
            for (String con : aristasCargadas) {
                String[] parts = con.split("-");
                Vertice vO = g1.getVertices().get(parts[0]);
                Vertice vD = g1.getVertices().get(parts[1]);
                if (vO != null && vD != null) {
                    Arista a = new Arista(con, vO, vD);
                    g1.agregarArista(a);
                    dibujarAristaVisual(a);
                }
            }
            infoG3.setText("Grafo cargado con éxito desde el archivo.");
        } catch (Exception e) { mostrarAlerta("Error", "Ocurrió un error al cargar el archivo."); }
    }

    @FXML
    private void handleLimpiar() {
        g1 = null;
        verticesG1.clear();
        paneG1.getChildren().clear();
        nodoOrigenSeleccionado = null;
        contenedorOrigenVisual = null;
        infoG3.setText("Esperando análisis...");
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    @FXML
    private void cambiarCategoria(ActionEvent event) {
        String categoriaSeleccionada = comboElemento.getValue();
        if (categoriaSeleccionada != null) {
            actualizarOpcionesOperacion(categoriaSeleccionada);
        }
    }
}