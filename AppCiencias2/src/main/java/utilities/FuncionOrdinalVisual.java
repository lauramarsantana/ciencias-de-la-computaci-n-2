package utilities;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class FuncionOrdinalVisual {

    public static void dibujar(GrafoOrdinal grafo, Pane panel) {
        panel.getChildren().clear();

        if (grafo == null || grafo.getVertices().isEmpty()) {
            return;
        }

        // Primero dibujar las aristas dirigidas
        for (AristaDirigida arista : grafo.getAristas()) {
            VerticeOrdinal origen = grafo.buscarVertice(arista.getOrigen());
            VerticeOrdinal destino = grafo.buscarVertice(arista.getDestino());

            if (origen == null || destino == null) {
                continue;
            }

            dibujarFlecha(panel, origen.getX(), origen.getY(), destino.getX(), destino.getY());
        }

        // Luego dibujar los vértices
        dibujarVertices(grafo, panel);
    }

    private static void dibujarVertices(GrafoOrdinal grafo, Pane panel) {
        for (VerticeOrdinal v : grafo.getVertices()) {
            Circle circulo = new Circle(v.getX(), v.getY(), 20);
            circulo.setFill(Color.LIGHTBLUE);
            circulo.setStroke(Color.BLACK);

            Label nombre = new Label(v.getNombre());
            nombre.setLayoutX(v.getX() - 6);
            nombre.setLayoutY(v.getY() - 10);
            nombre.setStyle("-fx-font-weight: bold;");

            Label ordinal = new Label(
                    v.getEtiquetaOrdinal() > 0 ? String.valueOf(v.getEtiquetaOrdinal()) : ""
            );
            ordinal.setLayoutX(v.getX() - 4);
            ordinal.setLayoutY(v.getY() - 36);
            ordinal.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-padding: 1 4 1 4;");

            panel.getChildren().addAll(circulo, nombre, ordinal);
        }
    }

    private static void dibujarFlecha(Pane panel, double x1, double y1, double x2, double y2) {
        double radio = 20;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double longitud = Math.sqrt(dx * dx + dy * dy);

        if (longitud == 0) {
            return;
        }

        double ux = dx / longitud;
        double uy = dy / longitud;

        double inicioX = x1 + radio * ux;
        double inicioY = y1 + radio * uy;
        double finX = x2 - radio * ux;
        double finY = y2 - radio * uy;

        Line linea = new Line(inicioX, inicioY, finX, finY);
        linea.setStroke(Color.GRAY);
        linea.setStrokeWidth(2);

        double tamFlecha = 10;

        double angulo = Math.atan2(dy, dx);

        double xArrow1 = finX - tamFlecha * Math.cos(angulo - Math.PI / 6);
        double yArrow1 = finY - tamFlecha * Math.sin(angulo - Math.PI / 6);

        double xArrow2 = finX - tamFlecha * Math.cos(angulo + Math.PI / 6);
        double yArrow2 = finY - tamFlecha * Math.sin(angulo + Math.PI / 6);

        Polygon punta = new Polygon();
        punta.getPoints().addAll(
                finX, finY,
                xArrow1, yArrow1,
                xArrow2, yArrow2
        );
        punta.setFill(Color.GRAY);

        panel.getChildren().addAll(linea, punta);
    }
}