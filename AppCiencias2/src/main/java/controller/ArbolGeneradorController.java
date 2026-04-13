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

    private GrafoPonderado construirGrafo() {
        GrafoPonderado g = new GrafoPonderado();

        String[] vertices = verticesField.getText().split(",");
        for (String v : vertices) {
            String limpio = v.trim();
            if (!limpio.isEmpty()) {
                g.agregarVertice(limpio);
            }
        }

        String[] aristas = aristasField.getText().split(",");
        for (String a : aristas) {
            String[] partes = a.trim().split("-");

            if (partes.length != 3) {
                throw new IllegalArgumentException("Formato inválido en arista: " + a + ". Usa origen-destino-peso");
            }

            String origen = partes[0].trim();
            String destino = partes[1].trim();
            int peso = Integer.parseInt(partes[2].trim());

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
