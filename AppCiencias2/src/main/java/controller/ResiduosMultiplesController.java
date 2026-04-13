package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import utilities.ArbolResiduosMultiples;
import utilities.CodigoLetra; // Importamos la clase compartida
import utilities.NodoMulti;

import java.util.*;

public class ResiduosMultiplesController {

    @FXML private TextField mensajeField;
    @FXML private Label resultadoLabel;
    @FXML private Pane arbolPane;
    @FXML private Slider sliderM;
    @FXML private Label labelM;

    // Tabla minimalista
    @FXML private TableView<CodigoLetra> tablaCodigos;
    @FXML private TableColumn<CodigoLetra, String> colLetra;
    @FXML private TableColumn<CodigoLetra, Integer> colAlfabeto;
    @FXML private TableColumn<CodigoLetra, String> colBinario;

    private ArbolResiduosMultiples arbol; // Asegúrate de que esta clase exista en tu proyecto
    private int m = 2;

    @FXML
    public void initialize() {
        // Configuración de columnas
        colLetra.setCellValueFactory(new PropertyValueFactory<>("letra"));
        colAlfabeto.setCellValueFactory(new PropertyValueFactory<>("valorAlfabeto"));
        colBinario.setCellValueFactory(new PropertyValueFactory<>("binario"));

        // TRUCO DE CENTRADO: Escuchar cuando el panel cambia de tamaño para redibujar
        arbolPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!mensajeField.getText().isEmpty()) {
                actualizarArbolVisual();
            }
        });

        // Listener del Slider
        sliderM.valueProperty().addListener((obs, oldVal, newVal) -> {
            m = newVal.intValue();
            int M = (int) Math.pow(2, m);
            labelM.setText("m = " + m + " (M = " + M + " hijos)");
            reiniciarArbol();
        });

        reiniciarArbol();
    }

    @FXML
    private void procesarMensaje() {
        String texto = mensajeField.getText().toUpperCase().trim();
        if (texto.isEmpty()) return;

        reiniciarArbol();
        tablaCodigos.getItems().clear();

        for (char c : texto.toCharArray()) {
            if (Character.isLetter(c) || c == 'Ñ') {
                int valor = (c == 'Ñ') ? 27 : (c - 'A' + 1);
                // Usamos 6 bits para que sea múltiplo de 2 y 3 (más versátil)
                String binario = String.format("%5s", Integer.toBinaryString(valor)).replace(' ', '0');

                tablaCodigos.getItems().add(new CodigoLetra(String.valueOf(c), valor, binario));
                arbol.insertar(String.valueOf(c), binario);
            }
        }
        actualizarArbolVisual();
        resultadoLabel.setText("Árbol generado con m = " + m);
    }

    private void reiniciarArbol() {
        arbol = new ArbolResiduosMultiples(m); // Instancia de tu lógica
        arbolPane.getChildren().clear();
    }

    private void actualizarArbolVisual() {
        arbolPane.getChildren().clear();

        // 1. Usamos el ancho real de la ventana para el centrado
        double anchoPanel = arbolPane.getWidth();
        if (anchoPanel <= 0) anchoPanel = 1200; // Valor base por seguridad

        double centroX = anchoPanel / 2;

        if (arbol != null && arbol.getRaiz() != null) {
            // --- LÓGICA ADAPTATIVA ---
            double xOffsetInicial;
            double verticalGap;
            double factorReduccion;

            if (m == 1) {
                // Para m=1 (Binario), necesitamos que sea ancho y alto
                xOffsetInicial = anchoPanel * 0.25;
                verticalGap = 60;
                factorReduccion = 0.5; // Reducción estándar para binarios
            } else {
                // Para m >= 2 (Multicamino), comprimimos más para que no se salga
                xOffsetInicial = anchoPanel / (Math.pow(2, m) + 1);
                verticalGap = 80;
                factorReduccion = 0.35; // Reducción agresiva
            }

            pintarNodo(arbol.getRaiz(), centroX, 40, xOffsetInicial, verticalGap, factorReduccion);
        }
    }

    // Actualiza la firma del método para recibir el factorReduccion
    private void pintarNodo(NodoMulti nodo, double x, double y, double xOffset, double yGap, double factorReduccion) {
        double radio = (m > 2) ? 10 : 13;

        Circle circulo = new Circle(x, y, radio);
        circulo.setStyle("-fx-fill: white; -fx-stroke: #2262C6; -fx-stroke-width: 1.5;");
        if (nodo.esHoja) {
            circulo.setStyle("-fx-fill: #b2ffb2; -fx-stroke: #27AE60;");
        }
        arbolPane.getChildren().add(circulo);

        if (nodo.esHoja) {
            Text t = new Text(x - 4, y + 4, nodo.letra);
            t.setStyle("-fx-font-weight: bold; -fx-fill: #1B4F72; -fx-font-size: 11px;");
            arbolPane.getChildren().add(t);
            return;
        }

        int M = (int) Math.pow(2, m);
        for (int i = 0; i < M; i++) {
            if (nodo.hijos[i] != null) {
                // Posicionamiento simétrico
                double hijoX = x + (i - (M - 1) / 2.0) * xOffset;
                double hijoY = y + yGap;

                // Dibujar línea
                Line linea = new Line(x, y + radio, hijoX, hijoY - radio);
                linea.setStyle("-fx-stroke: #D5DBDB;");
                arbolPane.getChildren().add(linea);

                // Etiquetas de bits más pequeñas
                String bitLabel = String.format("%" + m + "s", Integer.toBinaryString(i)).replace(' ', '0');
                Text txtBit = new Text((x + hijoX) / 2 - 8, (y + hijoY) / 2, bitLabel);
                txtBit.setStyle("-fx-fill: #E74C3C; -fx-font-size: 9px; -fx-font-weight: bold;");
                arbolPane.getChildren().add(txtBit);

                // Reducción agresiva del offset para que los nietos no se pisen
                // Usamos un factor menor (0.35) para mantener todo compacto
                // Usamos el factor de reducción que calculamos arriba
                pintarNodo(nodo.hijos[i], hijoX, hijoY, xOffset * factorReduccion, yGap, factorReduccion);}
        }
    }
}