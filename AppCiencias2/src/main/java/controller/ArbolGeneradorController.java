package controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import utilities.ArbolGeneradorService;
import utilities.ArbolGeneradorVisual;
import utilities.AristaPonderada;
import utilities.GrafoPonderado;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import utilities.ResultadoKruskal;
import utilities.ArchivoEstructuraService;
import utilities.DatosArchivo;
import java.util.LinkedHashSet;
import java.util.Set;

public class ArbolGeneradorController {

    @FXML
    private TextField verticesField;

    @FXML
    private TextField aristasField;

    @FXML
    private TextArea infoArea;

    @FXML
    private Pane panelResultado;

    private LayoutController layoutController;
    private GrafoPonderado grafo;

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void mostrarGrafoCompleto() {
        try {
            grafo = construirGrafo();

            // Dibuja el grafo completo sin resaltar árbol mínimo o máximo
            ArbolGeneradorVisual.dibujarGrafoCompleto(grafo, panelResultado);

            StringBuilder sb = new StringBuilder();
            sb.append("Grafo original ingresado\n\n");
            sb.append("Vértices:\n");

            for (String v : grafo.getVertices()) {
                sb.append("- ").append(v).append("\n");
            }

            sb.append("\nAristas:\n");
            for (AristaPonderada a : grafo.getAristas()) {
                sb.append(a).append("\n");
            }

            infoArea.setText(sb.toString());

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void generarMinimo() {
        generar(false);
    }

    @FXML
    private void generarMaximo() {
        generar(true);
    }

    private void generar(boolean maximo) {
    try {
        grafo = construirGrafo();

        ResultadoKruskal resultadoKruskal = ArbolGeneradorService.kruskalConDetalle(grafo, maximo);
        List<AristaPonderada> resultado = resultadoKruskal.getSeleccionadas();

        if (resultado.size() != grafo.getVertices().size() - 1) {
            throw new IllegalArgumentException("El grafo no es conexo. No se puede formar un árbol generador completo.");
        }

        ArbolGeneradorVisual.dibujar(grafo, resultado, panelResultado);

        StringBuilder sb = new StringBuilder();
        sb.append(maximo ? "Árbol Generador Máximo\n\n" : "Árbol Generador Mínimo\n\n");

        sb.append("Aristas seleccionadas:\n");
        for (AristaPonderada a : resultado) {
            sb.append(a).append("\n");
        }

        if (!resultadoKruskal.getDescartadasPorCiclo().isEmpty()) {
            sb.append("\nAdvertencia:\n");
            sb.append("Las siguientes aristas no se incluyeron porque formarían ciclos:\n");

            for (AristaPonderada a : resultadoKruskal.getDescartadasPorCiclo()) {
                sb.append("- ").append(a).append("\n");
            }
        }

        sb.append("\nPeso total: ")
          .append(ArbolGeneradorService.calcularPesoTotal(resultado));

        infoArea.setText(sb.toString());

    } catch (Exception e) {
        mostrarAlerta("Error", e.getMessage());
    }
}
    
    @FXML
private void guardarGrafo() {
    try {
        GrafoPonderado grafoAGuardar = construirGrafo();

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar grafo ponderado");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Grafo Ponderado (*.gra)", "*.gra")
        );

        File file = fc.showSaveDialog(panelResultado.getScene().getWindow());
        if (file == null) return;

        ArchivoEstructuraService.guardarGrafoGenerador(file, grafoAGuardar);

        this.grafo = grafoAGuardar;
        infoArea.setText("Grafo guardado correctamente: " + file.getName());

    } catch (Exception e) {
        mostrarAlerta("Error", "Error guardando: " + e.getMessage());
    }
}

    @FXML
private void cargarGrafo() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Cargar grafo ponderado");
    fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Estructuras (*.gra, *.arb)", "*.gra", "*.arb")
    );

    File file = fc.showOpenDialog(panelResultado.getScene().getWindow());
    if (file == null) return;

    try {
        DatosArchivo datos = ArchivoEstructuraService.cargarArchivo(file);

        String verticesTexto;
        StringBuilder aristasTexto = new StringBuilder();

        if ("GRAFO_GENERADOR".equals(datos.getTipo())) {

            verticesTexto = String.join(",", datos.getVertices());

            for (AristaPonderada a : datos.getAristas()) {
                if (aristasTexto.length() > 0) {
                    aristasTexto.append(", ");
                }

                aristasTexto.append(a.getOrigen())
                        .append("-")
                        .append(a.getDestino())
                        .append("-")
                        .append(a.getPeso());
            }

        } else if ("ARBOL".equals(datos.getTipo())) {

            Set<String> vertices = new LinkedHashSet<>();

            if (datos.getRaiz() != null && !datos.getRaiz().isEmpty()) {
                vertices.add(datos.getRaiz());
            }

            for (String[] relacion : datos.getRelaciones()) {
                String padre = relacion[0].trim();
                String hijo = relacion[1].trim();

                vertices.add(padre);
                vertices.add(hijo);

                if (aristasTexto.length() > 0) {
                    aristasTexto.append(", ");
                }

                aristasTexto.append(padre)
                        .append("-")
                        .append(hijo)
                        .append("-")
                        .append(1);
            }

            verticesTexto = String.join(",", vertices);

        } else {
            mostrarAlerta("Error", "Este archivo no se puede cargar como grafo generador.");
            return;
        }

        if (verticesTexto == null || verticesTexto.isEmpty()) {
            mostrarAlerta("Error", "Archivo inválido: no contiene vértices.");
            return;
        }

        verticesField.setText(verticesTexto);
        aristasField.setText(aristasTexto.toString());

        grafo = construirGrafo();
        ArbolGeneradorVisual.dibujarGrafoCompleto(grafo, panelResultado);

        StringBuilder sb = new StringBuilder();
        sb.append("Grafo cargado\n\n");
        sb.append("Vértices:\n");

        for (String v : grafo.getVertices()) {
            sb.append("- ").append(v).append("\n");
        }

        sb.append("\nAristas:\n");

        for (AristaPonderada a : grafo.getAristas()) {
            sb.append(a).append("\n");
        }

        infoArea.setText(sb.toString());

    } catch (Exception e) {
        mostrarAlerta("Error", "Error cargando: " + e.getMessage());
    }
}
    
    @FXML
    private void limpiarEstructura() {
        // Limpiar modelo
        grafo = null;

        // Limpiar campos de entrada
        verticesField.clear();
        aristasField.clear();

        // Limpiar área de información
        infoArea.clear();
        infoArea.setText("Estructura limpiada.");

        // Limpiar panel gráfico
        panelResultado.getChildren().clear();
    }

    private GrafoPonderado construirGrafo() {
        GrafoPonderado g = new GrafoPonderado();

        String textoVertices = verticesField.getText().trim();
        if (textoVertices.isEmpty()) {
            throw new IllegalArgumentException("Debes ingresar los vértices.");
        }

        String[] vertices = textoVertices.split(",");
        for (String v : vertices) {
            String limpio = v.trim();
            if (!limpio.isEmpty()) {
                g.agregarVertice(limpio);
            }
        }

        String textoAristas = aristasField.getText().trim();
        if (textoAristas.isEmpty()) {
            throw new IllegalArgumentException("Debes ingresar las aristas.");
        }

        String[] aristas = textoAristas.split(",");
        for (String a : aristas) {
            String[] partes = a.trim().split("-");

            if (partes.length != 3) {
                throw new IllegalArgumentException("Formato inválido en arista: " + a + ". Usa origen-destino-peso");
            }

            String origen = partes[0].trim();
            String destino = partes[1].trim();
            int peso;

            try {
                peso = Integer.parseInt(partes[2].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Peso inválido en la arista: " + a);
            }

            g.agregarArista(origen, destino, peso);
        }

        return g;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}