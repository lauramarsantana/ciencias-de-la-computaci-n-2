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
import utilities.ArchivoEstructuraService;
import utilities.AristaPonderada;
import utilities.DatosArchivo;

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

    fc.setTitle("Cargar estructura");

    fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                    "Estructuras compatibles (*.ord, *.gra, *.arb)",
                    "*.ord",
                    "*.gra",
                    "*.arb"
            )
    );

    File file = fc.showOpenDialog(panelGrafo.getScene().getWindow());

    if (file == null) {
        return;
    }

    try {

        DatosArchivo datos =
                ArchivoEstructuraService.cargarArchivo(file);

        if (!"GRAFO_ORDINAL".equals(datos.getTipo())
                && !"GRAFO_CAMINOS".equals(datos.getTipo())
                && !"GRAFO_GENERADOR".equals(datos.getTipo())
                && !"ARBOL".equals(datos.getTipo())) {

            mostrarAlerta(
                    "Error",
                    "El archivo no corresponde a una estructura compatible."
            );

            return;
        }

        List<String[]> nuevosVertices = new ArrayList<>();
        List<String[]> nuevasAristas = new ArrayList<>();

        List<String> verticesCargados = new ArrayList<>();

        // =========================
        // VERTICES
        // =========================

        if ("ARBOL".equals(datos.getTipo())) {

            if (datos.getRaiz() != null
                    && !datos.getRaiz().isEmpty()) {

                verticesCargados.add(datos.getRaiz());
            }

            for (String[] relacion : datos.getRelaciones()) {

                if (!verticesCargados.contains(relacion[0])) {
                    verticesCargados.add(relacion[0]);
                }

                if (!verticesCargados.contains(relacion[1])) {
                    verticesCargados.add(relacion[1]);
                }
            }

        } else {

            verticesCargados.addAll(datos.getVertices());
        }

        if (verticesCargados.isEmpty()) {

            mostrarAlerta(
                    "Error",
                    "La estructura no contiene vértices."
            );

            return;
        }

        int ancho = 700;
        int alto = 420;

        int total = verticesCargados.size();

        for (int i = 0; i < total; i++) {

            String nombre = verticesCargados.get(i);

            double angulo =
                    2 * Math.PI * i / total;

            int x = (int) (
                    ancho / 2
                    + 180 * Math.cos(angulo)
            );

            int y = (int) (
                    alto / 2
                    + 180 * Math.sin(angulo)
            );

            nuevosVertices.add(
                    new String[]{
                            nombre,
                            String.valueOf(x),
                            String.valueOf(y)
                    }
            );
        }

        // =========================
        // ARISTAS
        // =========================

        if ("ARBOL".equals(datos.getTipo())
                || "GRAFO_ORDINAL".equals(datos.getTipo())) {

            for (String[] relacion : datos.getRelaciones()) {

                nuevasAristas.add(
                        new String[]{
                                relacion[0],
                                relacion[1]
                        }
                );
            }

        } else {

            for (AristaPonderada a : datos.getAristas()) {

                nuevasAristas.add(
                        new String[]{
                                a.getOrigen(),
                                a.getDestino()
                        }
                );
            }
        }

        // =========================
        // LIMPIAR
        // =========================

        grafo = null;
        verticeOrigenSeleccionado = null;

        verticesTemporales.clear();
        aristasTemporales.clear();
        nombresVertices.clear();

        panelGrafo.getChildren().clear();
        infoArea.clear();

        // =========================
        // CARGAR
        // =========================

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
                "Estructura cargada: "
                + file.getName()
                + "\n\n"
                + "Puedes seguir agregando aristas."
        );

    } catch (Exception e) {

        e.printStackTrace();

        mostrarAlerta(
                "Error",
                "Error cargando: " + e.getMessage()
        );
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

    sb.append("Cantidad de vértices: ")
      .append(grafo.contarVertices())
      .append("\n");

    sb.append("Cantidad de aristas: ")
      .append(grafo.contarAristas())
      .append("\n\n");

    sb.append("Descripción:\n");
    sb.append("La función ordinal etiqueta los vértices ")
      .append("siguiendo el orden de dependencia del grafo.\n");

    sb.append("En cada paso se selecciona un vértice ")
      .append("sin predecesores pendientes.\n");

    sb.append("Si existen varios candidatos, se elige ")
      .append("el vértice más arriba y luego el más ")
      .append("a la izquierda.\n\n");

    sb.append("Pasos del algoritmo:\n");

    for (String paso : resultado.getPasos()) {
        sb.append("- ").append(paso).append("\n");
    }

    sb.append("\nResultado final:\n");

    if (resultado.isHayCiclo()) {
        sb.append("Se detectó un ciclo en el grafo.\n");
        sb.append("La función ordinal no pudo completarse.");
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