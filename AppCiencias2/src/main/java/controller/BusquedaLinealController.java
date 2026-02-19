package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import utilities.SlotClave;

public class BusquedaLinealController {

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;

    @FXML private TableView<SlotClave> tabla;
    @FXML private TableColumn<SlotClave, Integer> colPos;
    @FXML private TableColumn<SlotClave, String> colClave;

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;
    @FXML private VBox linealPane;

    private final ObservableList<SlotClave> data = FXCollections.observableArrayList();
    private int digitos = 2; // por defecto
    private boolean creada = false;

    
    @FXML
    public void initialize() {
        // llenar choice de dígitos 
        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));

        tabla.setItems(data);
    }
    

    @FXML
    private void crearEstructura() {
        Integer n = leerEntero(nField);
        if (n == null || n < 1) {
            resultadoLabel.setText("N debe ser un número >= 1.");
            return;
        }

        digitos = digitosChoice.getValue() != null ? digitosChoice.getValue() : 2;

        data.clear();
        for (int i = 0; i < n; i++) {
            data.add(new SlotClave(i + 1, "")); // clave vacía
        }

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
        if (usados >= data.size()) {
            resultadoLabel.setText("La estructura está llena. No se puede insertar más.");
            limpiarInsercion();
            return;
        }

        // Insertar en la primera posición vacía
        for (SlotClave s : data) {
                if (s.getClave() == null || s.getClave().isBlank()) {
                   s.setClave(claveTxt);
                break; 
            }
        }

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
