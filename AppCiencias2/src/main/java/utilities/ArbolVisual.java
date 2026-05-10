package utilities;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class ArbolVisual {

    private static final double RADIO_NODO = 20;
    private static final double ESPACIO_HORIZONTAL = 90;
    private static final double ESPACIO_VERTICAL = 110;
    private static final double MARGEN_X = 60;
    private static final double MARGEN_Y = 70;

    private static double siguienteX;

    private static class AristaVisual {
        NodoArbol padre;
        NodoArbol hijo;
        Line linea;

        AristaVisual(NodoArbol padre, NodoArbol hijo, Line linea) {
            this.padre = padre;
            this.hijo = hijo;
            this.linea = linea;
        }
    }

    public static void dibujar(Arbol arbol, Pane panel) {
        panel.getChildren().clear();

        if (arbol == null || arbol.getRaiz() == null) {
            return;
        }

        siguienteX = MARGEN_X;

        asignarPosiciones(arbol.getRaiz());
        centrarArbol(arbol, panel);

        List<AristaVisual> aristasVisuales = new ArrayList<>();

        dibujarAristas(arbol, panel, aristasVisuales);
        dibujarNodos(arbol, panel, aristasVisuales);
    }

    private static void asignarPosiciones(NodoArbol nodo) {
        double y = MARGEN_Y + nodo.getNivel() * ESPACIO_VERTICAL;
        nodo.setY(y);

        List<NodoArbol> hijos = nodo.getHijos();

        if (hijos == null || hijos.isEmpty()) {
            nodo.setX(siguienteX);
            siguienteX += ESPACIO_HORIZONTAL;
            return;
        }

        for (NodoArbol hijo : hijos) {
            asignarPosiciones(hijo);
        }

        double minX = hijos.get(0).getX();
        double maxX = hijos.get(hijos.size() - 1).getX();
        nodo.setX((minX + maxX) / 2.0);
    }

    private static void centrarArbol(Arbol arbol, Pane panel) {
        double anchoPanel = panel.getWidth();

        if (anchoPanel <= 0) {
            anchoPanel = panel.getPrefWidth();
        }

        if (anchoPanel <= 0) {
            anchoPanel = 800;
        }

        List<NodoArbol> nodos = new ArrayList<>(arbol.getNodos().values());

        if (nodos.isEmpty()) {
            return;
        }

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;

        for (NodoArbol nodo : nodos) {
            minX = Math.min(minX, nodo.getX());
            maxX = Math.max(maxX, nodo.getX());
        }

        double anchoArbol = maxX - minX;
        double desplazamiento = (anchoPanel - anchoArbol) / 2.0 - minX;

        for (NodoArbol nodo : nodos) {
            nodo.setX(nodo.getX() + desplazamiento);
        }
    }

    private static void dibujarAristas(
            Arbol arbol,
            Pane panel,
            List<AristaVisual> aristasVisuales
    ) {
        for (NodoArbol nodo : arbol.getNodos().values()) {
            for (NodoArbol hijo : nodo.getHijos()) {
                Line linea = new Line(
                        nodo.getX(), nodo.getY(),
                        hijo.getX(), hijo.getY()
                );

                linea.setStrokeWidth(2);

                aristasVisuales.add(new AristaVisual(nodo, hijo, linea));
                panel.getChildren().add(linea);
            }
        }
    }

    private static void dibujarNodos(
            Arbol arbol,
            Pane panel,
            List<AristaVisual> aristasVisuales
    ) {
        List<NodoArbol> centros = arbol.hallarCentroOBicentro();

        for (NodoArbol nodo : arbol.getNodos().values()) {
            Circle circulo = new Circle(nodo.getX(), nodo.getY(), RADIO_NODO);
            circulo.setStroke(Color.BLACK);

            if (centros.contains(nodo)) {
                circulo.setFill(Color.LIGHTGREEN);
            } else {
                circulo.setFill(Color.LIGHTBLUE);
            }

            Label texto = new Label(nodo.getNombre());
            texto.setLayoutX(nodo.getX() - 6);
            texto.setLayoutY(nodo.getY() - 10);
            texto.setMouseTransparent(true);

            habilitarMovimiento(nodo, circulo, texto, aristasVisuales, panel);

            panel.getChildren().addAll(circulo, texto);
        }
    }

    private static void habilitarMovimiento(
        NodoArbol nodo,
        Circle circulo,
        Label texto,
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

        texto.setLayoutX(nuevoX - 6);
        texto.setLayoutY(nuevoY - 10);

        nodo.setX(nuevoX);
        nodo.setY(nuevoY);

        actualizarAristas(aristasVisuales);

        e.consume();
    });
}

    private static void actualizarAristas(List<AristaVisual> aristasVisuales) {
        for (AristaVisual av : aristasVisuales) {
            av.linea.setStartX(av.padre.getX());
            av.linea.setStartY(av.padre.getY());
            av.linea.setEndX(av.hijo.getX());
            av.linea.setEndY(av.hijo.getY());
        }
    }
}