package utilities;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Text;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GrafoVisual {

    public static void dibujar(Grafo grafo, Pane panel) {
        panel.getChildren().clear();

        // 1. AGRUPAMIENTO PARA CURVAS
        Map<String, List<Arista>> grupos = new HashMap<>();
        for (Arista a : grafo.getAristas()) {
            String v1 = a.getVerticeOrigen().getName().trim();
            String v2 = a.getVerticeDestino().getName().trim();
            String clave = (v1.compareTo(v2) < 0) ? v1 + "-" + v2 : v2 + "-" + v1;
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(a);
        }

        // 2. DIBUJAR ARISTAS
        for (List<Arista> listaAristas : grupos.values()) {
            int totalEnGrupo = listaAristas.size();
            for (int i = 0; i < totalEnGrupo; i++) {
                Arista a = listaAristas.get(i);
                Vertice o = a.getVerticeOrigen();
                Vertice d = a.getVerticeDestino();

                double x1 = o.getPositionX();
                double y1 = o.getPositionY();
                double x2 = d.getPositionX();
                double y2 = d.getPositionY();

                if (totalEnGrupo == 1) {
                    Line linea = new Line(x1, y1, x2, y2);
                    linea.setStroke(Color.BLUE);
                    linea.setStrokeWidth(2);
                    panel.getChildren().add(linea);
                } else {
                    double midX = (x1 + x2) / 2;
                    double midY = (y1 + y2) / 2;
                    double dx = x2 - x1;
                    double dy = y2 - y1;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    double baseOffset = 50.0;
                    double offset = (i % 2 == 0) ? baseOffset * (1 + i / 2) : -baseOffset * (1 + i / 2);

                    QuadCurve curva = new QuadCurve(x1, y1, midX - (dy * offset / dist), midY + (dx * offset / dist), x2, y2);
                    curva.setStroke(Color.BLUE);
                    curva.setStrokeWidth(2);
                    curva.setFill(null);
                    panel.getChildren().add(curva);
                }
            }
        }

        // 3. DIBUJAR VÉRTICES Y RAYITAS
        for (Vertice v : grafo.getVertices().values()) {
            Circle circulo = new Circle(v.getPositionX(), v.getPositionY(), 15.0);
            circulo.setFill(Color.WHITE);
            circulo.setStroke(Color.BLUE);
            circulo.setStrokeWidth(2);

            String nombreOriginal = v.getName();
            String nombreLimpio = formatearNombre(nombreOriginal); // MÉTODO AHORA DENTRO DE LA CLASE

            Text texto = new Text(v.getPositionX() - 5, v.getPositionY() + 5, nombreLimpio);
            texto.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            panel.getChildren().addAll(circulo, texto);

            // DIBUJO DE LA RAYA NOTORIA (SI ES DEL GRAFO 2)
            if (nombreOriginal.startsWith("2_")) {
                Line rayita = new Line(
                        v.getPositionX() - 7, v.getPositionY() - 10, // Un poco más arriba del texto
                        v.getPositionX() + 7, v.getPositionY() - 10
                );
                rayita.setStroke(Color.BLACK);
                rayita.setStrokeWidth(2.0); // Bien gruesa para que se note
                panel.getChildren().add(rayita);
            }
        }
    }

    // MÉTODO AGREGADO PARA EVITAR EL ERROR DE "CANNOT FIND SYMBOL"
    public static String formatearNombre(String nombre) {
        if (nombre.startsWith("2_") || nombre.startsWith("1_")) {
            return nombre.split("_")[1];
        }
        return nombre;
    }

    public static void reacomodarCircular(Pane panel, List<Vertice> vertices) {
        if (vertices.isEmpty()) return;
        vertices.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));
        double centroX = panel.getWidth() / 2;
        double centroY = panel.getHeight() / 2;
        double radioCirculo = Math.min(centroX, centroY) - 50;
        int total = vertices.size();
        for (int i = 0; i < total; i++) {
            double angulo = 2 * Math.PI * i / total;
            vertices.get(i).setPositionX(centroX + radioCirculo * Math.cos(angulo));
            vertices.get(i).setPositionY(centroY + radioCirculo * Math.sin(angulo));
        }
    }
}