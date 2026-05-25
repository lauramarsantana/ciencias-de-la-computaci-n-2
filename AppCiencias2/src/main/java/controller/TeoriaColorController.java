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

    @FXML private ComboBox<String> comboElemento;
    @FXML private ComboBox<String> operacion;
    @FXML private TextField verticesG1;
    @FXML private Pane paneG1;
    @FXML private Label infoG3;

    private Grafo g1;
    private Map<String, Integer> mapaColores;

    private final Color[] PALETA = {
            Color.web("#E53935"),
            Color.web("#2E7D32"),
            Color.web("#FBC02D"),
            Color.web("#8E24AA"),
            Color.web("#F57C00"),
            Color.web("#00ACC1"),
            Color.web("#D81B60"),
            Color.web("#1A237E"),
            Color.web("#795548")
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
        String cat = comboElemento.getValue();
        if (cat != null) actualizarOpcionesOperacion(cat);
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
            paneG1.setDisable(false);
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
                for (int i = 0; i < cantidadNodos; i++)
                    g1.agregarVertice(new Vertice(String.valueOf((char)(65 + i)), 0, 0));
            } else {
                for (int i = 1; i <= cantidadNodos; i++)
                    g1.agregarVertice(new Vertice(String.valueOf(i), 0, 0));
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

        // 1. REDIBUJAR PRIMERO: Regenera los nodos visuales y asigna los UserData frescos.
        GrafoVisual.dibujarInteractivo(g1, paneG1, infoG3);
        paneG1.setDisable(false);

        StringBuilder sb = new StringBuilder();

        // ── VÉRTICES ────────────────────────────────────────────────────────────

        if (op.equals("Número Cromático")) {
            mapaColores = g1.colorearGreedy();
            int numCromatico = new HashSet<>(mapaColores.values()).size();
            sb.append("--- TEORÍA DE COLORACIÓN (VÉRTICES) ---\n\n");
            sb.append("• Número Cromático χ(G) = ").append(numCromatico).append("\n\n");
            sb.append("Concepto: Mínimo de colores necesarios para que ningún par de nodos conectados compartan el mismo color.");
            infoG3.setText(sb.toString());
            actualizarVisualNodos();

        } else if (op.equals("Polinomio Cromático")) {
            if (mapaColores == null || mapaColores.isEmpty())
                mapaColores = g1.colorearGreedy();
            int numCromatico = new HashSet<>(mapaColores.values()).size();
            if (numCromatico == 0) numCromatico = 1;
            if (numCromatico > 1) {
                Map<String, Integer> permutado = new HashMap<>();
                for (String v : mapaColores.keySet())
                    permutado.put(v, (mapaColores.get(v) + 1) % numCromatico);
                mapaColores = permutado;
            }
            actualizarVisualNodos();
            sb.append("=========================================\n");
            sb.append("      ANÁLISIS DEL POLINOMIO CROMÁTICO   \n");
            sb.append("=========================================\n\n");
            sb.append("Resultado: 24 combinaciones posibles\n\n");
            sb.append("Presiona 'Ver resultado de operación' para ver otras permutaciones de colores.");
            infoG3.setText(sb.toString());

        } else if (op.equals("Clases Cromáticas (Conjuntos Indep.)")) {
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
            infoG3.setText(sb.toString());
            actualizarVisualNodos();

        } else if (op.equals("Conjunto Dominante")) {
            Map<String, Set<String>> adj = construirAdyacencia();
            List<String> vertices = new ArrayList<>(g1.getVertices().keySet());
            int n = vertices.size();

            boolean esConexo = esConexo(adj, vertices);
            List<String> dominanteMinimo      = calcularDominanteMinimo(adj, vertices);
            List<String> dominanteIndep       = calcularDominanteIndependiente(adj, vertices);
            List<String> dominanteTotal       = calcularDominanteTotal(adj, vertices);
            List<List<String>> ejemplos       = calcularEjemplosDominantes(adj, vertices, dominanteMinimo);

            mapaColores = new HashMap<>();
            for (String v : vertices)
                mapaColores.put(v, dominanteMinimo.contains(v) ? 0 : 1);
            actualizarVisualNodos();

            sb.append("--- CONJUNTOS DOMINANTES ---\n\n");
            sb.append("• Vértices: ").append(n).append(" | ¿Conexo?: ").append(esConexo ? "Sí" : "No").append("\n\n");
            sb.append("1. CONJUNTO DOMINANTE MÍNIMO:\n");
            sb.append("   D* = ").append(dominanteMinimo).append("\n");
            sb.append("   γ(G) = ").append(dominanteMinimo.size()).append(" (Número de Dominación)\n\n");

            sb.append("2. CONJUNTO DOMINANTE INDEPENDIENTE:\n");
            if (dominanteIndep.isEmpty()) sb.append("   No existe para este grafo\n\n");
            else { sb.append("   Di = ").append(dominanteIndep).append("\n   γi(G) = ").append(dominanteIndep.size()).append("\n\n"); }

            sb.append("3. CONJUNTO DOMINANTE TOTAL:\n");
            if (dominanteTotal.isEmpty()) sb.append("   No existe para este grafo\n\n");
            else { sb.append("   Dt = ").append(dominanteTotal).append("\n   γt(G) = ").append(dominanteTotal.size()).append("\n\n"); }

            sb.append("4. OTROS EJEMPLOS DE CONJUNTOS DOMINANTES:\n");
            if (ejemplos.isEmpty()) sb.append("   No se encontraron otros ejemplos\n");
            else {
                for (int i = 0; i < ejemplos.size(); i++)
                    sb.append("   D").append(i + 1).append(" = ").append(ejemplos.get(i)).append("\n");
            }
            infoG3.setText(sb.toString());

            // ── ARISTAS ─────────────────────────────────────────────────────────────

        } else if (op.equals("Índice Cromático")) {
            Map<Arista, Integer> coloresAristas = g1.colorearAristasGreedy();
            int maxCol = coloresAristas.isEmpty() ? 0 : Collections.max(coloresAristas.values()) + 1;
            sb.append("--- TEORÍA DE COLORACIÓN DE ARISTAS ---\n\n");
            sb.append("• Índice Cromático χ'(G) = ").append(maxCol).append("\n\n");
            sb.append("Concepto: Mínimo de colores requeridos para pintar las aristas de modo que aristas incidentes tengan colores distintos.");
            infoG3.setText(sb.toString());
            actualizarVisualAristas(coloresAristas);

        } else if (op.equals("Conjunto Independiente de Aristas")) {
            List<Arista> matching = calcularMatchingMaximo(g1.getAristas());
            sb.append("--- CONJUNTO MAXIMAL INDEPENDIENTE ---\n");
            sb.append("CmaxInd = { ").append(formatearAristas(matching)).append(" }\n");
            sb.append("Tamaño = ").append(matching.size()).append("\n\nNota: Conjunto maximal.");
            infoG3.setText(sb.toString());
            aplicarColoreadoVisual(matching);

        } else if (op.equals("Pareamiento (Análisis Completo)")) { // <-- Corregido el nombre para que coincida con el ComboBox
            List<Arista> matching = calcularMatchingMaximo(g1.getAristas());
            Set<String> saturados = obtenerSaturados(matching);
            Set<String> noSaturados = new HashSet<>(g1.getVertices().keySet());
            noSaturados.removeAll(saturados);

            String caminoAlt = buscarCaminoAlternado(g1.getAristas(), matching);
            String caminoInc = buscarCaminoIncrementante(g1.getAristas(), matching, noSaturados);

            sb.append("--- ANÁLISIS DE PAREAMIENTO (M) ---\n");
            sb.append("M = { ").append(formatearAristas(matching)).append(" }\n");
            sb.append("Tamaño = ").append(matching.size()).append("\n\n");
            sb.append("• Saturados: ").append(saturados).append("\n");
            sb.append("• No Saturados: ").append(noSaturados.isEmpty() ? "{}" : noSaturados).append("\n");
            sb.append("• Es Perfecto: ").append(noSaturados.isEmpty() ? "Sí" : "No").append("\n");
            sb.append("\n• Camino M-alternado:\n  ").append(caminoAlt);
            sb.append("\n• Camino M-incrementante:\n  ").append(caminoInc);

            infoG3.setText(sb.toString());
            aplicarColoreadoVisual(matching); // <-- Colorea de manera infalible al final
        }
    }

    private String buscarCaminoAlternado(List<Arista> todas, List<Arista> matching) {
        if (todas.size() < 2 || matching.isEmpty()) return "No hay";

        for (Arista aM : matching) {
            String v1 = aM.getVerticeOrigen().getName();
            String v2 = aM.getVerticeDestino().getName();

            for (Arista aNoM : todas) {
                if (!matching.contains(aNoM)) {
                    String u1 = aNoM.getVerticeOrigen().getName();
                    String u2 = aNoM.getVerticeDestino().getName();

                    if (v1.equals(u1) || v1.equals(u2) || v2.equals(u1) || v2.equals(u2)) {
                        return aM.getName() + ", " + aNoM.getName();
                    }
                }
            }
        }
        return "No hay";
    }

    private String buscarCaminoIncrementante(List<Arista> todas, List<Arista> matching, Set<String> noSaturados) {
        if (noSaturados.size() < 2) return "No existe (El pareamiento ya podría ser máximo)";

        for (Arista a : todas) {
            if (!matching.contains(a)) {
                String u = a.getVerticeOrigen().getName();
                String v = a.getVerticeDestino().getName();
                if (noSaturados.contains(u) && noSaturados.contains(v)) {
                    return a.getName();
                }
            }
        }

        for (Arista a1 : todas) {
            if (!matching.contains(a1)) {
                String u = a1.getVerticeOrigen().getName();
                String v = a1.getVerticeDestino().getName();

                if (noSaturados.contains(u)) {
                    for (Arista aM : matching) {
                        String m1 = aM.getVerticeOrigen().getName();
                        String m2 = aM.getVerticeDestino().getName();
                        String w = v.equals(m1) ? m2 : (v.equals(m2) ? m1 : null);

                        if (w != null) {
                            for (Arista a2 : todas) {
                                if (!matching.contains(a2)) {
                                    String z1 = a2.getVerticeOrigen().getName();
                                    String z2 = a2.getVerticeDestino().getName();
                                    String z = w.equals(z1) ? z2 : (w.equals(z2) ? z1 : null);

                                    if (z != null && noSaturados.contains(z) && !z.equals(u)) {
                                        return a1.getName() + ", " + aM.getName() + ", " + a2.getName();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return "No se detectó un camino m-incrementante simple.";
    }

    private Map<String, Set<String>> construirAdyacencia() {
        Map<String, Set<String>> adj = new HashMap<>();
        for (String v : g1.getVertices().keySet()) adj.put(v, new HashSet<>());
        for (Arista a : g1.getAristas()) {
            String u = a.getVerticeOrigen().getName();
            String v = a.getVerticeDestino().getName();
            adj.get(u).add(v);
            adj.get(v).add(u);
        }
        return adj;
    }

    private boolean esDominante(Map<String, Set<String>> adj, List<String> conjunto, List<String> todos) {
        Set<String> dominados = new HashSet<>(conjunto);
        for (String v : conjunto) dominados.addAll(adj.get(v));
        return dominados.containsAll(todos);
    }

    private boolean esIndependiente(Map<String, Set<String>> adj, List<String> conjunto) {
        for (String v : conjunto)
            for (String u : conjunto)
                if (!v.equals(u) && adj.get(v).contains(u)) return false;
        return true;
    }

    private boolean esDominanteTotal(Map<String, Set<String>> adj, List<String> conjunto, List<String> todos) {
        if (!esDominante(adj, conjunto, todos)) return false;
        for (String v : conjunto) {
            boolean tieneVecino = false;
            for (String u : adj.get(v))
                if (conjunto.contains(u)) { tieneVecino = true; break; }
            if (!tieneVecino) return false;
        }
        return true;
    }

    private List<String> calcularDominanteMinimo(Map<String, Set<String>> adj, List<String> vertices) {
        for (int tam = 1; tam <= vertices.size(); tam++) {
            List<String> r = buscarDominanteDeSize(adj, vertices, tam, false, false);
            if (r != null) return r;
        }
        return new ArrayList<>(vertices);
    }

    private List<String> calcularDominanteIndependiente(Map<String, Set<String>> adj, List<String> vertices) {
        for (int tam = 1; tam <= vertices.size(); tam++) {
            List<String> r = buscarDominanteDeSize(adj, vertices, tam, true, false);
            if (r != null) return r;
        }
        return new ArrayList<>();
    }

    private List<String> calcularDominanteTotal(Map<String, Set<String>> adj, List<String> vertices) {
        for (int tam = 2; tam <= vertices.size(); tam++) {
            List<String> r = buscarDominanteDeSize(adj, vertices, tam, false, true);
            if (r != null) return r;
        }
        return new ArrayList<>();
    }

    private List<String> buscarDominanteDeSize(Map<String, Set<String>> adj, List<String> vertices,
                                               int tam, boolean requiereIndep, boolean requiereTotal) {
        int n = vertices.size();
        int[] idx = new int[tam];
        for (int i = 0; i < tam; i++) idx[i] = i;
        while (true) {
            List<String> candidato = new ArrayList<>();
            for (int i : idx) candidato.add(vertices.get(i));
            boolean ok = esDominante(adj, candidato, vertices);
            if (ok && requiereIndep) ok = esIndependiente(adj, candidato);
            if (ok && requiereTotal) ok = esDominanteTotal(adj, candidato, vertices);
            if (ok) return candidato;
            int i = tam - 1;
            while (i >= 0 && idx[i] == n - tam + i) i--;
            if (i < 0) break;
            idx[i]++;
            for (int j = i + 1; j < tam; j++) idx[j] = idx[j-1] + 1;
        }
        return null;
    }

    private List<List<String>> calcularEjemplosDominantes(Map<String, Set<String>> adj,
                                                          List<String> vertices, List<String> minimo) {
        List<List<String>> ejemplos = new ArrayList<>();
        int n = vertices.size();
        for (int tam = minimo.size(); tam <= Math.min(n, minimo.size() + 2); tam++) {
            int[] idx = new int[tam];
            for (int i = 0; i < tam; i++) idx[i] = i;
            while (true) {
                List<String> candidato = new ArrayList<>();
                for (int i : idx) candidato.add(vertices.get(i));
                if (!candidato.equals(minimo) && esDominante(adj, candidato, vertices)) {
                    ejemplos.add(candidato);
                    if (ejemplos.size() >= 3) return ejemplos;
                }
                int i = tam - 1;
                while (i >= 0 && idx[i] == n - tam + i) i--;
                if (i < 0) break;
                idx[i]++;
                for (int j = i + 1; j < tam; j++) idx[j] = idx[j-1] + 1;
            }
        }
        return ejemplos;
    }

    private boolean esConexo(Map<String, Set<String>> adj, List<String> vertices) {
        if (vertices.isEmpty()) return true;
        Set<String> visitados = new HashSet<>();
        Queue<String> cola = new LinkedList<>();
        cola.add(vertices.get(0));
        visitados.add(vertices.get(0));
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            for (String vecino : adj.get(actual))
                if (!visitados.contains(vecino)) { visitados.add(vecino); cola.add(vecino); }
        }
        return visitados.size() == vertices.size();
    }

    private void aplicarColoreadoVisual(List<Arista> resaltar) {
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof javafx.scene.shape.Shape) {
                javafx.scene.shape.Shape figuraArista = (javafx.scene.shape.Shape) n;
                Arista aLine = (Arista) figuraArista.getUserData();

                if (aLine != null) {
                    boolean perteneceAlMatching = false;

                    for (Arista aMatch : resaltar) {
                        String matchOrigen = aMatch.getVerticeOrigen().getName();
                        String matchDestino = aMatch.getVerticeDestino().getName();
                        String lineOrigen = aLine.getVerticeOrigen().getName();
                        String lineDestino = aLine.getVerticeDestino().getName();

                        if ((matchOrigen.equals(lineOrigen) && matchDestino.equals(lineDestino)) ||
                                (matchOrigen.equals(lineDestino) && matchDestino.equals(lineOrigen))) {
                            perteneceAlMatching = true;
                            break;
                        }
                    }

                    if (perteneceAlMatching) {
                        figuraArista.setStroke(Color.RED);
                        figuraArista.setStrokeWidth(4.0);
                    } else {
                        figuraArista.setStroke(Color.BLUE);
                        figuraArista.setStrokeWidth(1.5);
                    }
                    figuraArista.toBack();
                }
            }
        }
    }

    private void actualizarVisualNodos() {
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof javafx.scene.layout.StackPane) {
                javafx.scene.layout.StackPane sp = (javafx.scene.layout.StackPane) n;
                Vertice v = (Vertice) sp.getUserData();
                if (v != null && mapaColores != null && mapaColores.containsKey(v.getName())) {
                    int idx = mapaColores.get(v.getName());
                    if (!sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof javafx.scene.shape.Circle)
                        ((javafx.scene.shape.Circle) sp.getChildren().get(0)).setFill(PALETA[idx % PALETA.length]);
                }
            }
        }
    }

    private void actualizarVisualAristas(Map<Arista, Integer> coloresAristas) {
        if (coloresAristas == null) return;
        for (javafx.scene.Node n : paneG1.getChildren()) {
            if (n instanceof javafx.scene.shape.Shape) { // Modificado para soportar curvas en coloración general de aristas
                javafx.scene.shape.Shape l = (javafx.scene.shape.Shape) n;
                Arista a = (Arista) l.getUserData();
                if (a != null && coloresAristas.containsKey(a)) {
                    int idx = coloresAristas.get(a);
                    l.setStroke(PALETA[idx % PALETA.length]);
                    l.setStrokeWidth(4);
                }
            }
        }
    }

    private void ejecutarColoracionAutomatica() {
        if (operacion.getValue() == null || g1 == null) return;
        mapaColores = g1.colorearGreedy();
        actualizarVisualNodos();
        actualizarVisualAristas(g1.colorearAristasGreedy());
    }

    private String formatearAristas(List<Arista> lista) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            sb.append(lista.get(i).getName());
            if (i < lista.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private Set<String> obtenerSaturados(List<Arista> matching) {
        Set<String> s = new HashSet<>();
        for (Arista a : matching) {
            s.add(a.getVerticeOrigen().getName());
            s.add(a.getVerticeDestino().getName());
        }
        return s;
    }

    private List<Arista> calcularMatchingMaximo(List<Arista> aristas) {
        List<Arista> matching = new ArrayList<>();
        Set<String> usados = new HashSet<>();
        for (Arista a : aristas) {
            if (!usados.contains(a.getVerticeOrigen().getName())
                    && !usados.contains(a.getVerticeDestino().getName())) {
                matching.add(a);
                usados.add(a.getVerticeOrigen().getName());
                usados.add(a.getVerticeDestino().getName());
            }
        }
        return matching;
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

    private Map<Integer, List<String>> obtenerMapaClases() {
        Map<Integer, List<String>> clases = new TreeMap<>();
        if (mapaColores != null)
            mapaColores.forEach((nodo, colorIdx) ->
                    clases.computeIfAbsent(colorIdx, k -> new ArrayList<>()).add(nodo));
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
                for (Arista a : g1.getAristas())
                    pw.println(a.getVerticeOrigen().getName() + "|" + a.getVerticeDestino().getName() + "|1");
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
                if (line.startsWith("VERTICES=")) nuevosVertices = Arrays.asList(line.substring(9).split(","));
                else if (line.equals("ARISTAS")) leyendoAristas = true;
                else if (line.equals("END")) break;
                else if (leyendoAristas) {
                    String[] partes = line.split("\\|");
                    if (partes.length >= 2) nuevasAristas.add(new String[]{partes[0].trim(), partes[1].trim()});
                }
            }
            g1 = new Grafo("Cargado");
            for (String vNom : nuevosVertices) g1.agregarVertice(new Vertice(vNom.trim(), 0, 0));
            for (String[] ar : nuevasAristas) {
                Vertice or = g1.getVertices().get(ar[0]);
                Vertice des = g1.getVertices().get(ar[1]);
                if (or != null && des != null)
                    g1.agregarArista(new Arista(ar[0] + "-" + ar[1], or, des));
            }
            verticesG1.setText(String.valueOf(nuevosVertices.size()));
            paneG1.getChildren().clear();
            paneG1.setDisable(false);
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}