package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.util.*;
import utilities.CodigoLetra; // Asegúrate de haber creado este archivo en utilities

public class TriesResiduosController {

    @FXML private TextField mensajeField;
    @FXML private TextField buscarField;
    @FXML private Label resultadoLabel;
    @FXML private Pane arbolPane;

    // Configuración de la Tabla
    @FXML private TableView<CodigoLetra> tablaCodigos;
    @FXML private TableColumn<CodigoLetra, String> colLetra;
    @FXML private TableColumn<CodigoLetra, Integer> colAlfabeto;
    @FXML private TableColumn<CodigoLetra, String> colBinario;

    private NodoDigital raiz = new NodoDigital();

    @FXML
    public void initialize() {
        // Enlazar columnas con los atributos de CodigoLetra
        colLetra.setCellValueFactory(new PropertyValueFactory<>("letra"));
        colAlfabeto.setCellValueFactory(new PropertyValueFactory<>("valorAlfabeto"));
        colBinario.setCellValueFactory(new PropertyValueFactory<>("binario"));

        // Limitar búsqueda a 1 carácter
        buscarField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 1) {
                buscarField.setText(newText.substring(0, 1));
            }
        });
    }

    @FXML
    private void procesarMensaje() {
        String texto = mensajeField.getText().trim().toUpperCase();
        if (texto.isEmpty()) {
            resultadoLabel.setText("Ingresa un mensaje válido.");
            return;
        }

        initializeTree(); // Limpia todo antes de empezar

        for (char c : texto.toCharArray()) {
            if (Character.isLetter(c) || c == 'Ñ') {
                int valor = (c == 'Ñ') ? 27 : (c - 'A' + 1);
                String binario = String.format("%5s", Integer.toBinaryString(valor)).replace(' ', '0');

                // Llenar tabla y árbol
                tablaCodigos.getItems().add(new CodigoLetra(String.valueOf(c), valor, binario));
                insertarClave(binario, String.valueOf(c));
            }
        }

        actualizarArbolVisual();
        resultadoLabel.setText("Trie construido exitosamente.");
    }

    private void insertarClave(String binario, String letra) {
        if (raiz.letra == null && raiz.hijos.isEmpty()) {
            raiz.letra = letra;
            return;
        }
        insertarRecursivo(raiz, letra, binario, 0);
    }

    private void insertarRecursivo(NodoDigital actual, String nuevaLetra, String nuevoBinario, int bitIndex) {
        if (actual.letra != null) {
            String letraExistente = actual.letra;
            int valorE = (letraExistente.charAt(0) == 'Ñ') ? 27 : (letraExistente.charAt(0) - 'A' + 1);
            String binarioExistente = String.format("%5s", Integer.toBinaryString(valorE)).replace(' ', '0');

            actual.letra = null; // Nodo intermedio
            moverABajo(actual, letraExistente, binarioExistente, bitIndex);
            moverABajo(actual, nuevaLetra, nuevoBinario, bitIndex);
        } else {
            moverABajo(actual, nuevaLetra, nuevoBinario, bitIndex);
        }
    }

    private void moverABajo(NodoDigital actual, String letra, String binario, int bitIndex) {
        if (bitIndex >= binario.length()) return;

        char bit = binario.charAt(bitIndex);
        NodoDigital hijo = actual.hijos.get(bit);

        if (hijo == null) {
            NodoDigital nuevo = new NodoDigital();
            nuevo.letra = letra;
            actual.hijos.put(bit, nuevo);
        } else {
            insertarRecursivo(hijo, letra, binario, bitIndex + 1);
        }
    }

    @FXML
    private void buscarClave() {
        String letraInput = buscarField.getText().trim().toUpperCase();
        if (letraInput.isEmpty()) return;

        int valor = (letraInput.charAt(0) == 'Ñ') ? 27 : (letraInput.charAt(0) - 'A' + 1);
        String claveBinaria = String.format("%5s", Integer.toBinaryString(valor)).replace(' ', '0');

        NodoDigital actual = raiz;
        boolean encontrada = true;

        for (char bit : claveBinaria.toCharArray()) {
            actual = actual.hijos.get(bit);
            if (actual == null) {
                encontrada = false;
                break;
            }
            if (actual.letra != null && actual.letra.equals(letraInput)) break;
        }

        if (encontrada && actual != null && letraInput.equals(actual.letra)) {
            resultadoLabel.setText("Letra '" + letraInput + "' encontrada.");
        } else {
            resultadoLabel.setText("Letra '" + letraInput + "' no está en el árbol.");
        }
    }

    private void actualizarArbolVisual() {
        arbolPane.getChildren().clear();
        dibujarNodo(raiz, arbolPane.getWidth() / 2, 40, 150);
    }

    private void dibujarNodo(NodoDigital nodo, double x, double y, double spacing) {
        if (nodo == null) return;

        double radio = 14;
        Circle circle = new Circle(x, y, radio);
        circle.setStyle("-fx-fill: white; -fx-stroke: black;");
        if (nodo.letra != null) circle.setStyle("-fx-fill: #D4E6F1; -fx-stroke: #2E86C1;");

        Text text = new Text(x - 5, y + 5, nodo.letra != null ? nodo.letra : "");
        text.setStyle("-fx-font-weight: bold; -fx-fill: #1B4F72;");

        arbolPane.getChildren().addAll(circle, text);

        if (nodo.hijos.containsKey('0')) {
            dibujarConexion(x, y, x - spacing, y + 60, "0", radio);
            dibujarNodo(nodo.hijos.get('0'), x - spacing, y + 60, spacing * 0.5);
        }
        if (nodo.hijos.containsKey('1')) {
            dibujarConexion(x, y, x + spacing, y + 60, "1", radio);
            dibujarNodo(nodo.hijos.get('1'), x + spacing, y + 60, spacing * 0.5);
        }
    }

    private void dibujarConexion(double x1, double y1, double x2, double y2, String bit, double r) {
        Line line = new Line(x1, y1 + r, x2, y2 - r);
        Text label = new Text((x1 + x2) / 2 - 10, (y1 + y2) / 2, bit);
        label.setStyle("-fx-fill: red; -fx-font-weight: bold;");
        arbolPane.getChildren().addAll(line, label);
    }

    @FXML
    private void initializeTree() {
        raiz = new NodoDigital();
        tablaCodigos.getItems().clear();
        arbolPane.getChildren().clear();
        resultadoLabel.setText("Sistema reiniciado.");
    }

    @FXML private void clearSearch() { buscarField.clear(); resultadoLabel.setText("Listo."); }

    // Clases Auxiliares
    public static class NodoDigital {
        Map<Character, NodoDigital> hijos = new HashMap<>();
        String letra = null;
    }
}