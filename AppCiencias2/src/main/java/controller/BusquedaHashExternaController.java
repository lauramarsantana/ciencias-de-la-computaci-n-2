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
import javafx.stage.FileChooser;
import utilities.BloqueHash;
import utilities.SlotHashExterno;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BusquedaHashExternaController {

    @FXML private AnchorPane hashExternoPane;

    @FXML private TextField nField;
    @FXML private TextField tamBloqueField;
    @FXML private ChoiceBox<Integer> digitosChoice;
    @FXML private TextField modField;

    @FXML private ChoiceBox<String> hashChoice;
    @FXML private ChoiceBox<String> colisionChoice;
    @FXML private CheckBox resolverCheck;

    @FXML private TableView<SlotHashExterno> tabla;
    @FXML private TableColumn<SlotHashExterno, Integer> colBloque;
    @FXML private TableColumn<SlotHashExterno, Integer> colPos;
    @FXML private TableColumn<SlotHashExterno, Integer> colHash;
    @FXML private TableColumn<SlotHashExterno, String> colClave;
    @FXML private TableColumn<SlotHashExterno, String> colColisiones;

    @FXML private TextField claveInsertField;
    @FXML private TextField claveBuscarField;
    @FXML private Label resultadoLabel;

    @FXML private AnchorPane menuPane;
    @FXML private VBox subMenuBusquedas;
    @FXML private VBox subMenuInternas;

    private final ObservableList<SlotHashExterno> dataTabla = FXCollections.observableArrayList();
    private final List<BloqueHash> bloques = new ArrayList<>();

    private boolean creada = false;
    private int digitos = 2;
    private int N = 0;
    private int tamBloque = 1;
    private int MOD = 100;
    private int cantidadBloques = 0;

    @FXML
    public void initialize() {
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

        hashChoice.setItems(FXCollections.observableArrayList(
                "MOD",
                "Cuadrada (Mid-Square)",
                "Truncamiento",
                "Plegamiento"
        ));
        hashChoice.setValue("MOD");

        colisionChoice.setItems(FXCollections.observableArrayList(
                "Lineal entre bloques",
                "Cuadrática entre bloques",
                "Doble Hash entre bloques",
                "Encadenamiento de bloques"
        ));
        colisionChoice.setValue("Lineal entre bloques");

        hashChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if ("MOD".equals(newV)) {
                modField.setDisable(false);
            } else {
                modField.setDisable(true);
                modField.setText("");
            }
        });

        colisionChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean encadenamiento = "Encadenamiento de bloques".equals(newV);
            if (encadenamiento) {
                resolverCheck.setSelected(true);
                resolverCheck.setDisable(true);
            } else {
                resolverCheck.setDisable(false);
            }
        });

        resolverCheck.setSelected(true);

        colBloque.setCellValueFactory(new PropertyValueFactory<>("bloque"));
        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colHash.setCellValueFactory(new PropertyValueFactory<>("hash"));
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));
        colColisiones.setCellValueFactory(new PropertyValueFactory<>("colisionesTexto"));

        colColisiones.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.trim().startsWith("⚠")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        tabla.setItems(dataTabla);
    }

    @FXML
    private void crearEstructura() {
        Integer n = leerEntero(nField.getText());
        Integer tb = leerEntero(tamBloqueField.getText());

        if (n == null || n <= 0) {
            resultadoLabel.setText("N inválido (debe ser > 0).");
            return;
        }

        if (tb == null || tb <= 0) {
            resultadoLabel.setText("El tamaño de bloque debe ser mayor que 0.");
            return;
        }

        if (n % tb != 0) {
            resultadoLabel.setText("N debe ser múltiplo del tamaño de bloque.");
            return;
        }

        N = n;
        tamBloque = tb;
        cantidadBloques = N / tamBloque;
        digitos = digitosChoice.getValue() == null ? 2 : digitosChoice.getValue();

        if ("MOD".equals(hashChoice.getValue())) {
            Integer m = leerEntero(modField.getText());
            if (m == null || m <= 0) {
                resultadoLabel.setText("MOD inválido (debe ser > 0).");
                return;
            }
            MOD = m;
        }

        bloques.clear();
        dataTabla.clear();

        int posicionInicial = 1;
        for (int i = 0; i < cantidadBloques; i++) {
            BloqueHash bloque = new BloqueHash(i + 1, tamBloque, posicionInicial);
            bloques.add(bloque);
            posicionInicial += tamBloque;
        }

        creada = true;
        actualizarVista();

        resultadoLabel.setText("Estructura externa creada: " + cantidadBloques +
                " bloques de " + tamBloque + " posiciones.");
    }

    private int calcularHash(String claveTxt) {
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
            String s = claveTxt;
            String sub = (s.length() > 2) ? s.substring(s.length() - 2) : s;
            h = Integer.parseInt(sub);
        }
        case "Plegamiento" -> {
            int sum = 0;
            for (int i = 0; i < claveTxt.length(); i += 2) {
                int end = Math.min(claveTxt.length(), i + 2);
                sum += Integer.parseInt(claveTxt.substring(i, end));
            }
            h = sum;
        }
        default -> h = claveNum % MOD;
    }

    if (h < 0) h = -h;

    // el hash mostrado debe apuntar a una posición global de la estructura
    return h % N;   // 0..N-1
    }

    private int calcularIndiceBase(String claveTxt) {
        return calcularHash(claveTxt); // índice global 0..N-1
    }

    private int calcularBloqueBase(String claveTxt) {
        return calcularIndiceBase(claveTxt) / tamBloque; // 0..cantidadBloques-1
    }

private int calcularOffsetDentroBloque(String claveTxt) {
    return calcularIndiceBase(claveTxt) % tamBloque; // 0..tamBloque-1
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

        if (existeClaveEnEstructura(claveTxt)) {
            resultadoLabel.setText("Esa clave ya existe en la estructura.");
            limpiarInsercion();
            return;
        }

        int hash = calcularHash(claveTxt);              // ejemplo: 34
        int indiceBase = calcularIndiceBase(claveTxt); // ejemplo: 34
        int bloqueBaseIdx = indiceBase / tamBloque;    // ejemplo: 3
        int offsetBase = indiceBase % tamBloque;       // ejemplo: 4

        String estrategia = colisionChoice.getValue();
        boolean resolver = resolverCheck.isSelected();

        int comparaciones = 0;
        long inicio = System.nanoTime();

        BloqueHash bloqueBase = bloques.get(bloqueBaseIdx);
        SlotHashExterno slotBase = bloqueBase.getSlots().get(offsetBase);
        comparaciones++;

        // Caso normal: la posición base está libre
        if (slotBase.isVacio()) {
            slotBase.setClave(claveTxt);
            slotBase.setHash(hash);
            actualizarVista();

            long fin = System.nanoTime();
            resultadoLabel.setText("Insertada en bloque " + slotBase.getBloque() +
                    ", posición " + slotBase.getPosicion() +
                    " | Hash: " + hash +
                    " | Tiempo: " + (fin - inicio) + " ns");

            limpiarInsercion();
            return;
        }

        // Aquí sí hay colisión real
        if (!resolver) {
            String marca = "⚠ " + claveTxt;

            boolean yaExiste = slotBase.getColisiones().stream()
                    .anyMatch(c -> claveTxt.equals(extraerClaveReal(c)));

            if (!yaExiste) {
                slotBase.getColisiones().add(marca);
            }

            actualizarVista();

            long fin = System.nanoTime();
            resultadoLabel.setText("Colisión en posición base " + slotBase.getPosicion() +
                    " | Hash: " + hash +
                    " | Pendiente por resolver" +
                    " | Tiempo: " + (fin - inicio) + " ns");

            limpiarInsercion();
            return;
        }

        // Encadenamiento: guardar la colisión en la misma posición base
        if ("Encadenamiento de bloques".equals(estrategia)) {
            if (!slotBase.getColisiones().contains(claveTxt)) {
                slotBase.getColisiones().add(claveTxt);
            }

            actualizarVista();

            long fin = System.nanoTime();
            resultadoLabel.setText("Insertada por encadenamiento en posición base " + slotBase.getPosicion() +
                    " | Hash: " + hash +
                    " | Tiempo: " + (fin - inicio) + " ns");

            limpiarInsercion();
            return;
        }

        // Probing sobre TODA la estructura (no solo por bloques)
        for (int i = 1; i < N; i++) {
            int idx;

            if ("Cuadrática entre bloques".equals(estrategia)) {
                idx = (indiceBase + i * i) % N;
            } else if ("Doble Hash entre bloques".equals(estrategia)) {
                int k = Integer.parseInt(claveTxt);
                int h2 = 1 + (k % Math.max(1, N - 1));
                idx = (indiceBase + i * h2) % N;
            } else { // Lineal
                idx = (indiceBase + i) % N;
            }

            int bloqueIdx = idx / tamBloque;
            int offset = idx % tamBloque;

            SlotHashExterno slot = bloques.get(bloqueIdx).getSlots().get(offset);
            comparaciones++;

            if (slot.isVacio()) {
                slot.setClave(claveTxt);
                slot.setHash(hash);

                // quitar posible marca pendiente
                slotBase.getColisiones().removeIf(c -> claveTxt.equals(extraerClaveReal(c)));

                actualizarVista();
                seleccionarSlot(slot);

                long fin = System.nanoTime();
                resultadoLabel.setText("Insertada en bloque " + slot.getBloque() +
                        ", posición " + slot.getPosicion() +
                        " | Hash base: " + hash +
                        " | Colisiones: " + i +
                        " | Comparaciones: " + comparaciones +
                        " | Tiempo: " + (fin - inicio) + " ns");

                limpiarInsercion();
                return;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("Estructura llena | Hash base: " + hash +
                " | Comparaciones: " + comparaciones +
                " | Tiempo: " + (fin - inicio) + " ns");
        limpiarInsercion();
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
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarBusqueda();
            return;
        }

        int hash = calcularHash(claveTxt);
        int indiceBase = calcularIndiceBase(claveTxt);
        int bloqueBaseIdx = indiceBase / tamBloque;
        int offsetBase = indiceBase % tamBloque;

        String estrategia = colisionChoice.getValue();

        int comparaciones = 0;
        long inicio = System.nanoTime();

        SlotHashExterno slotBase = bloques.get(bloqueBaseIdx).getSlots().get(offsetBase);
        comparaciones++;

        if (claveTxt.equals(slotBase.getClave())) {
            long fin = System.nanoTime();
            seleccionarSlot(slotBase);
            resultadoLabel.setText("Encontrada en bloque " + slotBase.getBloque() +
                    ", posición " + slotBase.getPosicion() +
                    " | Hash: " + hash +
                    " | Comparaciones: " + comparaciones +
                    " | Tiempo: " + (fin - inicio) + " ns");
            limpiarBusqueda();
            return;
        }

        if (existeEnColisiones(slotBase, claveTxt)) {
            long fin = System.nanoTime();
            seleccionarSlot(slotBase);
            resultadoLabel.setText("Encontrada en colisiones de la posición base " + slotBase.getPosicion() +
                    " | Hash: " + hash +
                    " | Comparaciones: " + comparaciones +
                    " | Tiempo: " + (fin - inicio) + " ns");
            limpiarBusqueda();
            return;
        }

        if ("Encadenamiento de bloques".equals(estrategia)) {
            long fin = System.nanoTime();
            resultadoLabel.setText("No encontrada | Hash: " + hash +
                    " | Comparaciones: " + comparaciones +
                    " | Tiempo: " + (fin - inicio) + " ns");
            limpiarBusqueda();
            return;
        }

        for (int i = 1; i < N; i++) {
            int idx;

            if ("Cuadrática entre bloques".equals(estrategia)) {
                idx = (indiceBase + i * i) % N;
            } else if ("Doble Hash entre bloques".equals(estrategia)) {
                int k = Integer.parseInt(claveTxt);
                int h2 = 1 + (k % Math.max(1, N - 1));
                idx = (indiceBase + i * h2) % N;
            } else {
                idx = (indiceBase + i) % N;
            }

            int bloqueIdx = idx / tamBloque;
            int offset = idx % tamBloque;
            SlotHashExterno slot = bloques.get(bloqueIdx).getSlots().get(offset);

            comparaciones++;

            if (claveTxt.equals(slot.getClave())) {
                long fin = System.nanoTime();
                seleccionarSlot(slot);
                resultadoLabel.setText("Encontrada en bloque " + slot.getBloque() +
                        ", posición " + slot.getPosicion() +
                        " | Hash: " + hash +
                        " | Comparaciones: " + comparaciones +
                        " | Tiempo: " + (fin - inicio) + " ns");
                limpiarBusqueda();
                return;
            }

            if (slot.isVacio()) {
                break;
            }
        }

        long fin = System.nanoTime();
        resultadoLabel.setText("No encontrada | Hash: " + hash +
                " | Comparaciones: " + comparaciones +
                " | Tiempo: " + (fin - inicio) + " ns");
        limpiarBusqueda();
    }

    @FXML
    private void eliminarClave() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        String claveTxt = normalizarClave(claveBuscarField.getText(), digitos);
        claveBuscarField.setText(claveTxt);

        if (!claveValidaPorDigitos(claveTxt, digitos)) {
            resultadoLabel.setText("La clave debe tener exactamente " + digitos + " dígitos.");
            limpiarBusqueda();
            return;
        }

        // eliminar de colisiones pendientes
        for (BloqueHash bloque : bloques) {
            for (SlotHashExterno slot : bloque.getSlots()) {
                boolean removed = slot.getColisiones().removeIf(item -> claveTxt.equals(extraerClaveReal(item)));
                if (removed) {
                    actualizarVista();
                    resultadoLabel.setText("Eliminada de colisiones del bloque " + bloque.getNumeroBloque());
                    limpiarBusqueda();
                    return;
                }
            }
        }

        // eliminar clave real
        for (BloqueHash bloque : bloques) {
            for (SlotHashExterno slot : bloque.getSlots()) {
                if (claveTxt.equals(slot.getClave())) {
                    slot.setClave("");
                    slot.setHash(-1);
                    actualizarVista();
                    resultadoLabel.setText("Eliminada del bloque " + bloque.getNumeroBloque() +
                            ", posición " + slot.getPosicion());
                    limpiarBusqueda();
                    return;
                }
            }
        }

        resultadoLabel.setText("No se encontró la clave para eliminar.");
        limpiarBusqueda();
    }
    
    @FXML
    private void limpiarEstructura() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        for (BloqueHash bloque : bloques) {
            for (SlotHashExterno slot : bloque.getSlots()) {
                slot.setClave("");
                slot.setHash(-1);
                slot.getColisiones().clear();
            }
        }

        tabla.getSelectionModel().clearSelection();
        actualizarVista();

        claveInsertField.clear();
        claveBuscarField.clear();
        claveInsertField.requestFocus();

        resultadoLabel.setText("La estructura hash externa fue limpiada.");
    }

    @FXML
    private void arreglarColisiones() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        String estrategia = colisionChoice.getValue();
        int revisadas = 0;
        int resueltas = 0;
        int noSePudo = 0;

        long inicio = System.nanoTime();

        for (BloqueHash bloque : bloques) {
            for (SlotHashExterno slot : bloque.getSlots()) {
                if (slot.getColisiones() == null || slot.getColisiones().isEmpty()) continue;

                List<String> pendientes = new ArrayList<>(slot.getColisiones());

                for (String item : pendientes) {
                    if (!esNoResuelta(item)) continue;

                    String claveReal = extraerClaveReal(item);
                    revisadas++;

                    boolean ok = insertarReubicando(claveReal, estrategia);
                    if (ok) {
                        slot.getColisiones().remove(item);
                        resueltas++;
                    } else {
                        noSePudo++;
                    }
                }
            }
        }

        actualizarVista();

        long fin = System.nanoTime();
        resultadoLabel.setText("Arreglo terminado" +
                " | Revisadas: " + revisadas +
                " | Resueltas: " + resueltas +
                " | No se pudo: " + noSePudo +
                " | Tiempo: " + (fin - inicio) + " ns");
    }

    private boolean insertarReubicando(String claveTxt, String estrategia) {
        int hash = calcularHash(claveTxt);

        int indiceBase = calcularIndiceBase(claveTxt);

        int bloqueBaseIdx = indiceBase / tamBloque;

        int offsetBase = indiceBase % tamBloque;

        BloqueHash bloqueBase = bloques.get(bloqueBaseIdx);
        SlotHashExterno libreBase = bloqueBase.buscarEspacioLibre();
        
        if (libreBase != null) {
            libreBase.setClave(claveTxt);
            libreBase.setHash(bloqueBaseIdx);
            return true;
        }

        for (int i = 1; i < cantidadBloques; i++) {
            int idx;

            if ("Cuadrática entre bloques".equals(estrategia)) {
                idx = (bloqueBaseIdx + i * i) % cantidadBloques;
            } else if ("Doble Hash entre bloques".equals(estrategia)) {
                int k = Integer.parseInt(claveTxt);
                int h2 = 1 + (k % Math.max(1, cantidadBloques - 1));
                idx = (bloqueBaseIdx + i * h2) % cantidadBloques;
            } else {
                idx = (bloqueBaseIdx + i) % cantidadBloques;
            }

            BloqueHash bloque = bloques.get(idx);
            SlotHashExterno libre = bloque.buscarEspacioLibre();
            if (libre != null) {
                libre.setClave(claveTxt);
                libre.setHash(bloqueBaseIdx);
                return true;
            }
        }

        return false;
    }

    @FXML
    private void guardarEstructura() {
        if (!creada) {
            resultadoLabel.setText("Primero debes crear la estructura.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar estructura hash externa");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo Hash Externo (*.hexth)", "*.hexth"));
        File file = fc.showSaveDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("N=" + N); bw.newLine();
            bw.write("TAMBLOQUE=" + tamBloque); bw.newLine();
            bw.write("DIGITOS=" + digitos); bw.newLine();
            bw.write("HASH=" + hashChoice.getValue()); bw.newLine();
            bw.write("MOD=" + MOD); bw.newLine();
            bw.write("COLISION=" + colisionChoice.getValue()); bw.newLine();
            bw.write("RESOLVER=" + resolverCheck.isSelected()); bw.newLine();
            bw.write("DATABLOQUES"); bw.newLine();

            for (BloqueHash bloque : bloques) {
                for (SlotHashExterno s : bloque.getSlots()) {
                    String clave = s.getClave() == null ? "" : s.getClave();
                    String colisiones = "";
                    if (s.getColisiones() != null && !s.getColisiones().isEmpty()) {
                        colisiones = s.getColisiones().stream()
                                .map(String::trim)
                                .collect(Collectors.joining(","));
                    }

                    bw.write(bloque.getNumeroBloque() + "|" +
                            s.getPosicion() + "|" +
                            s.getHash() + "|" +
                            clave + "|" +
                            colisiones);
                    bw.newLine();
                }
            }

            bw.write("END"); bw.newLine();
            resultadoLabel.setText("Estructura guardada: " + file.getName());

        } catch (IOException e) {
            e.printStackTrace();
            resultadoLabel.setText("Error guardando: " + e.getMessage());
        }
    }

    @FXML
    private void cargarEstructura() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar estructura hash externa");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo Hash Externo (*.hexth)", "*.hexth"));
        File file = fc.showOpenDialog(tabla.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;

            int newN = 0;
            int newTamBloque = 1;
            int newDigitos = 2;
            int newMOD = 100;
            String newHash = "MOD";
            String newColision = "Lineal entre bloques";
            boolean newResolver = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("DATABLOQUES")) break;

                if (line.startsWith("N=")) newN = Integer.parseInt(line.substring(2));
                else if (line.startsWith("TAMBLOQUE=")) newTamBloque = Integer.parseInt(line.substring(10));
                else if (line.startsWith("DIGITOS=")) newDigitos = Integer.parseInt(line.substring(8));
                else if (line.startsWith("HASH=")) newHash = line.substring(5);
                else if (line.startsWith("MOD=")) newMOD = Integer.parseInt(line.substring(4));
                else if (line.startsWith("COLISION=")) newColision = line.substring(9);
                else if (line.startsWith("RESOLVER=")) newResolver = Boolean.parseBoolean(line.substring(9));
            }

            if (newN <= 0 || newTamBloque <= 0 || newN % newTamBloque != 0) {
                resultadoLabel.setText("Archivo inválido.");
                return;
            }

            N = newN;
            tamBloque = newTamBloque;
            digitos = newDigitos;
            MOD = newMOD;
            cantidadBloques = N / tamBloque;

            nField.setText(String.valueOf(N));
            tamBloqueField.setText(String.valueOf(tamBloque));
            digitosChoice.setValue(digitos);
            hashChoice.setValue(newHash);
            colisionChoice.setValue(newColision);
            resolverCheck.setSelected(newResolver);
            modField.setText(String.valueOf(MOD));
            modField.setDisable(!"MOD".equals(newHash));

            bloques.clear();
            dataTabla.clear();

            int posicionInicial = 1;
            for (int i = 0; i < cantidadBloques; i++) {
                BloqueHash bloque = new BloqueHash(i + 1, tamBloque, posicionInicial);
                bloques.add(bloque);
                posicionInicial += tamBloque;
            }

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("END")) break;
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) continue;

                int numBloque = Integer.parseInt(parts[0]);
                int posicion = Integer.parseInt(parts[1]);
                int hash = Integer.parseInt(parts[2]);
                String clave = parts[3].trim();
                String cols = parts[4].trim();

                BloqueHash bloque = bloques.get(numBloque - 1);

                for (SlotHashExterno s : bloque.getSlots()) {
                    if (s.getPosicion() == posicion) {
                        s.setHash(hash);
                        s.setClave(clave);

                        s.getColisiones().clear();
                        if (!cols.isEmpty()) {
                            Arrays.stream(cols.split(","))
                                    .map(String::trim)
                                    .filter(c -> !c.isEmpty())
                                    .forEach(c -> s.getColisiones().add(c));
                        }
                        break;
                    }
                }
            }

            creada = true;
            actualizarVista();
            resultadoLabel.setText("Estructura cargada: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error cargando: " + e.getMessage());
        }
    }

    private void actualizarVista() {
        List<SlotHashExterno> visibles = new ArrayList<>();

        for (BloqueHash bloque : bloques) {
            for (SlotHashExterno slot : bloque.getSlots()) {
                if (!slot.isVacio() || (slot.getColisiones() != null && !slot.getColisiones().isEmpty())) {
                    visibles.add(slot);
                }
            }
        }

        dataTabla.setAll(visibles);
        tabla.refresh();
    }

    private boolean existeClaveEnEstructura(String claveTxt) {
        for (BloqueHash bloque : bloques) {
            for (SlotHashExterno slot : bloque.getSlots()) {
                if (claveTxt.equals(slot.getClave())) return true;

                if (slot.getColisiones() != null) {
                    for (String c : slot.getColisiones()) {
                        if (claveTxt.equals(extraerClaveReal(c))) return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean existeEnColisiones(SlotHashExterno base, String claveTxt) {
    if (base.getColisiones() == null || base.getColisiones().isEmpty()) return false;

    for (String item : base.getColisiones()) {
        String real = extraerClaveReal(item);
        if (claveTxt.equals(real)) return true;
    }
    return false;
}   

    private boolean esNoResuelta(String s) {
        return s != null && s.trim().startsWith("⚠");
    }

    private String extraerClaveReal(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("⚠")) {
            return s.substring(1).trim();
        }
        return s;
    }

    private void seleccionarSlot(SlotHashExterno slot) {
        tabla.getSelectionModel().select(slot);
        tabla.scrollTo(slot);
    }

    private void limpiarBusqueda() {
        claveBuscarField.clear();
        claveBuscarField.requestFocus();
    }

    private void limpiarInsercion() {
        claveInsertField.clear();
        claveInsertField.requestFocus();
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

    // ===== menú =====

    @FXML
    private void openMenu(javafx.scene.input.MouseEvent event) {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
    }

    @FXML
    private void closeMenu(javafx.scene.input.MouseEvent event) {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
    }

    @FXML
    private void openMenuBusquedas(javafx.scene.input.MouseEvent event) {
        boolean isVisible = subMenuBusquedas.isVisible();
        subMenuBusquedas.setVisible(!isVisible);
        subMenuBusquedas.setManaged(!isVisible);
    }

    @FXML
    private void openMenuInternas(javafx.scene.input.MouseEvent event) {
        boolean isVisible = subMenuInternas.isVisible();
        subMenuInternas.setVisible(!isVisible);
        subMenuInternas.setManaged(!isVisible);
    }

    @FXML
    private void mostrarBusquedaLineal(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaLineal.fxml");
    }

    @FXML
    private void openBinario(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaBinaria.fxml");
    }

    @FXML
    private void openFuncionHash(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaHash.fxml");
    }

    @FXML
    private void openFuncionHashExterna(javafx.scene.input.MouseEvent event) {
        loadPanel("busquedaHashExterna.fxml");
    }

    @FXML
    private void openGrafos(javafx.scene.input.MouseEvent event) {
        loadPanel("grafos.fxml");
    }

    @FXML
    private void openInicio(javafx.scene.input.MouseEvent event) {
        loadPanel("inicio.fxml");
    }

    private void loadPanel(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent panel = loader.load();

            hashExternoPane.getChildren().clear();
            hashExternoPane.getChildren().add(panel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
