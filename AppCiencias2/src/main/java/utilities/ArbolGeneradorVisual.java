package utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class ArbolGeneradorVisual {

    public static void dibujar(GrafoPonderado grafo, List<AristaPonderada> aristasResultado, Pane panel) {
        panel.getChildren().clear();

        if (grafo == null || grafo.getVertices().isEmpty()) {
            return;
        }

        double ancho = panel.getWidth();
        double alto = panel.getHeight();

        if (ancho <= 0) ancho = panel.getPrefWidth();
        if (alto <= 0) alto = panel.getPrefHeight();
        if (ancho <= 0) ancho = 780;
        if (alto <= 0) alto = 430;

        Map<String, double[]> posiciones = calcularPosiciones(grafo.getVertices(), ancho, alto);

        // Dibujar aristas seleccionadas
        for (AristaPonderada arista : aristasResultado) {
            double[] p1 = posiciones.get(arista.getOrigen());
            double[] p2 = posiciones.get(arista.getDestino());

            Line linea = new Line(p1[0], p1[1], p2[0], p2[1]);
            linea.setStrokeWidth(2);

            double midX = (p1[0] + p2[0]) / 2.0;
            double midY = (p1[1] + p2[1]) / 2.0;

            Label peso = new Label(String.valueOf(arista.getPeso()));
            peso.setLayoutX(midX + 5);
            peso.setLayoutY(midY - 5);

            panel.getChildren().addAll(linea, peso);
        }

        // Dibujar nodos
        for (String v : grafo.getVertices()) {
            double[] p = posiciones.get(v);

            Circle circulo = new Circle(p[0], p[1], 20);
            circulo.setFill(Color.LIGHTBLUE);
            circulo.setStroke(Color.BLACK);

            Label nombre = new Label(v);
            nombre.setLayoutX(p[0] - 5);
            nombre.setLayoutY(p[1] - 10);

            panel.getChildren().addAll(circulo, nombre);
        }
    }

    private static Map<String, double[]> calcularPosiciones(List<String> vertices, double ancho, double alto) {
        Map<String, double[]> posiciones = new HashMap<>();

        int n = vertices.size();

        // Caso especial: 5 vértices en forma de trapecio/camino como en el cuaderno
        if (n == 5) {
            double centroX = ancho / 2.0;
            double centroY = alto / 2.0;

            double topY = centroY - 90;
            double midY = centroY;
            double bottomY = centroY + 90;

            double leftX = centroX - 170;
            double centerLeftX = centroX - 60;
            double centerX = centroX;
            double centerRightX = centroX + 60;
            double rightX = centroX + 170;

            // Se asignan según el orden en que el usuario escribe los vértices
            posiciones.put(vertices.get(0), new double[]{centerLeftX, topY});    // arriba izquierda
            posiciones.put(vertices.get(1), new double[]{leftX, midY});          // medio izquierda
            posiciones.put(vertices.get(2), new double[]{centerX, bottomY});     // abajo centro
            posiciones.put(vertices.get(3), new double[]{centerRightX, topY});   // arriba derecha
            posiciones.put(vertices.get(4), new double[]{rightX, midY});         // medio derecha

            return posiciones;
        }

        // Caso especial: 3 vértices, forma simple
        if (n == 3) {
            double centroX = ancho / 2.0;
            double centroY = alto / 2.0;

            posiciones.put(vertices.get(0), new double[]{centroX, centroY - 80});
            posiciones.put(vertices.get(1), new double[]{centroX - 100, centroY + 60});
            posiciones.put(vertices.get(2), new double[]{centroX + 100, centroY + 60});

            return posiciones;
        }

        // Caso especial: 4 vértices, forma de trapecio simple
        if (n == 4) {
            double centroX = ancho / 2.0;
            double centroY = alto / 2.0;

            posiciones.put(vertices.get(0), new double[]{centroX - 100, centroY - 80});
            posiciones.put(vertices.get(1), new double[]{centroX + 100, centroY - 80});
            posiciones.put(vertices.get(2), new double[]{centroX - 150, centroY + 80});
            posiciones.put(vertices.get(3), new double[]{centroX + 150, centroY + 80});

            return posiciones;
        }

        // Caso general: distribución horizontal por niveles visuales simples
        double separacion = ancho / (n + 1);
        double y = alto / 2.0;

        for (int i = 0; i < n; i++) {
            posiciones.put(vertices.get(i), new double[]{(i + 1) * separacion, y});
        }

        return posiciones;
    }
}