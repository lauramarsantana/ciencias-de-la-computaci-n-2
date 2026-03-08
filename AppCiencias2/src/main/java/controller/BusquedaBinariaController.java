package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;

    @FXML private AnchorPane binarioPane;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private VBox subMenuBusquedas;
    @FXML
    private VBox subMenuInternas;

    private final ObservableList<SlotClave> data = FXCollections.observableArrayList();
    private boolean creada = false;
    private int digitos = 2;

    @FXML
    public void initialize() {
        // Inicializar menú
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);

        digitosChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
        digitosChoice.setValue(2);

        digitosChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) digitos = newV;
        });

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));
        tabla.setItems(data);
        tabla.getColumns().setAll(colPos, colClave);
        tabla.getColumns().setAll(colPos, colClave);
        colPos.prefWidthProperty().bind(tabla.widthProperty().multiply(0.3));
        colClave.prefWidthProperty().bind(tabla.widthProperty().multiply(0.7));
    }

    @FXML
    private void openInternas(javafx.scene.input.MouseEvent event){
        System.out.println("Abriedno busquedasInternas.fxml");
        loadPanel("busquedasInternas.fxml");
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
    }

    // Inserta y mantiene ORDEN ASCENDENTE
    @FXML
    private void insertarClaveOrdenada() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarInsercion();
            return;
        }

        String claveTxt = normalizarClave(claveInsertField.getText(), digitos);
        claveInsertField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
            limpiarInsercion();
            return;
        }

        // No repetición
        for (SlotClave s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la estructura.");
                limpiarInsercion();
                return;
            }
        }

        // ¿Hay espacio?
        int usados = contarClaves();
        if (usados >= n) {
            resultadoLabel.setText("La estructura está llena. No se puede insertar más.");
            limpiarInsercion();
            return;
        }

        /* debido a que ya no contamos las filas vacias esto ya no funciona
        // 1) obtener todas las claves existentes + la nueva, ordenarlas
        var clavesOrdenadas = data.stream()
                .map(SlotClave::getClave)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        var lista = new java.util.ArrayList<String>(clavesOrdenadas);
        lista.add(claveTxt);
        lista.sort(String::compareTo); // orden lexicográfico funciona por ceros a la izquierda

        // 2) limpiar y reinsertar desde posición 1
        for (SlotClave s : data) s.setClave("");
        for (int i = 0; i < lista.size(); i++) data.get(i).setClave(lista.get(i));
        */

        // Insertar nueva clave
        data.add(new SlotClave(usados + 1, claveTxt));

        // Ordenar por clave
        var listaOrdenada = data.stream()
                .sorted((a, b) -> a.getClave().compareTo(b.getClave()))
                .toList();

        // Reconstruir la tabla con posiciones actualizadas
        data.clear();
        for (int i = 0; i < listaOrdenada.size(); i++) {
            data.add(new SlotClave(i + 1, listaOrdenada.get(i).getClave()));
        }

        tabla.refresh();
        resultadoLabel.setText("Insertada y ordenada. Total claves: " + listaOrdenada.size() + ".");
        // limpiar solo si fue exitoso
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }

    @FXML
    private void buscarClaveBinaria() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            limpiarBusqueda();
            return;
        }

        String claveTxt = normalizarClave(claveBuscarField.getText(), digitos);
        claveBuscarField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
            limpiarBusqueda();
            return;
        }

        // Tomamos solo el segmento lleno (0..usados-1) porque el resto está vacío
        int usados = contarClaves();
        if (usados == 0) {
            resultadoLabel.setText("No hay claves insertadas.");
            limpiarBusqueda();
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

                limpiarBusqueda();
                return;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Comparaciones: " + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");
        limpiarBusqueda();
    }
    
    @FXML
    private void limpiarEstructura() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        data.clear();
        tabla.getSelectionModel().clearSelection();

        claveInsertField.clear();
        claveBuscarField.clear();
        claveInsertField.requestFocus();

        resultadoLabel.setText("La tabla fue limpiada.");
    }

        // =====================
    // GUARDAR / CARGAR / ELIMINAR (BINARIA)
    // =====================

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

            // Guardar en el orden actual (ya está ordenado)
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

            // Leer cabecera hasta DATA
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

            // Aplicar config
            this.n = newN;
            this.digitos = newDig;
            this.creada = true;

            nField.setText(String.valueOf(n));
            digitosChoice.setValue(digitos);

            // Leer todas las claves (sin importar posiciones guardadas)
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

            // Quitar duplicadas por seguridad (binaria no debe tener repetidas)
            claves = claves.stream().distinct().toList();

            // No exceder N
            if (claves.size() > n) {
                claves = claves.subList(0, n);
            }

            // Ordenar y reconstruir data con posiciones 1..k
            claves = new ArrayList<>(claves);
            claves.sort(String::compareTo);

            data.clear();
            for (int i = 0; i < claves.size(); i++) {
                data.add(new SlotClave(i + 1, claves.get(i)));
            }

            tabla.refresh();
            resultadoLabel.setText("Cargado: " + file.getName() + " | Total claves: " + data.size());

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

        // 1) Si el usuario escribió una clave, SIEMPRE eliminar por esa clave
        String input = claveBuscarField.getText() == null ? "" : claveBuscarField.getText().trim();
        if (!input.isEmpty()) {
            String claveTxt = normalizarClave(input, digitos);
            claveBuscarField.setText(claveTxt);

            if (!claveValidaPorDigitos(claveTxt, digitos)) {
                resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
                limpiarBusqueda();
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

            limpiarBusqueda();
            return;
        }

        // 2) Si el campo está vacío, eliminar lo seleccionado (si hay selección)
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
        // Después de eliminar, mantener posiciones 1..k (binaria depende de orden + segmento lleno)
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setPosicion(i + 1);
        }
    }

    // Helpers
    private void limpiarBusqueda() {
        this.claveBuscarField.clear();
        this.claveBuscarField.requestFocus();
    }
    
    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
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
        try { return Integer.parseInt(txt); } catch (NumberFormatException e) { return null; }
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

    // configurando cada boton del menu desplegable
    @FXML
    private void openMenu(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo menu...");
        menuPane.setVisible(true); // para que el panel del menu se vea
        menuPane.setManaged(true);// posiciona de primeras al panel
    }
    @FXML
    private void closeMenu(javafx.scene.input.MouseEvent event){
        System.out.println("cerrando menu...");
        menuPane.setVisible(false);// para que el panel del menu se oculte
        menuPane.setManaged(false);// lo quita de la primera capa, para liberar el espacio
    }

    @FXML
    private void openMenuBusquedas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo submenu de busquedas...");
        boolean isVisible = subMenuBusquedas.isVisible();
        subMenuBusquedas.setVisible(!isVisible);
        subMenuBusquedas.setManaged(!isVisible);
    }
    @FXML
    private void openMenuInternas(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo submenu de busquedas internas...");
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);
    }
    @FXML
    private void mostrarBusquedaLineal(javafx.scene.input.MouseEvent event) {
        System.out.println("Abriendo busquedaLineal.fxml");
        loadPanel("busquedaLineal.fxml");
    }
    @FXML
    private void openBinario(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaBinaria.fxml");
        loadPanel("busquedaBinaria.fxml");
    }
    @FXML
    private void openFuncionHash(javafx.scene.input.MouseEvent event){
        System.out.println("abriendo busquedaHash.fxml");
        loadPanel("busquedaHash.fxml");
    }
    @FXML
    private void openGrafos(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo grafos.fxml...");
        loadPanel("grafos.fxml");
    }

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event){
        System.out.println("Abriendo inicio.fxml...");
        loadPanel("inicio.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            binarioPane.getChildren().clear();
            binarioPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

