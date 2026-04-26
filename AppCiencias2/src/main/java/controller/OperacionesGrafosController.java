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
                                    "Producto Tensorial", "Composición");
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
                    "Producto Tensorial".equals(opSeleccionada) ||
                    "Composición".equals(opSeleccionada);
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
                    "Producto Tensorial".equals(opSeleccionada) ||
                    "Composición".equals(opSeleccionada);
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
            case "Composición":
                g3 =Grafo.composicion(g1,g2);
                actualizarPanelProducto(g3, paneG3, infoG3, g1.getVertices().size(), g2.getVertices().size());
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

    @FXML
    private void handleGuardar() {
        // 1. Verificar cuáles grafos existen (no son nulos)
        boolean tieneG1 = (g1 != null && !g1.getVertices().isEmpty());
        boolean tieneG2 = (g2 != null && !g2.getVertices().isEmpty());
        boolean tieneG3 = (g3 != null && !g3.getVertices().isEmpty());

        // Caso A: Todo vacío
        if (!tieneG1 && !tieneG2 && !tieneG3) {
            mostrarAlerta("Error", "No hay ningún grafo en pantalla para guardar.");
            return;
        }

        // Caso B: Solo hay uno lleno (Acción directa)
        if (tieneG1 && !tieneG2 && !tieneG3) { guardarLogica(g1, "Grafo1"); return; }
        if (!tieneG1 && tieneG2 && !tieneG3) { guardarLogica(g2, "Grafo2"); return; }
        if (!tieneG1 && !tieneG2 && tieneG3) { guardarLogica(g3, "Resultado"); return; }

        // Caso C: Hay más de uno lleno (Preguntar al usuario)
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
                    // Formato: origen|destino|peso(1)
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
                    // Usamos el separador unificado pipe |
                    String[] partes = line.split("\\|");
                    if (partes.length >= 2) {
                        // partes[0]=origen, partes[1]=destino, partes[2]=peso (que ignoramos)
                        nuevasAristas.add(new String[]{partes[0].trim(), partes[1].trim()});
                    }
                }
            }

            // Una vez leído el archivo, preguntamos dónde cargarlo
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

            // 1. Crear vértices
            for (String vNom : vList) {
                nuevoG.agregarVertice(new Vertice(vNom.trim(), 0, 0));
            }

            // 2. Crear aristas
            for (String[] ar : aList) {
                Vertice or = nuevoG.getVertices().get(ar[0]);
                Vertice des = nuevoG.getVertices().get(ar[1]);
                if (or != null && des != null) {
                    nuevoG.agregarArista(new Arista(ar[0] + "-" + ar[1], or, des));
                }
            }

            // 3. Asignar y Dibujar
            if (seleccion.equals("Grafo 1")) {
                this.g1 = nuevoG;
                actualizarPanel(g1, paneG1, infoG1, false); // o true si prefieres vertical
                verticesG1.setText(String.join(",", vList)); // Opcional: llenar el TextField
            } else {
                this.g2 = nuevoG;
                actualizarPanel(g2, paneG2, infoG2, false);
                verticesG2.setText(String.join(",", vList));
            }

            // 3. Preparar el string de aristas para el TextField (ej: "a-b,c-d")
            List<String> aristasTexto = new ArrayList<>();
            for (String[] ar : aList) {
                aristasTexto.add(ar[0] + "-" + ar[1]);
            }
            String stringAristas = String.join(",", aristasTexto);

            // 4. Asignar, Dibujar y LLENAR LOS TEXTFIELDS
            if (seleccion.equals("Grafo 1")) {
                this.g1 = nuevoG;
                actualizarPanel(g1, paneG1, infoG1, false);
                verticesG1.setText(String.join(",", vList));
                aristasG1.setText(stringAristas); // <--- ESTO FALTABA
            } else {
                this.g2 = nuevoG;
                actualizarPanel(g2, paneG2, infoG2, false);
                verticesG2.setText(String.join(",", vList));
                aristasG2.setText(stringAristas); // <--- ESTO FALTABA
            }
        });
    }

    @FXML
    private void handleLimpiar() {
        // 1. Verificar qué grafos tienen contenido (vértices o aristas)
        boolean tieneG1 = (g1 != null && !g1.getVertices().isEmpty()) || !verticesG1.getText().isEmpty();
        boolean tieneG2 = (g2 != null && !g2.getVertices().isEmpty()) || !verticesG2.getText().isEmpty();
        boolean tieneG3 = (g3 != null && !g3.getVertices().isEmpty());

        // Caso A: Nada que limpiar
        if (!tieneG1 && !tieneG2 && !tieneG3) {
            mostrarAlerta("Información", "No hay nada que limpiar en los paneles.");
            return;
        }

        // Caso B: Solo un panel tiene contenido
        if (tieneG1 && !tieneG2 && !tieneG3) { limpiarG1(); return; }
        if (!tieneG1 && tieneG2 && !tieneG3) { limpiarG2(); return; }
        if (!tieneG1 && !tieneG2 && tieneG3) { limpiarG3(); return; }

        // Caso C: Hay contenido en varios paneles, preguntamos
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
        aristasG1.clear();
        paneG1.getChildren().clear();
        infoG1.setText("");
    }

    private void limpiarG2() {
        g2 = null;
        verticesG2.clear();
        aristasG2.clear();
        paneG2.getChildren().clear();
        infoG2.setText("");
    }

    private void limpiarG3() {
        g3 = null;
        paneG3.getChildren().clear();
        infoG3.setText("");
    }
}