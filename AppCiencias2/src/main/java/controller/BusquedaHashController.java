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
import utilities.SlotHash;

import java.util.ArrayList;
import java.util.List;

public class BusquedaHashController {

    @FXML private TextField nField;
    @FXML private ChoiceBox<Integer> digitosChoice;
    @FXML private TextField modField;

    @FXML private TableView<SlotHash> tabla;
    @FXML private TableColumn<SlotHash, Integer> colPos;
    @FXML private TableColumn<SlotHash, Integer> colHash;
    @FXML private TableColumn<SlotHash, String> colClave;

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;
    
    @FXML private ChoiceBox<String> hashChoice;
    @FXML private ChoiceBox<String> colisionChoice;
    @FXML private CheckBox resolverCheck;

    @FXML private TableColumn<SlotHash, String> colColisiones;

    @FXML private AnchorPane hashPane;
    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;

    private final ObservableList<SlotHash> data = FXCollections.observableArrayList();

    private boolean creada = false;
    private int digitos = 2;
    private int N = 0;
    private int MOD = 100;

    @FXML
    public void initialize() {
        // Inicializar menú
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        subMenuBusquedas.setVisible(false);
        subMenuBusquedas.setManaged(false);
        subMenuInternas.setVisible(false);
        subMenuInternas.setManaged(false);

        digitosChoice.setItems(FXCollections.observableArrayList(1,2,3,4,5,6));
        digitosChoice.setValue(2);

        digitosChoice.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) digitos = b;
        });

        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));

        tabla.setItems(data);
        
        hashChoice.setItems(FXCollections.observableArrayList(
        "MOD",
        "Cuadrada (Mid-Square)",
        "Truncamiento",
        "Plegamiento"
        ));
        hashChoice.setValue("MOD");

        colisionChoice.setItems(FXCollections.observableArrayList(
                "Lineal",
                "Cuadrática",
                "Doble Hash",
                "Arreglos anidados",
                "Listas enlazadas"
        ));
        colisionChoice.setValue("Lineal");
        
        colisionChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
        boolean esEncadenamiento = "Arreglos anidados".equals(newV) || "Listas enlazadas".equals(newV);
        

        if (esEncadenamiento) {
            resolverCheck.setSelected(true);
            resolverCheck.setDisable(true);
        } else {
            resolverCheck.setDisable(false);
        }
    });
        hashChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {

            if ("MOD".equals(newV)) {
                modField.setDisable(false);
            } else {
                modField.setDisable(true);
                modField.setText(""); // opcional: limpiar
            }
        });

        resolverCheck.setSelected(true);

        colColisiones.setCellValueFactory(new PropertyValueFactory<>("colisionesTexto"));
    }

    @FXML
    private void crearEstructura() {
        Integer n = leerEntero(nField.getText());

        if (n == null || n <= 0) {
            resultadoLabel.setText("N inválido (debe ser > 0).");
            return;
        }

        N = n;
        digitos = digitosChoice.getValue() == null ? 2 : digitosChoice.getValue();

        // ✅ Solo validar/asignar MOD si la función es "MOD"
        if ("MOD".equals(hashChoice.getValue())) {
            Integer m = leerEntero(modField.getText());
            if (m == null || m <= 0) {
                resultadoLabel.setText("MOD inválido (debe ser > 0).");
                return;
            }
            MOD = m; // solo aquí
        }
        // else: no se usa MOD, entonces no lo leas ni lo asignes

        data.clear();
        for (int i = 0; i < N; i++) {
            data.add(new SlotHash(i + 1)); // posición 1..N
        }

        creada = true;

        if ("MOD".equals(hashChoice.getValue())) {
            resultadoLabel.setText("Tabla creada (1.." + N + "), MOD=" + MOD + ".");
        } else {
            resultadoLabel.setText("Tabla creada (1.." + N + "), Hash=" + hashChoice.getValue() + ".");
        }
    }

    private int hashBase(String claveTxt) {
    int claveNum = Integer.parseInt(claveTxt);

    String tipo = hashChoice.getValue();
    int h;

    switch (tipo) {
        case "Cuadrada (Mid-Square)" -> {
            long k = claveNum;
            long sq = k * k;
            String s = Long.toString(sq);
            int mid = s.length() / 2;
            int start = Math.max(0, mid - 2);
            int end = Math.min(s.length(), start + 4);
            int midVal = Integer.parseInt(s.substring(start, end));
            h = midVal;
        }
        case "Truncamiento" -> {
            // últimos 2 dígitos (puedes cambiar a 3 si quieres)
            String s = claveTxt;
            String sub = (s.length() > 2) ? s.substring(s.length() - 2) : s;
            h = Integer.parseInt(sub);
        }
        case "Plegamiento" -> {
            // bloques de 2 dígitos sumados
            int sum = 0;
            for (int i = 0; i < claveTxt.length(); i += 2) {
                int end = Math.min(claveTxt.length(), i + 2);
                sum += Integer.parseInt(claveTxt.substring(i, end));
            }
            h = sum;
        }
        default -> { // MOD
            h = claveNum % MOD;
        }
    }

    if (h < 0) h = -h;
    return h % N; // 0..N-1
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

        // No repetición (revisar en toda la tabla)
        for (SlotHash s : data) {
            if (claveTxt.equals(s.getClave())) {
                resultadoLabel.setText("Esa clave ya existe en la tabla.");
                limpiarInsercion();
                return;
            }
        }

        int claveNum = Integer.parseInt(claveTxt);
        int start = hashBase(claveTxt);

        boolean resolver = resolverCheck.isSelected();
        String estrategia = colisionChoice.getValue();

        int comparaciones = 0;
        long inicio = System.nanoTime();

        SlotHash base = data.get(start);
        comparaciones++;

        if (base.isVacio()) {
            base.setClave(claveTxt);
            tabla.refresh();

            long fin = System.nanoTime();
            resultadoLabel.setText("Insertada en posición " + base.getPosicion()
                    + " | Hash(base): " + start
                    + " | Tiempo: " + (fin - inicio) + " ns");

            claveInsertField.clear();
            claveInsertField.requestFocus();
            return;
        }

        // Si es encadenamiento, SIEMPRE se resuelve
        if ("Arreglos anidados".equals(estrategia) || "Listas enlazadas".equals(estrategia)) {
            resolver = true;
            resolverCheck.setSelected(true); // opcional: para que se vea reflejado
        }

        //Hay colisión
        if (!resolver) {
            // Guardar como "no resuelta" y evitar duplicados visuales
            String marca = "⚠ NO RESUELTA: " + claveTxt;

            // Evitar repetir la misma clave marcada
            boolean yaExiste = base.getColisiones().stream()
                    .anyMatch(s -> s.endsWith(claveTxt) || s.equals(marca) || s.equals(claveTxt));

            if (!yaExiste) base.getColisiones().add(marca);

            tabla.refresh();

            long fin = System.nanoTime();
            resultadoLabel.setText("COLISIÓN en posición " + base.getPosicion()
                    + " | (no resuelta) | Tiempo: " + (fin - inicio) + " ns");

            limpiarInsercion();
            return;
        }

        // Encadenamiento (Arreglos anidados / Listas enlazadas)
        if ("Arreglos anidados".equals(estrategia) || "Listas enlazadas".equals(estrategia)) {
            // Evitar duplicados reales en el bucket
            if (base.getColisiones().contains(claveTxt)) {
                resultadoLabel.setText("Esa clave ya existe en el encadenamiento de la posición " + base.getPosicion() + ".");
                limpiarInsercion();
                return;
            }

            base.getColisiones().add(claveTxt);
            tabla.refresh();

            long fin = System.nanoTime();
            resultadoLabel.setText("Insertada por encadenamiento en posición " + base.getPosicion()
                    + " | Tiempo: " + (fin - inicio) + " ns");

            limpiarInsercion();
            return;
        }

        // Probing: Lineal / Cuadrática / Doble hash
        for (int i = 1; i < N; i++) {
            int idx;

            if ("Cuadrática".equals(estrategia)) {
                idx = (start + i * i) % N;
            } else if ("Doble Hash".equals(estrategia)) {
                int k = Integer.parseInt(claveTxt);
                int h2 = 1 + (k % Math.max(1, (N - 1)));
                idx = (start + i * h2) % N;
            } else { // Lineal
                idx = (start + i) % N;
            }

            SlotHash slot = data.get(idx);
            comparaciones++;

            if (slot.isVacio()) {
                slot.setClave(claveTxt);
                tabla.refresh();

                long fin = System.nanoTime();
                resultadoLabel.setText("Insertada en posición " + slot.getPosicion()
                + " (índice " + idx + ")"
                + " | Hash base: " + start
                + " | Colisiones: " + i
                + " | Comparaciones: " + comparaciones
                + " | Tiempo: " + (fin - inicio) + " ns");

                claveInsertField.clear();
                claveInsertField.requestFocus();
                return;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("Tabla llena (sin espacio) | Comparaciones: " + comparaciones + " | Tiempo: " + (fin - inicio) + " ns");

    }

    @FXML
private void buscarClave() {
    if (!creada) {
        resultadoLabel.setText("Primero debes crear la estructura.");
        return;
    }

    String claveTxt = normalizarClave(claveBuscarField.getText(), digitos);
    claveBuscarField.setText(claveTxt);

    if (!claveValidaPorDigitos(claveTxt, digitos)) {
        resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos. ");
        limpiarBusqueda();
        return;
    }

    int start = hashBase(claveTxt);
    String estrategia = colisionChoice.getValue();

    int comparaciones = 0;
    long inicio = System.nanoTime();

    // 1) Revisar el slot base
    SlotHash base = data.get(start);
    comparaciones++;

    // Si la clave está en el slot base
    // 2) Buscar también dentro del registro de colisiones (incluye NO RESUELTA y encadenamiento)
if (base.getColisiones() != null && !base.getColisiones().isEmpty()) {

    boolean encontradaEnBucket = base.getColisiones().stream().anyMatch(s ->
            s.equals(claveTxt) || s.endsWith(": " + claveTxt)
    );

    if (encontradaEnBucket) {
        long fin = System.nanoTime();
        tabla.getSelectionModel().select(base);
        tabla.scrollTo(base);
        resultadoLabel.setText("Encontrada en colisiones de la posición " + base.getPosicion()
                + " | Hash: " + start
                + " | Método: " + estrategia
                + " | Comparaciones: " + comparaciones
                + " | Tiempo: " + (fin - inicio) + " ns");

        limpiarBusqueda();
        return;
    }
}

    // 2) Si hay encadenamiento, buscar dentro del bucket (colisiones)
    if ("Arreglos anidados".equals(estrategia) || "Listas enlazadas".equals(estrategia)) {
        if (base.getColisiones().contains(claveTxt)) {
            long fin = System.nanoTime();
            tabla.getSelectionModel().select(base);
            tabla.scrollTo(base);
            resultadoLabel.setText("Encontrada en colisiones de la posición " + base.getPosicion()
                    + " | Hash: " + start
                    + " | Comparaciones: " + comparaciones
                    + " | Tiempo: " + (fin - inicio) + " ns");
            limpiarBusqueda();
            return;
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Hash: " + start
                + " | Comparaciones: " + comparaciones
                + " | Tiempo: " + (fin - inicio) + " ns");
        limpiarBusqueda();
        return;
    }

    // 3) Probing: Lineal / Cuadrática / Doble hash
    for (int i = 1; i < N; i++) {
        int idx;

        if ("Cuadrática".equals(estrategia)) {
            idx = (start + i * i) % N;
        } else if ("Doble Hash".equals(estrategia)) {
            int k = Integer.parseInt(claveTxt);
            int h2 = 1 + (k % Math.max(1, (N - 1)));
            idx = (start + i * h2) % N;
        } else { // Lineal
            idx = (start + i) % N;
        }

        SlotHash slot = data.get(idx);
        comparaciones++;

        if (claveTxt.equals(slot.getClave())) {
            long fin = System.nanoTime();
            tabla.getSelectionModel().select(slot);
            tabla.scrollTo(slot);

            resultadoLabel.setText("Encontrada en posición " + slot.getPosicion()
                    + " | Hash: " + start
                    + " | Comparaciones: " + comparaciones
                    + " | Tiempo: " + (fin - inicio) + " ns");
            limpiarBusqueda();
            return;
        }

        if (slot.isVacio()) {
            long fin = System.nanoTime();
            resultadoLabel.setText("No encontrada | Hash: " + start
                    + " | Comparaciones: " + comparaciones
                    + " | Tiempo: " + (fin - inicio) + " ns");
            limpiarBusqueda();
            return;
        }
    }

    long fin = System.nanoTime();
    resultadoLabel.setText("No encontrada | Hash: " + start
            + " | Comparaciones: " + comparaciones
            + " | Tiempo: " + (fin - inicio) + " ns");
    limpiarBusqueda();
    }

    @FXML
        private void arreglarColisiones() {
            if (!creada) {
                resultadoLabel.setText("Primero debes crear la estructura.");
                return;
            }

            String estrategia = colisionChoice.getValue();

            // Si es encadenamiento, realmente "arreglar" no aplica porque ya queda resuelto en la misma celda.
            // Pero igual podemos "limpiar" etiquetas NO RESUELTA si quedaron de antes.
            boolean esEncadenamiento = "Arreglos anidados".equals(estrategia) || "Listas enlazadas".equals(estrategia);

            int movidas = 0;
            int noSePudo = 0;
            int revisadas = 0;

            long inicio = System.nanoTime();

            // Recorremos toda la tabla
            for (int baseIdx = 0; baseIdx < N; baseIdx++) {
                SlotHash base = data.get(baseIdx);

                if (base.getColisiones() == null || base.getColisiones().isEmpty()) continue;

                // Copia segura, porque vamos a quitar elementos mientras iteramos
                List<String> pendientes = new ArrayList<>(base.getColisiones());

                for (String item : pendientes) {
                    String claveReal = extraerClaveReal(item);

                    // Solo arreglamos las que estaban marcadas como NO RESUELTA (o sea: pendientes)
                    if (!esNoResuelta(item)) continue;

                    revisadas++;

                    // Si es encadenamiento, la "resolución" es dejarla en el bucket sin etiqueta
                    if (esEncadenamiento) {
                        base.getColisiones().remove(item);
                        if (!base.getColisiones().contains(claveReal)) {
                            base.getColisiones().add(claveReal);
                        }
                        movidas++;
                        continue;
                    }

                    // Para probing (Lineal / Cuadrática / Doble Hash): intentamos reubicar en una celda vacía
                    boolean ok = reubicarPorProbing(claveReal, estrategia);

                    if (ok) {
                        base.getColisiones().remove(item); // quitar el "NO RESUELTA"
                        movidas++;
                    } else {
                        noSePudo++;
                    }
                }
            }

            tabla.refresh();
            long fin = System.nanoTime();

            resultadoLabel.setText("Arreglo terminado | Pendientes revisadas: " + revisadas
                    + " | Resueltas: " + movidas
                    + " | No se pudo: " + noSePudo
                    + " | Tiempo: " + (fin - inicio) + " ns");
        }

        /** Devuelve true si el texto guardado corresponde a una colisión marcada como no resuelta. */
        private boolean esNoResuelta(String s) {
            return s != null && s.startsWith("⚠ NO RESUELTA:");
        }

        /** Si viene "⚠ NO RESUELTA: 4234" devuelve "4234". Si viene "4234" devuelve "4234". */
        private String extraerClaveReal(String s) {
            if (s == null) return "";
            s = s.trim();
            String pref = "⚠ NO RESUELTA:";
            if (s.startsWith(pref)) return s.substring(pref.length()).trim();
            return s;
        }

        /**
         * Intenta insertar la clave en una posición vacía usando el probing de la estrategia actual.
         * Retorna true si la pudo ubicar, false si la tabla está llena o no encontró hueco.
         */
        private boolean reubicarPorProbing(String claveTxt, String estrategia) {
            int start = hashBase(claveTxt);

            // Si el slot base está vacío, va directo
            SlotHash base = data.get(start);
            if (base.isVacio()) {
                base.setClave(claveTxt);
                return true;
            }

            // Probing
            for (int i = 1; i < N; i++) {
                int idx;

                if ("Cuadrática".equals(estrategia)) {
                    idx = (start + i * i) % N;
                } else if ("Doble Hash".equals(estrategia)) {
                    int k = Integer.parseInt(claveTxt);
                    int h2 = 1 + (k % Math.max(1, (N - 1)));
                    idx = (start + i * h2) % N;
                } else { // Lineal
                    idx = (start + i) % N;
                }

                SlotHash slot = data.get(idx);
                if (slot.isVacio()) {
                    slot.setClave(claveTxt);
                    return true;
                }
            }
            return false;
        }

    private void limpiarBusqueda() {
        claveBuscarField.clear();
        claveBuscarField.requestFocus();
    }
    
    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
    }

    // ===== Helpers de validación / normalización =====

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

            hashPane.getChildren().clear();
            hashPane.getChildren().add(panel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
