package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import utilities.CodigoLetra;
import utilities.NodoHuffman;

import java.util.*;

public class HuffmanController {

    @FXML private TextField mensajeField;
    @FXML private Label resultadoLabel;
    @FXML private Pane arbolPane;
    @FXML private TableView<CodigoLetra> tablaHuffman;
    @FXML private TableColumn<CodigoLetra, String> colLetra;
    @FXML private TableColumn<CodigoLetra, Integer> colFrecuencia;
    @FXML private TableColumn<CodigoLetra, String> colCodigo;

    private NodoHuffman raiz;

    @FXML
    public void initialize() {
        // Vinculación de columnas con la clase CodigoLetra
        colLetra.setCellValueFactory(new PropertyValueFactory<>("letra"));
        colFrecuencia.setCellValueFactory(new PropertyValueFactory<>("valorAlfabeto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("binario"));

        // Centrado dinámico al cambiar tamaño de ventana
        arbolPane.widthProperty().addListener((obs, old, val) -> {
            if (raiz != null) actualizarArbolVisual();
        });
    }

    @FXML
    private void procesarHuffman() {
        String texto = mensajeField.getText().toUpperCase().trim();
        if (texto.isEmpty()) return;

        // 1. Frecuencias
        Map<Character, Integer> freqs = new HashMap<>();
        for (char c : texto.toCharArray()) freqs.put(c, freqs.getOrDefault(c, 0) + 1);

        // 2. PriorityQueue para el árbol
        PriorityQueue<NodoHuffman> pq = new PriorityQueue<>();
        freqs.forEach((k, v) -> pq.add(new NodoHuffman(k, v)));

        if (pq.isEmpty()) return;

        while (pq.size() > 1) {
            NodoHuffman izq = pq.poll();
            NodoHuffman der = pq.poll();
            NodoHuffman padre = new NodoHuffman('\0', izq.frecuencia + der.frecuencia);
            padre.izquierdo = izq;
            padre.derecho = der;
            pq.add(padre);
        }
        raiz = pq.poll();

        // 3. Generar códigos y llenar tabla compacta
        Map<Character, String> mapaCodigos = new HashMap<>();
        generarCodigos(raiz, "", mapaCodigos);

        tablaHuffman.getItems().clear();
        freqs.forEach((k, v) -> {
            tablaHuffman.getItems().add(new CodigoLetra(String.valueOf(k), v, mapaCodigos.get(k)));
        });

        actualizarArbolVisual();
        resultadoLabel.setText("Árbol construido con " + freqs.size() + " caracteres únicos.");
    }

    private void generarCodigos(NodoHuffman nodo, String codigo, Map<Character, String> mapa) {
        if (nodo == null) return;
        if (nodo.izquierdo == null && nodo.derecho == null) {
            mapa.put(nodo.letra, codigo.isEmpty() ? "0" : codigo);
        }
        generarCodigos(nodo.izquierdo, codigo + "0", mapa);
        generarCodigos(nodo.derecho, codigo + "1", mapa);
    }

    private void actualizarArbolVisual() {
        arbolPane.getChildren().clear();
        double centroX = arbolPane.getWidth() / 2;
        double xOffsetInicial = arbolPane.getWidth() * 0.25; // Más espacio lateral
        pintarNodo(raiz, centroX, 40, xOffsetInicial);
    }

    private void pintarNodo(NodoHuffman nodo, double x, double y, double xOffset) {
        if (nodo == null) return;

        Circle c = new Circle(x, y, 14);
        c.setStyle("-fx-fill: white; -fx-stroke: #2262C6; -fx-stroke-width: 1.5;");

        if (nodo.izquierdo == null && nodo.derecho == null) {
            c.setStyle("-fx-fill: #b2ffb2; -fx-stroke: #27AE60;"); // Hojas verdes
            Text t = new Text(x - 5, y + 5, String.valueOf(nodo.letra));
            t.setStyle("-fx-font-weight: bold;");
            arbolPane.getChildren().addAll(c, t);
        } else {
            Text f = new Text(x - 5, y + 5, String.valueOf(nodo.frecuencia));
            f.setStyle("-fx-font-size: 9px; -fx-fill: #777;");
            arbolPane.getChildren().addAll(c, f);

            double hijoY = y + 70;
            if (nodo.izquierdo != null) {
                double hijoX = x - xOffset;
                dibujarRama(x, y, hijoX, hijoY, "0");
                pintarNodo(nodo.izquierdo, hijoX, hijoY, xOffset * 0.6);
            }
            if (nodo.derecho != null) {
                double hijoX = x + xOffset;
                dibujarRama(x, y, hijoX, hijoY, "1");
                pintarNodo(nodo.derecho, hijoX, hijoY, xOffset * 0.6);
            }
        }
    }

    private void dibujarRama(double x1, double y1, double x2, double y2, String bit) {
        Line l = new Line(x1, y1 + 14, x2, y2 - 14);
        l.setStyle("-fx-stroke: #ABB2B9;");
        Text t = new Text((x1 + x2) / 2, (y1 + y2) / 2, bit);
        t.setStyle("-fx-fill: #E74C3C; -fx-font-weight: bold;"); // Bits rojos
        arbolPane.getChildren().addAll(l, t);
    }
}