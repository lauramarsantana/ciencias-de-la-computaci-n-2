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

public class FuncionOrdinalVisual {

    private static final double RADIO_NODO = 20;

    private static class FlechaVisual {
        AristaDirigida arista;
        Line linea;
        Polygon punta;

        FlechaVisual(AristaDirigida arista, Line linea, Polygon punta) {
            this.arista = arista;
            this.linea = linea;
            this.punta = punta;
        }
    }

    public static void dibujar(GrafoOrdinal grafo, Pane panel) {
        dibujar(grafo, panel, false, null);
    }

    public static void dibujarSinNombres(
            GrafoOrdinal grafo,
            Pane panel,
            Consumer<String> manejadorClick
    ) {
        dibujar(grafo, panel, true, manejadorClick);
    }

    private static void dibujar(
            GrafoOrdinal grafo,
            Pane panel,
            boolean ocultarNombres,
            Consumer<String> manejadorClick
    ) {
        panel.getChildren().clear();

        if (grafo == null || grafo.getVertices().isEmpty()) {
            return;
        }

        List<FlechaVisual> flechasVisuales = new ArrayList<>();

        for (AristaDirigida arista : grafo.getAristas()) {
            VerticeOrdinal origen = grafo.buscarVertice(arista.getOrigen());
            VerticeOrdinal destino = grafo.buscarVertice(arista.getDestino());

            if (origen == null || destino == null) {
                continue;
            }

            FlechaVisual fv = dibujarFlecha(
                    panel,
                    arista,
                    origen.getX(),
                    origen.getY(),
                    destino.getX(),
                    destino.getY()
            );

            flechasVisuales.add(fv);
        }

        dibujarVertices(grafo, panel, ocultarNombres, manejadorClick, flechasVisuales);
    }

    private static void dibujarVertices(
            GrafoOrdinal grafo,
            Pane panel,
            boolean ocultarNombres,
            Consumer<String> manejadorClick,
            List<FlechaVisual> flechasVisuales
    ) {
        for (VerticeOrdinal v : grafo.getVertices()) {
            Circle circulo = new Circle(v.getX(), v.getY(), RADIO_NODO);
            circulo.setFill(Color.LIGHTBLUE);
            circulo.setStroke(Color.BLACK);

            Label nombre = null;

            if (!ocultarNombres) {
                nombre = new Label(v.getNombre());
                nombre.setLayoutX(v.getX() - 6);
                nombre.setLayoutY(v.getY() - 10);
                nombre.setStyle("-fx-font-weight: bold;");
                nombre.setMouseTransparent(true);
            }

            Label ordinal = new Label(
                    v.getEtiquetaOrdinal() > 0 ? String.valueOf(v.getEtiquetaOrdinal()) : ""
            );
            ordinal.setLayoutX(v.getX() - 4);
            ordinal.setLayoutY(v.getY() - 36);
            ordinal.setStyle(
                    "-fx-font-weight: bold; " +
                    "-fx-background-color: white; " +
                    "-fx-padding: 1 4 1 4;"
            );
            ordinal.setMouseTransparent(true);

            habilitarMovimiento(
                    grafo,
                    v,
                    circulo,
                    nombre,
                    ordinal,
                    flechasVisuales,
                    panel,
                    manejadorClick
            );

            panel.getChildren().add(circulo);

            if (nombre != null) {
                panel.getChildren().add(nombre);
            }

            panel.getChildren().add(ordinal);
        }
    }

    private static void habilitarMovimiento(
            GrafoOrdinal grafo,
            VerticeOrdinal vertice,
            Circle circulo,
            Label nombre,
            Label ordinal,
            List<FlechaVisual> flechasVisuales,
            Pane panel,
            Consumer<String> manejadorClick
    ) {
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

            if (nombre != null) {
                nombre.setLayoutX(nuevoX - 6);
                nombre.setLayoutY(nuevoY - 10);
            }

            ordinal.setLayoutX(nuevoX - 4);
            ordinal.setLayoutY(nuevoY - 36);

            vertice.setX((int) nuevoX);
            vertice.setY((int) nuevoY);

            actualizarFlechas(grafo, flechasVisuales);

            e.consume();
        });
    }

    private static void actualizarFlechas(GrafoOrdinal grafo, List<FlechaVisual> flechasVisuales) {
        for (FlechaVisual fv : flechasVisuales) {
            VerticeOrdinal origen = grafo.buscarVertice(fv.arista.getOrigen());
            VerticeOrdinal destino = grafo.buscarVertice(fv.arista.getDestino());

            if (origen == null || destino == null) {
                continue;
            }

            actualizarFlecha(
                    fv.linea,
                    fv.punta,
                    origen.getX(),
                    origen.getY(),
                    destino.getX(),
                    destino.getY()
            );
        }
    }

    private static FlechaVisual dibujarFlecha(
            Pane panel,
            AristaDirigida arista,
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        Line linea = new Line();
        linea.setStroke(Color.GRAY);
        linea.setStrokeWidth(2);

        Polygon punta = new Polygon();
        punta.setFill(Color.GRAY);

        actualizarFlecha(linea, punta, x1, y1, x2, y2);

        panel.getChildren().addAll(linea, punta);

        return new FlechaVisual(arista, linea, punta);
    }

    private static void actualizarFlecha(
            Line linea,
            Polygon punta,
            double x1,
            double y1,
            double x2,
            double y2
    ) {
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