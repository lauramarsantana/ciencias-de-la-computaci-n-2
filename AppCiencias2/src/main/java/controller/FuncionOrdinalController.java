package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import utilities.AristaDirigida;
import utilities.FuncionOrdinalService;
import utilities.FuncionOrdinalVisual;
import utilities.GrafoOrdinal;
import utilities.ResultadoOrdinal;
import utilities.VerticeOrdinal;

public class FuncionOrdinalController {

    @FXML
    private TextField nombreVerticeField;

    @FXML
    private ComboBox<String> origenCombo;

    @FXML
    private ComboBox<String> destinoCombo;

    @FXML
    private TextArea infoArea;

    @FXML
    private Pane panelGrafo;

    private GrafoOrdinal grafo;

    // nombre, x, y
    private final List<String[]> verticesTemporales = new ArrayList<>();

    // origen, destino
    private final List<String[]> aristasTemporales = new ArrayList<>();

    private final List<String> nombresVertices = new ArrayList<>();

    @FXML
    private void agregarVertice() {
        try {
            String nombre = nombreVerticeField.getText().trim();

            if (nombre.isEmpty()) {
                mostrarAlerta("Error", "Debes ingresar el nombre del vértice.");
                return;
            }

            if (nombresVertices.contains(nombre)) {
                mostrarAlerta("Error", "Ese vértice ya existe.");
                return;
            }

            double[] posicion = calcularSiguientePosicion();
            int x = (int) posicion[0];
            int y = (int) posicion[1];

            verticesTemporales.add(new String[]{nombre, String.valueOf(x), String.valueOf(y)});
            nombresVertices.add(nombre);

            actualizarCombos();
            nombreVerticeField.clear();

            mostrarInformacionTemporal("Se agregó el vértice: " + nombre + " (" + x + ", " + y + ")");

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void agregarArista() {
        try {
            if (verticesTemporales.isEmpty()) {
                mostrarAlerta("Error", "Primero debes agregar vértices.");
                return;
            }

            String origen = origenCombo.getValue();
            String destino = destinoCombo.getValue();

            if (origen == null || origen.isEmpty()) {
                mostrarAlerta("Error", "Debes seleccionar el vértice origen.");
                return;
            }

            if (destino == null || destino.isEmpty()) {
                mostrarAlerta("Error", "Debes seleccionar el vértice destino.");
                return;
            }

            if (origen.equals(destino)) {
                mostrarAlerta("Error", "Un vértice no puede apuntarse a sí mismo.");
                return;
            }

            for (String[] arista : aristasTemporales) {
                if (arista[0].equals(origen) && arista[1].equals(destino)) {
                    mostrarAlerta("Error", "Esa arista ya existe.");
                    return;
                }
            }

            aristasTemporales.add(new String[]{origen, destino});

            origenCombo.setValue(origen);
            destinoCombo.setValue(destino);

            mostrarInformacionTemporal("Se agregó la arista: " + origen + " -> " + destino);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void ejecutarFuncionOrdinal() {
        try {
            if (verticesTemporales.isEmpty()) {
                mostrarAlerta("Error", "Debes agregar al menos un vértice.");
                return;
            }

            grafo = construirGrafoActual();

            FuncionOrdinalService service = new FuncionOrdinalService();
            ResultadoOrdinal resultado = service.calcularFuncionOrdinal(
                    grafo.getVertices(),
                    grafo.getAristas()
            );

            FuncionOrdinalVisual.dibujar(grafo, panelGrafo);
            mostrarInformacion(resultado);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void guardarGrafo() {
        if (verticesTemporales.isEmpty()) {
            mostrarAlerta("Error", "Primero debes crear un grafo.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar grafo ordinal");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Grafo Ordinal (*.ord)", "*.ord")
        );

        File file = fc.showSaveDialog(panelGrafo.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=GRAFO_ORDINAL");
            bw.newLine();

            bw.write("VERTICES");
            bw.newLine();

            for (String[] vertice : verticesTemporales) {
                bw.write(vertice[0] + "|" + vertice[1] + "|" + vertice[2]);
                bw.newLine();
            }

            bw.write("ARISTAS");
            bw.newLine();

            for (String[] arista : aristasTemporales) {
                bw.write(arista[0] + "|" + arista[1]);
                bw.newLine();
            }

            bw.write("END");
            bw.newLine();

            mostrarInformacionTemporal("Grafo guardado: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error guardando: " + e.getMessage());
        }
    }

    @FXML
    private void cargarGrafo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar grafo ordinal");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Grafo Ordinal (*.ord)", "*.ord")
        );

        File file = fc.showOpenDialog(panelGrafo.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            List<String[]> nuevosVertices = new ArrayList<>();
            List<String[]> nuevasAristas = new ArrayList<>();

            String line;
            boolean leyendoVertices = false;
            boolean leyendoAristas = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("TIPO=")) {
                    if (!line.equals("TIPO=GRAFO_ORDINAL")) {
                        mostrarAlerta("Error", "El archivo no corresponde a un grafo ordinal.");
                        return;
                    }
                    continue;
                }

                if (line.equals("VERTICES")) {
                    leyendoVertices = true;
                    leyendoAristas = false;
                    continue;
                }

                if (line.equals("ARISTAS")) {
                    leyendoVertices = false;
                    leyendoAristas = true;
                    continue;
                }

                if (line.equals("END")) {
                    break;
                }

                if (leyendoVertices) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length == 3) {
                        String nombre = parts[0].trim();
                        String x = parts[1].trim();
                        String y = parts[2].trim();

                        if (!nombre.isEmpty() && !x.isEmpty() && !y.isEmpty()) {
                            nuevosVertices.add(new String[]{nombre, x, y});
                        }
                    }
                }

                if (leyendoAristas) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length == 2) {
                        String origen = parts[0].trim();
                        String destino = parts[1].trim();

                        if (!origen.isEmpty() && !destino.isEmpty()) {
                            nuevasAristas.add(new String[]{origen, destino});
                        }
                    }
                }
            }

            if (nuevosVertices.isEmpty()) {
                mostrarAlerta("Error", "El archivo no contiene vértices válidos.");
                return;
            }

            grafo = null;
            verticesTemporales.clear();
            aristasTemporales.clear();
            nombresVertices.clear();
            panelGrafo.getChildren().clear();
            infoArea.clear();

            verticesTemporales.addAll(nuevosVertices);
            aristasTemporales.addAll(nuevasAristas);

            for (String[] vertice : verticesTemporales) {
                nombresVertices.add(vertice[0]);
            }

            actualizarCombos();
            nombreVerticeField.clear();

            if (!nombresVertices.isEmpty()) {
                origenCombo.setValue(nombresVertices.get(0));
                destinoCombo.setValue(nombresVertices.get(0));
            }

            grafo = construirGrafoActual();

            FuncionOrdinalService service = new FuncionOrdinalService();
            ResultadoOrdinal resultado = service.calcularFuncionOrdinal(
                    grafo.getVertices(),
                    grafo.getAristas()
            );

            FuncionOrdinalVisual.dibujar(grafo, panelGrafo);
            mostrarInformacion(resultado);

            mostrarInformacionTemporal("Grafo cargado: " + file.getName());
            mostrarInformacion(resultado);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando: " + e.getMessage());
        }
    }
    
    @FXML
    private void eliminarArista() {
        try {
            if (aristasTemporales.isEmpty()) {
                mostrarAlerta("Error", "No hay aristas para eliminar.");
                return;
            }

            String origen = origenCombo.getValue();
            String destino = destinoCombo.getValue();

            if (origen == null || origen.isEmpty()) {
                mostrarAlerta("Error", "Debes seleccionar el vértice origen.");
                return;
            }

            if (destino == null || destino.isEmpty()) {
                mostrarAlerta("Error", "Debes seleccionar el vértice destino.");
                return;
            }

            boolean eliminada = false;

            for (int i = 0; i < aristasTemporales.size(); i++) {
                String[] arista = aristasTemporales.get(i);

                if (arista[0].equals(origen) && arista[1].equals(destino)) {
                    aristasTemporales.remove(i);
                    eliminada = true;
                    break;
                }
            }

            if (!eliminada) {
                mostrarAlerta("Error", "Esa arista no existe.");
                return;
            }

            mostrarInformacionTemporal("Se eliminó la arista: " + origen + " -> " + destino);

            if (!verticesTemporales.isEmpty()) {
                grafo = construirGrafoActual();
                FuncionOrdinalVisual.dibujar(grafo, panelGrafo);
            } else {
                panelGrafo.getChildren().clear();
            }

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void limpiar() {
        grafo = null;

        verticesTemporales.clear();
        aristasTemporales.clear();
        nombresVertices.clear();

        nombreVerticeField.clear();
        infoArea.clear();

        origenCombo.setItems(FXCollections.observableArrayList());
        destinoCombo.setItems(FXCollections.observableArrayList());

        origenCombo.setValue(null);
        destinoCombo.setValue(null);

        panelGrafo.getChildren().clear();
    }

    private GrafoOrdinal construirGrafoActual() {
        GrafoOrdinal nuevoGrafo = new GrafoOrdinal();

        for (String[] vertice : verticesTemporales) {
            String nombre = vertice[0];
            int x = Integer.parseInt(vertice[1]);
            int y = Integer.parseInt(vertice[2]);

            nuevoGrafo.agregarVertice(nombre, x, y);
        }

        for (String[] arista : aristasTemporales) {
            nuevoGrafo.agregarArista(arista[0], arista[1]);
        }

        return nuevoGrafo;
    }

    private void actualizarCombos() {
        origenCombo.setItems(FXCollections.observableArrayList(nombresVertices));
        destinoCombo.setItems(FXCollections.observableArrayList(nombresVertices));
    }

    private double[] calcularSiguientePosicion() {
    int cantidad = verticesTemporales.size();

    double ancho = panelGrafo.getWidth();
    double alto = panelGrafo.getHeight();

    if (ancho <= 0) {
        ancho = panelGrafo.getPrefWidth();
    }
    if (alto <= 0) {
        alto = panelGrafo.getPrefHeight();
    }
    if (ancho <= 0) {
        ancho = 780;
    }
    if (alto <= 0) {
        alto = 450;
    }

    double centroX = ancho / 2.0;
    double centroY = alto / 2.0;

    // Caso 1
    if (cantidad == 0) {
        return new double[]{centroX, 90};
    }

    // Caso 2
    if (cantidad == 1) {
        return new double[]{centroX - 120, 220};
    }

    // Caso 3
    if (cantidad == 2) {
        return new double[]{centroX + 120, 220};
    }

    // Caso 4
    if (cantidad == 3) {
        return new double[]{centroX - 160, 90};
    }

    // Caso 5
    if (cantidad == 4) {
        return new double[]{centroX + 160, 90};
    }

    // Caso 6
    if (cantidad == 5) {
        return new double[]{centroX, 340};
    }

    // Para 7 o más: distribución por filas
    int indice = cantidad - 6;
    int maxPorFila = 3;

    int fila = indice / maxPorFila;
    int columna = indice % maxPorFila;

    double margenX = 120;
    double inicioY = 430;
    double espacioHorizontal = 180;
    double espacioVertical = 110;

    double x = margenX + columna * espacioHorizontal;
    double y = inicioY + fila * espacioVertical;

    return new double[]{x, y};
}
    private void mostrarInformacion(ResultadoOrdinal resultado) {
        StringBuilder sb = new StringBuilder();

        sb.append("Cantidad de vértices: ").append(grafo.contarVertices()).append("\n");
        sb.append("Cantidad de aristas: ").append(grafo.contarAristas()).append("\n\n");

        sb.append("Vértices:\n");
        for (VerticeOrdinal v : grafo.getVertices()) {
            sb.append(v.getNombre())
              .append(" (")
              .append((int) v.getX())
              .append(", ")
              .append((int) v.getY())
              .append(")\n");
        }

        sb.append("\nAristas:\n");
        for (AristaDirigida arista : grafo.getAristas()) {
            sb.append(arista.toString()).append("\n");
        }

        sb.append("\nEtiquetas ordinales:\n");
        for (VerticeOrdinal v : grafo.getVertices()) {
            int etiqueta = resultado.getEtiquetas().get(v.getNombre());
            if (etiqueta > 0) {
                sb.append(v.getNombre()).append(" -> ").append(etiqueta).append("\n");
            } else {
                sb.append(v.getNombre()).append(" -> sin etiquetar\n");
            }
        }

        sb.append("\nOrden de etiquetado:\n");
        if (resultado.getOrdenEtiquetado().isEmpty()) {
            sb.append("No se pudo etiquetar ningún vértice.\n");
        } else {
            for (String nombre : resultado.getOrdenEtiquetado()) {
                sb.append(nombre).append("\n");
            }
        }

        sb.append("\nPasos:\n");
        for (String paso : resultado.getPasos()) {
            sb.append(paso).append("\n");
        }

        sb.append("\nResultado final:\n");
        if (resultado.isHayCiclo()) {
            sb.append("Se encontró un ciclo. La función ordinal se detuvo.");
        } else {
            sb.append("La función ordinal se completó correctamente.");
        }

        infoArea.setText(sb.toString());
    }

    private void mostrarInformacionTemporal(String mensaje) {
        infoArea.setText(mensaje);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}