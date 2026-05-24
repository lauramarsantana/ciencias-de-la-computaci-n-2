package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class GrafoCaminoVisual {

    private static final double RADIO_NODO = 20;

    private static class FlechaVisual {
        AristaCamino arista;
        Line linea;
        Polygon punta;
        Label peso;

        FlechaVisual(AristaCamino arista, Line linea, Polygon punta, Label peso) {
            this.arista = arista;
            this.linea = linea;
            this.punta = punta;
            this.peso = peso;
        }
    }

    public static void dibujar(GrafoCamino grafo, Pane panel) {
        dibujar(grafo, panel, null, null);
    }

    public static void dibujar(
            GrafoCamino grafo,
            Pane panel,
            Consumer<String> manejadorClick
    ) {
        dibujar(grafo, panel, manejadorClick, null);
    }

    public static void dibujar(
            GrafoCamino grafo,
            Pane panel,
            Consumer<String> manejadorClick,
            Map<String, Integer> etiquetasOrdinales
    ) {
        panel.getChildren().clear();

        if (grafo == null || grafo.getVertices().isEmpty()) {
            return;
        }

        List<FlechaVisual> flechasVisuales = new ArrayList<>();

        for (AristaCamino arista : grafo.getAristas()) {
            VerticeCamino origen = grafo.buscarVertice(arista.getOrigen());
            VerticeCamino destino = grafo.buscarVertice(arista.getDestino());

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

        dibujarVertices(grafo, panel, manejadorClick, flechasVisuales, etiquetasOrdinales);
    }

    private static void dibujarVertices(
            GrafoCamino grafo,
            Pane panel,
            Consumer<String> manejadorClick,
            List<FlechaVisual> flechasVisuales,
            Map<String, Integer> etiquetasOrdinales
    ) {
        for (VerticeCamino v : grafo.getVertices()) {
            Circle circulo = new Circle(v.getX(), v.getY(), RADIO_NODO);
            circulo.setFill(Color.LIGHTBLUE);
            circulo.setStroke(Color.BLACK);

            String textoVertice = v.getNombre();

            if (etiquetasOrdinales != null && etiquetasOrdinales.containsKey(v.getNombre())) {
                textoVertice = String.valueOf(etiquetasOrdinales.get(v.getNombre()));
            }

            Label nombre = new Label(textoVertice);
            nombre.setLayoutX(v.getX() - 7);
            nombre.setLayoutY(v.getY() - 10);
            nombre.setStyle("-fx-font-weight: bold;");
            nombre.setMouseTransparent(true);

            habilitarMovimiento(
                    grafo,
                    v,
                    circulo,
                    nombre,
                    flechasVisuales,
                    panel,
                    manejadorClick
            );

            panel.getChildren().add(circulo);
            panel.getChildren().add(nombre);
        }
    }

    private static void habilitarMovimiento(
            GrafoCamino grafo,
            VerticeCamino vertice,
            Circle circulo,
            Label nombre,
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

            nombre.setLayoutX(nuevoX - 7);
            nombre.setLayoutY(nuevoY - 10);

            vertice.setX((int) nuevoX);
            vertice.setY((int) nuevoY);

            actualizarFlechas(grafo, flechasVisuales);

            e.consume();
        });
    }

    private static void actualizarFlechas(GrafoCamino grafo, List<FlechaVisual> flechasVisuales) {
        for (FlechaVisual fv : flechasVisuales) {
            VerticeCamino origen = grafo.buscarVertice(fv.arista.getOrigen());
            VerticeCamino destino = grafo.buscarVertice(fv.arista.getDestino());

            if (origen == null || destino == null) {
                continue;
            }

            actualizarFlecha(
                    fv.linea,
                    fv.punta,
                    fv.peso,
                    origen.getX(),
                    origen.getY(),
                    destino.getX(),
                    destino.getY()
            );
        }
    }

    private static FlechaVisual dibujarFlecha(
            Pane panel,
            AristaCamino arista,
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

        Label peso = new Label(String.valueOf(arista.getPeso()));
        peso.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-background-color: white;" +
                "-fx-border-color: gray;" +
                "-fx-padding: 1 5 1 5;"
        );
        peso.setMouseTransparent(true);

        actualizarFlecha(linea, punta, peso, x1, y1, x2, y2);

        panel.getChildren().addAll(linea, punta, peso);

        return new FlechaVisual(arista, linea, punta, peso);
    }

    private static void actualizarFlecha(
            Line linea,
            Polygon punta,
            Label peso,
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

        double medioX = (inicioX + finX) / 2;
        double medioY = (inicioY + finY) / 2;

        peso.setLayoutX(medioX - 10);
        peso.setLayoutY(medioY - 22);
    }
}