package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import utilities.HashExpTotales;
import utilities.SlotCubeta;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusquedaExpTotalesController {

    @FXML private TextField nField;                 // n inicial (2,4,8...) o cualquier número (se ajusta)
    @FXML private ChoiceBox<Integer> digitosChoice; // 1..4

    @FXML private TableView<ObservableList<String>> tabla;
    @FXML private TableColumn<ObservableList<String>, String> colRow;

    private final ObservableList<ObservableList<String>> dataUI = FXCollections.observableArrayList();

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;

    // Opcionales (si los agregas al FXML)
    @FXML private Label doLabel;
    @FXML private Label pendientesLabel;

    // =========================
    // Estado
    // =========================
    private final ObservableList<SlotCubeta> data = FXCollections.observableArrayList();
    private int digitos = 2;
    private boolean creada = false;
    private int nInicial = 0;

    private HashExpTotales estructura; // nuestro “modelo”

    @FXML
    public void initialize() {

        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        tabla.setItems(dataUI);
    }

    private void construirTablaInvertida(int nCubetas) {
    tabla.getColumns().clear();

    // Columna izquierda: muestra "1" y "2"
    TableColumn<ObservableList<String>, String> c0 = new TableColumn<>("");
    c0.setPrefWidth(60);
    c0.setCellValueFactory(p ->
            new javafx.beans.property.SimpleStringProperty(p.getValue().get(0))
    );
    tabla.getColumns().add(c0);

    // Columnas cubetas 1..N
    for (int i = 0; i < nCubetas; i++) {
        final int colIndex = i + 1; // 0 es etiqueta
        TableColumn<ObservableList<String>, String> col = new TableColumn<>(String.valueOf(i));
        col.setPrefWidth(80);
        col.setCellValueFactory(p ->
                new javafx.beans.property.SimpleStringProperty(p.getValue().get(colIndex))
        );
        tabla.getColumns().add(col);
    }

    // 2 filas: "1" y "2"
    dataUI.clear();
    ObservableList<String> fila1 = FXCollections.observableArrayList();
    ObservableList<String> fila2 = FXCollections.observableArrayList();

    fila1.add("1");
    fila2.add("2");

    for (int i = 0; i < nCubetas; i++) {
        fila1.add("");
        fila2.add("");
    }

    dataUI.add(fila1);
    dataUI.add(fila2);

    tabla.setItems(dataUI);
    }

    private void refrescarTablaInvertida() {
        if (!creada || estructura == null) return;

        int n = estructura.getN();

        // 1. Verificar si el número de columnas coincide con N cubetas + 1 (etiqueta)
        if (tabla.getColumns().size() != n + 1) {
            construirTablaInvertida(n);
        }

        // 2. Obtener los datos actuales de la estructura
        List<SlotCubeta> snapshot = estructura.snapshotTabla();

        // 3. Limpiar y volver a llenar las filas para asegurar que los datos se refresquen
        ObservableList<String> nuevaFila1 = FXCollections.observableArrayList();
        ObservableList<String> nuevaFila2 = FXCollections.observableArrayList();

        nuevaFila1.add("1"); // Etiqueta de fila
        nuevaFila2.add("2"); // Etiqueta de fila

        for (int c = 0; c < n; c++) {
            SlotCubeta sc = snapshot.get(c);
            nuevaFila1.add(sc.getFila1() == null ? "" : sc.getFila1());
            nuevaFila2.add(sc.getFila2() == null ? "" : sc.getFila2());
        }

        // Actualizar la lista que observa la tabla
        dataUI.clear();
        dataUI.addAll(nuevaFila1, nuevaFila2);

        tabla.refresh();

        // Actualizar Labels de estado
        if (doLabel != null) {
            double doVal = estructura.densidadOcupacional();
            doLabel.setText(String.format("DO: %.2f%% (%d/%d) | Exp: 75%% Red: 25%%",
                    doVal * 100.0,
                    estructura.totalOcupados(),
                    estructura.getN() * 2)); // 2 es FILAS (HashExpTotales.FILAS)
        }
    }

    // =========================
    // Lógica principal
    // =========================

    @FXML
    private void crearEstructura() {
        Integer n = leerEntero(nField);
        if (n == null || n < 2) {
            resultadoLabel.setText("N debe ser un número >= 2 (inicia en 2xN).");
            return;
        }

        digitos = digitosChoice.getValue() != null ? digitosChoice.getValue() : 2;
        nInicial = n;

        estructura = new HashExpTotales(n);
        creada = true;

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        refrescarTabla();
        resultadoLabel.setText("Estructura creada 2x" + estructura.getN()
                + " | Umbrales: expandir 75%, reducir 25% | Dígitos=" + digitos);
        limpiarInsercion();
        limpiarBusqueda();
    }

    @FXML
    private void insertarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        String claveTxt = normalizarClave(claveInsertField.getText(), digitos);
        // ... Validaciones de digitos y existencia ...

        // Realizar la inserción en la lógica
        estructura.insertar(claveTxt);

        // IMPORTANTE: Refrescar la UI DESPUÉS de insertar
        refrescarTabla();

        // Mostrar en qué cubeta quedó (usando el N actual)
        int nActual = estructura.getN();
        int cubetaDestino = (Integer.parseInt(claveTxt) % nActual);

        resultadoLabel.setText("Insertada " + claveTxt + " en Cubeta " + cubetaDestino);
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
        claveBuscarField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarBusqueda();
            return;
        }

        long inicio = System.nanoTime();
        String info = estructura.buscarInfo(claveTxt);
        long fin = System.nanoTime();

        if (info == null) {
            resultadoLabel.setText("No encontrada | Tiempo: " + (fin - inicio) + " ns");
        } else {
            resultadoLabel.setText(info + " | Tiempo: " + (fin - inicio) + " ns");
            // (Opcional) podrías seleccionar la fila de la cubeta:
            // tabla.getSelectionModel().select(residuo);
        }

        limpiarBusqueda();
    }

    @FXML
    private void eliminarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        String input = claveBuscarField.getText() == null ? "" : claveBuscarField.getText().trim();
        if (input.isEmpty()) {
            resultadoLabel.setText("Escribe una clave para eliminar.");
            return;
        }

        String claveTxt = normalizarClave(input, digitos);
        claveBuscarField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarBusqueda();
            return;
        }

        int nAntes = estructura.getN();
        boolean ok = estructura.eliminar(claveTxt);
        int nDespues = estructura.getN();

        if (!ok) {
            resultadoLabel.setText("No se encontró la clave para eliminar.");
            limpiarBusqueda();
            return;
        }

        refrescarTabla();
        String msg = "Eliminada " + claveTxt;
        if (nDespues != nAntes) msg += " | REDUJO: 2x" + nAntes + " → 2x" + nDespues;
        resultadoLabel.setText(msg);

        limpiarBusqueda();
    }

    private void refrescarTabla() {
    refrescarTablaInvertida();
    }
    
    @FXML
    private void limpiarEstructura() {
        if (!creada || estructura == null) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        estructura = new HashExpTotales(nInicial);

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        claveInsertField.clear();
        claveBuscarField.clear();
        claveInsertField.requestFocus();

        resultadoLabel.setText("La estructura fue limpiada y reiniciada.");
    }

    // =========================
    // Guardar / Cargar
    // =========================

    @FXML
    private void guardarTabla() {
        if (!creada || estructura == null) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar expansiones totales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exp Totales (*.ext)", "*.ext"));
        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=EXP_TOTALES"); bw.newLine();
            bw.write("N=" + estructura.getN()); bw.newLine();
            bw.write("DIGITOS=" + digitos); bw.newLine();
            bw.write("DATA"); bw.newLine();

            // Guardar desde la estructura real
            List<SlotCubeta> snapshot = estructura.snapshotTabla();
            for (SlotCubeta s : snapshot) {
                bw.write(s.getCubeta() + "|" +
                        (s.getFila1() == null ? "" : s.getFila1()) + "|" +
                        (s.getFila2() == null ? "" : s.getFila2()));
                bw.newLine();
            }

            bw.write("PENDIENTES"); bw.newLine();
            for (String p : estructura.getPendientes()) {
                bw.write(p);
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
        fc.setTitle("Cargar expansiones totales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exp Totales (*.ext)", "*.ext"));
        File file = fc.showOpenDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            Integer newN = null;
            Integer newDig = null;

            // Leer cabecera hasta DATA
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("DATA")) break;
                if (line.startsWith("N=")) newN = Integer.parseInt(line.substring(2).trim());
                else if (line.startsWith("DIGITOS=")) newDig = Integer.parseInt(line.substring(8).trim());
            }

            if (newN == null || newN < 2) {
                resultadoLabel.setText("Archivo inválido: N.");
                return;
            }
            if (newDig == null || newDig < 1) newDig = 2;

            digitos = newDig;
            digitosChoice.setValue(digitos);
            nField.setText(String.valueOf(newN));
            nInicial = newN;

            estructura = new HashExpTotales(newN);
            creada = true;
            construirTablaInvertida(estructura.getN());

            // Leer cuadro
            List<String> claves = new ArrayList<>();
            List<String> pend = new ArrayList<>();
            boolean leyendoPend = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("PENDIENTES")) {
                    leyendoPend = true;
                    continue;
                }
                if (line.equals("END")) break;
                if (line.isEmpty()) continue;

                if (!leyendoPend) {
                    // cubeta|fila1|fila2
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 3) {
                        String f1 = parts[1].trim();
                        String f2 = parts[2].trim();

                        if (!f1.isEmpty()) claves.add(normalizarClave(f1, digitos));
                        if (!f2.isEmpty()) claves.add(normalizarClave(f2, digitos));
                    }
                } else {
                    pend.add(normalizarClave(line, digitos));
                }
            }

            // reconstruir metiendo todo por insertar
            for (String c : claves) {
                if (claveValidaPorDigitos(c, digitos)) estructura.insertar(c);
            }
            for (String p : pend) {
                if (claveValidaPorDigitos(p, digitos)) estructura.insertar(p);
            }

            refrescarTabla();
            resultadoLabel.setText("Cargado: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
    }

    // =========================
    // Helpers (iguales a tu estilo)
    // =========================

    private Integer leerEntero(TextField tf) {
        try {
            String t = tf.getText();
            if (t == null || t.trim().isEmpty()) return null;
            return Integer.parseInt(t.trim());
        } catch (Exception e) {
            return null;
        }
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

    private void limpiarBusqueda() {
        claveBuscarField.clear();
        claveBuscarField.requestFocus();
    }

    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }
}