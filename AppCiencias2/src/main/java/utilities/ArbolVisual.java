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

    public static void dibujar(Arbol arbol, Pane panel) {
        panel.getChildren().clear();

        if (arbol == null || arbol.getRaiz() == null) {
            return;
        }

        siguienteX = MARGEN_X;

        // 1. Asignar posiciones según estructura del árbol
        asignarPosiciones(arbol.getRaiz());

        // 2. Ajustar todo al centro del panel si sobra espacio
        centrarArbol(arbol, panel);

        // 3. Dibujar primero las aristas
        dibujarAristas(arbol, panel);

        // 4. Dibujar nodos
        dibujarNodos(arbol, panel);
    }

    /**
     * Ubica las hojas de izquierda a derecha.
     * Los nodos internos quedan centrados respecto a sus hijos.
     */
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

    /**
     * Centra horizontalmente el árbol dentro del panel.
     */
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
            if (nodo.getX() < minX) {
                minX = nodo.getX();
            }
            if (nodo.getX() > maxX) {
                maxX = nodo.getX();
            }
        }

        double anchoArbol = maxX - minX;
        double desplazamiento = (anchoPanel - anchoArbol) / 2.0 - minX;

        for (NodoArbol nodo : nodos) {
            nodo.setX(nodo.getX() + desplazamiento);
        }
    }

    private static void dibujarAristas(Arbol arbol, Pane panel) {
        for (NodoArbol nodo : arbol.getNodos().values()) {
            for (NodoArbol hijo : nodo.getHijos()) {
                Line linea = new Line(
                        nodo.getX(), nodo.getY(),
                        hijo.getX(), hijo.getY()
                );
                linea.setStrokeWidth(2);
                panel.getChildren().add(linea);
            }
        }
    }

    private static void dibujarNodos(Arbol arbol, Pane panel) {
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

            panel.getChildren().addAll(circulo, texto);
        }
    }
}