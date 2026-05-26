package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import utilities.SlotBloqueClave;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusquedaLinealExternaController {

    private Integer numeroBloques;
    private Integer registrosPorBloque;

    @FXML private AnchorPane linealExternaPane;

    @FXML private TextField bloquesField;
    @FXML private TextField registrosField;
    @FXML private ChoiceBox<Integer> digitosChoice;

    @FXML private TableView<SlotBloqueClave> tabla;
    @FXML private TableColumn<SlotBloqueClave, Integer> colBloque;
    @FXML private TableColumn<SlotBloqueClave, Integer> colPosicion;
    @FXML private TableColumn<SlotBloqueClave, String> colClave;

    @FXML private TextField claveField;
    @FXML private Label resultadoLabel;

    private final ObservableList<SlotBloqueClave> data = FXCollections.observableArrayList();

    private int digitos = 2;
    private boolean creada = false;

    @FXML
    public void initialize() {
        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        colBloque.setCellValueFactory(new PropertyValueFactory<>("bloque"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));

        tabla.setItems(data);

        colBloque.prefWidthProperty().bind(tabla.widthProperty().multiply(0.25));
        colPosicion.prefWidthProperty().bind(tabla.widthProperty().multiply(0.25));
        colClave.prefWidthProperty().bind(tabla.widthProperty().multiply(0.50));
    }

    @FXML
    private void crearEstructura() {
        this.numeroBloques = leerEntero(bloquesField);
        this.registrosPorBloque = leerEntero(registrosField);

        if (numeroBloques == null || numeroBloques < 1) {
            resultadoLabel.setText("El número de bloques debe ser >= 1.");
            return;
        }

        if (registrosPorBloque == null || registrosPorBloque < 1) {
            resultadoLabel.setText("Los registros por bloque deben ser >= 1.");
            return;
        }

        digitos = digitosChoice.getValue() != null ? digitosChoice.getValue() : 2;

        data.clear();

        for (int b = 1; b <= numeroBloques; b++) {
            for (int p = 1; p <= registrosPorBloque; p++) {
                data.add(new SlotBloqueClave(b, p, ""));
            }
        }

        creada = true;

        resultadoLabel.setText("Estructura externa creada con "
                + numeroBloques + " bloque(s), "
                + registrosPorBloque + " registro(s) por bloque y claves de "
                + digitos + " dígitos.");
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

        for (SlotBloqueClave s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la estructura.");
                limpiarClave();
                return;
            }
        }

        SlotBloqueClave libre = buscarPrimerEspacioLibre();

        if (libre == null) {
            resultadoLabel.setText("La estructura externa está llena.");
            limpiarClave();
            return;
        }

        libre.setClave(claveTxt);

        ordenarClavesEnBloques();

        tabla.refresh();
        resultadoLabel.setText("Insertada y ordenada automáticamente en bloques.");
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

        for (SlotBloqueClave s : data) {
            if (s.getClave() == null || s.getClave().isBlank()) {
                continue;
            }

            comparaciones++;

            if (claveTxt.equals(s.getClave())) {
                long fin = System.nanoTime();

                tabla.getSelectionModel().select(s);
                tabla.scrollTo(s);

                resultadoLabel.setText("Encontrada en bloque " + s.getBloque()
                        + ", posición " + s.getPosicion()
                        + " | Comparaciones: " + comparaciones
                        + " | Tiempo: " + (fin - inicio) + " ns");

                limpiarClave();
                return;
            }
        }

        long fin = System.nanoTime();

        resultadoLabel.setText("No encontrada | Comparaciones: "
                + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");

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

            for (SlotBloqueClave s : data) {
                if (claveTxt.equals(s.getClave())) {
                    s.setClave("");
                    ordenarClavesEnBloques();
                    tabla.refresh();
                    resultadoLabel.setText("Eliminada la clave " + claveTxt);
                    limpiarClave();
                    return;
                }
            }

            resultadoLabel.setText("No se encontró la clave.");
            limpiarClave();
            return;
        }

        SlotBloqueClave sel = tabla.getSelectionModel().getSelectedItem();

        if (sel != null && sel.getClave() != null && !sel.getClave().isBlank()) {
            sel.setClave("");
            ordenarClavesEnBloques();
            tabla.refresh();
            resultadoLabel.setText("Eliminada la clave seleccionada.");
        } else {
            resultadoLabel.setText("Escribe una clave o selecciona una fila con clave.");
        }
    }

    @FXML
    private void limpiarEstructura() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        for (SlotBloqueClave s : data) {
            s.setClave("");
        }

        tabla.getSelectionModel().clearSelection();
        tabla.refresh();
        limpiarClave();

        resultadoLabel.setText("La estructura externa fue limpiada.");
    }

    @FXML
    private void guardarTabla() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar búsqueda lineal externa");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Búsqueda Lineal Externa (*.linext)", "*.linext")
        );

        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=LINEAL_EXTERNA");
            bw.newLine();
            bw.write("BLOQUES=" + numeroBloques);
            bw.newLine();
            bw.write("REGISTROS_POR_BLOQUE=" + registrosPorBloque);
            bw.newLine();
            bw.write("DIGITOS=" + digitos);
            bw.newLine();
            bw.write("DATA");
            bw.newLine();

            for (SlotBloqueClave s : data) {
                String clave = s.getClave() == null ? "" : s.getClave().trim();
                bw.write(s.getBloque() + "|" + s.getPosicion() + "|" + clave);
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
        fc.setTitle("Cargar búsqueda lineal externa");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Búsqueda Lineal Externa (*.linext)", "*.linext")
        );

        File file = fc.showOpenDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            Integer newBloques = null;
            Integer newRegistros = null;
            Integer newDig = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("DATA")) break;

                if (line.startsWith("BLOQUES=")) {
                    newBloques = Integer.parseInt(line.substring(8).trim());
                } else if (line.startsWith("REGISTROS_POR_BLOQUE=")) {
                    newRegistros = Integer.parseInt(line.substring(21).trim());
                } else if (line.startsWith("DIGITOS=")) {
                    newDig = Integer.parseInt(line.substring(8).trim());
                }
            }

            if (newBloques == null || newBloques < 1) {
                resultadoLabel.setText("Archivo inválido: BLOQUES.");
                return;
            }

            if (newRegistros == null || newRegistros < 1) {
                resultadoLabel.setText("Archivo inválido: REGISTROS_POR_BLOQUE.");
                return;
            }

            if (newDig == null || newDig < 1) {
                newDig = 2;
            }

            this.numeroBloques = newBloques;
            this.registrosPorBloque = newRegistros;
            this.digitos = newDig;
            this.creada = true;

            bloquesField.setText(String.valueOf(numeroBloques));
            registrosField.setText(String.valueOf(registrosPorBloque));
            digitosChoice.setValue(digitos);

            data.clear();

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("END")) break;
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);

                if (parts.length < 3) continue;

                int bloque = Integer.parseInt(parts[0].trim());
                int posicion = Integer.parseInt(parts[1].trim());
                String clave = parts[2].trim();

                if (!clave.isEmpty()) {
                    clave = normalizarClave(clave, digitos);

                    if (!claveValidaPorDigitos(clave, digitos)) {
                        clave = "";
                    }
                }

                data.add(new SlotBloqueClave(bloque, posicion, clave));
            }

            if (data.isEmpty()) {
                for (int b = 1; b <= numeroBloques; b++) {
                    for (int p = 1; p <= registrosPorBloque; p++) {
                        data.add(new SlotBloqueClave(b, p, ""));
                    }
                }
            }

            ordenarClavesEnBloques();

            tabla.refresh();
            resultadoLabel.setText("Cargado: " + file.getName());
            limpiarClave();

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
    }

    private SlotBloqueClave buscarPrimerEspacioLibre() {
        for (SlotBloqueClave s : data) {
            if (s.getClave() == null || s.getClave().isBlank()) {
                return s;
            }
        }

        return null;
    }

    private void ordenarClavesEnBloques() {
        List<String> clavesOrdenadas = new ArrayList<>();

        for (SlotBloqueClave s : data) {
            if (s.getClave() != null && !s.getClave().isBlank()) {
                clavesOrdenadas.add(s.getClave());
            }
        }

        clavesOrdenadas.sort(String::compareTo);

        for (SlotBloqueClave s : data) {
            s.setClave("");
        }

        for (int i = 0; i < clavesOrdenadas.size() && i < data.size(); i++) {
            data.get(i).setClave(clavesOrdenadas.get(i));
        }
    }

    private void limpiarClave() {
        claveField.clear();
        claveField.requestFocus();
    }

    private Integer leerEntero(TextField tf) {
        try {
            String t = tf.getText();

            if (t == null || t.trim().isEmpty()) {
                return null;
            }

            return Integer.parseInt(t.trim());

        } catch (Exception e) {
            return null;
        }
    }

    private String normalizarClave(String clave, int digitos) {
        if (clave == null) return "";

        clave = clave.trim();

        if (!clave.matches("\\d+")) {
            return clave;
        }

        return String.format("%0" + digitos + "d", Integer.parseInt(clave));
    }

    private boolean claveValidaPorDigitos(String clave, int digitos) {
        if (clave == null) return false;
        if (clave.length() != digitos) return false;

        for (int i = 0; i < clave.length(); i++) {
            if (!Character.isDigit(clave.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
