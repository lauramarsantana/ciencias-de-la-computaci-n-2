package utilities;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Text;
import java.util.*;

public class GrafoVisual {

    public static void dibujar(Grafo grafo, Pane panel) {
        // 1. AGRUPAR ARISTAS
        Map<String, List<Arista>> grupos = new HashMap<>();
        for (Arista a : grafo.getAristas()) {
            String v1 = a.getVerticeOrigen().getName();
            String v2 = a.getVerticeDestino().getName();

            String clave;
            if (v1.equals(v2)) {
                clave = "LOOP-" + v1; // Clave especial para autolazos
            } else {
                clave = (v1.compareTo(v2) < 0) ? v1 + "-" + v2 : v2 + "-" + v1;
            }
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(a);
        }

        // 2. DIBUJAR ARISTAS
        for (List<Arista> listaAristas : grupos.values()) {
            int total = listaAristas.size();
            for (int i = 0; i < total; i++) {
                Arista a = listaAristas.get(i);
                double x1 = a.getVerticeOrigen().getPositionX();
                double y1 = a.getVerticeOrigen().getPositionY();
                double x2 = a.getVerticeDestino().getPositionX();
                double y2 = a.getVerticeDestino().getPositionY();

                // CASO A: Autolazo (Bucle sobre el mismo vértice)
                if (a.getVerticeOrigen().equals(a.getVerticeDestino())) {
                    // Dibujamos un círculo pequeño arriba del vértice
                    double radioLazo = 15 + (i * 10); // Se hacen más grandes si hay varios loops
                    Circle lazo = new Circle(x1, y1 - 15, radioLazo);
                    lazo.setFill(null);
                    lazo.setStroke(Color.BLUE);
                    lazo.setStrokeWidth(1.5);
                    panel.getChildren().add(lazo);
                }
                // CASO B: Arista única entre dos vértices diferentes
                else if (total == 1) {
                    Line linea = new Line(x1, y1, x2, y2);
                    linea.setStroke(Color.BLUE);
                    linea.setStrokeWidth(1.5);
                    panel.getChildren().add(linea);
                }
                // CASO C: Multigrafo
                // CASO C: Multigrafo
                else {
                    double midX = (x1 + x2) / 2;
                    double midY = (y1 + y2) / 2;
                    double dx = x2 - x1;
                    double dy = y2 - y1;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    // Si es la primera arista (i=0), hacemos línea recta, pero AZUL
                    if (i == 0) {
                        Line linea = new Line(x1, y1, x2, y2);
                        linea.setStroke(Color.BLUE);
                        linea.setStrokeWidth(1.5);
                        panel.getChildren().add(linea);
                    } else {
                        // Para las demás, curvas para que no se tapen
                        double offset = (i % 2 == 0 ? 35.0 : -35.0) * ((i + 1) / 2);
                        QuadCurve curva = new QuadCurve(x1, y1, midX - (dy * offset / dist), midY + (dx * offset / dist), x2, y2);
                        curva.setStroke(Color.BLUE);
                        curva.setStrokeWidth(1.5);
                        curva.setFill(null);
                        panel.getChildren().add(curva);
                    }
                }
            }
        }

        // 3. DIBUJAR VÉRTICES (Encima de las aristas)
        for (Vertice v : grafo.getVertices().values()) {
            double x = v.getPositionX();
            double y = v.getPositionY();

            Circle circulo = new Circle(x, y, 15);
            circulo.setFill(Color.WHITE);
            circulo.setStroke(Color.BLUE);
            circulo.setStrokeWidth(1.5);

            // Reemplazamos el guion bajo por coma solo para mostrarlo bonito
            String nombreVisual = v.getName().replace("_", ",");
            Text texto = new Text(x - 6, y + 4, nombreVisual);
            texto.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

            panel.getChildren().addAll(circulo, texto);
        }
    }

    public static void reacomodarCircular(Pane panel, List<Vertice> vertices) {
        if (vertices.isEmpty()) return;
        double centroX = panel.getWidth() / 2;
        double centroY = panel.getHeight() / 2;
        double radio = Math.min(centroX, centroY) - 40;

        for (int i = 0; i < vertices.size(); i++) {
            double angulo = 2 * Math.PI * i / vertices.size();
            vertices.get(i).setPositionX(centroX + radio * Math.cos(angulo));
            vertices.get(i).setPositionY(centroY + radio * Math.sin(angulo));
        }
    }

    public static void reacomodarMatriz(Pane pane, Grafo g, int numFilas, int numCols) {
        double ancho = pane.getWidth();
        double alto = pane.getHeight();
        double margenX = ancho / (numCols + 1);
        double margenY = alto / (numFilas + 1);

        List<Vertice> lista = new ArrayList<>(g.getVertices().values());
        // No ordenes aquí, usa el orden en que fueron agregados al Grafo

        int k = 0;
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numCols; j++) {
                if (k < lista.size()) {
                    Vertice v = lista.get(k++);
                    v.setPositionX(margenX * (j + 1)); // Columnas (c, d, e)
                    v.setPositionY(margenY * (i + 1)); // Filas (a, b)
                }
            }
        }
    }

    public static void reacomodarVertical(Pane panel, List<Vertice> vertices) {
        if (vertices.isEmpty()) return;
        double centroX = panel.getWidth() / 2;
        double espaciadoY = panel.getHeight() / (vertices.size() + 1);

        for (int i = 0; i < vertices.size(); i++) {
            vertices.get(i).setPositionX(centroX);
            vertices.get(i).setPositionY(espaciadoY * (i + 1));
        }
    }
}