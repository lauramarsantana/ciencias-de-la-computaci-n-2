package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import utilities.EntradaIndice;
import utilities.RegistroDato;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusquedaIndiceSecundarioController {

    private Integer n;
    private int digitos = 2;
    private int registrosPorBloque = 3;
    private boolean creada = false;

    @FXML private TextField nField;
    @FXML private TextField bloqueField;
    @FXML private ChoiceBox<Integer> digitosChoice;

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;

    @FXML private Label resultadoLabel;
    @FXML private TextArea recorridoArea;

    // Tabla de datos
    @FXML private TableView<RegistroDato> tablaDatos;
    @FXML private TableColumn<RegistroDato, Integer> colPosDato;
    @FXML private TableColumn<RegistroDato, String> colClaveDato;
    @FXML private TableColumn<RegistroDato, Integer> colBloqueDato;

    // Tabla índice secundario
    @FXML private TableView<EntradaIndice> tablaIndice;
    @FXML private TableColumn<EntradaIndice, String> colClaveIndice;
    @FXML private TableColumn<EntradaIndice, Integer> colBloqueIndice;
    @FXML private TableColumn<EntradaIndice, Integer> colPosIndice;

    private final ObservableList<RegistroDato> datos = FXCollections.observableArrayList();
    private final ObservableList<EntradaIndice> indiceSecundario = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        digitosChoice.getItems().addAll(1, 2, 3, 4);
        digitosChoice.setValue(2);

        colPosDato.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClaveDato.setCellValueFactory(new PropertyValueFactory<>("clave"));
        colBloqueDato.setCellValueFactory(new PropertyValueFactory<>("bloque"));
        tablaDatos.setItems(datos);

        colClaveIndice.setCellValueFactory(new PropertyValueFactory<>("clave"));
        colBloqueIndice.setCellValueFactory(new PropertyValueFactory<>("bloqueDestino"));
        colPosIndice.setCellValueFactory(new PropertyValueFactory<>("posicionDestino"));
        tablaIndice.setItems(indiceSecundario);
    }

    @FXML
    private void crearEstructura() {
        this.n = leerEntero(nField);

        if (this.n == null || this.n < 1) {
            resultadoLabel.setText("N debe ser mayor o igual a 1.");
            return;
        }

        Integer bloqueLeido = leerEntero(bloqueField);
        if (bloqueLeido == null || bloqueLeido < 1) {
            resultadoLabel.setText("Registros por bloque debe ser mayor o igual a 1.");
            return;
        }

        registrosPorBloque = bloqueLeido;
        digitos = digitosChoice.getValue() != null ? digitosChoice.getValue() : 2;

        datos.clear();
        indiceSecundario.clear();
        recorridoArea.clear();

        creada = true;
        resultadoLabel.setText("Estructura creada correctamente.");
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

        for (RegistroDato r : datos) {
            if (claveTxt.equals(r.getClave())) {
                resultadoLabel.setText("La clave ya existe.");
                limpiarInsercion();
                return;
            }
        }

        if (datos.size() >= n) {
            resultadoLabel.setText("La estructura está llena.");
            limpiarInsercion();
            return;
        }

        datos.add(new RegistroDato(0, claveTxt, 0));
        reconstruirDatosEIndice();

        resultadoLabel.setText("Clave insertada correctamente.");
        limpiarInsercion();
    }

    @FXML
    private void buscarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarBusqueda();
            return;
        }

        if (datos.isEmpty()) {
            resultadoLabel.setText("No hay datos en la estructura.");
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

        recorridoArea.clear();
        recorridoArea.appendText("=== BÚSQUEDA POR ÍNDICE SECUNDARIO ===\n");
        recorridoArea.appendText("Clave buscada: " + claveTxt + "\n\n");

        int accesosIndice = 0;
        int accesosDatos = 0;

        EntradaIndice encontrada = null;

        for (EntradaIndice e : indiceSecundario) {
            accesosIndice++;
            recorridoArea.appendText("Revisando entrada índice: clave " + e.getClave()
                    + " -> bloque " + e.getBloqueDestino()
                    + ", posición " + e.getPosicionDestino() + "\n");

            if (claveTxt.equals(e.getClave())) {
                encontrada = e;
                break;
            }
        }

        if (encontrada == null) {
            resultadoLabel.setText("No encontrada | Accesos índice: " + accesosIndice);
            recorridoArea.appendText("\nLa clave no está en el índice secundario.\n");
            limpiarBusqueda();
            return;
        }

        recorridoArea.appendText("\nEntrada encontrada en índice.\n");
        recorridoArea.appendText("Ir al bloque " + encontrada.getBloqueDestino()
                + ", posición " + encontrada.getPosicionDestino() + "\n");

        for (RegistroDato r : datos) {
            if (r.getPosicion() == encontrada.getPosicionDestino()) {
                accesosDatos++;
                tablaDatos.getSelectionModel().select(r);
                tablaDatos.scrollTo(r);

                resultadoLabel.setText("Encontrada en posición " + r.getPosicion()
                        + " | Bloque " + r.getBloque()
                        + " | Accesos índice: " + accesosIndice
                        + " | Accesos datos: " + accesosDatos
                        + " | Total accesos: " + (accesosIndice + accesosDatos));

                recorridoArea.appendText("Registro encontrado: clave " + r.getClave() + "\n");
                limpiarBusqueda();
                return;
            }
        }

        resultadoLabel.setText("No encontrada.");
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
            resultadoLabel.setText("Escribe la clave a eliminar en el campo de búsqueda.");
            return;
        }

        String claveTxt = normalizarClave(input, digitos);
        claveBuscarField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarBusqueda();
            return;
        }

        boolean removed = datos.removeIf(r -> claveTxt.equals(r.getClave()));
        if (!removed) {
            resultadoLabel.setText("No se encontró la clave para eliminar.");
            limpiarBusqueda();
            return;
        }

        reconstruirDatosEIndice();
        resultadoLabel.setText("Clave eliminada correctamente.");
        limpiarBusqueda();
    }

    @FXML
    private void limpiarEstructura() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        datos.clear();
        indiceSecundario.clear();
        tablaDatos.getSelectionModel().clearSelection();
        tablaIndice.getSelectionModel().clearSelection();
        recorridoArea.clear();

        claveInsertField.clear();
        claveBuscarField.clear();

        resultadoLabel.setText("La estructura fue limpiada.");
    }

    @FXML
    private void guardarEstructura() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar índice secundario");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Índice Secundario (*.isr)", "*.isr")
        );

        File file = fc.showSaveDialog(tablaDatos.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=INDICE_SECUNDARIO");
            bw.newLine();
            bw.write("N=" + n);
            bw.newLine();
            bw.write("DIGITOS=" + digitos);
            bw.newLine();
            bw.write("REGISTROS_POR_BLOQUE=" + registrosPorBloque);
            bw.newLine();
            bw.write("DATOS");
            bw.newLine();

            for (RegistroDato r : datos) {
                bw.write(r.getClave());
                bw.newLine();
            }

            bw.write("END");
            bw.newLine();

            resultadoLabel.setText("Estructura guardada: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error guardando: " + e.getMessage());
        }
    }

    @FXML
    private void cargarEstructura() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar índice secundario");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Índice Secundario (*.isr)", "*.isr")
        );

        File file = fc.showOpenDialog(tablaDatos.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            Integer nuevoN = null;
            Integer nuevosDigitos = null;
            Integer nuevoRegistrosPorBloque = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("DATOS")) {
                    break;
                }

                if (line.startsWith("N=")) {
                    nuevoN = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("DIGITOS=")) {
                    nuevosDigitos = Integer.parseInt(line.substring(8).trim());
                } else if (line.startsWith("REGISTROS_POR_BLOQUE=")) {
                    nuevoRegistrosPorBloque = Integer.parseInt(
                            line.substring("REGISTROS_POR_BLOQUE=".length()).trim()
                    );
                }
            }

            if (nuevoN == null || nuevoN < 1) {
                resultadoLabel.setText("Archivo inválido: N.");
                return;
            }

            if (nuevosDigitos == null || nuevosDigitos < 1) {
                nuevosDigitos = 2;
            }

            if (nuevoRegistrosPorBloque == null || nuevoRegistrosPorBloque < 1) {
                resultadoLabel.setText("Archivo inválido: registros por bloque.");
                return;
            }

            List<String> claves = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("END")) {
                    break;
                }

                if (line.isEmpty()) continue;

                String clave = normalizarClave(line, nuevosDigitos);
                if (claveValidaPorDigitos(clave, nuevosDigitos)) {
                    claves.add(clave);
                }
            }

            claves.sort(String::compareTo);

            if (claves.size() > nuevoN) {
                claves = claves.subList(0, nuevoN);
            }

            this.n = nuevoN;
            this.digitos = nuevosDigitos;
            this.registrosPorBloque = nuevoRegistrosPorBloque;
            this.creada = true;

            nField.setText(String.valueOf(n));
            bloqueField.setText(String.valueOf(registrosPorBloque));
            digitosChoice.setValue(digitos);

            datos.clear();
            indiceSecundario.clear();
            recorridoArea.clear();

            for (int i = 0; i < claves.size(); i++) {
                int posicion = i + 1;
                int bloque = (i / registrosPorBloque) + 1;
                datos.add(new RegistroDato(posicion, claves.get(i), bloque));
            }

            construirIndiceSecundario();

            tablaDatos.refresh();
            tablaIndice.refresh();

            resultadoLabel.setText("Estructura cargada: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
    }

    private void reconstruirDatosEIndice() {
        List<String> clavesOrdenadas = datos.stream()
                .map(RegistroDato::getClave)
                .sorted()
                .toList();

        datos.clear();

        for (int i = 0; i < clavesOrdenadas.size(); i++) {
            int posicion = i + 1;
            int bloque = (i / registrosPorBloque) + 1;
            datos.add(new RegistroDato(posicion, clavesOrdenadas.get(i), bloque));
        }

        construirIndiceSecundario();

        tablaDatos.refresh();
        tablaIndice.refresh();
    }

    private void construirIndiceSecundario() {
        indiceSecundario.clear();

        for (RegistroDato r : datos) {
            indiceSecundario.add(new EntradaIndice(
                    r.getClave(),
                    r.getBloque(),
                    r.getPosicion()
            ));
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
