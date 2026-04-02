package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.util.*;

public class ResiduosMultiplesController {

    @FXML private TextField mensajeField;
    @FXML private TextField buscarField;
    @FXML private TableView<CodigoLetra> tablaCodigos;
    @FXML private TableColumn<CodigoLetra, String> colLetra;
    @FXML private TableColumn<CodigoLetra, Integer> colPos;
    @FXML private TableColumn<CodigoLetra, String> colBinario;
    @FXML private Label resultadoLabel;
    @FXML private Pane arbolPane;
    @FXML private AnchorPane mainPane;
    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;
    @FXML private AnchorPane multiplePane;

    private NodoMultiple raiz = new NodoMultiple();

    @FXML
    public void initialize() {
        colLetra.setCellValueFactory(new PropertyValueFactory<>("letra"));
        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colBinario.setCellValueFactory(new PropertyValueFactory<>("binario"));

        if (menuPane != null) {
            menuPane.setVisible(false);
            menuPane.setManaged(false);
        }
    }

    @FXML
    private void procesarMensaje() {
        String mensaje = mensajeField.getText().trim().toUpperCase();
        if (mensaje.isEmpty()) return;

        tablaCodigos.getItems().clear();
        raiz = new NodoMultiple();

        int pos = 1;
        for (char c : mensaje.toCharArray()) {
            if (Character.isLetter(c) || c == 'Ñ') {
                String binario = convertirABinario(c);
                tablaCodigos.getItems().add(new CodigoLetra(String.valueOf(c), pos++, binario));
                insertarClave(binario, String.valueOf(c));
            }
        }
        actualizarArbolVisual();
        resultadoLabel.setText("Trie Base 4 (Residuos de 2 bits) construido.");
    }

    private String convertirABinario(char c) {
        int valor = (c == 'Ñ') ? 27 : (c - 'A' + 1);
        String b = Integer.toBinaryString(valor);
        while (b.length() < 6) b = "0" + b;
        return b;
    }

    private void insertarClave(String binario, String letra) {
        // Si el árbol está vacío, la raíz toma la primera letra
        if (raiz.letra == null && raiz.hijos.isEmpty()) {
            raiz.letra = letra;
            return;
        }
        insertarRecursivo(raiz, letra, binario, 0);
    }

    private void insertarRecursivo(NodoMultiple actual, String nuevaLetra, String nuevoBinario, int index) {
        if (index >= nuevoBinario.length()) return;

        // Si el nodo actual tiene una letra, hay colisión
        if (actual.letra != null) {
            String letraEx = actual.letra;
            String binEx = convertirABinario(letraEx.charAt(0));
            actual.letra = null; // Se convierte en nodo de enlace

            // Re-insertamos ambas letras desde este nivel
            moverABajo(actual, letraEx, binEx, index);
            moverABajo(actual, nuevaLetra, nuevoBinario, index);
        } else {
            // Si es nodo de enlace, simplemente intentamos bajar la nueva letra
            moverABajo(actual, nuevaLetra, nuevoBinario, index);
        }
    }

    private void moverABajo(NodoMultiple actual, String letra, String binario, int index) {
        if (index + 2 > binario.length()) return;

        String residuo = binario.substring(index, index + 2);
        NodoMultiple hijo = actual.hijos.get(residuo);

        if (hijo == null) {
            // Camino libre: creamos hoja con la letra (Detención temprana)
            NodoMultiple nuevo = new NodoMultiple();
            nuevo.letra = letra;
            actual.hijos.put(residuo, nuevo);
        } else {
            // El camino ya existe (es un nodo de enlace o una hoja que colisionará)
            insertarRecursivo(hijo, letra, binario, index + 2);
        }
    }

    // ================== Dibujo (Abanico de 4 hijos) ==================
    private void actualizarArbolVisual() {
        arbolPane.getChildren().clear();
        double startX = arbolPane.getWidth() / 2;
        dibujarNodo(raiz, startX, 40, startX * 0.5);
    }

    private void dibujarNodo(NodoMultiple nodo, double x, double y, double spacing) {
        if (nodo == null) return;

        Circle circle = new Circle(x, y, 14);
        circle.setStyle("-fx-fill: white; -fx-stroke: #2262C6; -fx-stroke-width: 2;");

        Text text = new Text(x - 6, y + 5, nodo.letra != null ? nodo.letra : "");
        text.setStyle("-fx-font-weight: bold; -fx-fill: #2262C6;");
        arbolPane.getChildren().addAll(circle, text);

        String[] residuos = {"00", "01", "10", "11"};
        double verticalGap = 80;

        for (int i = 0; i < residuos.length; i++) {
            String r = residuos[i];
            if (nodo.hijos.containsKey(r)) {
                // Cálculo de posición horizontal para los 4 hijos
                double childX = x + (i - 1.5) * spacing;

                Line line = new Line(x, y + 14, childX, y + verticalGap - 14);
                line.setStrokeWidth(1.5);

                Text label = new Text((x + childX) / 2 - 10, (y + y + verticalGap) / 2, r);
                label.setStyle("-fx-fill: red; -fx-font-weight: bold; -fx-font-size: 10px;");

                arbolPane.getChildren().addAll(line, label);
                dibujarNodo(nodo.hijos.get(r), childX, y + verticalGap, spacing * 0.5);
            }
        }
    }

    // Clases Auxiliares
    public static class NodoMultiple {
        Map<String, NodoMultiple> hijos = new HashMap<>(); // Llave: "00", "01", etc.
        String letra = null;
    }

    @FXML
    private void openMenu(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo menu...");
        menuPane.setVisible(true); // para que el panel del menu se vea
        menuPane.setManaged(true);// posiciona de primeras al panel
    }
    @FXML
    private void closeMenu(javafx.scene.input.MouseEvent event){
        System.out.println("cerrando menu...");
        menuPane.setVisible(false);// para que el panel del menu se oculte
        menuPane.setManaged(false);// lo quita de la primera capa, para liberar el espacio
    }

    @FXML
    private void openMenuBusquedas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo submenu de busquedas...");
        boolean isVisible = subMenuBusquedas.isVisible();
        subMenuBusquedas.setVisible(!isVisible);
        subMenuBusquedas.setManaged(!isVisible);
    }
    @FXML
    private void openMenuInternas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo submenu de busquedas internas...");
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);
    }
    @FXML
    private void mostrarBusquedaLineal(javafx.scene.input.MouseEvent event) {
        System.out.println("Abriendo busquedaLineal.fxml");
        loadPanel("busquedaLineal.fxml");
    }
    @FXML
    private void openBinario(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaBinaria.fxml");
        loadPanel("busquedaBinaria.fxml");
    }
    @FXML
    private void openFuncionHash(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaHash.fxml");
        loadPanel("busquedaHash.fxml");
    }
    @FXML
    private void openBResiduos(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaPorResiduos.fxml...");
        loadPanel("busquedaPorResiduos.fxml");
    }
    @FXML
    private void openGrafos(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo grafos.fxml...");
        loadPanel("grafos.fxml");
    }

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo inicio.fxml...");
        loadPanel("inicio.fxml");
    }
    @FXML
    private void openInternas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedasInternas.fxml...");
        loadPanel("busquedasInternas.fxml");
    }

    @FXML
    private void openLineal(javafx.scene.input.MouseEvent event) {
        System.out.println("abriendo busquedaILineal.fxml...");
        loadPanel("busquedaLineal.fxml");
    }

    @FXML private void buscarClave() {
        // Lógica de búsqueda simplificada
        resultadoLabel.setText("Búsqueda realizada en estructura Base 4.");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            multiplePane.getChildren().clear();
            multiplePane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
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