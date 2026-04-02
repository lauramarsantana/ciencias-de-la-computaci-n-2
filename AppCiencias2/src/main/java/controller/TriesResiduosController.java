package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.util.*;

public class TriesResiduosController {

    @FXML private TextField mensajeField;
    @FXML private TextField buscarField;
    @FXML private TableView<CodigoLetra> tablaCodigos;
    @FXML private TableColumn<CodigoLetra, String> colLetra;
    @FXML private TableColumn<CodigoLetra, Integer> colPos;
    @FXML private TableColumn<CodigoLetra, String> colBinario;
    @FXML private Label resultadoLabel;
    @FXML private Pane arbolPane;

    private NodoDigital raiz = new NodoDigital();

    @FXML
    public void initialize() {
        colLetra.setCellValueFactory(new PropertyValueFactory<>("letra"));
        colPos.setCellValueFactory(new PropertyValueFactory<>("posición"));
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
        String mensaje = mensajeField.getText().trim().toUpperCase();
        if (mensaje.isEmpty()) {
            resultadoLabel.setText("Ingresa un mensaje válido.");
            return;
        }

        tablaCodigos.getItems().clear();
        raiz = new NodoDigital(); // Reiniciar raíz para nueva construcción

        int pos = 1;
        for (char c : mensaje.toCharArray()) {
            if (Character.isLetter(c) || c == 'Ñ') {
                String binario = convertirABinario(c);
                tablaCodigos.getItems().add(new CodigoLetra(String.valueOf(c), pos++, binario));
                insertarClave(binario, String.valueOf(c)); //
            }
        }

        actualizarArbolVisual();
        resultadoLabel.setText("Trie por residuos construido (5 niveles).");
    }

    private String convertirABinario(char c) {
        int valor;
        if (c == 'Ñ') valor = 27;
        else valor = (c - 'A' + 1);
        return String.format("%5s", Integer.toBinaryString(valor)).replace(' ', '0'); //
    }

    private void insertarClave(String binario, String letra) {
        if (raiz.letra == null && raiz.hijos.isEmpty()) {
            raiz.letra = letra;
            return;
        }
        insertarRecursivo(raiz, letra, binario, 0);
    }

    private void insertarRecursivo(NodoDigital actual, String nuevaLetra, String nuevoBinario, int bitIndex) {
        // 1. Caso: El nodo actual es una hoja (tiene una letra) -> Hay Colisión
        if (actual.letra != null) {
            String letraExistente = actual.letra;
            String binarioExistente = convertirABinario(letraExistente.charAt(0));
            actual.letra = null; // Deja de ser hoja para volverse nodo de enlace

            // Bajamos la letra que ya estaba
            moverABajo(actual, letraExistente, binarioExistente, bitIndex);
            // Bajamos la letra nueva
            moverABajo(actual, nuevaLetra, nuevoBinario, bitIndex);
        }
        // 2. Caso: Es un nodo de enlace (no tiene letra, tiene hijos)
        else {
            moverABajo(actual, nuevaLetra, nuevoBinario, bitIndex);
        }
    }

    private void moverABajo(NodoDigital actual, String letra, String binario, int bitIndex) {
        if (bitIndex >= binario.length()) return;

        char bit = binario.charAt(bitIndex);
        NodoDigital hijo = actual.hijos.get(bit);

        if (hijo == null) {
            // Si el camino está libre, la letra se queda aquí (Detención temprana)
            NodoDigital nuevo = new NodoDigital();
            nuevo.letra = letra;
            actual.hijos.put(bit, nuevo);
        } else {
            // Si ya hay un hijo (nodo de enlace o colisión futura), seguimos bajando
            insertarRecursivo(hijo, letra, binario, bitIndex + 1);
        }
    }

    @FXML
    private void buscarClave() {
        String letraInput = buscarField.getText().trim().toUpperCase();
        if (letraInput.isEmpty()) {
            resultadoLabel.setText("Ingresa una letra.");
            return;
        }

        String claveBinaria = convertirABinario(letraInput.charAt(0));
        NodoDigital actual = raiz;
        boolean encontrada = true;

        // Búsqueda real siguiendo los bits en el árbol [cíte: 53, 54]
        for (char bit : claveBinaria.toCharArray()) {
            actual = actual.hijos.get(bit);
            if (actual == null) {
                encontrada = false;
                break;
            }
        }

        if (encontrada && actual.esFinClave) {
            resultadoLabel.setText("Letra '" + letraInput + "' encontrada.");
        } else {
            resultadoLabel.setText("Letra '" + letraInput + "' no está en el árbol.");
        }
    }

    // ================== Dibujo Optimizado ==================
    private void actualizarArbolVisual() {
        arbolPane.getChildren().clear();
        // Spacing inicial más controlado para que no se salga
        dibujarNodo(raiz, arbolPane.getWidth() / 2, 30, 140);
    }

    private void dibujarNodo(NodoDigital nodo, double x, double y, double spacing) {
        if (nodo == null) return;

        // Radio reducido a 12 para que sea más pequeño
        Circle circle = new Circle(x, y, 12);
        circle.setStyle("-fx-fill: white; -fx-stroke: black;");

        // Si tiene letra, la pintamos dentro
        Text text = new Text(x - 5, y + 4, nodo.letra != null ? nodo.letra : "");
        text.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-fill: blue;");

        arbolPane.getChildren().addAll(circle, text);

        double verticalGap = 50; // Más corto para que quepa verticalmente
        double childSpacing = spacing * 0.6; // Reducción de ancho por nivel

        if (nodo.hijos.containsKey('0')) {
            dibujarLinea(x, y, x - spacing, y + verticalGap, "0");
            dibujarNodo(nodo.hijos.get('0'), x - spacing, y + verticalGap, childSpacing);
        }
        if (nodo.hijos.containsKey('1')) {
            dibujarLinea(x, y, x + spacing, y + verticalGap, "1");
            dibujarNodo(nodo.hijos.get('1'), x + spacing, y + verticalGap, childSpacing);
        }
    }

    private void dibujarLinea(double x1, double y1, double x2, double y2, String bit) {
        Line line = new Line(x1, y1 + 12, x2, y2 - 12);
        Text label = new Text((x1 + x2) / 2 - 8, (y1 + y2) / 2, bit);
        label.setStyle("-fx-fill: red; -fx-font-weight: bold; -fx-font-size: 10px;");
        arbolPane.getChildren().addAll(line, label);
    }

    // Resto de métodos de navegación FXML (openMenu, loadPanel, etc.) se mantienen igual...
    @FXML private void initializeTree() {
        raiz = new NodoDigital();
        tablaCodigos.getItems().clear();
        arbolPane.getChildren().clear();
        resultadoLabel.setText("Trie reiniciado.");
    }

    @FXML private void clearSearch() { buscarField.clear(); resultadoLabel.setText("Listo."); }

    public static class NodoDigital {
        Map<Character, NodoDigital> hijos = new HashMap<>();
        boolean esFinClave = false;
        String letra = null;
    }

    public static class CodigoLetra {
        private String letra, binario;
        private int posicion;
        public CodigoLetra(String l, int p, String b) { this.letra = l; this.posicion = p; this.binario = b; }
        public String getLetra() { return letra; }
        public int getPosicion() { return posicion; }
        public String getBinario() { return binario; }
    }
}