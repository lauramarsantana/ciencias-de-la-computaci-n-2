package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    @FXML private TableView<ObservableList<String>> tabla;
    @FXML private TableColumn<ObservableList<String>, String> colRow;

    private final ObservableList<ObservableList<String>> dataUI = FXCollections.observableArrayList();

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;

    @FXML private Label doLabel;
    @FXML private Label pendientesLabel;

    private int digitos = 2;
    private boolean creada = false;
    private int nInicial = 0;

    private HashExpParciales estructura;

    @FXML
    public void initialize() {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);

        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        tabla.setItems(dataUI);
    }

    // =========================
    // Menú
    // =========================
    @FXML
    private void openMenu(javafx.scene.input.MouseEvent event){
        menuPane.setVisible(true);
        menuPane.setManaged(true);
    }

    @FXML
    private void closeMenu(javafx.scene.input.MouseEvent event){
        menuPane.setVisible(false);
        menuPane.setManaged(false);
    }

    @FXML
    private void openMenuBusquedas(javafx.scene.input.MouseEvent event){
        boolean isVisible = subMenuBusquedas.isVisible();
        subMenuBusquedas.setVisible(!isVisible);
        subMenuBusquedas.setManaged(!isVisible);
    }

    @FXML
    private void openMenuInternas(javafx.scene.input.MouseEvent event){
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);
    }

    @FXML
    private void mostrarBusquedaLineal(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaLineal.fxml");
    }

    @FXML
    private void openBinario(javafx.scene.input.MouseEvent event){
        loadPanel("busquedaBinaria.fxml");
    }

    @FXML
    private void openFuncionHash(javafx.scene.input.MouseEvent event){
        loadPanel("busquedaHash.fxml");
    }

    @FXML
    private void openGrafos(javafx.scene.input.MouseEvent event){
        loadPanel("grafos.fxml");
    }

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event){
        loadPanel("inicio.fxml");
    }

    @FXML
    private void openInternas(javafx.scene.input.MouseEvent event){
        loadPanel("busquedasInternas.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();
            expPane.getChildren().clear();
            expPane.getChildren().add(panel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // Tabla invertida
    // =========================
    private void construirTablaInvertida(int nCubetas) {
        tabla.getColumns().clear();

        TableColumn<ObservableList<String>, String> c0 = new TableColumn<>("");
        c0.setPrefWidth(60);
        c0.setCellValueFactory(p ->
                new javafx.beans.property.SimpleStringProperty(p.getValue().get(0))
        );
        tabla.getColumns().add(c0);

        for (int i = 0; i < nCubetas; i++) {
            final int colIndex = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(String.valueOf(i));
            col.setPrefWidth(80);
            col.setCellValueFactory(p ->
                    new javafx.beans.property.SimpleStringProperty(p.getValue().get(colIndex))
            );
            tabla.getColumns().add(col);
        }

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

        if (tabla.getColumns().size() != n + 1) {
            construirTablaInvertida(n);
        }

        ObservableList<String> fila1 = dataUI.get(0);
        ObservableList<String> fila2 = dataUI.get(1);

        List<SlotCubeta> snapshot = estructura.snapshotTabla();

        for (int c = 0; c < n; c++) {
            SlotCubeta sc = snapshot.get(c);

            String v1 = sc.getFila1() == null ? "" : sc.getFila1();
            String v2 = sc.getFila2() == null ? "" : sc.getFila2();

            fila1.set(c + 1, v1);
            fila2.set(c + 1, v2);
        }

        tabla.refresh();

        if (doLabel != null) {
            double doVal = estructura.densidadOcupacional();
            doLabel.setText(String.format("DO: %.2f%% (%d/%d) | 75/25",
                    doVal * 100.0,
                    estructura.totalOcupados(),
                    estructura.getN() * HashExpParciales.FILAS));
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
        nInicial = n;

        estructura = new HashExpParciales(n);
        creada = true;

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        resultadoLabel.setText("Estructura creada 2x" + estructura.getN()
                + " | Expansiones parciales | Dígitos=" + digitos);

        limpiarInsercion();
        limpiarBusqueda();
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

        if (estructura.contiene(claveTxt)) {
            resultadoLabel.setText("Esa clave ya existe (en tabla o pendiente).");
            limpiarInsercion();
            return;
        }

        int nAntes = estructura.getN();
        int pendientesAntes = estructura.getPendientes().size();

        estructura.insertar(claveTxt);

        int nDespues = estructura.getN();
        int pendientesDespues = estructura.getPendientes().size();

        refrescarTabla();

        String msg = "Insertada " + claveTxt + " | h(k)=" + ((Integer.parseInt(claveTxt) % nDespues + nDespues) % nDespues);
        if (nDespues != nAntes) msg += " | EXPANDIÓ: 2x" + nAntes + " → 2x" + nDespues;
        if (pendientesDespues > pendientesAntes) msg += " | Colisión: quedó pendiente";
        resultadoLabel.setText(msg);

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

        estructura = new HashExpParciales(nInicial);

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
        fc.setTitle("Guardar expansiones parciales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exp Parciales (*.exp)", "*.exp"));
        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=EXP_PARCIALES"); bw.newLine();
            bw.write("N=" + estructura.getN()); bw.newLine();
            bw.write("DIGITOS=" + digitos); bw.newLine();
            bw.write("DATA"); bw.newLine();

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
        fc.setTitle("Cargar expansiones parciales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exp Parciales (*.exp)", "*.exp"));
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

            if (newN == null || newN < 2) {
                resultadoLabel.setText("Archivo inválido: N.");
                return;
            }
            if (newDig == null || newDig < 1) newDig = 2;

            digitos = newDig;
            digitosChoice.setValue(digitos);
            nField.setText(String.valueOf(newN));

            estructura = new HashExpParciales(newN);
            nInicial = newN;
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

    private void limpiarBusqueda() {
        claveBuscarField.clear();
        claveBuscarField.requestFocus();
    }

    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }
}
