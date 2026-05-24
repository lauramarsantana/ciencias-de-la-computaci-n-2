package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import utilities.ArchivoEstructuraService;
import utilities.AristaPonderada;
import utilities.DatosArchivo;
import utilities.DistanciaVerticesService;
import utilities.GrafoCamino;
import utilities.GrafoCaminoVisual;
import utilities.ResultadoDistanciaVertices;

public class DistanciaVerticesController {

    @FXML
    private TextField cantidadVerticesField;

    @FXML
    private TextArea infoArea;

    @FXML
    private Pane panelGrafo;

    private GrafoCamino grafo;

    // nombre, x, y
    private final List<String[]> verticesTemporales = new ArrayList<>();

    // origen, destino, peso
    private final List<String[]> aristasTemporales = new ArrayList<>();

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
            verticeOrigenSeleccionado = null;
            modoEliminarArista = false;

            panelGrafo.getChildren().clear();
            infoArea.clear();

            for (int i = 0; i < cantidad; i++) {
                String nombre = "V" + (i + 1);
                double[] posicion = calcularPosicionCircular(i, cantidad);

                verticesTemporales.add(new String[]{
                    nombre,
                    String.valueOf((int) posicion[0]),
                    String.valueOf((int) posicion[1])
                });
            }

            grafo = construirGrafoActual();

            GrafoCaminoVisual.dibujar(
                    grafo,
                    panelGrafo,
                    this::seleccionarVerticeVisual
            );

            mostrarInformacionTemporal(
                    "Se generaron " + cantidad + " vértices.\n" +
                    "Haz clic en un vértice origen y luego en un vértice destino para crear una arista con peso."
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
                            "Origen seleccionado para eliminar: " + nombreVertice + "\n" +
                            "Ahora haz clic en el vértice destino."
                    );
                } else {
                    mostrarInformacionTemporal(
                            "Origen seleccionado: " + nombreVertice + "\n" +
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
            verticeOrigenSeleccionado = null;
            modoEliminarArista = false;
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void agregarAristaEntre(String origen, String destino) {
        for (String[] arista : aristasTemporales) {
            if (arista[0].equals(origen) && arista[1].equals(destino)) {
                mostrarAlerta("Error", "Esa arista ya existe.");
                return;
            }
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Peso de la arista");
        dialog.setHeaderText(null);
        dialog.setContentText("Ingrese el peso de la arista " + origen + " -> " + destino + ":");

        Optional<String> respuesta = dialog.showAndWait();

        if (respuesta.isEmpty()) {
            return;
        }

        try {
            int peso = Integer.parseInt(respuesta.get().trim());

            if (peso <= 0) {
                mostrarAlerta("Error", "El peso debe ser mayor que cero.");
                return;
            }

            aristasTemporales.add(new String[]{
                origen,
                destino,
                String.valueOf(peso)
            });

            grafo = construirGrafoActual();

            GrafoCaminoVisual.dibujar(
                    grafo,
                    panelGrafo,
                    this::seleccionarVerticeVisual
            );

            mostrarInformacionTemporal(
                    "Se agregó la arista " + origen + " -> " + destino + " con peso " + peso + "."
            );

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El peso debe ser un número entero.");
        }
    }

    @FXML
    private void calcularDistancias() {
        try {
            if (verticesTemporales.isEmpty()) {
                mostrarAlerta("Error", "Debes generar los vértices.");
                return;
            }

            if (aristasTemporales.isEmpty()) {
                mostrarAlerta("Error", "Debes crear al menos una arista.");
                return;
            }

            grafo = construirGrafoActual();

            DistanciaVerticesService service = new DistanciaVerticesService();

            ResultadoDistanciaVertices resultado = service.calcular(
                    grafo.getVertices(),
                    grafo.getAristas()
            );

            String texto = service.formatearResultado(
                    grafo.getVertices(),
                    resultado
            );

            infoArea.setText(texto);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void guardarGrafo() {
        try {
            if (verticesTemporales.isEmpty()) {
                mostrarAlerta("Error", "Primero debes crear un grafo.");
                return;
            }

            grafo = construirGrafoActual();

            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar grafo de distancias");

            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Grafo Distancias (*.gra)",
                            "*.gra"
                    )
            );

            File file = fc.showSaveDialog(panelGrafo.getScene().getWindow());

            if (file == null) {
                return;
            }

            ArchivoEstructuraService.guardarGrafoCaminos(file, grafo);

            mostrarInformacionTemporal("Grafo guardado correctamente: " + file.getName());

        } catch (Exception e) {
            mostrarAlerta("Error", "Error guardando: " + e.getMessage());
        }
    }

    @FXML
    private void cargarGrafo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar grafo de distancias");

        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Estructuras compatibles (*.gra, *.arb, *.ord)",
                        "*.gra",
                        "*.arb",
                        "*.ord"
                )
        );

        File file = fc.showOpenDialog(panelGrafo.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            DatosArchivo datos = ArchivoEstructuraService.cargarArchivo(file);

            if (!"GRAFO_CAMINOS".equals(datos.getTipo())
                    && !"GRAFO_GENERADOR".equals(datos.getTipo())
                    && !"ARBOL".equals(datos.getTipo())
                    && !"GRAFO_ORDINAL".equals(datos.getTipo())
                    && !"GRAFO_REPRESENTACION".equals(datos.getTipo())) {

                mostrarAlerta("Error", "Este archivo no corresponde a una estructura compatible.");
                return;
            }

            verticesTemporales.clear();
            aristasTemporales.clear();
            verticeOrigenSeleccionado = null;
            modoEliminarArista = false;

            panelGrafo.getChildren().clear();
            infoArea.clear();

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

                verticesTemporales.add(new String[]{
                    nombre,
                    String.valueOf((int) posicion[0]),
                    String.valueOf((int) posicion[1])
                });
            }

            if ("ARBOL".equals(datos.getTipo())
                    || "GRAFO_ORDINAL".equals(datos.getTipo())
                    || "GRAFO_REPRESENTACION".equals(datos.getTipo())) {

                for (String[] relacion : datos.getRelaciones()) {
                    aristasTemporales.add(new String[]{
                        relacion[0],
                        relacion[1],
                        "1"
                    });
                }

            } else {
                for (AristaPonderada a : datos.getAristas()) {
                    aristasTemporales.add(new String[]{
                        a.getOrigen(),
                        a.getDestino(),
                        String.valueOf(a.getPeso())
                    });
                }
            }

            grafo = construirGrafoActual();

            GrafoCaminoVisual.dibujar(
                    grafo,
                    panelGrafo,
                    this::seleccionarVerticeVisual
            );

            mostrarInformacionTemporal(
                    "Estructura cargada correctamente: " + file.getName() +
                    "\n\nPuedes seguir agregando aristas o calcular las distancias."
            );

        } catch (Exception e) {
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

        GrafoCaminoVisual.dibujar(
                grafo,
                panelGrafo,
                this::seleccionarVerticeVisual
        );

        mostrarInformacionTemporal("Se eliminó la arista " + origen + " -> " + destino + ".");
    }

    @FXML
    private void limpiar() {
        grafo = null;
        verticeOrigenSeleccionado = null;
        modoEliminarArista = false;

        verticesTemporales.clear();
        aristasTemporales.clear();

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

    private GrafoCamino construirGrafoActual() {
        GrafoCamino nuevoGrafo = new GrafoCamino();

        for (String[] vertice : verticesTemporales) {
            String nombre = vertice[0];
            int x = Integer.parseInt(vertice[1]);
            int y = Integer.parseInt(vertice[2]);

            nuevoGrafo.agregarVertice(nombre, x, y);
        }

        for (String[] arista : aristasTemporales) {
            String origen = arista[0];
            String destino = arista[1];
            int peso = Integer.parseInt(arista[2]);

            nuevoGrafo.agregarArista(origen, destino, peso);
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
