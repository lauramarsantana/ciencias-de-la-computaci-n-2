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
import utilities.SlotClave;


public class BusquedaBinariaController {

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;

    @FXML private TableView<SlotClave> tabla;
    @FXML private TableColumn<SlotClave, Integer> colPos;
    @FXML private TableColumn<SlotClave, String> colClave;

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;

    @FXML private AnchorPane binarioPane;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private VBox subMenuBusquedas;
    @FXML
    private VBox subMenuInternas;

    private final ObservableList<SlotClave> data = FXCollections.observableArrayList();
    private boolean creada = false;
    private int digitos = 2;

    @FXML
    public void initialize() {
        // Inicializar menú
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);

        digitosChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
        digitosChoice.setValue(2);

        digitosChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) digitos = newV;
        });

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));
        tabla.setItems(data);
    }

    @FXML
    private void openInternas(javafx.scene.input.MouseEvent event){
        System.out.println("Abriedno busquedasInternas.fxml");
        loadPanel("busquedasInternas.fxml");
    }

    @FXML
    private void crearEstructura() {
        Integer n = leerEntero(nField.getText());
        if (n == null || n <= 0) {
            resultadoLabel.setText("Ingresa un N válido (mayor que 0).");
            return;
        }

        digitos = digitosChoice.getValue() == null ? 2 : digitosChoice.getValue();

        data.clear();
        for (int i = 0; i < n; i++) {
            data.add(new SlotClave(i + 1, "")); 
        }

        creada = true;
        resultadoLabel.setText("Estructura creada (1.." + n + ").");
    }

    // Inserta y mantiene ORDEN ASCENDENTE
    @FXML
    private void insertarClaveOrdenada() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarInsercion();
            return;
        }

        String claveTxt = normalizarClave(claveInsertField.getText(), digitos);
        claveInsertField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
            limpiarInsercion();
            return;
        }

        // No repetición
        for (SlotClave s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la estructura.");
                limpiarInsercion();
                return;
            }
        }

        // ¿Hay espacio?
        int usados = contarClaves();
        if (usados >= data.size()) {
            resultadoLabel.setText("La estructura está llena. No se puede insertar más.");
            limpiarInsercion();
            return;
        }

        // 1) obtener todas las claves existentes + la nueva, ordenarlas
        var clavesOrdenadas = data.stream()
                .map(SlotClave::getClave)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        var lista = new java.util.ArrayList<String>(clavesOrdenadas);
        lista.add(claveTxt);
        lista.sort(String::compareTo); // orden lexicográfico funciona por ceros a la izquierda

        // 2) limpiar y reinsertar desde posición 1
        for (SlotClave s : data) s.setClave("");
        for (int i = 0; i < lista.size(); i++) data.get(i).setClave(lista.get(i));

        tabla.refresh();
        resultadoLabel.setText("Insertada y ordenada. Total claves: " + lista.size() + ".");
        // limpiar solo si fue exitoso
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }

    @FXML
    private void buscarClaveBinaria() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarBusqueda();
            return;
        }

        String claveTxt = normalizarClave(claveBuscarField.getText(), digitos);
        claveBuscarField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
            limpiarBusqueda();
            return;
        }

        // Tomamos solo el segmento lleno (0..usados-1) porque el resto está vacío
        int usados = contarClaves();
        if (usados == 0) {
            resultadoLabel.setText("No hay claves insertadas.");
            limpiarBusqueda();
            return;
        }

        int low = 0;
        int high = usados - 1;
        int comparaciones = 0;

        long inicio = System.nanoTime();

        while (low <= high) {
            int mid = (low + high) / 2;
            String midClave = data.get(mid).getClave();
            comparaciones++;

            int cmp = claveTxt.compareTo(midClave);
            if (cmp == 0) {
                long fin = System.nanoTime();
                SlotClave encontrado = data.get(mid);

                tabla.getSelectionModel().select(encontrado);
                tabla.scrollTo(encontrado);

                resultadoLabel.setText("Encontrada en posición " + encontrado.getPosicion()
                        + " | Comparaciones: " + comparaciones
                        + " | Tiempo: " + (fin - inicio) + " ns");

                limpiarBusqueda();
                return;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Comparaciones: " + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");
        limpiarBusqueda();
    }

    // Helpers
    private void limpiarBusqueda() {
        this.claveBuscarField.clear();
        this.claveBuscarField.requestFocus();
    }
    
    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }

    private int contarClaves() {
        int count = 0;
        for (SlotClave s : data) {
            if (s.getClave() != null && !s.getClave().isBlank()) count++;
        }
        return count;
    }

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

            binarioPane.getChildren().clear();
            binarioPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

