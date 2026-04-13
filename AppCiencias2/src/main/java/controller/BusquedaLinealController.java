package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import utilities.SlotClave;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusquedaLinealController {

    private Integer n;

    @FXML private AnchorPane linealPane;

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;

    @FXML private TableView<SlotClave> tabla;
    @FXML private TableColumn<SlotClave, Integer> colPos;
    @FXML private TableColumn<SlotClave, String> colClave;

    @FXML private TextField claveField;
    @FXML private Label resultadoLabel;

    private final ObservableList<SlotClave> data = FXCollections.observableArrayList();
    private int digitos = 2;
    private boolean creada = false;

    @FXML
    public void initialize() {

        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));

        tabla.setItems(data);

        colPos.prefWidthProperty().bind(tabla.widthProperty().multiply(0.3));
        colClave.prefWidthProperty().bind(tabla.widthProperty().multiply(0.7));
    }

    @FXML
    private void crearEstructura() {
        this.n = leerEntero(nField);
        if (this.n == null || this.n < 1) {
            resultadoLabel.setText("N debe ser un número >= 1.");
            return;
        }

        digitos = digitosChoice.getValue() != null ? digitosChoice.getValue() : 2;

        data.clear();
        creada = true;

        resultadoLabel.setText("Estructura creada con N=" + n + " y claves de " + digitos + " dígitos.");
    }

    @FXML
    private void insertarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarClave();
            return;
        }

        String input = claveField.getText() == null ? "" : claveField.getText().trim();
        if (input.isEmpty()) {
            resultadoLabel.setText("Escribe una clave para insertar.");
            limpiarClave();
            return;
        }

        String claveTxt = normalizarClave(input, digitos);
        claveField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarClave();
            return;
        }

        for (SlotClave s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la estructura.");
                limpiarClave();
                return;
            }
        }

        int usados = contarClaves();
        if (usados >= n) {
            resultadoLabel.setText("La estructura está llena.");
            limpiarClave();
            return;
        }

        data.add(new SlotClave(usados + 1, claveTxt));

        var clavesOrdenadas = data.stream()
                .map(SlotClave::getClave)
                .filter(c -> c != null && !c.isBlank())
                .sorted()
                .toList();

        for (SlotClave s : data) {
            s.setClave("");
        }

        for (int i = 0; i < clavesOrdenadas.size(); i++) {
            data.get(i).setClave(clavesOrdenadas.get(i));
        }

        tabla.refresh();
        resultadoLabel.setText("Insertada y ordenada automáticamente.");
        limpiarClave();
    }

    @FXML
    private void buscarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarClave();
            return;
        }

        String input = claveField.getText() == null ? "" : claveField.getText().trim();
        if (input.isEmpty()) {
            resultadoLabel.setText("Escribe una clave para buscar.");
            limpiarClave();
            return;
        }

        String claveTxt = normalizarClave(input, digitos);
        claveField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("Clave inválida.");
            limpiarClave();
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

                limpiarClave();
                return;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Comparaciones: " + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");
        limpiarClave();
    }

    @FXML
    private void eliminarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        String input = claveField.getText() == null ? "" : claveField.getText().trim();

        if (!input.isEmpty()) {
            String claveTxt = normalizarClave(input, digitos);
            claveField.setText(claveTxt);

            boolean removed = data.removeIf(s -> claveTxt.equals(s.getClave()));

            if (removed) {
                reindexarPosiciones();
                tabla.refresh();
                resultadoLabel.setText("Eliminada la clave " + claveTxt);
            } else {
                resultadoLabel.setText("No se encontró la clave.");
            }

            limpiarClave();
            return;
        }

        SlotClave sel = tabla.getSelectionModel().getSelectedItem();
        if (sel != null) {
            data.remove(sel);
            reindexarPosiciones();
            tabla.refresh();
            resultadoLabel.setText("Eliminada la clave seleccionada.");
        } else {
            resultadoLabel.setText("Escribe una clave o selecciona una fila.");
        }
    }

    @FXML
    private void limpiarEstructura() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        data.clear();
        tabla.getSelectionModel().clearSelection();
        limpiarClave();

        resultadoLabel.setText("La tabla fue limpiada.");
    }

    private void limpiarClave() {
        claveField.clear();
        claveField.requestFocus();
    }
    
    @FXML
    private void guardarTabla() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar búsqueda lineal");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Búsqueda Lineal (*.lin)", "*.lin"));
        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=LINEAL");
            bw.newLine();
            bw.write("N=" + n);
            bw.newLine();
            bw.write("DIGITOS=" + digitos);
            bw.newLine();
            bw.write("DATA");
            bw.newLine();

            for (SlotClave s : data) {
                String clave = s.getClave() == null ? "" : s.getClave().trim();
                bw.write(s.getPosicion() + "|" + clave);
                bw.newLine();
            }

            bw.write("END");
            bw.newLine();

            resultadoLabel.setText("Guardado: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error guardando: " + e.getMessage());
        }
    }

    @FXML
    private void cargarTabla() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar búsqueda lineal");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Búsqueda Lineal (*.lin)", "*.lin"));
        File file = fc.showOpenDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            Integer newN = null;
            Integer newDig = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("DATA")) break;

                if (line.startsWith("N=")) {
                    newN = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("DIGITOS=")) {
                    newDig = Integer.parseInt(line.substring(8).trim());
                }
            }

            if (newN == null || newN < 1) {
                resultadoLabel.setText("Archivo inválido: N.");
                return;
            }

            if (newDig == null || newDig < 1) {
                newDig = 2;
            }

            this.n = newN;
            this.digitos = newDig;
            this.creada = true;

            nField.setText(String.valueOf(n));
            digitosChoice.setValue(digitos);

            List<String> claves = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("END")) break;
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;

                String clave = parts[1].trim();
                if (!clave.isEmpty()) {
                    clave = normalizarClave(clave, digitos);
                    if (claveValidaPorDigitos(clave, digitos)) {
                        claves.add(clave);
                    }
                }
            }

            if (claves.size() > n) {
                claves = claves.subList(0, n);
            }

            claves.sort(String::compareTo);
            data.clear();

            for (int i = 0; i < claves.size(); i++) {
                data.add(new SlotClave(i + 1, claves.get(i)));
            }

            tabla.refresh();
            resultadoLabel.setText("Cargado: " + file.getName());
            limpiarClave();

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
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
        if (!clave.matches("\\d+")) return clave;
        return String.format("%0" + digitos + "d", Integer.parseInt(clave));
    }

    private boolean claveValidaPorDigitos(String clave, int digitos) {
        if (clave == null) return false;
        if (clave.length() != digitos) return false;
        for (int i = 0; i < clave.length(); i++) {
            if (!Character.isDigit(clave.charAt(i))) return false;
        }
        return true;
    }

    private void reindexarPosiciones() {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setPosicion(i + 1);
        }
    }
}