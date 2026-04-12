package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utilities.HashExpParciales;
import utilities.SlotCubeta;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusquedaExpParcialesController {

    @FXML private AnchorPane expPane;
    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;
    @FXML private ChoiceBox<Integer> filasChoice;

    @FXML private TableView<ObservableList<String>> tabla;
    @FXML private TableColumn<ObservableList<String>, String> colRow;

    private final ObservableList<ObservableList<String>> dataUI = FXCollections.observableArrayList();

    @FXML private TextField claveField;
    @FXML private Label resultadoLabel;

    @FXML private Label doLabel;
    @FXML private Label pendientesLabel;

    private int digitos = 2;
    private int filas = 2;
    private boolean creada = false;
    private int nInicial = 0;

    private HashExpParciales estructura;

    @FXML
    public void initialize() {
        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        filasChoice.getItems().addAll(2, 3, 4, 5, 6);
        filasChoice.setValue(2);

        tabla.setItems(dataUI);
    }

    // =========================
    // Tabla invertida
    // =========================
    private void construirTablaInvertida(int nCubetas) {
        tabla.getColumns().clear();

        TableColumn<ObservableList<String>, String> c0 = new TableColumn<>("");
        c0.setPrefWidth(60);
        c0.setCellValueFactory(p -> {
            ObservableList<String> fila = p.getValue();
            String valor = (fila != null && fila.size() > 0) ? fila.get(0) : "";
            return new javafx.beans.property.SimpleStringProperty(valor);
        });
        tabla.getColumns().add(c0);

        for (int i = 0; i < nCubetas; i++) {
            final int colIndex = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(String.valueOf(i));
            col.setPrefWidth(80);
            col.setCellValueFactory(p -> {
                ObservableList<String> fila = p.getValue();
                String valor = (fila != null && fila.size() > colIndex) ? fila.get(colIndex) : "";
                return new javafx.beans.property.SimpleStringProperty(valor);
            });
            tabla.getColumns().add(col);
        }

        dataUI.clear();

        for (int f = 0; f < filas; f++) {
            ObservableList<String> fila = FXCollections.observableArrayList();
            fila.add(String.valueOf(f + 1));

            for (int c = 0; c < nCubetas; c++) {
                fila.add("");
            }

            dataUI.add(fila);
        }

        tabla.setItems(dataUI);
    }

    private void refrescarTablaInvertida() {
        if (!creada || estructura == null) return;

        int n = estructura.getN();

        if (tabla.getColumns().size() != n + 1) {
            construirTablaInvertida(n);
        }

        List<SlotCubeta> snapshot = estructura.snapshotTabla();

        for (int c = 0; c < n; c++) {
            SlotCubeta sc = snapshot.get(c);

            for (int f = 0; f < filas; f++) {
                String valor = sc.getFila(f);
                dataUI.get(f).set(c + 1, valor == null ? "" : valor);
            }
        }

        tabla.refresh();

        if (doLabel != null) {
            double doExp = estructura.densidadExpansion();
            double doRed = estructura.densidadReduccion();

            doLabel.setText(String.format(
                    "DO Exp: %.2f%% (%d/%d) | DO Red: %.2f%% (%d/%d cubetas)",
                    doExp * 100.0,
                    estructura.totalOcupados(),
                    estructura.getN() * estructura.getFilas(),
                    doRed * 100.0,
                    estructura.totalCubetasOcupadas(),
                    estructura.getN()
            ));
        }

        if (pendientesLabel != null) {
            pendientesLabel.setText("Pendientes: " + estructura.getPendientes());
        }
    }

    // =========================
    // Lógica principal
    // =========================
    @FXML
    private void crearEstructura() {
        Integer n = leerEntero(nField);
        if (n == null || n < 2) {
            resultadoLabel.setText("N debe ser un número >= 2.");
            return;
        }

        digitos = digitosChoice.getValue() != null ? digitosChoice.getValue() : 2;
        filas = filasChoice.getValue() != null ? filasChoice.getValue() : 2;
        nInicial = n;

        estructura = new HashExpParciales(n, filas);
        creada = true;

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        resultadoLabel.setText("Estructura creada " + filas + "x" + estructura.getN()
                + " | Expansiones parciales | Dígitos=" + digitos);

        limpiarClave();
    }

    @FXML
    private void insertarClave() {
        if (!creada || estructura == null) {
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

        if (estructura.existeClave(claveTxt)) {
            resultadoLabel.setText("Esa clave ya existe (en tabla o pendiente).");
            limpiarClave();
            return;
        }

        int pendientesAntes = estructura.getPendientes().size();

        boolean ok = estructura.insertar(claveTxt);
        if (!ok) {
            resultadoLabel.setText("No se pudo insertar la clave.");
            limpiarClave();
            return;
        }

        int pendientesDespues = estructura.getPendientes().size();

        refrescarTabla();

        int hk = ((Integer.parseInt(claveTxt) % estructura.getN()) + estructura.getN()) % estructura.getN();

        StringBuilder msg = new StringBuilder();
        msg.append("Insertada ").append(claveTxt).append(" | h(k)=").append(hk);

        if (pendientesDespues > pendientesAntes) {
            msg.append(" | Colisión: quedó pendiente");
        }

        if (estructura.densidadExpansion() >= 0.75) {
            msg.append(" | Se recomienda expansión");
        }

        resultadoLabel.setText(msg.toString());
        limpiarClave();
    }

    @FXML
    private void buscarClave() {
        if (!creada || estructura == null) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarClave();
            return;
        }

        String claveTxt = normalizarClave(claveField.getText(), digitos);
        claveField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarClave();
            return;
        }

        long inicio = System.nanoTime();
        String info = estructura.buscarInfo(claveTxt);
        long fin = System.nanoTime();

        if (info == null) {
            resultadoLabel.setText("No encontrada | Tiempo: " + (fin - inicio) + " ns");
        } else {
            resultadoLabel.setText(info + " | Tiempo: " + (fin - inicio) + " ns");
        }

        limpiarClave();
    }

    @FXML
    private void eliminarClave() {
        if (!creada || estructura == null) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarClave();
            return;
        }

        String input = claveField.getText() == null ? "" : claveField.getText().trim();
        if (input.isEmpty()) {
            resultadoLabel.setText("Escribe una clave para eliminar.");
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

        boolean ok = estructura.eliminar(claveTxt);

        if (!ok) {
            resultadoLabel.setText("No se encontró la clave para eliminar.");
            limpiarClave();
            return;
        }

        refrescarTabla();

        StringBuilder msg = new StringBuilder();
        msg.append("Eliminada ").append(claveTxt);

        if (estructura.densidadReduccion() <= 0.25) {
            msg.append(" | Se recomienda reducción");
        }

        resultadoLabel.setText(msg.toString());
        limpiarClave();
    }

    @FXML
    private void expandirManual() {
        if (!creada || estructura == null) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        int nAntes = estructura.getN();
        int faseAntes = estructura.getFase();
        boolean ok = estructura.expandir();
        int nDespues = estructura.getN();
        int faseDespues = estructura.getFase();

        if (!ok) {
            resultadoLabel.setText("No se puede expandir. La DO de expansión debe ser 75% o más.");
            return;
        }

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        resultadoLabel.setText("Expansión realizada: " + filas + "x" + nAntes + " → " + filas + "x" + nDespues
                + " | Fase: " + faseAntes + " → " + faseDespues);
    }

    @FXML
    private void reducirManual() {
        if (!creada || estructura == null) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        int nAntes = estructura.getN();
        int faseAntes = estructura.getFase();
        boolean ok = estructura.reducir();
        int nDespues = estructura.getN();
        int faseDespues = estructura.getFase();

        if (!ok) {
            resultadoLabel.setText("No se puede reducir. La DO de reducción debe ser 25% o menos.");
            return;
        }

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        resultadoLabel.setText("Reducción realizada: " + filas + "x" + nAntes + " → " + filas + "x" + nDespues
                + " | Fase: " + faseAntes + " → " + faseDespues);
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

        estructura = new HashExpParciales(nInicial, filas);

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        claveField.clear();
        claveField.requestFocus();

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
        fc.setTitle("Guardar expansiones parciales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exp Parciales (*.exp)", "*.exp"));
        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=EXP_PARCIALES"); bw.newLine();
            bw.write("N=" + estructura.getN()); bw.newLine();
            bw.write("DIGITOS=" + digitos); bw.newLine();
            bw.write("FILAS=" + filas); bw.newLine();
            bw.write("NBASE=" + estructura.getNBase()); bw.newLine();
            bw.write("FASE=" + estructura.getFase()); bw.newLine();
            bw.write("DATA"); bw.newLine();

            List<SlotCubeta> snapshot = estructura.snapshotTabla();
            for (SlotCubeta s : snapshot) {
                StringBuilder sb = new StringBuilder();
                sb.append(s.getCubeta());

                for (int f = 0; f < filas; f++) {
                    String valor = s.getFila(f);
                    sb.append("|").append(valor == null ? "" : valor);
                }

                bw.write(sb.toString());
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
        fc.setTitle("Cargar expansiones parciales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exp Parciales (*.exp)", "*.exp"));
        File file = fc.showOpenDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            Integer newN = null;
            Integer newDig = null;
            Integer newFilas = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("DATA")) break;
                else if (line.startsWith("FILAS=")) newFilas = Integer.parseInt(line.substring(6).trim());
                else if (line.startsWith("N=")) newN = Integer.parseInt(line.substring(2).trim());
                else if (line.startsWith("DIGITOS=")) newDig = Integer.parseInt(line.substring(8).trim());
            }

            if (newN == null || newN < 2) {
                resultadoLabel.setText("Archivo inválido: N.");
                return;
            }

            if (newDig == null || newDig < 1) newDig = 2;
            if (newFilas == null || newFilas < 2) newFilas = 2;

            digitos = newDig;
            digitosChoice.setValue(digitos);

            filas = newFilas;
            filasChoice.setValue(filas);

            nField.setText(String.valueOf(newN));
            nInicial = newN;

            estructura = new HashExpParciales(newN, filas);
            creada = true;
            construirTablaInvertida(estructura.getN());

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
                    String[] parts = line.split("\\|", -1);

                    if (parts.length > 1) {
                        for (int i = 1; i < parts.length; i++) {
                            String valor = parts[i].trim();
                            if (!valor.isEmpty()) {
                                claves.add(normalizarClave(valor, digitos));
                            }
                        }
                    }
                } else {
                    pend.add(normalizarClave(line, digitos));
                }
            }

            for (String c : claves) {
                if (claveValidaPorDigitos(c, digitos)) {
                    estructura.insertar(c);
                }
            }

            for (String p : pend) {
                if (claveValidaPorDigitos(p, digitos)) {
                    estructura.insertar(p);
                }
            }

            refrescarTabla();
            resultadoLabel.setText("Cargado: " + file.getName());
            limpiarClave();

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
    }

    // =========================
    // Helpers
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

    private void limpiarClave() {
        claveField.clear();
        claveField.requestFocus();
    }
}