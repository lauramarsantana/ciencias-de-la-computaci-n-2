package utilities;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.util.List;

public class GrafoVisual {

    public static void dibujar(Grafo grafo, Pane panel) {
        panel.getChildren().clear();

        // 1. Dibujar Aristas
        for (Arista arista : grafo.getAristas()) {
            Vertice origen = arista.getVerticeOrigen();
            Vertice destino = arista.getVerticeDestino();

            // Usamos setStartX/Y con los doubles de tu clase Vertice
            Line linea = new Line();
            linea.setStartX(origen.getPositionX());
            linea.setStartY(origen.getPositionY());
            linea.setEndX(destino.getPositionX());
            linea.setEndY(destino.getPositionY());

            linea.setStroke(Color.BLUE);
            linea.setStrokeWidth(2);
            panel.getChildren().add(linea);
        }

        // 2. Dibujar Vértices
        for (Vertice v : grafo.getVertices().values()) {
            double radio = 12.0;
            // Posicionamos el círculo usando los métodos de tu clase
            Circle circulo = new Circle(v.getPositionX(), v.getPositionY(), radio);
            circulo.setFill(Color.WHITE);
            circulo.setStroke(Color.BLUE);
            circulo.setStrokeWidth(2);

            Text texto = new Text(v.getPositionX() - 5, v.getPositionY() + 5, v.getName());
            texto.setStyle("-fx-font-weight: bold;");

            panel.getChildren().addAll(circulo, texto);
        }
    }

    public static void reacomodarCircular(Pane panel, List<Vertice> vertices) {
        if (vertices.isEmpty()) return;

        double centroX = panel.getWidth() / 2;
        double centroY = panel.getHeight() / 2;
        double radioCirculo = Math.min(centroX, centroY) - 50;

        int total = vertices.size();
        for (int i = 0; i < total; i++) {
            double angulo = 2 * Math.PI * i / total;
            double nuevoX = centroX + radioCirculo * Math.cos(angulo);
            double nuevoY = centroY + radioCirculo * Math.sin(angulo);

            // Actualizamos los doubles en tu objeto Vertice
            vertices.get(i).setPositionX(nuevoX);
            vertices.get(i).setPositionY(nuevoY);
        }

        // ¡IMPORTANTE! Al no usar bind, hay que volver a dibujar para ver los cambios
    }
}