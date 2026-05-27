package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import utilities.SlotBloqueClave;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusquedaBinariaExternaController {

    private Integer numeroBloques;
    private Integer registrosPorBloque;

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

    private boolean creada = false;
    private int digitos = 2;

    @FXML
    public void initialize() {
        digitosChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
        digitosChoice.setValue(2);

        digitosChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) digitos = newV;
        });

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
        this.numeroBloques = leerEntero(bloquesField.getText());
        this.registrosPorBloque = leerEntero(registrosField.getText());

        if (numeroBloques == null || numeroBloques <= 0) {
            resultadoLabel.setText("Ingresa un número de bloques válido.");
            return;
        }

        if (registrosPorBloque == null || registrosPorBloque <= 0) {
            resultadoLabel.setText("Ingresa una cantidad válida de registros por bloque.");
            return;
        }

        digitos = digitosChoice.getValue() == null ? 2 : digitosChoice.getValue();

        data.clear();

        for (int b = 1; b <= numeroBloques; b++) {
            for (int p = 1; p <= registrosPorBloque; p++) {
                data.add(new SlotBloqueClave(b, p, ""));
            }
        }

        creada = true;

        resultadoLabel.setText("Estructura binaria externa creada con "
                + numeroBloques + " bloque(s) y "
                + registrosPorBloque + " registro(s) por bloque.");

        limpiarClave();
    }

    @FXML
    private void insertarClaveOrdenada() {
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

        if (contarClaves() >= data.size()) {
            resultadoLabel.setText("La estructura externa está llena.");
            limpiarClave();
            return;
        }

        List<String> claves = obtenerClaves();
        claves.add(claveTxt);
        claves.sort(String::compareTo);

        llenarEstructuraConClaves(claves);

        tabla.refresh();
        resultadoLabel.setText("Insertada y ordenada. Total claves: " + claves.size() + ".");
        limpiarClave();
    }

    @FXML
    private void buscarClaveBinaria() {
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
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarClave();
            return;
        }

        int usados = contarClaves();

        if (usados == 0) {
            resultadoLabel.setText("No hay claves insertadas.");
            limpiarClave();
            return;
        }

        int low = 0;
        int high = usados - 1;
        int comparaciones = 0;

        long inicio = System.nanoTime();

        while (low <= high) {
            int mid = (low + high) / 2;

            SlotBloqueClave actual = data.get(mid);
            String midClave = actual.getClave();

            comparaciones++;

            int cmp = claveTxt.compareTo(midClave);

            if (cmp == 0) {
                long fin = System.nanoTime();

                tabla.getSelectionModel().select(actual);
                tabla.scrollTo(actual);

                resultadoLabel.setText("Encontrada en bloque " + actual.getBloque()
                        + ", posición " + actual.getPosicion()
                        + " | Comparaciones: " + comparaciones
                        + " | Tiempo: " + (fin - inicio) + " ns");

                limpiarClave();
                return;

            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
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

            if (!claveValidaPorDigitos(claveTxt, digitos)) {
                resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
                limpiarClave();
                return;
            }

            List<String> claves = obtenerClaves();

            if (claves.removeIf(c -> c.equals(claveTxt))) {
                llenarEstructuraConClaves(claves);
                tabla.getSelectionModel().clearSelection();
                tabla.refresh();
                resultadoLabel.setText("Eliminada la clave " + claveTxt);
            } else {
                resultadoLabel.setText("No se encontró la clave para eliminar.");
            }

            limpiarClave();
            return;
        }

        SlotBloqueClave sel = tabla.getSelectionModel().getSelectedItem();

        if (sel != null && sel.getClave() != null && !sel.getClave().isBlank()) {
            String clave = sel.getClave();

            List<String> claves = obtenerClaves();
            claves.removeIf(c -> c.equals(clave));

            llenarEstructuraConClaves(claves);

            tabla.getSelectionModel().clearSelection();
            tabla.refresh();

            resultadoLabel.setText("Eliminada la clave " + clave);
        } else {
            resultadoLabel.setText("Escribe una clave o selecciona una fila para eliminar.");
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

        resultadoLabel.setText("La estructura fue limpiada.");
        limpiarClave();
    }

    @FXML
    private void guardarTabla() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar búsqueda binaria externa");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Búsqueda Binaria Externa (*.binext)", "*.binext")
        );

        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=BINARIA_EXTERNA");
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
        fc.setTitle("Cargar búsqueda binaria externa");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Búsqueda Binaria Externa (*.binext)", "*.binext")
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

            List<String> claves = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("END")) break;
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);

                if (parts.length < 3) continue;

                String clave = parts[2].trim();

                if (!clave.isEmpty()) {
                    clave = normalizarClave(clave, digitos);

                    if (claveValidaPorDigitos(clave, digitos)) {
                        claves.add(clave);
                    }
                }
            }

            claves = new ArrayList<>(claves.stream().distinct().toList());
            claves.sort(String::compareTo);

            int capacidad = numeroBloques * registrosPorBloque;

            if (claves.size() > capacidad) {
                claves = claves.subList(0, capacidad);
            }

            data.clear();

            for (int b = 1; b <= numeroBloques; b++) {
                for (int p = 1; p <= registrosPorBloque; p++) {
                    data.add(new SlotBloqueClave(b, p, ""));
                }
            }

            llenarEstructuraConClaves(claves);

            tabla.refresh();
            resultadoLabel.setText("Cargado: " + file.getName() + " | Total claves: " + contarClaves());
            limpiarClave();

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
    }

    private List<String> obtenerClaves() {
        List<String> claves = new ArrayList<>();

        for (SlotBloqueClave s : data) {
            if (s.getClave() != null && !s.getClave().isBlank()) {
                claves.add(s.getClave());
            }
        }

        return claves;
    }

    private void llenarEstructuraConClaves(List<String> claves) {
        for (SlotBloqueClave s : data) {
            s.setClave("");
        }

        for (int i = 0; i < claves.size() && i < data.size(); i++) {
            data.get(i).setClave(claves.get(i));
        }
    }

    private int contarClaves() {
        int count = 0;

        for (SlotBloqueClave s : data) {
            if (s.getClave() != null && !s.getClave().isBlank()) {
                count++;
            }
        }

        return count;
    }

    private void limpiarClave() {
        claveField.clear();
        claveField.requestFocus();
    }

    private Integer leerEntero(String txt) {
        if (txt == null) return null;

        txt = txt.trim();

        if (txt.isEmpty()) return null;

        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            return null;
        }
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
}
