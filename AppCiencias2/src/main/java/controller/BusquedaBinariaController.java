package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utilities.SlotClave;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusquedaBinariaController {

    private Integer n; // tamaño máximo de la estructura

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;

    @FXML private TableView<SlotClave> tabla;
    @FXML private TableColumn<SlotClave, Integer> colPos;
    @FXML private TableColumn<SlotClave, String> colClave;

    @FXML private TextField claveField;
    @FXML private Label resultadoLabel;

    private final ObservableList<SlotClave> data = FXCollections.observableArrayList();
    private boolean creada = false;
    private int digitos = 2;

    @FXML
    public void initialize() {

        digitosChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
        digitosChoice.setValue(2);

        digitosChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) digitos = newV;
        });

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));

        tabla.setItems(data);
        tabla.getColumns().setAll(colPos, colClave);

        colPos.prefWidthProperty().bind(tabla.widthProperty().multiply(0.3));
        colClave.prefWidthProperty().bind(tabla.widthProperty().multiply(0.7));
    }

    @FXML
    private void crearEstructura() {
        this.n = leerEntero(nField.getText());
        if (this.n == null || this.n <= 0) {
            resultadoLabel.setText("Ingresa un N válido (mayor que 0).");
            return;
        }

        digitos = digitosChoice.getValue() == null ? 2 : digitosChoice.getValue();

        data.clear();

        creada = true;
        resultadoLabel.setText("Estructura creada (1.." + n + ").");
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

        for (SlotClave s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la estructura.");
                limpiarClave();
                return;
            }
        }

        int usados = contarClaves();
        if (usados >= n) {
            resultadoLabel.setText("La estructura está llena. No se puede insertar más.");
            limpiarClave();
            return;
        }

        data.add(new SlotClave(usados + 1, claveTxt));

        var listaOrdenada = data.stream()
                .sorted((a, b) -> a.getClave().compareTo(b.getClave()))
                .toList();

        data.clear();
        for (int i = 0; i < listaOrdenada.size(); i++) {
            data.add(new SlotClave(i + 1, listaOrdenada.get(i).getClave()));
        }

        tabla.refresh();
        resultadoLabel.setText("Insertada y ordenada. Total claves: " + listaOrdenada.size() + ".");
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

                limpiarClave();
                return;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Comparaciones: " + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");
        limpiarClave();
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

    @FXML
    private void guardarTabla() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar búsqueda binaria");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Búsqueda Binaria (*.bin)", "*.bin"));
        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=BINARIA"); bw.newLine();
            bw.write("N=" + n); bw.newLine();
            bw.write("DIGITOS=" + digitos); bw.newLine();
            bw.write("DATA"); bw.newLine();

            for (SlotClave s : data) {
                String clave = (s.getClave() == null) ? "" : s.getClave().trim();
                bw.write(s.getPosicion() + "|" + clave);
                bw.newLine();
            }

            bw.write("END"); bw.newLine();
            resultadoLabel.setText("Guardado: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error guardando: " + e.getMessage());
        }
    }

    @FXML
    private void cargarTabla() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar búsqueda binaria");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Búsqueda Binaria (*.bin)", "*.bin"));
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

                if (line.startsWith("N=")) newN = Integer.parseInt(line.substring(2).trim());
                else if (line.startsWith("DIGITOS=")) newDig = Integer.parseInt(line.substring(8).trim());
            }

            if (newN == null || newN < 1) {
                resultadoLabel.setText("Archivo inválido: N.");
                return;
            }
            if (newDig == null || newDig < 1) newDig = 2;

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

            claves = claves.stream().distinct().toList();

            if (claves.size() > n) {
                claves = claves.subList(0, n);
            }

            claves = new ArrayList<>(claves);
            claves.sort(String::compareTo);

            data.clear();
            for (int i = 0; i < claves.size(); i++) {
                data.add(new SlotClave(i + 1, claves.get(i)));
            }

            tabla.refresh();
            resultadoLabel.setText("Cargado: " + file.getName() + " | Total claves: " + data.size());
            limpiarClave();

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
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

            boolean removed = data.removeIf(s -> claveTxt.equals(s.getClave()));
            if (removed) {
                reindexarPosiciones();
                tabla.getSelectionModel().clearSelection();
                tabla.refresh();
                resultadoLabel.setText("Eliminada la clave " + claveTxt);
            } else {
                resultadoLabel.setText("No se encontró la clave para eliminar.");
            }

            limpiarClave();
            return;
        }

        SlotClave sel = tabla.getSelectionModel().getSelectedItem();
        if (sel != null && sel.getClave() != null && !sel.getClave().isBlank()) {
            String clave = sel.getClave();
            data.removeIf(s -> clave.equals(s.getClave()));
            reindexarPosiciones();
            tabla.getSelectionModel().clearSelection();
            tabla.refresh();
            resultadoLabel.setText("Eliminada la clave " + clave);
        } else {
            resultadoLabel.setText("Escribe una clave o selecciona una fila para eliminar.");
        }
    }

    private void reindexarPosiciones() {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setPosicion(i + 1);
        }
    }

    private void limpiarClave() {
        claveField.clear();
        claveField.requestFocus();
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