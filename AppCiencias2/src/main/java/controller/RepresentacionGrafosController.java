package controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import utilities.GrafoOrdinal;
import utilities.MatrizGrafosService;
import utilities.RepresentacionGrafoVisual;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javafx.stage.FileChooser;
import utilities.ArchivoEstructuraService;
import utilities.AristaPonderada;
import utilities.DatosArchivo;

public class RepresentacionGrafosController {

    @FXML
    private TextField cantidadVerticesField;

    @FXML
    private CheckBox dirigidoCheck;

    @FXML
    private TextArea infoArea;

    @FXML
    private Pane panelGrafo;

    private GrafoOrdinal grafo;

    private final List<String[]> verticesTemporales = new ArrayList<>();
    private final List<String[]> aristasTemporales = new ArrayList<>();
    private final List<String> nombresVertices = new ArrayList<>();

    private String verticeOrigenSeleccionado = null;
    private boolean modoEliminarArista = false;

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
            verticeOrigenSeleccionado = null;
            modoEliminarArista = false;

            for (int i = 0; i < cantidad; i++) {
                String nombre = "V" + (i + 1);

                double[] posicion = calcularPosicionCircular(i, cantidad);

                verticesTemporales.add(new String[]{
                    nombre,
                    String.valueOf((int) posicion[0]),
                    String.valueOf((int) posicion[1])
                });

                nombresVertices.add(nombre);
            }

            grafo = construirGrafoActual();

            redibujarGrafo();

            mostrarInformacionTemporal(
                    "Se generaron " + cantidad + " vértices.\n" +
                    "Haz clic en un vértice origen y luego en un vértice destino para crear una arista."
            );

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad de vértices debe ser un número entero.");
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
                mostrarAlerta("Error", "Un vértice no puede conectarse consigo mismo.");
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

    private void agregarAristaEntre(String origen, String destino) {
        boolean dirigido = esDirigido();

        for (String[] arista : aristasTemporales) {
            boolean existeMismaDireccion =
                    arista[0].equals(origen) && arista[1].equals(destino);

            boolean existeContrariaNoDirigida =
                    !dirigido &&
                    arista[0].equals(destino) &&
                    arista[1].equals(origen);

            if (existeMismaDireccion || existeContrariaNoDirigida) {
                mostrarAlerta("Error", "Esa arista ya existe.");
                return;
            }
        }

        aristasTemporales.add(new String[]{origen, destino});

        grafo = construirGrafoActual();
        redibujarGrafo();

        mostrarInformacionTemporal("Se agregó una arista: " + origen + " - " + destino);
    }
    
    @FXML
private void guardarGrafo() {
    if (verticesTemporales.isEmpty()) {
        mostrarAlerta("Error", "Primero debes crear un grafo.");
        return;
    }

    FileChooser fc = new FileChooser();
    fc.setTitle("Guardar representación de grafo");
    fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Grafo (*.gra)", "*.gra")
    );

    File file = fc.showSaveDialog(panelGrafo.getScene().getWindow());
    if (file == null) {
        return;
    }

    try (BufferedWriter bw = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

        bw.write("TIPO=GRAFO_REPRESENTACION");
        bw.newLine();

        bw.write("DIRIGIDO=" + esDirigido());
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
                    "*.ord", "*.gra", "*.arb"
            )
    );

    File file = fc.showOpenDialog(panelGrafo.getScene().getWindow());

    if (file == null) {
        return;
    }

    try {
        DatosArchivo datos = ArchivoEstructuraService.cargarArchivo(file);

        if (!"GRAFO_ORDINAL".equals(datos.getTipo())
                && !"GRAFO_CAMINOS".equals(datos.getTipo())
                && !"GRAFO_GENERADOR".equals(datos.getTipo())
                && !"GRAFO_REPRESENTACION".equals(datos.getTipo())
                && !"ARBOL".equals(datos.getTipo())) {

            mostrarAlerta("Error", "El archivo no corresponde a una estructura compatible.");
            return;
        }

        List<String[]> nuevosVertices = new ArrayList<>();
        List<String[]> nuevasAristas = new ArrayList<>();
        List<String> verticesCargados = new ArrayList<>();

        if ("ARBOL".equals(datos.getTipo())) {
            if (datos.getRaiz() != null && !datos.getRaiz().isEmpty()) {
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
            mostrarAlerta("Error", "La estructura no contiene vértices.");
            return;
        }

        int total = verticesCargados.size();

        for (int i = 0; i < total; i++) {
            String nombre = verticesCargados.get(i);
            double[] posicion = calcularPosicionCircular(i, total);

            nuevosVertices.add(new String[]{
                nombre,
                String.valueOf((int) posicion[0]),
                String.valueOf((int) posicion[1])
            });
        }

        if ("ARBOL".equals(datos.getTipo())
                || "GRAFO_ORDINAL".equals(datos.getTipo())
                || "GRAFO_REPRESENTACION".equals(datos.getTipo())) {

            for (String[] relacion : datos.getRelaciones()) {
                nuevasAristas.add(new String[]{relacion[0], relacion[1]});
            }

        } else {
            for (AristaPonderada a : datos.getAristas()) {
                nuevasAristas.add(new String[]{a.getOrigen(), a.getDestino()});
            }
        }

        grafo = null;
        verticeOrigenSeleccionado = null;
        modoEliminarArista = false;

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
        redibujarGrafo();

        mostrarInformacionTemporal(
                "Estructura cargada: " + file.getName() + "\n\n" +
                "Puedes seguir agregando aristas o calcular matrices."
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
                "Haz clic en el vértice origen y luego en el vértice destino."
        );
    }

    private void eliminarAristaEntre(String origen, String destino) {
        boolean dirigido = esDirigido();
        boolean eliminada = false;

        for (int i = 0; i < aristasTemporales.size(); i++) {
            String[] arista = aristasTemporales.get(i);

            boolean mismaDireccion =
                    arista[0].equals(origen) && arista[1].equals(destino);

            boolean direccionContrariaNoDirigida =
                    !dirigido &&
                    arista[0].equals(destino) &&
                    arista[1].equals(origen);

            if (mismaDireccion || direccionContrariaNoDirigida) {
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
        redibujarGrafo();

        mostrarInformacionTemporal("Se eliminó la arista.");
    }

    @FXML
    private void calcularMatrizAdyacencia() {
        if (!validarGrafo()) {
            return;
        }

        MatrizGrafosService service = new MatrizGrafosService();

        int[][] matriz = service.generarMatrizAdyacencia(
                nombresVertices,
                aristasTemporales,
                esDirigido()
        );

        mostrarMatriz(
                "MATRIZ DE ADYACENCIA ENTRE VÉRTICES",
                nombresVertices,
                nombresVertices,
                matriz
        );
    }

    @FXML
    private void calcularMatrizIncidencia() {
        if (!validarGrafo()) {
            return;
        }

        MatrizGrafosService service = new MatrizGrafosService();

        int[][] matriz = service.generarMatrizIncidencia(
                nombresVertices,
                aristasTemporales,
                esDirigido()
        );

        List<String> nombresAristas = obtenerNombresAristas();

        mostrarMatriz(
                "MATRIZ DE INCIDENCIA",
                nombresVertices,
                nombresAristas,
                matriz
        );
    }

    @FXML
    private void calcularMatrizAdyacenciaAristas() {
        if (!validarGrafo()) {
            return;
        }

        MatrizGrafosService service = new MatrizGrafosService();

        int[][] matriz = service.generarMatrizAdyacenciaAristas(aristasTemporales);

        List<String> nombresAristas = obtenerNombresAristas();

        mostrarMatriz(
                "MATRIZ DE ADYACENCIA ENTRE ARISTAS",
                nombresAristas,
                nombresAristas,
                matriz
        );
    }

    @FXML
    private void limpiar() {
        grafo = null;
        verticeOrigenSeleccionado = null;
        modoEliminarArista = false;

        verticesTemporales.clear();
        aristasTemporales.clear();
        nombresVertices.clear();

        if (cantidadVerticesField != null) {
            cantidadVerticesField.clear();
        }

        if (infoArea != null) {
            infoArea.clear();
        }

        if (panelGrafo != null) {
            panelGrafo.getChildren().clear();
        }
    }

    @FXML
    private void cambiarTipoGrafo() {
        verticeOrigenSeleccionado = null;
        modoEliminarArista = false;

        grafo = construirGrafoActual();
        redibujarGrafo();

        if (esDirigido()) {
            mostrarInformacionTemporal("Modo dirigido activado. Las aristas se dibujan con flecha.");
        } else {
            mostrarInformacionTemporal("Modo no dirigido activado. Las aristas se dibujan sin flecha.");
        }
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

    private void redibujarGrafo() {
        RepresentacionGrafoVisual.dibujar(
                grafo,
                panelGrafo,
                esDirigido(),
                this::seleccionarVerticeVisual
        );
    }

    private boolean validarGrafo() {
        if (verticesTemporales.isEmpty()) {
            mostrarAlerta("Error", "Primero debes generar los vértices.");
            return false;
        }

        if (aristasTemporales.isEmpty()) {
            mostrarAlerta("Error", "Debes agregar al menos una arista.");
            return false;
        }

        return true;
    }

    private boolean esDirigido() {
        return dirigidoCheck != null && dirigidoCheck.isSelected();
    }

    private List<String> obtenerNombresAristas() {
    List<String> nombres = new ArrayList<>();

    for (int i = 0; i < aristasTemporales.size(); i++) {
        nombres.add(generarNombreArista(i));
    }

    return nombres;
}

private String generarNombreArista(int indice) {
    int letra = indice % 26;
    int grupo = indice / 26;

    char base = (char) ('a' + letra);

    if (grupo == 0) {
        return String.valueOf(base);
    }

    return String.valueOf(base) + grupo;
}

    private void mostrarMatriz(
            String titulo,
            List<String> nombresFilas,
            List<String> nombresColumnas,
            int[][] matriz) {

        StringBuilder sb = new StringBuilder();

        sb.append(titulo).append("\n\n");

        sb.append("      ");

        for (String columna : nombresColumnas) {
            sb.append(String.format("%6s", columna));
        }

        sb.append("\n");

        for (int i = 0; i < matriz.length; i++) {
            sb.append(String.format("%6s", nombresFilas.get(i)));

            for (int j = 0; j < matriz[i].length; j++) {
                sb.append(String.format("%6d", matriz[i][j]));
            }

            sb.append("\n");
        }

        sb.append("\n");

        if (titulo.contains("INCIDENCIA")) {
            if (esDirigido()) {
                sb.append("Nota: 1 indica que la arista sale del vértice y -1 indica que entra al vértice.");
            } else {
                sb.append("Nota: 1 indica que el vértice pertenece a la arista.");
            }
        } else {
            sb.append("Nota: 1 indica relación y 0 indica que no hay relación.");
        }

        infoArea.setText(sb.toString());
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