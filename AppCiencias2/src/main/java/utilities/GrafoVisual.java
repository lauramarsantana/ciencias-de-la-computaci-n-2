package utilities;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import utilities.Grafo;
import utilities.Arista;
import utilities.Vertice;

public class GrafoVisual{

    public static void dibujar(Grafo grafo, Pane panel) {
        panel.getChildren().clear(); // Paso 1: Limpiar

        // Paso 2: Dibujar Aristas
        for (Arista arista : grafo.getAristas()) {
            Vertice origen = arista.getVerticeOrigen();
            Vertice destino = arista.getVerticeDestino();

            Line linea = new Line(
                    origen.getPositionX(), origen.getPositionY(),
                    destino.getPositionX(), destino.getPositionY()
            );
            linea.setStroke(Color.BLUE); // El color de tus bordes en el FXML
            linea.setStrokeWidth(2);

            // Cálculo del punto medio para la etiqueta
            double midX = (origen.getPositionX() + destino.getPositionX()) / 2;
            double midY = (origen.getPositionY() + destino.getPositionY()) / 2;

            // Creamos el texto de la arista (ej: "A-B" o el peso)
            Text etiquetaArista = new Text(midX, midY - 5, arista.getName());
            etiquetaArista.setFill(Color.DARKRED);
            etiquetaArista.setStyle("-fx-font-weight: bold;");

            panel.getChildren().add(linea);
        }

        // Paso 3: Dibujar Vértices
        for (Vertice v : grafo.getVertices().values()) {
            double radio = 10.0;
            Circle circulo = new Circle(v.getPositionX(), v.getPositionY(), radio);
            circulo.setFill(Color.WHITE);
            circulo.setStroke(Color.BLUE);
            circulo.setStrokeWidth(2);

            // La etiqueta (nombre) del vértice
            Text texto = new Text(v.getPositionX() - 5, v.getPositionY() + 5, v.getName());

            panel.getChildren().addAll(circulo, texto);
        }
    }
}