package utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class ArbolGeneradorVisual {

    public static void dibujarGrafoCompleto(GrafoPonderado grafo, Pane panel) {
        panel.getChildren().clear();

        if (grafo == null || grafo.getVertices().isEmpty()) {
            return;
        }

        List<AristaVisual> aristasVisuales = new ArrayList<>();
        
        double ancho = panel.getWidth();
        double alto = panel.getHeight();

        if (ancho <= 0) ancho = panel.getPrefWidth();
        if (alto <= 0) alto = panel.getPrefHeight();
        if (ancho <= 0) ancho = 780;
        if (alto <= 0) alto = 430;

        Map<String, double[]> posiciones = calcularPosiciones(grafo.getVertices(), ancho, alto);

        // Dibujar todas las aristas del grafo
        for (AristaPonderada arista : grafo.getAristas()) {
            double[] p1 = posiciones.get(arista.getOrigen());
            double[] p2 = posiciones.get(arista.getDestino());

            if (p1 == null || p2 == null) {
                continue;
            }

            Line linea = new Line(p1[0], p1[1], p2[0], p2[1]);
            linea.setStroke(Color.GRAY);
            linea.setStrokeWidth(2);

            Label peso = crearLabelPeso(arista, p1, p2);
            
            
            aristasVisuales.add(new AristaVisual(
                    arista.getOrigen(),
                    arista.getDestino(),
                    linea,
                    peso
            ));
            
            panel.getChildren().addAll(linea, peso);
        }

        dibujarVertices(grafo, panel, posiciones, aristasVisuales);
    }

    public static void dibujar(GrafoPonderado grafo, List<AristaPonderada> aristasResultado, Pane panel) {
        panel.getChildren().clear();

        if (grafo == null || grafo.getVertices().isEmpty()) {
            return;
        }
        
        List<AristaVisual> aristasVisuales = new ArrayList<>();

        double ancho = panel.getWidth();
        double alto = panel.getHeight();

        if (ancho <= 0) ancho = panel.getPrefWidth();
        if (alto <= 0) alto = panel.getPrefHeight();
        if (ancho <= 0) ancho = 780;
        if (alto <= 0) alto = 430;

        Map<String, double[]> posiciones = calcularPosiciones(grafo.getVertices(), ancho, alto);

        // Primero dibujar todo el grafo en gris claro
        for (AristaPonderada arista : grafo.getAristas()) {
        double[] p1 = posiciones.get(arista.getOrigen());
        double[] p2 = posiciones.get(arista.getDestino());

        if (p1 == null || p2 == null) {
            continue;
        }

        Line linea = new Line(p1[0], p1[1], p2[0], p2[1]);
        linea.setStroke(Color.LIGHTGRAY);
        linea.setStrokeWidth(1.5);
        
        aristasVisuales.add(new AristaVisual(
            arista.getOrigen(),
            arista.getDestino(),
            linea,
            null
    ));
        panel.getChildren().add(linea);
    }

        // Luego dibujar las aristas del árbol resaltadas
        for (AristaPonderada arista : aristasResultado) {
        double[] p1 = posiciones.get(arista.getOrigen());
        double[] p2 = posiciones.get(arista.getDestino());

        if (p1 == null || p2 == null) {
            continue;
        }

        Line linea = new Line(p1[0], p1[1], p2[0], p2[1]);
        linea.setStroke(Color.RED);
        linea.setStrokeWidth(3);

        Label peso = crearLabelPeso(arista, p1, p2);
        peso.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-padding: 1 3 1 3;");
        
        aristasVisuales.add(new AristaVisual(
        arista.getOrigen(),
        arista.getDestino(),
        linea,
        peso
        ));

        panel.getChildren().addAll(linea, peso);
    }

        dibujarVertices(grafo, panel, posiciones, aristasVisuales);
    }

    private static Label crearLabelPeso(AristaPonderada arista, double[] p1, double[] p2) {
    double midX = (p1[0] + p2[0]) / 2.0;
    double midY = (p1[1] + p2[1]) / 2.0;

    double dx = p2[0] - p1[0];
    double dy = p2[1] - p1[1];
    double longitud = Math.sqrt(dx * dx + dy * dy);

    if (longitud == 0) {
        longitud = 1;
    }

    // Vector perpendicular normalizado
    double perpX = -dy / longitud;
    double perpY = dx / longitud;

    // Distancia más corta (ajustada)
    double offset = 9;

    Label peso = new Label(String.valueOf(arista.getPeso()));
    peso.setLayoutX(midX + perpX * offset - 6);
    peso.setLayoutY(midY + perpY * offset - 8);
    peso.setStyle("-fx-background-color: white; -fx-padding: 1 3 1 3; -fx-font-weight: bold;");

    return peso;
}

    private static void dibujarVertices(
        GrafoPonderado grafo,
        Pane panel,
        Map<String, double[]> posiciones,
        List<AristaVisual> aristasVisuales
) {
    for (String v : grafo.getVertices()) {
        double[] p = posiciones.get(v);

        if (p == null) {
            continue;
        }

        Circle circulo = new Circle(p[0], p[1], RADIO_NODO);
        circulo.setFill(Color.LIGHTBLUE);
        circulo.setStroke(Color.BLACK);

        Label nombre = new Label(v);
        nombre.setLayoutX(p[0] - 5);
        nombre.setLayoutY(p[1] - 10);
        nombre.setStyle("-fx-font-weight: bold;");
        nombre.setMouseTransparent(true);

        habilitarMovimiento(v, circulo, nombre, posiciones, aristasVisuales, panel);

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

            posiciones.put(vertices.get(0), new double[]{centerLeftX, topY});
            posiciones.put(vertices.get(1), new double[]{leftX, midY});
            posiciones.put(vertices.get(2), new double[]{centerX, bottomY});
            posiciones.put(vertices.get(3), new double[]{centerRightX, topY});
            posiciones.put(vertices.get(4), new double[]{rightX, midY});

            return posiciones;
        }

        // Caso especial: 3 vértices
        if (n == 3) {
            double centroX = ancho / 2.0;
            double centroY = alto / 2.0;

            posiciones.put(vertices.get(0), new double[]{centroX, centroY - 80});
            posiciones.put(vertices.get(1), new double[]{centroX - 100, centroY + 60});
            posiciones.put(vertices.get(2), new double[]{centroX + 100, centroY + 60});

            return posiciones;
        }

        // Caso especial: 4 vértices
        if (n == 4) {
            double centroX = ancho / 2.0;
            double centroY = alto / 2.0;

            posiciones.put(vertices.get(0), new double[]{centroX - 100, centroY - 80});
            posiciones.put(vertices.get(1), new double[]{centroX + 100, centroY - 80});
            posiciones.put(vertices.get(2), new double[]{centroX - 150, centroY + 80});
            posiciones.put(vertices.get(3), new double[]{centroX + 150, centroY + 80});

            return posiciones;
        }
        
        // Caso especial: 7 vértices, distribución tipo trapecio por niveles
        if (n == 7) {
            double centroX = ancho / 2.0;
            double centroY = alto / 2.0;

            double topY = centroY - 100;
            double midY = centroY;
            double bottomY = centroY + 100;

            posiciones.put(vertices.get(0), new double[]{centroX - 120, topY});
            posiciones.put(vertices.get(1), new double[]{centroX + 120, topY});

            posiciones.put(vertices.get(2), new double[]{centroX - 220, midY});
            posiciones.put(vertices.get(3), new double[]{centroX, midY});
            posiciones.put(vertices.get(4), new double[]{centroX + 220, midY});

            posiciones.put(vertices.get(5), new double[]{centroX - 120, bottomY});
            posiciones.put(vertices.get(6), new double[]{centroX + 120, bottomY});

            return posiciones;
        }

        // Caso general: distribución automática por filas
        int maxPorFila = 3;
        int filas = (int) Math.ceil((double) n / maxPorFila);

        double margenX = 80;
        double margenY = 70;

        double espacioVertical = 0;
        if (filas > 1) {
            espacioVertical = (alto - 2 * margenY) / (filas - 1);
        }

        int indice = 0;

        for (int fila = 0; fila < filas; fila++) {
            int restantes = n - indice;
            int enEstaFila = Math.min(maxPorFila, restantes);

            double espacioHorizontal = (ancho - 2 * margenX) / (enEstaFila + 1);
            double y = margenY + fila * espacioVertical;

            for (int col = 0; col < enEstaFila; col++) {
                double x = margenX + (col + 1) * espacioHorizontal;
                posiciones.put(vertices.get(indice), new double[]{x, y});
                indice++;
            }
        }

        return posiciones;
    }
    
    private static final double RADIO_NODO = 20;

    private static class AristaVisual {
        String origen;
        String destino;
        Line linea;
        Label peso;

        AristaVisual(String origen, String destino, Line linea, Label peso) {
            this.origen = origen;
            this.destino = destino;
            this.linea = linea;
            this.peso = peso;
        }
    }
    private static void habilitarMovimiento(
        String vertice,
        Circle circulo,
        Label nombre,
        Map<String, double[]> posiciones,
        List<AristaVisual> aristasVisuales,
        Pane panel
) {
    final double[] offset = new double[2];

    circulo.setOnMousePressed(e -> {
        offset[0] = e.getSceneX() - circulo.getCenterX();
        offset[1] = e.getSceneY() - circulo.getCenterY();
        e.consume();
    });

    circulo.setOnMouseDragged(e -> {
        double nuevoX = e.getSceneX() - offset[0];
        double nuevoY = e.getSceneY() - offset[1];

        nuevoX = Math.max(RADIO_NODO, Math.min(nuevoX, panel.getWidth() - RADIO_NODO));
        nuevoY = Math.max(RADIO_NODO, Math.min(nuevoY, panel.getHeight() - RADIO_NODO));

        circulo.setCenterX(nuevoX);
        circulo.setCenterY(nuevoY);

        nombre.setLayoutX(nuevoX - 5);
        nombre.setLayoutY(nuevoY - 10);

        posiciones.put(vertice, new double[]{nuevoX, nuevoY});

        actualizarAristas(posiciones, aristasVisuales);

        e.consume();
    });
    }
    private static void actualizarAristas(
        Map<String, double[]> posiciones,
        List<AristaVisual> aristasVisuales
) {
    for (AristaVisual av : aristasVisuales) {
        double[] p1 = posiciones.get(av.origen);
        double[] p2 = posiciones.get(av.destino);

        if (p1 == null || p2 == null) {
            continue;
        }

        av.linea.setStartX(p1[0]);
        av.linea.setStartY(p1[1]);
        av.linea.setEndX(p2[0]);
        av.linea.setEndY(p2[1]);

        if (av.peso != null) {
            actualizarLabelPeso(av.peso, p1, p2);
        }
    }}
    private static void actualizarLabelPeso(Label peso, double[] p1, double[] p2) {
    double midX = (p1[0] + p2[0]) / 2.0;
    double midY = (p1[1] + p2[1]) / 2.0;

    double dx = p2[0] - p1[0];
    double dy = p2[1] - p1[1];
    double longitud = Math.sqrt(dx * dx + dy * dy);

    if (longitud == 0) {
        longitud = 1;
    }

    double perpX = -dy / longitud;
    double perpY = dx / longitud;

    double offset = 9;

    peso.setLayoutX(midX + perpX * offset - 6);
    peso.setLayoutY(midY + perpY * offset - 8);
}
}

