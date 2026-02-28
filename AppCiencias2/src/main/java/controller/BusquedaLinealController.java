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

public class BusquedaLinealController {

    private Integer n; // tamaño máximo de la estructura

    @FXML private AnchorPane linealPane;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private VBox subMenuBusquedas;
    @FXML
    private VBox subMenuInternas;

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;

    @FXML private TableView<SlotClave> tabla;
    @FXML private TableColumn<SlotClave, Integer> colPos;
    @FXML private TableColumn<SlotClave, String> colClave;

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;

    private final ObservableList<SlotClave> data = FXCollections.observableArrayList();
    private int digitos = 2; // por defecto
    private boolean creada = false;


    @FXML
    public void initialize() {
        // Inicializar menú
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);

        // Inicializar lógica propia de este controlador
        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));

        tabla.setItems(data);
        tabla.getColumns().setAll(colPos, colClave);
        tabla.getColumns().setAll(colPos, colClave);

        // Opcional: ajustar ancho proporcional
        colPos.prefWidthProperty().bind(tabla.widthProperty().multiply(0.3));
        colClave.prefWidthProperty().bind(tabla.widthProperty().multiply(0.7));

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

    @FXML
    private void crearEstructura() {
        this.n = leerEntero(nField);
        if (this.n == null || this.n < 1) {
            resultadoLabel.setText("N debe ser un número >= 1.");
            return;
        }

        digitos = digitosChoice.getValue() != null ? digitosChoice.getValue() : 2;

        data.clear(); // no agrega filas vacías

        creada = true;
        resultadoLabel.setText("Estructura creada con N=" + n + " y claves de " + digitos + " dígitos.");
    }

    @FXML
    private void insertarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarInsercion();
            return;
        }

        String claveTxt = normalizarClave(claveInsertField.getText(), digitos);
        claveInsertField.setText(claveTxt); // para que el usuario vea el 0 agregado

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
            limpiarInsercion();
            return;
        }
        


        // Evitar repetidos
        for (SlotClave s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la estructura.");
                limpiarInsercion();
                return;
            }
        }
        
        //¿Hay espacio?
        int usados = contarClaves();
        if (usados >= n) {
            resultadoLabel.setText("La estructura está llena. No se puede insertar más.");
            limpiarInsercion();
            return;
        }

        // Insertar en la primera posición vacía
        /*

        ya no tenemos filas vacías, ya no funciona esto
        for (SlotClave s : data) {
                if (s.getClave() == null || s.getClave().isBlank()) {
                   s.setClave(claveTxt);
                break;
            }
        }*/

        int posicion = usados + 1; // siguiente posición disponible
        data.add(new SlotClave(posicion, claveTxt));

        var clavesOrdenadas = data.stream()
                .map(SlotClave::getClave)
                .filter(c -> c != null && !c.isBlank())
                .sorted()
                .toList();

        // limpiar toda la estructura
        for (SlotClave s : data) {
            s.setClave("");
        }

        // reinsertar ordenadas
        int limite = Math.min(clavesOrdenadas.size(), data.size());
        for (int i = 0; i < limite; i++) {
            data.get(i).setClave(clavesOrdenadas.get(i));
        }

        tabla.refresh();
        resultadoLabel.setText("Insertada y ordenada automáticamente.");
        limpiarInsercion();
        
    
    }

    @FXML
    private void buscarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarBusqueda();
            return;
        }

        String claveTxt = normalizarClave(claveBuscarField.getText(), digitos);
        claveBuscarField.setText(claveTxt); // para que el usuario vea el 0 agregado

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
            limpiarBusqueda();
            return;
        }


        int comparaciones = 0;
        long inicio = System.nanoTime();

        for (SlotClave s : data) {
            comparaciones++;
            if (claveTxt.equals(s.getClave())) {
                long fin = System.nanoTime();
                tabla.getSelectionModel().select(s);
                tabla.scrollTo(s);
                resultadoLabel.setText("Encontrada en posición " + s.getPosicion()
                        + " | Comparaciones: " + comparaciones
                        + " | Tiempo: " + (fin - inicio) + " ns");
                limpiarBusqueda();
                return;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Comparaciones: " + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");
        limpiarBusqueda();
    }
    
    @FXML
    private void ordenarClaves() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
    }

        // 1) Tomar todas las claves no vacías
        var claves = data.stream()
                .map(SlotClave::getClave)
                .filter(c -> c != null && !c.isBlank())
                .sorted() // orden ascendente (funciona bien porque están normalizadas con ceros)
                .toList();

        // 2) Vaciar la estructura
        for (SlotClave s : data) {
            s.setClave("");
        }

        // 3) Reinsertar en orden desde la posición 0
        for (int i =1; i <= claves.size(); i++) {
            data.get(i).setClave(claves.get(i));
        }

        tabla.refresh();
        resultadoLabel.setText("Claves ordenadas de menor a mayor.");
        limpiarInsercion();
        limpiarBusqueda();
    }


    private Integer leerEntero(TextField tf) {
        try {
            String t = tf.getText();
            if (t == null || t.trim().isEmpty()) return null;
            return Integer.parseInt(t.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    private int contarClaves() {
        int count = 0;
        for (SlotClave s : data) {
            if (s.getClave() != null && !s.getClave().isBlank()) count++;
        }
        return count;
    }
    
    private String normalizarClave(String clave, int digitos) {
    if (clave == null) return "";

    clave = clave.trim();

    // Si no son números, se deuvuelve igual
    if (!clave.matches("\\d+")) return clave;

    // Completar con ceros a la izquierda
    return String.format("%0" + digitos + "d", Integer.parseInt(clave));
    }
    
    private void limpiarBusqueda() {
    claveBuscarField.clear();
    claveBuscarField.requestFocus();
    }

    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }

    @FXML
    private void openInternas(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo busquedasInternas.fxml...");
        loadPanel("busquedasInternas.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            linealPane.getChildren().clear();
            linealPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean claveValidaPorDigitos(String clave, int digitos) {
        if (clave == null) return false;
        if (clave.length() != digitos) return false;
        for (int i = 0; i < clave.length(); i++) {
            if (!Character.isDigit(clave.charAt(i))) return false;
        }
        return true;
    }
}
