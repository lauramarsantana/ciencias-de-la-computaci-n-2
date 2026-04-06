package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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

    @FXML private AnchorPane expPane;
    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;

    @FXML private TextField nField;                 
    @FXML private ChoiceBox<Integer> digitosChoice; // 1..4
    @FXML private ChoiceBox<Integer> filasChoice;

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
    private int filas = 2;

    private HashExpTotales estructura; // nuestro “modelo”

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

        filasChoice.getItems().addAll(2, 3, 4, 5, 6);
        filasChoice.setValue(2);

        tabla.setItems(dataUI);
    }

    // =========================
    // Menú (copiado de tu estilo)
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
            double doVal = estructura.densidadOcupacional();
            doLabel.setText(String.format("DO: %.2f%% (%d/%d) | 75/25",
                    doVal * 100.0,
                    estructura.totalOcupados(),
                    estructura.getN() * estructura.getFilas()));
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

        estructura = new HashExpTotales(n, filas);
        creada = true;

        construirTablaInvertida(estructura.getN());
        refrescarTabla();

        resultadoLabel.setText("Estructura creada " + filas + "x" + estructura.getN()
                + " | Umbrales: expandir 75%, reducir 25% | Dígitos=" + digitos);

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

        String msg = "Insertada " + claveTxt + " | h(k)=" + (Integer.parseInt(claveTxt) % nDespues + nDespues) % nDespues;
        if (nDespues != nAntes) msg += " | EXPANDIÓ: " + filas + "x" + nAntes + " → " + filas + "x" + nDespues;
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
        if (nDespues != nAntes) msg += " | REDUJO: " + filas + "x" + nAntes + " → " + filas + "x" + nDespues;
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

        estructura = new HashExpTotales(nInicial, filas);

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
            bw.write("FILAS=" + filas); bw.newLine();

            // Guardar desde la estructura real
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
        fc.setTitle("Cargar expansiones totales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exp Totales (*.ext)", "*.ext"));
        File file = fc.showOpenDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            Integer newN = null;
            Integer newDig = null;
            Integer newFilas = null;

            // Leer cabecera hasta DATA
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("DATA")) break;
                else if (line.startsWith("FILAS=")) {
                    newFilas = Integer.parseInt(line.substring(6).trim());
                }
                else if (line.startsWith("N=")) {
                    newN = Integer.parseInt(line.substring(2).trim());
                }
                else if (line.startsWith("DIGITOS=")) {
                    newDig = Integer.parseInt(line.substring(8).trim());
                }
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

            estructura = new HashExpTotales(newN, filas);
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

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
    }

    // =======
    // Helpers 
    // =======

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