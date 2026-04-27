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
    private TextField cantidadVerticesField;

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

    private boolean modoEliminarArista = false;
    private String verticeOrigenSeleccionado = null;
    
    @FXML
    private void generarVertices() {
        try {
            String texto = cantidadVerticesField.getText().trim();

            if (texto.isEmpty()) {
                mostrarAlerta("Error", "Debes ingresar la cantidad de vértices.");
                return;
            }

            int cantidad = Integer.parseInt(texto);

            if (cantidad <= 0) {
                mostrarAlerta("Error", "La cantidad de vértices debe ser mayor que cero.");
                return;
            }

            grafo = null;
            verticesTemporales.clear();
            aristasTemporales.clear();
            nombresVertices.clear();
            panelGrafo.getChildren().clear();
            infoArea.clear();

            for (int i = 0; i < cantidad; i++) {
                String nombreInterno = "V" + (i + 1);

                double[] posicion = calcularPosicionCircular(i, cantidad);
                int x = (int) posicion[0];
                int y = (int) posicion[1];

                verticesTemporales.add(new String[]{
                    nombreInterno,
                    String.valueOf(x),
                    String.valueOf(y)
                });

                nombresVertices.add(nombreInterno);
            }

            grafo = construirGrafoActual();
            FuncionOrdinalVisual.dibujarSinNombres(grafo, panelGrafo, this::seleccionarVerticeVisual);

            mostrarInformacionTemporal(
                "Se generaron " + cantidad + " vértices.\n" +
                "Haz clic en un vértice origen y luego en un vértice destino para crear una arista."
            );

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad de vértices debe ser un número entero.");
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }
    
    private void seleccionarVerticeVisual(String nombreVertice) {
    try {
        if (verticesTemporales.isEmpty()) {
            mostrarAlerta("Error", "Primero debes generar los vértices.");
            return;
        }

        if (verticeOrigenSeleccionado == null) {
            verticeOrigenSeleccionado = nombreVertice;

            if (modoEliminarArista) {
                mostrarInformacionTemporal(
                        "Origen seleccionado para eliminar.\n" +
                        "Ahora haz clic en el vértice destino."
                );
            } else {
                mostrarInformacionTemporal(
                        "Origen seleccionado.\n" +
                        "Ahora haz clic en el vértice destino."
                );
            }

            return;
        }

        String origen = verticeOrigenSeleccionado;
        String destino = nombreVertice;

        verticeOrigenSeleccionado = null;

        if (origen.equals(destino)) {
            modoEliminarArista = false;
            mostrarAlerta("Error", "Un vértice no puede apuntarse a sí mismo.");
            return;
        }

        if (modoEliminarArista) {
            eliminarAristaEntre(origen, destino);
            modoEliminarArista = false;
        } else {
            agregarAristaEntre(origen, destino);
        }

        } catch (Exception e) {
            modoEliminarArista = false;
            verticeOrigenSeleccionado = null;
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

            FuncionOrdinalVisual.dibujarSinNombres(
                    grafo,
                    panelGrafo,
                    this::seleccionarVerticeVisual
            );
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
            verticeOrigenSeleccionado = null;

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

            grafo = construirGrafoActual();

            FuncionOrdinalVisual.dibujarSinNombres(
                    grafo,
                    panelGrafo,
                    this::seleccionarVerticeVisual
            );

            mostrarInformacionTemporal(
                    "Grafo cargado: " + file.getName() + "\n" +
                    "Puedes seguir agregando aristas haciendo clic en un vértice origen y luego en un destino."
            );

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando: " + e.getMessage());
        }
    }
    
    @FXML
    private void eliminarArista() {
        if (aristasTemporales.isEmpty()) {
            mostrarAlerta("Error", "No hay aristas para eliminar.");
            return;
        }

        modoEliminarArista = true;
        verticeOrigenSeleccionado = null;

        mostrarInformacionTemporal(
                "Modo eliminar arista activado.\n" +
                "Haz clic en el vértice origen y luego en el vértice destino de la arista que deseas eliminar."
        );
    }
    
    private void agregarAristaEntre(String origen, String destino) {
    for (String[] arista : aristasTemporales) {
        if (arista[0].equals(origen) && arista[1].equals(destino)) {
            mostrarAlerta("Error", "Esa arista ya existe.");
            return;
        }
    }

    aristasTemporales.add(new String[]{origen, destino});

    grafo = construirGrafoActual();

    FuncionOrdinalVisual.dibujarSinNombres(
            grafo,
            panelGrafo,
            this::seleccionarVerticeVisual
    );

    mostrarInformacionTemporal("Se agregó una arista.");
}
    
    private void eliminarAristaEntre(String origen, String destino) {
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

    grafo = construirGrafoActual();

    FuncionOrdinalVisual.dibujarSinNombres(
            grafo,
            panelGrafo,
            this::seleccionarVerticeVisual
    );

    mostrarInformacionTemporal("Se eliminó una arista.");
}

    @FXML
    private void limpiar() {
        grafo = null;

        // Reset de selección
        verticeOrigenSeleccionado = null;
        modoEliminarArista = false;

        // Limpiar estructuras
        verticesTemporales.clear();
        aristasTemporales.clear();
        nombresVertices.clear();

        // Limpiar interfaz
        infoArea.clear();

        if (cantidadVerticesField != null) {
            cantidadVerticesField.clear();
        }

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
    
    private double[] calcularPosicionCircular(int indice, int total) {
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

    double radio = Math.min(ancho, alto) * 0.35;

    double angulo = 2 * Math.PI * indice / total - Math.PI / 2;

    double x = centroX + radio * Math.cos(angulo);
    double y = centroY + radio * Math.sin(angulo);

    return new double[]{x, y};
    }

    private void mostrarInformacion(ResultadoOrdinal resultado) {
    StringBuilder sb = new StringBuilder();

    sb.append("Cantidad de vértices: ").append(grafo.contarVertices()).append("\n");
    sb.append("Cantidad de aristas: ").append(grafo.contarAristas()).append("\n\n");

    sb.append("Aristas creadas: ").append(grafo.contarAristas()).append("\n\n");

    sb.append("Etiquetas ordinales:\n");
    for (VerticeOrdinal v : grafo.getVertices()) {
        int etiqueta = resultado.getEtiquetas().get(v.getNombre());

        if (etiqueta > 0) {
            sb.append("Vértice etiquetado con: ").append(etiqueta).append("\n");
        } else {
            sb.append("Vértice sin etiquetar\n");
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