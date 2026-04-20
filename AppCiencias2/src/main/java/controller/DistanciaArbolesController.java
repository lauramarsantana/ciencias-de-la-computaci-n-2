package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import utilities.AristaPonderada;
import utilities.ArbolDistanciaVisual;
import utilities.ArbolGeneradorVisual;
import utilities.DistanciaArbolesService;
import utilities.GrafoPonderado;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DistanciaArbolesController {

    @FXML
    private TextField verticesField1;

    @FXML
    private TextField aristasField1;

    @FXML
    private TextField verticesField2;

    @FXML
    private TextField aristasField2;

    @FXML
    private Label infoArbol1;

    @FXML
    private Label infoArbol2;

    @FXML
    private Label infoResultado;

    @FXML
    private TextArea infoArea;

    @FXML
    private Pane panelArbol1;

    @FXML
    private Pane panelArbol2;

    @FXML
    private Pane panelResultado;

    private LayoutController layoutController;
    private GrafoPonderado arbol1;
    private GrafoPonderado arbol2;

    public void setLayoutController(LayoutController layoutController) {
        this.layoutController = layoutController;
    }

    @FXML
    private void mostrarArbol1() {
        try {
            arbol1 = construirArbol(verticesField1.getText(), aristasField1.getText());

            ArbolGeneradorVisual.dibujarGrafoCompleto(arbol1, panelArbol1);
            infoArbol1.setText("Árbol 1");

            StringBuilder sb = new StringBuilder();
            sb.append("Árbol 1\n\n");
            sb.append("Vértices:\n");
            for (String v : arbol1.getVertices()) {
                sb.append("- ").append(v).append("\n");
            }

            sb.append("\nAristas:\n");
            for (AristaPonderada a : arbol1.getAristas()) {
                sb.append(a).append("\n");
            }

            infoArea.setText(sb.toString());

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void mostrarArbol2() {
        try {
            arbol2 = construirArbol(verticesField2.getText(), aristasField2.getText());

            ArbolGeneradorVisual.dibujarGrafoCompleto(arbol2, panelArbol2);
            infoArbol2.setText("Árbol 2");

            StringBuilder sb = new StringBuilder();
            sb.append("Árbol 2\n\n");
            sb.append("Vértices:\n");
            for (String v : arbol2.getVertices()) {
                sb.append("- ").append(v).append("\n");
            }

            sb.append("\nAristas:\n");
            for (AristaPonderada a : arbol2.getAristas()) {
                sb.append(a).append("\n");
            }

            infoArea.setText(sb.toString());

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void calcularDistancia() {
        try {
            arbol1 = construirArbol(verticesField1.getText(), aristasField1.getText());
            arbol2 = construirArbol(verticesField2.getText(), aristasField2.getText());

            validarMismosVertices(arbol1, arbol2);

            // Mostrar cada árbol si aún no se ha dibujado
            ArbolGeneradorVisual.dibujarGrafoCompleto(arbol1, panelArbol1);
            ArbolGeneradorVisual.dibujarGrafoCompleto(arbol2, panelArbol2);

            GrafoPonderado grafoBase = construirGrafoUnion(arbol1, arbol2);

            ArbolDistanciaVisual.dibujarComparacion(
                    grafoBase,
                    arbol1.getAristas(),
                    arbol2.getAristas(),
                    panelResultado
            );

            infoResultado.setText("Resultado / Comparación");

            int distancia = DistanciaArbolesService.calcularDistancia(
                    arbol1.getAristas(),
                    arbol2.getAristas()
            );

            Set<Integer> comunes = DistanciaArbolesService.obtenerComunes(
                    arbol1.getAristas(), arbol2.getAristas()
            );

            Set<Integer> solo1 = DistanciaArbolesService.obtenerSoloArbol1(
                    arbol1.getAristas(), arbol2.getAristas()
            );

            Set<Integer> solo2 = DistanciaArbolesService.obtenerSoloArbol2(
                    arbol1.getAristas(), arbol2.getAristas()
            );

            StringBuilder sb = new StringBuilder();
            sb.append("Comparación de árboles\n\n");

            sb.append("Aristas del Árbol 1:\n");
            for (AristaPonderada a : arbol1.getAristas()) {
                sb.append(a).append("\n");
            }

            sb.append("\nAristas del Árbol 2:\n");
            for (AristaPonderada a : arbol2.getAristas()) {
                sb.append(a).append("\n");
            }

            sb.append("\nAristas comunes (IDs): ").append(comunes).append("\n");
            sb.append("Solo en Árbol 1 (IDs): ").append(solo1).append("\n");
            sb.append("Solo en Árbol 2 (IDs): ").append(solo2).append("\n");
            sb.append("\nDistancia = ").append(distancia);

            infoArea.setText(sb.toString());

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }
    
    @FXML
    private void guardarEstructura() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar distancia entre árboles");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Distancia entre Árboles (*.arb)", "*.arb")
            );

            File file = fc.showSaveDialog(panelResultado.getScene().getWindow());
            if (file == null) return;

            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                bw.write("TIPO=DISTANCIA_ARBOLES");
                bw.newLine();

                bw.write("VERTICES1=" + verticesField1.getText().trim());
                bw.newLine();

                bw.write("ARISTAS1=" + aristasField1.getText().trim());
                bw.newLine();

                bw.write("VERTICES2=" + verticesField2.getText().trim());
                bw.newLine();

                bw.write("ARISTAS2=" + aristasField2.getText().trim());
                bw.newLine();

                bw.write("END");
                bw.newLine();
            }

            infoArea.setText("Estructura guardada correctamente: " + file.getName());

        } catch (Exception e) {
            mostrarAlerta("Error", "Error guardando: " + e.getMessage());
        }
    }
    
    @FXML
    private void cargarEstructura() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar distancia entre árboles");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Distancia entre Árboles (*.arb)", "*.arb")
        );

        File file = fc.showOpenDialog(panelResultado.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            String vertices1 = "";
            String aristas1 = "";
            String vertices2 = "";
            String aristas2 = "";

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("VERTICES1=")) {
                    vertices1 = line.substring("VERTICES1=".length()).trim();
                } else if (line.startsWith("ARISTAS1=")) {
                    aristas1 = line.substring("ARISTAS1=".length()).trim();
                } else if (line.startsWith("VERTICES2=")) {
                    vertices2 = line.substring("VERTICES2=".length()).trim();
                } else if (line.startsWith("ARISTAS2=")) {
                    aristas2 = line.substring("ARISTAS2=".length()).trim();
                } else if (line.equals("END")) {
                    break;
                }
            }

            verticesField1.setText(vertices1);
            aristasField1.setText(aristas1);
            verticesField2.setText(vertices2);
            aristasField2.setText(aristas2);

            arbol1 = null;
            arbol2 = null;

            panelArbol1.getChildren().clear();
            panelArbol2.getChildren().clear();
            panelResultado.getChildren().clear();

            infoArbol1.setText("Árbol 1");
            infoArbol2.setText("Árbol 2");
            infoResultado.setText("Resultado / Comparación");

            infoArea.setText("Estructura cargada correctamente: " + file.getName());

        } catch (Exception e) {
            mostrarAlerta("Error", "Error cargando: " + e.getMessage());
        }
    }

    @FXML
    private void limpiarEstructura() {
        arbol1 = null;
        arbol2 = null;

        verticesField1.clear();
        aristasField1.clear();
        verticesField2.clear();
        aristasField2.clear();

        infoArbol1.setText("Árbol 1");
        infoArbol2.setText("Árbol 2");
        infoResultado.setText("Resultado / Comparación");

        infoArea.setText("Estructura limpiada.");

        panelArbol1.getChildren().clear();
        panelArbol2.getChildren().clear();
        panelResultado.getChildren().clear();
    }

    private GrafoPonderado construirArbol(String textoVertices, String textoAristas) {
        GrafoPonderado g = new GrafoPonderado();

        textoVertices = textoVertices.trim();
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

        textoAristas = textoAristas.trim();
        if (textoAristas.isEmpty()) {
            throw new IllegalArgumentException("Debes ingresar las aristas.");
        }

        String[] aristas = textoAristas.split(",");
        for (String a : aristas) {
            String[] partes = a.trim().split("-");

            if (partes.length != 3) {
                throw new IllegalArgumentException(
                        "Formato inválido en arista: " + a + ". Usa origen-destino-id"
                );
            }

            String origen = partes[0].trim();
            String destino = partes[1].trim();
            int id;

            try {
                id = Integer.parseInt(partes[2].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID inválido en la arista: " + a);
            }

            g.agregarArista(origen, destino, id);
        }

        return g;
    }

    private void validarMismosVertices(GrafoPonderado a1, GrafoPonderado a2) {
        List<String> v1 = new ArrayList<>(a1.getVertices());
        List<String> v2 = new ArrayList<>(a2.getVertices());

        v1.sort(String::compareTo);
        v2.sort(String::compareTo);

        if (!v1.equals(v2)) {
            throw new IllegalArgumentException("Ambos árboles deben tener el mismo conjunto de vértices.");
        }
    }

    private GrafoPonderado construirGrafoUnion(GrafoPonderado a1, GrafoPonderado a2) {
        GrafoPonderado g = new GrafoPonderado();

        for (String v : a1.getVertices()) {
            g.agregarVertice(v);
        }

        for (AristaPonderada a : a1.getAristas()) {
            g.agregarArista(a.getOrigen(), a.getDestino(), a.getPeso());
        }

        for (AristaPonderada a : a2.getAristas()) {
            boolean existe = false;

            for (AristaPonderada b : g.getAristas()) {
                if (b.getPeso() == a.getPeso()) {
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                g.agregarArista(a.getOrigen(), a.getDestino(), a.getPeso());
            }
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