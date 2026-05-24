package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class RepresentacionGrafoVisual {

    private static final double RADIO_NODO = 20;

    private static class AristaVisual {
        AristaDirigida arista;
        Line linea;
        Polygon punta;
        Label nombre;

        AristaVisual(AristaDirigida arista, Line linea, Polygon punta, Label nombre) {
            this.arista = arista;
            this.linea = linea;
            this.punta = punta;
            this.nombre = nombre;
        }
    }

    public static void dibujar(
            GrafoOrdinal grafo,
            Pane panel,
            boolean dirigido,
            Consumer<String> manejadorClick) {

        panel.getChildren().clear();

        if (grafo == null || grafo.getVertices().isEmpty()) {
            return;
        }

        List<AristaVisual> aristasVisuales = new ArrayList<>();

        int contador = 0;

        for (AristaDirigida arista : grafo.getAristas()) {
            VerticeOrdinal origen = grafo.buscarVertice(arista.getOrigen());
            VerticeOrdinal destino = grafo.buscarVertice(arista.getDestino());

            if (origen == null || destino == null) {
                continue;
            }

            String nombreArista = generarNombreArista(contador);

            AristaVisual av = dibujarArista(
                    panel,
                    arista,
                    nombreArista,
                    origen.getX(),
                    origen.getY(),
                    destino.getX(),
                    destino.getY(),
                    dirigido
            );

            aristasVisuales.add(av);
            contador++;
        }

        dibujarVertices(grafo, panel, manejadorClick, aristasVisuales, dirigido);
    }

    private static String generarNombreArista(int indice) {
        int letra = indice % 26;
        int grupo = indice / 26;

        char base = (char) ('a' + letra);

        if (grupo == 0) {
            return String.valueOf(base);
        }

        return String.valueOf(base) + grupo;
    }

    private static void dibujarVertices(
            GrafoOrdinal grafo,
            Pane panel,
            Consumer<String> manejadorClick,
            List<AristaVisual> aristasVisuales,
            boolean dirigido) {

        for (VerticeOrdinal v : grafo.getVertices()) {
            Circle circulo = new Circle(v.getX(), v.getY(), RADIO_NODO);
            circulo.setFill(Color.LIGHTBLUE);
            circulo.setStroke(Color.BLACK);

            Label nombre = new Label(v.getNombre());
            nombre.setLayoutX(v.getX() - 8);
            nombre.setLayoutY(v.getY() - 10);
            nombre.setStyle("-fx-font-weight: bold;");
            nombre.setMouseTransparent(true);

            habilitarMovimiento(
                    grafo,
                    v,
                    circulo,
                    nombre,
                    aristasVisuales,
                    panel,
                    manejadorClick,
                    dirigido
            );

            panel.getChildren().addAll(circulo, nombre);
        }
    }

    private static void habilitarMovimiento(
            GrafoOrdinal grafo,
            VerticeOrdinal vertice,
            Circle circulo,
            Label nombre,
            List<AristaVisual> aristasVisuales,
            Pane panel,
            Consumer<String> manejadorClick,
            boolean dirigido) {

        final double[] offset = new double[2];

        circulo.setOnMouseClicked(e -> {
            if (manejadorClick != null && !e.isStillSincePress()) {
                return;
            }

            if (manejadorClick != null) {
                manejadorClick.accept(vertice.getNombre());
            }

            e.consume();
        });

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

            nombre.setLayoutX(nuevoX - 8);
            nombre.setLayoutY(nuevoY - 10);

            vertice.setX((int) nuevoX);
            vertice.setY((int) nuevoY);

            actualizarAristas(grafo, aristasVisuales, dirigido);

            e.consume();
        });
    }

    private static AristaVisual dibujarArista(
            Pane panel,
            AristaDirigida arista,
            String nombreArista,
            double x1,
            double y1,
            double x2,
            double y2,
            boolean dirigido) {

        Line linea = new Line();
        linea.setStroke(Color.GRAY);
        linea.setStrokeWidth(2);

        Polygon punta = new Polygon();
        punta.setFill(Color.GRAY);

        Label nombre = new Label(nombreArista);
        nombre.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-background-color: white;" +
                "-fx-border-color: gray;" +
                "-fx-padding: 1 4 1 4;"
        );
        nombre.setMouseTransparent(true);

        actualizarArista(linea, punta, nombre, x1, y1, x2, y2, dirigido);

        panel.getChildren().add(linea);

        if (dirigido) {
            panel.getChildren().add(punta);
        }

        panel.getChildren().add(nombre);

        return new AristaVisual(arista, linea, punta, nombre);
    }

    private static void actualizarAristas(
            GrafoOrdinal grafo,
            List<AristaVisual> aristasVisuales,
            boolean dirigido) {

        for (AristaVisual av : aristasVisuales) {
            VerticeOrdinal origen = grafo.buscarVertice(av.arista.getOrigen());
            VerticeOrdinal destino = grafo.buscarVertice(av.arista.getDestino());

            if (origen == null || destino == null) {
                continue;
            }

            actualizarArista(
                    av.linea,
                    av.punta,
                    av.nombre,
                    origen.getX(),
                    origen.getY(),
                    destino.getX(),
                    destino.getY(),
                    dirigido
            );
        }
    }

    private static void actualizarArista(
            Line linea,
            Polygon punta,
            Label nombre,
            double x1,
            double y1,
            double x2,
            double y2,
            boolean dirigido) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double longitud = Math.sqrt(dx * dx + dy * dy);

        if (longitud == 0) {
            return;
        }

        double ux = dx / longitud;
        double uy = dy / longitud;

        double inicioX = x1 + RADIO_NODO * ux;
        double inicioY = y1 + RADIO_NODO * uy;
        double finX = x2 - RADIO_NODO * ux;
        double finY = y2 - RADIO_NODO * uy;

        linea.setStartX(inicioX);
        linea.setStartY(inicioY);
        linea.setEndX(finX);
        linea.setEndY(finY);

        double medioX = (inicioX + finX) / 2.0;
        double medioY = (inicioY + finY) / 2.0;

        nombre.setLayoutX(medioX + 6);
        nombre.setLayoutY(medioY + 6);

        if (!dirigido) {
            return;
        }

        double tamFlecha = 10;
        double angulo = Math.atan2(dy, dx);

        double xArrow1 = finX - tamFlecha * Math.cos(angulo - Math.PI / 6);
        double yArrow1 = finY - tamFlecha * Math.sin(angulo - Math.PI / 6);

        double xArrow2 = finX - tamFlecha * Math.cos(angulo + Math.PI / 6);
        double yArrow2 = finY - tamFlecha * Math.sin(angulo + Math.PI / 6);

        punta.getPoints().setAll(
                finX, finY,
                xArrow1, yArrow1,
                xArrow2, yArrow2
        );
    }
}
