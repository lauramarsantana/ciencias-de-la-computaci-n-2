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
        operacion.getItems().addAll("Unión", "Intersección",
                                    "Suma Anular", "Complemento",
                                    "Suma", "Fusión de Vértices",
                                    "Adición de Vértice", "Eliminación de Vértice",
                                    "Contracción de Arista", "Adición de Arista",
                                    "Eliminación de Arista", "Producto Cartesiano",
                                    "Producto Tensorial");
        operacion.getSelectionModel().selectFirst();

        // Limpiar el texto inicial para que no se vea la palabra "Label"
        infoG1.setText("");
        infoG2.setText("");
        infoG3.setText("");

        // Configuración para que el texto no desplace los paneles
        configurarLabelInfo(infoG1);
        configurarLabelInfo(infoG2);
        configurarLabelInfo(infoG3);

        operacion.setOnAction(e -> {
            if (g1 != null) dibujarG1();
            if (g2 != null) dibujarG2();
        });
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
            // Obtenemos el valor directamente del ComboBox para que no de error
            String opSeleccionada = operacion.getSelectionModel().getSelectedItem();
            boolean modoVertical = "Producto Cartesiano".equals(opSeleccionada) ||
                    "Producto Tensorial".equals(opSeleccionada);
            actualizarPanel(g1, paneG1, infoG1, modoVertical);
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
            // Obtenemos el valor directamente del ComboBox para que no de error
            String opSeleccionada = operacion.getSelectionModel().getSelectedItem();
            boolean modoVertical = "Producto Cartesiano".equals(opSeleccionada) ||
                    "Producto Tensorial".equals(opSeleccionada);
            actualizarPanel(g2, paneG2, infoG2, modoVertical);
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
        if (op.equals("Adición de Vértice")) {
            Grafo seleccionado = elegirGrafo();
            if (seleccionado != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Adición");
                dialog.setHeaderText("Añadir vértice a " + seleccionado.getNombre());
                dialog.setContentText("Nombre del nuevo vértice:");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent() && !result.get().trim().isEmpty()) {
                    String nuevo = result.get().trim();

                    // CLAVE: Creamos una copia para no alterar el panel 1 o 2
                    g3 = Grafo.copiar(seleccionado);

                    if (!g3.getVertices().containsKey(nuevo)) {
                        g3.añadirVertice(nuevo);
                        g3.setNombre("Adición en " + seleccionado.getNombre());

                        // Mostramos el resultado SOLO en el Panel 3
                        actualizarPanel(g3, paneG3, infoG3);
                    } else {
                        mostrarAlerta("Error", "El vértice '" + nuevo + "' ya existe.");
                    }
                }
            }
            return;
        }
        if (op.equals("Eliminación de Vértice")) {
            Grafo seleccionado = elegirGrafo();
            if (seleccionado != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Eliminación");
                dialog.setHeaderText("Eliminar vértice de " + seleccionado.getNombre());
                dialog.setContentText("Nombre del vértice a eliminar:");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent() && !result.get().trim().isEmpty()) {
                    String aEliminar = result.get().trim();

                    if (seleccionado.getVertices().containsKey(aEliminar)) {
                        // Creamos la copia para el Panel 3
                        g3 = Grafo.copiar(seleccionado);

                        // Ejecutamos la eliminación en la copia
                        g3.eliminarVertice(aEliminar);
                        g3.setNombre("Eliminación en " + seleccionado.getNombre());

                        // Refrescamos el Panel 3
                        actualizarPanel(g3, paneG3, infoG3);
                    } else {
                        mostrarAlerta("Error", "El vértice '" + aEliminar + "' no existe.");
                    }
                }
            }
            return;
        }

        if (op.equals("Contracción de Arista")) {
            Grafo seleccionado = elegirGrafo();
            if (seleccionado != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Contracción");
                dialog.setHeaderText("Contraer arista en " + seleccionado.getNombre());
                dialog.setContentText("Ingrese la arista (ej: 1-3 o 3-1):");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()) {
                    String nombreA = result.get().trim(); // Quitamos espacios

                    g3 = Grafo.contraerArista(seleccionado, nombreA);

                    if (g3 != null) {
                        g3.setNombre("Contracción de " + nombreA);
                        actualizarPanel(g3, paneG3, infoG3);
                    } else {
                        mostrarAlerta("Error", "No se encontró la arista '" + nombreA + "'");
                    }
                }
            }
            return;
        }

        if (op.equals("Adición de Arista")) {
            Grafo seleccionado = elegirGrafo();
            if (seleccionado != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Nueva Arista");
                dialog.setHeaderText("Añadir arista en " + seleccionado.getNombre());
                dialog.setContentText("Ingrese los vértices a conectar (ej: 1,3):");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent() && result.get().contains(",")) {
                    String[] v = result.get().split(",");
                    String n1 = v[0].trim();
                    String n2 = v[1].trim();

                    g3 = Grafo.adicionarArista(seleccionado, n1, n2);

                    if (g3 != null) {
                        g3.setNombre("Arista añadida (" + n1 + "-" + n2 + ")");
                        actualizarPanel(g3, paneG3, infoG3);
                    } else {
                        mostrarAlerta("Error", "Uno o ambos vértices no existen en el grafo.");
                    }
                }
            }
            return;
        }

        if (op.equals("Eliminación de Arista")) {
            Grafo seleccionado = elegirGrafo();
            if (seleccionado != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Eliminar Arista");
                dialog.setHeaderText("Eliminar arista en " + seleccionado.getNombre());
                dialog.setContentText("Ingrese la arista a eliminar (ej: 1-2):");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent() && !result.get().trim().isEmpty()) {
                    String nombreA = result.get().trim();

                    // Realizamos la operación
                    g3 = Grafo.eliminarArista(seleccionado, nombreA);

                    // En esta operación siempre mostramos el resultado en el Panel 3
                    g3.setNombre("Arista " + nombreA + " eliminada");
                    actualizarPanel(g3, paneG3, infoG3);
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
            case "Unión":
                g3 = Grafo.union(g1, g2);
                break;
            case "Suma":
                g3 = Grafo.sumaNormal(g1, g2);
            break;
            case "Suma Anular":
                g3 = Grafo.sumaAnular(g1, g2);
                break;
            case "Intersección":
                g3 = Grafo.interseccion(g1, g2);
                break;
            case "Producto Cartesiano":
                g3 = Grafo.productoCartesiano(g1, g2);
                // n = filas (G1), m = columnas (G2)
                actualizarPanelProducto(g3, paneG3, infoG3, g1.getVertices().size(), g2.getVertices().size());
                return;
            case "Producto Tensorial":
                g3 = Grafo.productoTensorial(g1, g2);
                actualizarPanelProducto(g3, paneG3, infoG3, g1.getVertices().size(), g2.getVertices().size());
                return;
            default: return;
        }

        actualizarPanel(g3, paneG3, infoG3);
    }

    // Versión 1: La que ya usabas (por defecto es circular)
// Esto arreglará automáticamente los 8 errores.
    private void actualizarPanel(Grafo g, Pane pane, Label infoLabel) {
        actualizarPanel(g, pane, infoLabel, false); // 'false' significa circular
    }

    // Versión 2: La que tiene la opción de ser vertical
    private void actualizarPanel(Grafo g, Pane pane, Label infoLabel, boolean esVertical) {
        if (g == null) return;
        pane.getChildren().clear();
        List<Vertice> lista = new ArrayList<>(g.getVertices().values());

        lista.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        if (esVertical) {
            GrafoVisual.reacomodarVertical(pane, lista);
        } else {
            GrafoVisual.reacomodarCircular(pane, lista);
        }

        GrafoVisual.dibujar(g, pane);
        infoLabel.setText(generarInfoTexto(g));
    }

    private void actualizarPanelProducto(Grafo g, Pane pane, Label info, int n, int m) {
        pane.getChildren().clear();
        GrafoVisual.reacomodarMatriz(pane, g, n, m); // Aquí está la magia
        GrafoVisual.dibujar(g, pane);
        info.setText(generarInfoTexto(g));
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