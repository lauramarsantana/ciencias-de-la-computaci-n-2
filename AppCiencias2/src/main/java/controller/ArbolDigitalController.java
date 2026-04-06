package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.*;

public class ArbolDigitalController {

    @FXML private TextField mensajeField;
    @FXML private TextField buscarField;
    @FXML private TableView<CodigoLetra> tablaCodigos;
    @FXML private TableColumn<CodigoLetra, String> colLetra;
    @FXML private TableColumn<CodigoLetra, Integer> colPos;
    @FXML private TableColumn<CodigoLetra, String> colBinario;
    @FXML private Label resultadoLabel;
    @FXML private Pane arbolPane;

    private Nodo raiz = new Nodo("root");

    @FXML
    public void initialize() {
        colLetra.setCellValueFactory(new PropertyValueFactory<>("letra"));
        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colBinario.setCellValueFactory(new PropertyValueFactory<>("binario"));

        buscarField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 1) {
                buscarField.setText(newText.substring(0, 1));
            }
        });
    }

    @FXML
    private void procesarMensaje() {
        String mensaje = mensajeField.getText().trim().toUpperCase();
        if (mensaje.isEmpty()) {
            resultadoLabel.setText("Ingresa un mensaje válido.");
            return;
        }

        tablaCodigos.getItems().clear();
        raiz = new Nodo("root");

        int pos = 1;
        for (char c : mensaje.toCharArray()) {
            String binario = convertirABinario(c);
            tablaCodigos.getItems().add(new CodigoLetra(String.valueOf(c), pos++, binario));
            insertarEnArbol(String.valueOf(c), binario);
        }

        calculatePositions(raiz);
        actualizarArbolVisual();
        resultadoLabel.setText("Mensaje procesado y árbol construido.");
    }

    private String convertirABinario(char c) {
        int valor = (c - 'A' + 1);
        return String.format("%5s", Integer.toBinaryString(valor)).replace(' ', '0');
    }

    private void insertarEnArbol(String letra, String binario) {
        if (raiz.letra == null || raiz.letra.equals("root")) {
            raiz.letra = letra;
            return;
        }

        insertarRecursivo(raiz, letra, binario, 0);
    }

    private void insertarRecursivo(Nodo actual, String letra, String binario, int bitIndex) {
        // Si llegamos al límite de los bits (aunque en Árbol Digital usualmente se inserta antes)
        if (bitIndex >= binario.length()) return;

        char bit = binario.charAt(bitIndex);

        if (bit == '0') {
            if (actual.izq == null) {
                actual.izq = new Nodo("0");
                actual.izq.letra = letra; // Se queda aquí porque el nodo estaba vacío
            } else {
                // El nodo ya existe, bajamos al siguiente nivel con el siguiente bit
                insertarRecursivo(actual.izq, letra, binario, bitIndex + 1);
            }
        } else { // bit == '1'
            if (actual.der == null) {
                actual.der = new Nodo("1");
                actual.der.letra = letra; // Se queda aquí
            } else {
                // El nodo ya existe, bajamos al siguiente nivel
                insertarRecursivo(actual.der, letra, binario, bitIndex + 1);
            }
        }
    }

    // ================== Distribución ==================
    private void calculatePositions(Nodo root) {
        if (root == null) return;

        Map<Integer, List<Nodo>> niveles = new HashMap<>();
        contarNodosPorNivel(root, 0, niveles);

        int maxNodos = niveles.values().stream().mapToInt(List::size).max().orElse(1);
        double totalWidth = Math.max(800, maxNodos * 120);
        double levelHeight = 50; // más compacto

        asignarPosiciones(root, totalWidth / 2, 50, totalWidth / 4, levelHeight);
    }

    private void contarNodosPorNivel(Nodo nodo, int nivel, Map<Integer, List<Nodo>> niveles) {
        if (nodo == null) return;
        niveles.computeIfAbsent(nivel, k -> new ArrayList<>()).add(nodo);
        contarNodosPorNivel(nodo.izq, nivel + 1, niveles);
        contarNodosPorNivel(nodo.der, nivel + 1, niveles);
    }

    private void asignarPosiciones(Nodo nodo, double x, double y, double spacing, double levelHeight) {
        if (nodo == null) return;
        nodo.x = x;
        nodo.y = y;

        double childSpacing = spacing / 2.0;
        if (nodo.izq != null) {
            asignarPosiciones(nodo.izq, x - childSpacing, y + levelHeight, childSpacing, levelHeight);
        }
        if (nodo.der != null) {
            asignarPosiciones(nodo.der, x + childSpacing, y + levelHeight, childSpacing, levelHeight);
        }
    }

    // ================== Dibujar ==================
    private void actualizarArbolVisual() {
        arbolPane.getChildren().clear();
        dibujarNodo(raiz);
    }

    private void dibujarNodo(Nodo nodo) {
        if (nodo == null) return;

        Circle circle = new Circle(nodo.x, nodo.y, 15);
        circle.setStyle("-fx-fill: white; -fx-stroke: black;");

        String contenido = nodo.letra != null ? nodo.letra : "";
        Text text = new Text(nodo.x - 5, nodo.y + 4, contenido);
        text.setStyle("-fx-fill: blue; -fx-font-weight: bold;");

        arbolPane.getChildren().addAll(circle, text);

        if (nodo.izq != null) {
            Line line = new Line(nodo.x, nodo.y+15, nodo.izq.x, nodo.izq.y-15);
            Text label = new Text((nodo.x+nodo.izq.x)/2, (nodo.y+nodo.izq.y)/2, "0");
            label.setStyle("-fx-fill: red; -fx-font-weight: bold;");
            arbolPane.getChildren().addAll(line, label);
            dibujarNodo(nodo.izq);
        }
        if (nodo.der != null) {
            Line line = new Line(nodo.x, nodo.y+15, nodo.der.x, nodo.der.y-15);
            Text label = new Text((nodo.x+nodo.der.x)/2, (nodo.y+nodo.der.y)/2, "1");
            label.setStyle("-fx-fill: red; -fx-font-weight: bold;");
            arbolPane.getChildren().addAll(line, label);
            dibujarNodo(nodo.der);
        }
    }

    // ================== Búsqueda ==================
    @FXML
    private void buscarClave() {
        String letra = buscarField.getText().trim().toUpperCase();
        if (letra.isEmpty()) {
            resultadoLabel.setText("Ingresa una letra válida.");
            return;
        }

        boolean encontrada = buscarEnArbol(raiz, letra);
        resultadoLabel.setText(encontrada ? "Letra encontrada: " + letra : "Letra no encontrada.");
        buscarField.clear();
    }

    private boolean buscarEnArbol(Nodo actual, String letraBuscada) {
        if (actual == null) return false;
        if (letraBuscada.equals(actual.letra)) return true;

        // Obtenemos el binario de la letra que queremos buscar para saber qué ruta seguir
        String binarioBusqueda = convertirABinario(letraBuscada.charAt(0));

        // Necesitamos saber en qué nivel estamos para decidir el bit
        return buscarSiguiendoBits(raiz, letraBuscada, binarioBusqueda, 0);
    }

    private boolean buscarSiguiendoBits(Nodo actual, String letra, String binario, int bitIndex) {
        if (actual == null) return false;
        if (letra.equals(actual.letra)) return true;
        if (bitIndex >= binario.length()) return false;

        char bit = binario.charAt(bitIndex);
        if (bit == '0') {
            return buscarSiguiendoBits(actual.izq, letra, binario, bitIndex + 1);
        } else {
            return buscarSiguiendoBits(actual.der, letra, binario, bitIndex + 1);
        }
    }

    @FXML
    private void clearSearch() {
        buscarField.clear();
        resultadoLabel.setText("Búsqueda limpiada.");
    }

    @FXML
    private void initializeTree() {
        raiz = new Nodo("root");
        tablaCodigos.getItems().clear();
        arbolPane.getChildren().clear();
        resultadoLabel.setText("Árbol reiniciado.");
    }

    // ================== Clases auxiliares ==================
    public static class Nodo {
        String id;
        String letra;
        Nodo izq, der;
        double x, y;

        public Nodo(String id) {
            this.id = id;
        }
    }

    public static class CodigoLetra {
        private String letra;
        private int posicion;
        private String binario;

        public CodigoLetra(String letra, int posicion, String binario) {
            this.letra = letra;
            this.posicion = posicion;
            this.binario = binario;
        }

        public String getLetra() { return letra; }
        public int getPosicion() { return posicion; }
        public String getBinario() { return binario; }
    }
}