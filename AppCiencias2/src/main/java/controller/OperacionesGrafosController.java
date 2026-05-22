package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import utilities.Arista;
import utilities.Grafo;
import utilities.GrafoVisual;
import utilities.Vertice;

import java.io.*;
import java.util.*;

public class OperacionesGrafosController {

    @FXML private ComboBox<String> operacion;
    @FXML private TextField verticesG1, verticesG2; // Removidos aristasG1 y aristasG2 ya que no se usan en el FXML
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
                "Producto Tensorial", "Composición");
        operacion.getSelectionModel().selectFirst();

        infoG1.setText("");
        infoG2.setText("");
        infoG3.setText("");

        configurarLabelInfo(infoG1);
        configurarLabelInfo(infoG2);
        configurarLabelInfo(infoG3);

        operacion.setOnAction(e -> {
            if (g1 != null) GrafoVisual.dibujarInteractivo(g1, paneG1, infoG1);
            if (g2 != null) GrafoVisual.dibujarInteractivo(g2, paneG2, infoG2);
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
            paneG1.getChildren().clear();
            g1 = new Grafo("Grafo 1");

            String texto = verticesG1.getText().trim();
            if (texto.isEmpty() || !texto.matches("\\d+")) {
                mostrarAlerta("Formato Incorrecto", "Por favor, ingresa una cantidad numérica de vértices (ej: 5).");
                return;
            }

            int cantidad = Integer.parseInt(texto);
            String formato = pedirFormatoNombres();
            if (formato == null) return; // Si cancela la ventana, no hace nada

            if (formato.equals("Letras (A, B, C...)")) {
                for (int i = 0; i < cantidad; i++) {
                    // Genera A, B, C... usando el código ASCII (65 es 'A')
                    String nombre = String.valueOf((char) (65 + i));
                    g1.agregarVertice(new Vertice(nombre, 0, 0));
                }
            } else {
                for (int i = 1; i <= cantidad; i++) {
                    g1.agregarVertice(new Vertice(String.valueOf(i), 0, 0));
                }
            }

            List<Vertice> lista = new ArrayList<>(g1.getVertices().values());
            GrafoVisual.reacomodarCircular(paneG1, lista);

            // Llamamos al dibujador interactivo
            GrafoVisual.dibujarInteractivo(g1, paneG1, infoG1);
            infoG1.setText(generarInfoTexto(g1));
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar el Grafo 1");
        }
    }

    @FXML
    private void dibujarG2() {
        try {
            paneG2.getChildren().clear();
            g2 = new Grafo("Grafo 2");

            String texto = verticesG2.getText().trim();
            if (texto.isEmpty() || !texto.matches("\\d+")) {
                mostrarAlerta("Formato Incorrecto", "Por favor, ingresa una cantidad numérica de vértices (ej: 5).");
                return;
            }

            int cantidad = Integer.parseInt(texto);
            String formato = pedirFormatoNombres();
            if (formato == null) return;

            if (formato.equals("Letras (A, B, C...)")) {
                for (int i = 0; i < cantidad; i++) {
                    String nombre = String.valueOf((char) (65 + i));
                    g2.agregarVertice(new Vertice(nombre, 0, 0));
                }
            } else {
                for (int i = 1; i <= cantidad; i++) {
                    g2.agregarVertice(new Vertice(String.valueOf(i), 0, 0));
                }
            }

            List<Vertice> lista = new ArrayList<>(g2.getVertices().values());
            GrafoVisual.reacomodarCircular(paneG2, lista);

            GrafoVisual.dibujarInteractivo(g2, paneG2, infoG2);
            infoG2.setText(generarInfoTexto(g2));
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar el Grafo 2");
        }
    }

    // Método auxiliar para la ventana emergente de selección
    private String pedirFormatoNombres() {
        List<String> opciones = Arrays.asList("Números (1, 2, 3...)", "Letras (A, B, C...)");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Números (1, 2, 3...)", opciones);
        dialog.setTitle("Formato de Vértices");
        dialog.setHeaderText("Configuración del Grafo");
        dialog.setContentText("¿Cómo deseas nombrar los vértices?");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
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
                        mostrarAlerta("Vértice no encontrado", "Uno o ambos vértices no existen.");
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
                    g3 = Grafo.copiar(seleccionado);

                    if (!g3.getVertices().containsKey(nuevo)) {
                        g3.añadirVertice(nuevo);
                        g3.setNombre("Adición en " + seleccionado.getNombre());
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
                        g3 = Grafo.copiar(seleccionado);
                        g3.eliminarVertice(aEliminar);
                        g3.setNombre("Eliminación en " + seleccionado.getNombre());
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
                dialog.setContentText("Ingrese la arista (ej: 1-3):");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()) {
                    String nombreA = result.get().trim();
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
                    g3 = Grafo.eliminarArista(seleccionado, nombreA);
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
            case "Unión": g3 = Grafo.union(g1, g2); break;
            case "Suma": g3 = Grafo.sumaNormal(g1, g2); break;
            case "Suma Anular": g3 = Grafo.sumaAnular(g1, g2); break;
            case "Intersección": g3 = Grafo.interseccion(g1, g2); break;
            case "Producto Cartesiano":
                g3 = Grafo.productoCartesiano(g1, g2);
                actualizarPanelProducto(g3, paneG3, infoG3, g1.getVertices().size(), g2.getVertices().size());
                return;
            case "Producto Tensorial":
                g3 = Grafo.productoTensorial(g1, g2);
                actualizarPanelProducto(g3, paneG3, infoG3, g1.getVertices().size(), g2.getVertices().size());
                return;
            case "Composición":
                g3 = Grafo.composicion(g1, g2);
                actualizarPanelProducto(g3, paneG3, infoG3, g1.getVertices().size(), g2.getVertices().size());
                return;
            default: return;
        }

        actualizarPanel(g3, paneG3, infoG3);
    }

    private void actualizarPanel(Grafo g, Pane pane, Label infoLabel) {
        actualizarPanel(g, pane, infoLabel, false);
    }

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

        // 1. Generamos el texto formal del Grafo (S = {...}, A = {...})
        String textoFormal = generarInfoTexto(g);
        infoLabel.setText(textoFormal);

        // 2. Le pasamos el control a GrafoVisual pasándole la base del texto formal
        // para que cuando el usuario conecte aristas, el label muestre AMBAS cosas.
        GrafoVisual.dibujarInteractivo(g, pane, infoLabel);
    }

    private void actualizarPanelProducto(Grafo g, Pane pane, Label info, int n, int m) {
        pane.getChildren().clear();
        GrafoVisual.reacomodarMatriz(pane, g, n, m);
        GrafoVisual.dibujarInteractivo(g, pane, info);
        info.setText(generarInfoTexto(g));
    }

    public String generarInfoTexto(Grafo g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Grafo:\n");

        // Construir conjunto S
        sb.append("S = {");
        List<String> nombres = new ArrayList<>(g.getVertices().keySet());
        sb.append(String.join(", ", nombres));
        sb.append("}\n");

        // Construir conjunto A
        sb.append("A = {");
        List<String> aristasStr = new ArrayList<>();
        for (Arista a : g.getAristas()) {
            aristasStr.add(a.getVerticeOrigen().getName() + "-" + a.getVerticeDestino().getName());
        }
        sb.append(String.join(", ", aristasStr));
        sb.append("}");

        return sb.toString();
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

    private void parsearVertices(String texto, Grafo grafo) {
        grafo.getVertices().clear();
        if (texto == null || texto.trim().isEmpty()) return;
        String[] nombres = texto.split(",");
        for (String n : nombres) {
            String nombre = n.trim();
            if (!nombre.isEmpty()) grafo.agregarVertice(new Vertice(nombre, 0, 0));
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

    @FXML
    private void handleGuardar() {
        boolean tieneG1 = (g1 != null && !g1.getVertices().isEmpty());
        boolean tieneG2 = (g2 != null && !g2.getVertices().isEmpty());
        boolean tieneG3 = (g3 != null && !g3.getVertices().isEmpty());

        if (!tieneG1 && !tieneG2 && !tieneG3) {
            mostrarAlerta("Error", "No hay ningún grafo en pantalla para guardar.");
            return;
        }

        if (tieneG1 && !tieneG2 && !tieneG3) { guardarLogica(g1, "Grafo1"); return; }
        if (!tieneG1 && tieneG2 && !tieneG3) { guardarLogica(g2, "Grafo2"); return; }
        if (!tieneG1 && !tieneG2 && tieneG3) { guardarLogica(g3, "Resultado"); return; }

        List<String> opciones = new ArrayList<>();
        if (tieneG1) opciones.add("Grafo 1");
        if (tieneG2) opciones.add("Grafo 2");
        if (tieneG3) opciones.add("Resultado (G3)");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(opciones.get(opciones.size() - 1), opciones);
        dialog.setTitle("Seleccionar Grafo");
        dialog.setHeaderText("Varios grafos detectados");
        dialog.setContentText("¿Cuál de estos deseas guardar?");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(seleccion -> {
            if (seleccion.equals("Grafo 1")) guardarLogica(g1, "G1");
            else if (seleccion.equals("Grafo 2")) guardarLogica(g2, "G2");
            else guardarLogica(g3, "G3");
        });
    }

    private void guardarLogica(Grafo g, String nombreSugerido) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(nombreSugerido + ".gra");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Grafo (*.gra)", "*.gra"));
        File file = fc.showSaveDialog(paneG3.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                bw.write("TIPO=GRAFO_UNIFICADO"); bw.newLine();
                bw.write("VERTICES=" + String.join(",", g.getVertices().keySet())); bw.newLine();
                bw.write("ARISTAS"); bw.newLine();

                for (Arista a : g.getAristas()) {
                    bw.write(a.getVerticeOrigen().getName() + "|" + a.getVerticeDestino().getName() + "|1");
                    bw.newLine();
                }
                bw.write("END");
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al escribir el archivo.");
            }
        }
    }

    @FXML
    private void handleCargar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar Grafo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de Grafo (*.gra)", "*.gra"));
        File file = fc.showOpenDialog(paneG3.getScene().getWindow());

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
                    String vData = line.substring(9);
                    nuevosVertices = Arrays.asList(vData.split(","));
                } else if (line.equals("ARISTAS")) {
                    leyendoAristas = true;
                } else if (line.equals("END")) {
                    break;
                } else if (leyendoAristas) {
                    String[] partes = line.split("\\|");
                    if (partes.length >= 2) {
                        nuevasAristas.add(new String[]{partes[0].trim(), partes[1].trim()});
                    }
                }
            }

            preguntarDondeCargar(nuevosVertices, nuevasAristas);

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar el archivo: " + e.getMessage());
        }
    }

    private void preguntarDondeCargar(List<String> vList, List<String[]> aList) {
        List<String> opciones = Arrays.asList("Grafo 1", "Grafo 2");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Grafo 1", opciones);
        dialog.setTitle("Destino de Carga");
        dialog.setHeaderText("Grafo leído correctamente");
        dialog.setContentText("¿En qué panel deseas cargar este grafo?");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(seleccion -> {
            Grafo nuevoG = new Grafo("Cargado");

            for (String vNom : vList) {
                nuevoG.agregarVertice(new Vertice(vNom.trim(), 0, 0));
            }

            for (String[] ar : aList) {
                Vertice or = nuevoG.getVertices().get(ar[0]);
                Vertice des = nuevoG.getVertices().get(ar[1]);
                if (or != null && des != null) {
                    nuevoG.agregarArista(new Arista(ar[0] + "-" + ar[1], or, des));
                }
            }

            if (seleccion.equals("Grafo 1")) {
                this.g1 = nuevoG;
                actualizarPanel(g1, paneG1, infoG1, false);
                verticesG1.setText(String.valueOf(vList.size())); // Seteamos el número para la consistencia interactiva
            } else {
                this.g2 = nuevoG;
                actualizarPanel(g2, paneG2, infoG2, false);
                verticesG2.setText(String.valueOf(vList.size()));
            }
        });
    }

    @FXML
    private void handleLimpiar() {
        boolean tieneG1 = (g1 != null && !g1.getVertices().isEmpty()) || !verticesG1.getText().isEmpty();
        boolean tieneG2 = (g2 != null && !g2.getVertices().isEmpty()) || !verticesG2.getText().isEmpty();
        boolean tieneG3 = (g3 != null && !g3.getVertices().isEmpty());

        if (!tieneG1 && !tieneG2 && !tieneG3) {
            mostrarAlerta("Información", "No hay nada que limpiar.");
            return;
        }

        List<String> opciones = new ArrayList<>();
        if (tieneG1) opciones.add("Grafo 1");
        if (tieneG2) opciones.add("Grafo 2");
        if (tieneG3) opciones.add("Resultado (G3)");
        opciones.add("Limpiar Todo");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Limpiar Todo", opciones);
        dialog.setTitle("Limpiar Panel");
        dialog.setHeaderText("Varios paneles con datos");
        dialog.setContentText("¿Qué deseas limpiar?");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(seleccion -> {
            switch (seleccion) {
                case "Grafo 1": limpiarG1(); break;
                case "Grafo 2": limpiarG2(); break;
                case "Resultado (G3)": limpiarG3(); break;
                case "Limpiar Todo":
                    limpiarG1();
                    limpiarG2();
                    limpiarG3();
                    break;
            }
        });
    }

    private void limpiarG1() {
        g1 = null;
        verticesG1.clear();
        paneG1.getChildren().clear();
        infoG1.setText("");
    }

    private void limpiarG2() {
        g2 = null;
        verticesG2.clear();
        paneG2.getChildren().clear();
        infoG2.setText("");
    }

    private void limpiarG3() {
        g3 = null;
        paneG3.getChildren().clear();
        infoG3.setText("");
    }
}