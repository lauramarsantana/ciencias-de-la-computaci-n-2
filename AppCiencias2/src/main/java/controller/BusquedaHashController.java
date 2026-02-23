package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import utilities.SlotHash;

public class BusquedaHashController {

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;
    @FXML private TextField modField;

    @FXML private TableView<SlotHash> tabla;
    @FXML private TableColumn<SlotHash, Integer> colPos;
    @FXML private TableColumn<SlotHash, Integer> colHash;
    @FXML private TableColumn<SlotHash, String> colClave;

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;

    @FXML private AnchorPane hashPane;
    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;

    private final ObservableList<SlotHash> data = FXCollections.observableArrayList();

    private boolean creada = false;
    private int digitos = 2;
    private int N = 0;
    private int MOD = 100;

    @FXML
    public void initialize() {
        // Inicializar menú
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);

        digitosChoice.setItems(FXCollections.observableArrayList(1,2,3,4,5,6));
        digitosChoice.setValue(2);

        digitosChoice.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) digitos = b;
        });

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));

        tabla.setItems(data);
    }

    @FXML
    private void crearEstructura() {
        Integer n = leerEntero(nField.getText());
        Integer m = leerEntero(modField.getText());

        if (n == null || n <= 0) {
            resultadoLabel.setText("N inválido (debe ser > 0).");
            return;
        }
        if (m == null || m <= 0) {
            resultadoLabel.setText("MOD inválido (debe ser > 0).");
            return;
        }

        N = n;
        MOD = m;
        digitos = digitosChoice.getValue() == null ? 2 : digitosChoice.getValue();

        data.clear();
        for (int i = 0; i < N; i++) {
            data.add(new SlotHash(i + 1)); // posición 1..N
        }

        creada = true;
        resultadoLabel.setText("Tabla creada (1.." + N + "), MOD=" + MOD + ".");
    }

    // Hash base: (clave % MOD) y luego lo ajustamos al rango 0..N-1
    private int hashBase(int claveNum) {
        int h = claveNum % MOD;
        // asegurar positivo
        if (h < 0) h += MOD;
        return h % N;
    }

    @FXML
    private void insertarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarInsercion();
            return;
        }

        String claveTxt = normalizarClave(claveInsertField.getText(), digitos);
        claveInsertField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarInsercion();
            return;
        }

        // No repetición (revisar en toda la tabla)
        for (SlotHash s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la tabla.");
                limpiarInsercion();
                return;
            }
        }

        int claveNum = Integer.parseInt(claveTxt);
        int start = hashBase(claveNum);

        int comparaciones = 0;
        long inicio = System.nanoTime();

        // Sondeo lineal: (start + i) % N
        for (int i = 0; i < N; i++) {
            int idx = (start + i) % N;
            SlotHash slot = data.get(idx);
            comparaciones++;

            if (slot.isVacio()) {
                slot.setClave(claveTxt);
                slot.setHash(start); // guardamos el hash base para mostrarlo
                tabla.refresh();

                long fin = System.nanoTime();
                resultadoLabel.setText("Insertada en posición " + slot.getPosicion()
                        + " | Hash: " + start
                        + " | Colisiones: " + i
                        + " | Comparaciones: " + comparaciones
                        + " | Tiempo: " + (fin - inicio) + " ns");

                // limpiar solo si insertó bien
                claveInsertField.clear();
                claveInsertField.requestFocus();
                return;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("Tabla llena | Comparaciones: " + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");
    }

    @FXML
    private void buscarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        String claveTxt = normalizarClave(claveBuscarField.getText(), digitos);
        claveBuscarField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
            limpiarBusqueda();
            return;
        }

        int claveNum = Integer.parseInt(claveTxt);
        int start = hashBase(claveNum);

        int comparaciones = 0;
        long inicio = System.nanoTime();

        // Buscar con el mismo sondeo lineal
        for (int i = 0; i < N; i++) {
            int idx = (start + i) % N;
            SlotHash slot = data.get(idx);
            comparaciones++;

            // Si encontramos vacío, podemos cortar 
            if (slot.isVacio()) {
                long fin = System.nanoTime();
                resultadoLabel.setText("No encontrada | Hash: " + start
                        + " | Comparaciones: " + comparaciones
                        + " | Tiempo: " + (fin - inicio) + " ns");
                limpiarBusqueda();
                return;
            }

            if (claveTxt.equals(slot.getClave())) {
                long fin = System.nanoTime();
                tabla.getSelectionModel().select(slot);
                tabla.scrollTo(slot);

                resultadoLabel.setText("Encontrada en posición " + slot.getPosicion()
                        + " | Hash: " + start
                        + " | Comparaciones: " + comparaciones
                        + " | Tiempo: " + (fin - inicio) + " ns");
                limpiarBusqueda();
                return;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Hash: " + start
                + " | Comparaciones: " + comparaciones
                + " | Tiempo: " + (fin - inicio) + " ns");
        limpiarBusqueda();
    }

    private void limpiarBusqueda() {
        claveBuscarField.clear();
        claveBuscarField.requestFocus();
    }
    
    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }

    // ===== Helpers de validación / normalización =====

    private Integer leerEntero(String txt) {
        if (txt == null) return null;
        txt = txt.trim();
        if (txt.isEmpty()) return null;
        try { return Integer.parseInt(txt); } catch (NumberFormatException e) { return null; }
    }

    private boolean claveValidaPorDigitos(String clave, int digitos) {
        if (clave == null) return false;
        clave = clave.trim();
        return clave.matches("\\d{" + digitos + "}");
    }

    private String normalizarClave(String clave, int digitos) {
        if (clave == null) return "";
        clave = clave.trim();
        if (!clave.matches("\\d+")) return clave;
        return String.format("%0" + digitos + "d", Integer.parseInt(clave));
    }

    // configurando cada boton del menu desplegable
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
    private void openGrafos(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo grafos.fxml...");
        loadPanel("grafos.fxml");
    }

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo inicio.fxml...");
        loadPanel("inicio.fxml");
    }
    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            hashPane.getChildren().clear();
            hashPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
