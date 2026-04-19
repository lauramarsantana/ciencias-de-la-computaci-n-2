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
            sb.append("Grafo completo ingresado\n\n");
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
            if (grafo == null || grafo.getVertices().isEmpty()) {
                throw new IllegalArgumentException("Primero debes mostrar el grafo completo.");
            }

            List<AristaPonderada> resultado = ArbolGeneradorService.kruskal(grafo, maximo);

            if (resultado.size() != grafo.getVertices().size() - 1) {
                throw new IllegalArgumentException("El grafo no es conexo. No se puede formar un árbol generador completo.");
            }

            ArbolGeneradorVisual.dibujar(grafo, resultado, panelResultado);

            StringBuilder sb = new StringBuilder();
            sb.append(maximo ? "Árbol Generador Máximo\n\n" : "Árbol Generador Mínimo\n\n");

            for (AristaPonderada a : resultado) {
                sb.append(a).append("\n");
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

            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                bw.write("TIPO=GRAFO_GENERADOR");
                bw.newLine();

                bw.write("VERTICES=");
                bw.write(String.join(",", grafoAGuardar.getVertices()));
                bw.newLine();

                bw.write("ARISTAS");
                bw.newLine();

                for (AristaPonderada a : grafoAGuardar.getAristas()) {
                    bw.write(a.getOrigen() + "|" + a.getDestino() + "|" + a.getPeso());
                    bw.newLine();
                }

                bw.write("END");
                bw.newLine();
            }

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
                new FileChooser.ExtensionFilter("Grafo Ponderado (*.gra)", "*.gra")
        );

        File file = fc.showOpenDialog(panelResultado.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            String verticesTexto = null;
            StringBuilder aristasTexto = new StringBuilder();

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("ARISTAS")) {
                    break;
                }

                if (line.startsWith("VERTICES=")) {
                    verticesTexto = line.substring(9).trim();
                }
            }

            if (verticesTexto == null || verticesTexto.isEmpty()) {
                throw new IllegalArgumentException("Archivo inválido: no contiene vértices.");
            }

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("END")) {
                    break;
                }

                if (line.isEmpty()) continue;

                String[] partes = line.split("\\|", -1);
                if (partes.length != 3) continue;

                String origen = partes[0].trim();
                String destino = partes[1].trim();
                String peso = partes[2].trim();

                if (!origen.isEmpty() && !destino.isEmpty() && !peso.isEmpty()) {
                    if (aristasTexto.length() > 0) {
                        aristasTexto.append(", ");
                    }
                    aristasTexto.append(origen)
                               .append("-")
                               .append(destino)
                               .append("-")
                               .append(peso);
                }
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