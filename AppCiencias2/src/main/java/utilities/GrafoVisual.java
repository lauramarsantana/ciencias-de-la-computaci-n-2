package utilities;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import java.util.*;

public class GrafoVisual {

    private static Vertice nodoOrigenSeleccionado = null;
    private static Circle circuloOrigenVisual = null;

    // MÉTODO PRINCIPAL: Se llama al cargar o actualizar el grafo por completo
    public static void dibujarInteractivo(Grafo grafo, Pane panel, Label infoLabel) {
        panel.getChildren().clear();

        if (infoLabel != null) {
            infoLabel.setText(obtenerTextoMatematico(grafo));
        }

        // 1. Dibujar las aristas de fondo primero
        dibujarAristasFondo(grafo, panel);

        // 2. Dibujar los vértices e inyectar las físicas estables
        for (Vertice v : grafo.getVertices().values()) {
            StackPane contenedorNodo = new StackPane();
            contenedorNodo.setUserData(v); // Vinculamos el objeto matemático al componente visual

            Circle circulo = new Circle(15);
            if (nodoOrigenSeleccionado != null && nodoOrigenSeleccionado.getName().equals(v.getName())) {
                circulo.setFill(Color.WHITE);
                circulo.setStroke(Color.BLUE);
                circulo.setStrokeWidth(3.5);
                circuloOrigenVisual = circulo;
            } else {
                circulo.setFill(Color.WHITE);
                circulo.setStroke(Color.BLUE);
                circulo.setStrokeWidth(1.5);
            }

            String nombreVisual = v.getName().replace("_", ",");
            Text texto = new Text(nombreVisual);
            texto.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            texto.setMouseTransparent(true);

            contenedorNodo.getChildren().addAll(circulo, texto);

            // Posicionamiento inicial en el Lienzo
            contenedorNodo.setLayoutX(v.getPositionX() - 15);
            contenedorNodo.setLayoutY(v.getPositionY() - 15);

            // Variables locales para el seguimiento exacto del arrastre
            final double[] dragOffset = new double[2];

            contenedorNodo.setOnMousePressed(e -> {
                // Desfase en coordenadas locales del contenedor para evitar saltos bruscos
                dragOffset[0] = e.getX();
                dragOffset[1] = e.getY();
                e.consume();
            });

            contenedorNodo.setOnMouseDragged(e -> {
                // Calcular nueva posición basándose en el movimiento del mouse relativo al padre (Lienzo)
                double nuevoX = contenedorNodo.getLayoutX() + e.getX() - dragOffset[0];
                double nuevoY = contenedorNodo.getLayoutY() + e.getY() - dragOffset[1];

                double maxAncho = panel.getWidth() > 0 ? panel.getWidth() : panel.getPrefWidth();
                double maxAlto = panel.getHeight() > 0 ? panel.getHeight() : panel.getPrefHeight();

                // Mantener estrictamente dentro del contenedor
                nuevoX = Math.max(0, Math.min(nuevoX, maxAncho - 30));
                nuevoY = Math.max(0, Math.min(nuevoY, maxAlto - 30));

                // Actualizar las posiciones visuales del componente existente de forma inmediata
                contenedorNodo.setLayoutX(nuevoX);
                contenedorNodo.setLayoutY(nuevoY);

                // Actualizar las coordenadas del modelo matemático subyacente
                v.setPositionX(nuevoX + 15);
                v.setPositionY(nuevoY + 15);

                // RECALCULAR SOLO LAS ARISTAS (Sin destruir los StackPane)
                dibujarAristasFondo(grafo, panel);

                e.consume();
            });

            // GESTIÓN DE CLICS INDEPENDIENTE
            contenedorNodo.setOnMouseClicked(e -> {
                if (nodoOrigenSeleccionado == null) {
                    nodoOrigenSeleccionado = v;
                    circuloOrigenVisual = circulo;
                    circulo.setStrokeWidth(3.5);
                } else {
                    if (!nodoOrigenSeleccionado.getName().equals(v.getName())) {
                        String nombreArista = nodoOrigenSeleccionado.getName() + "-" + v.getName();
                        boolean aristaExiste = grafo.getAristas().stream().anyMatch(a ->
                                a.getName().equals(nombreArista) || a.getName().equals(v.getName() + "-" + nodoOrigenSeleccionado.getName())
                        );

                        if (!aristaExiste) {
                            grafo.agregarArista(new Arista(nombreArista, nodoOrigenSeleccionado, v));
                        }
                    }
                    nodoOrigenSeleccionado = null;
                    if (circuloOrigenVisual != null) {
                        circuloOrigenVisual.setStrokeWidth(1.5);
                    }
                    // Aquí sí refrescamos completo porque se alteró la estructura del Grafo
                    dibujarInteractivo(grafo, panel, infoLabel);
                }
                e.consume();
            });

            panel.getChildren().add(contenedorNodo);
        }
    }

    // MÉTODO AUXILIAR: Limpia y redibuja únicamente las líneas de fondo, protegiendo los StackPanes
    private static void dibujarAristasFondo(Grafo grafo, Pane panel) {
        // Remover del panel únicamente las líneas y curvas viejas (remanentes)
        panel.getChildren().removeIf(node -> node instanceof Line || node instanceof QuadCurve || (node instanceof Circle && ((Circle) node).getFill() == null));

        Map<String, List<Arista>> grupos = new HashMap<>();
        for (Arista a : grafo.getAristas()) {
            String v1 = a.getVerticeOrigen().getName();
            String v2 = a.getVerticeDestino().getName();
            String clave = v1.equals(v2) ? "LOOP-" + v1 : (v1.compareTo(v2) < 0 ? v1 + "-" + v2 : v2 + "-" + v1);
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(a);
        }

        // Insertar las nuevas aristas actualizadas en el índice 0 para que queden abajo de los círculos
        int insercionIndex = 0;
        for (List<Arista> listaAristas : grupos.values()) {
            int total = listaAristas.size();
            for (int i = 0; i < total; i++) {
                Arista a = listaAristas.get(i);
                double x1 = a.getVerticeOrigen().getPositionX();
                double y1 = a.getVerticeOrigen().getPositionY();
                double x2 = a.getVerticeDestino().getPositionX();
                double y2 = a.getVerticeDestino().getPositionY();

                if (a.getVerticeOrigen().equals(a.getVerticeDestino())) {
                    double radioLazo = 15 + (i * 10);
                    Circle lazo = new Circle(x1, y1 - 15, radioLazo);
                    lazo.setFill(null);
                    lazo.setStroke(Color.BLUE);
                    lazo.setStrokeWidth(1.5);
                    panel.getChildren().add(insercionIndex++, lazo);
                } else if (total == 1) {
                    Line linea = new Line(x1, y1, x2, y2);
                    linea.setStroke(Color.BLUE);
                    linea.setStrokeWidth(1.5);
                    panel.getChildren().add(insercionIndex++, linea);
                } else {
                    double midX = (x1 + x2) / 2;
                    double midY = (y1 + y2) / 2;
                    double dx = x2 - x1;
                    double dy = y2 - y1;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (i == 0) {
                        Line linea = new Line(x1, y1, x2, y2);
                        linea.setStroke(Color.BLUE);
                        linea.setStrokeWidth(1.5);
                        panel.getChildren().add(insercionIndex++, linea);
                    } else {
                        double offsetCurve = (i % 2 == 0 ? 35.0 : -35.0) * ((i + 1) / 2);
                        QuadCurve curva = new QuadCurve(x1, y1, midX - (dy * offsetCurve / dist), midY + (dx * offsetCurve / dist), x2, y2);
                        curva.setStroke(Color.BLUE);
                        curva.setStrokeWidth(1.5);
                        curva.setFill(null);
                        panel.getChildren().add(insercionIndex++, curva);
                    }
                }
            }
        }
    }

    private static String obtenerTextoMatematico(Grafo g) {
        if (g == null) return "";
        Set<String> vNombres = new LinkedHashSet<>();
        for (String key : g.getVertices().keySet()) {
            vNombres.add(key.replace("_", ","));
        }
        String verticesText = "S = {" + String.join(", ", vNombres) + "}";

        List<String> aristasLista = new ArrayList<>();
        for (Arista a : g.getAristas()) {
            String v1 = a.getVerticeOrigen().getName().replace("_", ",");
            String v2 = a.getVerticeDestino().getName().replace("_", ",");
            if (v1.compareTo(v2) < 0) aristasLista.add(v1 + "-" + v2);
            else aristasLista.add(v2 + "-" + v1);
        }
        Collections.sort(aristasLista);
        String aristasText = "A = {" + String.join(", ", aristasLista) + "}";

        return g.getNombre() + ":\n" + verticesText + "\n" + aristasText;
    }

    public static void reacomodarCircular(Pane panel, List<Vertice> vertices) {
        if (vertices.isEmpty()) return;
        double ancho = panel.getWidth() > 0 ? panel.getWidth() : (panel.getPrefWidth() > 0 ? panel.getPrefWidth() : 500);
        double alto = panel.getHeight() > 0 ? panel.getHeight() : (panel.getPrefHeight() > 0 ? panel.getPrefHeight() : 400);

        double centroX = ancho / 2;
        double centroY = alto / 2;
        double radio = Math.min(centroX, centroY) - 35;

        for (int i = 0; i < vertices.size(); i++) {
            double angulo = 2 * Math.PI * i / vertices.size();
            vertices.get(i).setPositionX(centroX + radio * Math.cos(angulo));
            vertices.get(i).setPositionY(centroY + radio * Math.sin(angulo));
        }
    }

    public static void reacomodarMatriz(Pane pane, Grafo g, int numFilas, int numCols) {
        double ancho = pane.getWidth() > 0 ? pane.getWidth() : (pane.getPrefWidth() > 0 ? pane.getPrefWidth() : 500);
        double alto = pane.getHeight() > 0 ? pane.getHeight() : (pane.getPrefHeight() > 0 ? pane.getPrefHeight() : 400);

        double margenX = ancho / (numCols + 1);
        double margenY = alto / (numFilas + 1);

        List<Vertice> lista = new ArrayList<>(g.getVertices().values());

        int k = 0;
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numCols; j++) {
                if (k < lista.size()) {
                    Vertice v = lista.get(k++);
                    v.setPositionX(margenX * (j + 1));
                    v.setPositionY(margenY * (i + 1));
                }
            }
        }
    }

    public static void reacomodarVertical(Pane panel, List<Vertice> vertices) {
        if (vertices.isEmpty()) return;
        double ancho = panel.getWidth() > 0 ? panel.getWidth() : (panel.getPrefWidth() > 0 ? panel.getPrefWidth() : 500);
        double alto = panel.getHeight() > 0 ? panel.getHeight() : (panel.getPrefHeight() > 0 ? panel.getPrefHeight() : 400);

        double centroX = ancho / 2;
        double espaciadoY = (alto - 40) / (vertices.size() + 1);

        for (int i = 0; i < vertices.size(); i++) {
            vertices.get(i).setPositionX(centroX);
            vertices.get(i).setPositionY(20 + (espaciadoY * (i + 1)));
        }
    }

    public static void dibujarNodoColoreado(Pane pane, Vertice v, Color color) {
        double radio = 15;
        Circle circulo = new Circle(v.getPositionX(), v.getPositionY(), radio);
        circulo.setFill(color);
        circulo.setStroke(Color.BLACK);
        circulo.setStrokeWidth(2);

        Text texto = new Text(v.getName());
        texto.setX(v.getPositionX() - 5);
        texto.setY(v.getPositionY() + 5);
        texto.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        texto.setMouseTransparent(true);

        pane.getChildren().addAll(circulo, texto);
    }

    public static void dibujarAristaColoreada(Pane pane, Arista a, Color color) {
        Line linea = new Line(a.getVerticeOrigen().getPositionX(), a.getVerticeOrigen().getPositionY(), a.getVerticeDestino().getPositionX(), a.getVerticeDestino().getPositionY());
        linea.setStroke(color);
        linea.setStrokeWidth(3);
        pane.getChildren().add(linea);
    }
}