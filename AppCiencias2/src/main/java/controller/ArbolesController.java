package controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import utilities.Arbol;
import utilities.ArbolVisual;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ArbolesController {

    @FXML
    private TextField raizField;

    @FXML
    private ComboBox<String> padreCombo;

    @FXML
    private TextField hijoField;

    @FXML
    private TextArea infoArea;

    @FXML
    private Pane panelArbol;

    private Arbol arbol;
    private String nombreRaiz;

    private final List<String[]> relaciones = new ArrayList<>();
    private final List<String> nodosExistentes = new ArrayList<>();

    @FXML
    private void crearRaiz() {
        try {
            String raiz = raizField.getText().trim();

            if (raiz.isEmpty()) {
                mostrarAlerta("Error", "Debes ingresar un valor para la raíz.");
                return;
            }

            if (nombreRaiz != null) {
                mostrarAlerta("Error", "La raíz ya fue creada. Si deseas empezar de nuevo, usa Limpiar.");
                return;
            }

            if (nodosExistentes.contains(raiz)) {
                mostrarAlerta("Error", "Ese nodo ya existe.");
                return;
            }

            nombreRaiz = raiz;
            nodosExistentes.add(raiz);
            actualizarPadresCombo();

            padreCombo.setValue(nombreRaiz);
            raizField.clear();

            mostrarInformacionTemporal("Se creó la raíz: " + nombreRaiz);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void agregarRelacion() {
        try {
            if (nombreRaiz == null) {
                mostrarAlerta("Error", "Primero debes crear la raíz.");
                return;
            }

            String padre = padreCombo.getValue();
            String hijo = hijoField.getText().trim();

            if (padre == null || padre.isEmpty()) {
                mostrarAlerta("Error", "Debes seleccionar un nodo padre.");
                return;
            }

            if (hijo.isEmpty()) {
                mostrarAlerta("Error", "Debes ingresar un nodo hijo.");
                return;
            }

            if (padre.equals(hijo)) {
                mostrarAlerta("Error", "Un nodo no puede ser padre de sí mismo.");
                return;
            }

            for (String[] relacion : relaciones) {
                if (relacion[0].equals(padre) && relacion[1].equals(hijo)) {
                    mostrarAlerta("Error", "Esa relación ya existe.");
                    return;
                }
            }

            validarRelacionAntesDeGuardar(padre, hijo);

            relaciones.add(new String[]{padre, hijo});

            if (!nodosExistentes.contains(hijo)) {
                nodosExistentes.add(hijo);
                actualizarPadresCombo();
            }

            hijoField.clear();
            padreCombo.setValue(padre);

            mostrarInformacionTemporal("Se agregó la relación: " + padre + " - " + hijo);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void eliminarRelacion() {
        try {
            if (nombreRaiz == null) {
                mostrarAlerta("Error", "Primero debes crear la raíz.");
                return;
            }

            String padre = padreCombo.getValue();
            String hijo = hijoField.getText().trim();

            if (padre == null || padre.isEmpty()) {
                mostrarAlerta("Error", "Debes seleccionar el nodo padre.");
                return;
            }

            if (hijo.isEmpty()) {
                mostrarAlerta("Error", "Debes ingresar el nodo hijo que deseas eliminar.");
                return;
            }

            boolean eliminada = false;

            for (int i = 0; i < relaciones.size(); i++) {
                String[] relacion = relaciones.get(i);
                if (relacion[0].equals(padre) && relacion[1].equals(hijo)) {
                    relaciones.remove(i);
                    eliminada = true;
                    break;
                }
            }

            if (!eliminada) {
                mostrarAlerta("Error", "Esa relación no existe.");
                return;
            }

            reconstruirNodosExistentes();

            hijoField.clear();
            actualizarPadresCombo();

            if (!nodosExistentes.isEmpty()) {
                padreCombo.setValue(nodosExistentes.get(0));
            }

            mostrarInformacionTemporal("Se eliminó la relación: " + padre + " - " + hijo);

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }
    
    @FXML
    private void guardarArbol() {
        if (nombreRaiz == null) {
            mostrarAlerta("Error", "Primero debes crear un árbol.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar árbol");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Árbol (*.arb)", "*.arb")
        );

        File file = fc.showSaveDialog(panelArbol.getScene().getWindow());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("TIPO=ARBOL");
            bw.newLine();

            bw.write("RAIZ=" + nombreRaiz);
            bw.newLine();

            bw.write("RELACIONES");
            bw.newLine();

            for (String[] relacion : relaciones) {
                bw.write(relacion[0] + "|" + relacion[1]);
                bw.newLine();
            }

            bw.write("END");
            bw.newLine();

            mostrarInformacionTemporal("Árbol guardado: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error guardando: " + e.getMessage());
        }
    }

    @FXML
    private void cargarArbol() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar árbol");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Árbol (*.arb)", "*.arb")
        );

        File file = fc.showOpenDialog(panelArbol.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            String nuevaRaiz = null;
            List<String[]> nuevasRelaciones = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("RELACIONES")) {
                    break;
                }

                if (line.startsWith("RAIZ=")) {
                    nuevaRaiz = line.substring(5).trim();
                }
            }

            if (nuevaRaiz == null || nuevaRaiz.isEmpty()) {
                mostrarAlerta("Error", "Archivo inválido: no tiene raíz.");
                return;
            }

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("END")) {
                    break;
                }

                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) continue;

                String padre = parts[0].trim();
                String hijo = parts[1].trim();

                if (!padre.isEmpty() && !hijo.isEmpty()) {
                    nuevasRelaciones.add(new String[]{padre, hijo});
                }
            }

            // Limpiar estado actual
            nombreRaiz = nuevaRaiz;
            relaciones.clear();
            nodosExistentes.clear();
            panelArbol.getChildren().clear();
            infoArea.clear();

            // Cargar datos nuevos
            relaciones.addAll(nuevasRelaciones);
            reconstruirNodosExistentes();
            actualizarPadresCombo();

            raizField.setText(nombreRaiz);
            hijoField.clear();

            if (!nodosExistentes.isEmpty()) {
                padreCombo.setValue(nombreRaiz);
            }

            // Reconstruir y dibujar automáticamente
            arbol = construirArbolActual();
            ArbolVisual.dibujar(arbol, panelArbol);
            mostrarInformacion();

            mostrarInformacionTemporal("Árbol cargado: " + file.getName());
            mostrarInformacion();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando: " + e.getMessage());
        }
    }

    @FXML
    private void dibujarArbol() {
        try {
            if (nombreRaiz == null) {
                mostrarAlerta("Error", "Debes crear la raíz primero.");
                return;
            }

            arbol = construirArbolActual();

            ArbolVisual.dibujar(arbol, panelArbol);
            mostrarInformacion();

        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    private void limpiarArbol() {
        nombreRaiz = null;
        arbol = null;

        relaciones.clear();
        nodosExistentes.clear();

        raizField.clear();
        hijoField.clear();
        infoArea.clear();

        padreCombo.setItems(FXCollections.observableArrayList());
        padreCombo.setValue(null);

        panelArbol.getChildren().clear();
    }

    private void validarRelacionAntesDeGuardar(String padre, String hijo) {
        Arbol arbolTemporal = new Arbol();
        arbolTemporal.agregarRaiz(nombreRaiz);

        for (String[] relacion : relaciones) {
            arbolTemporal.agregarRelacion(relacion[0], relacion[1]);
        }

        arbolTemporal.agregarRelacion(padre, hijo);
        arbolTemporal.prepararArbol();
    }

    private Arbol construirArbolActual() {
        Arbol nuevoArbol = new Arbol();
        nuevoArbol.agregarRaiz(nombreRaiz);

        for (String[] relacion : relaciones) {
            nuevoArbol.agregarRelacion(relacion[0], relacion[1]);
        }

        nuevoArbol.prepararArbol();
        return nuevoArbol;
    }

    private void reconstruirNodosExistentes() {
        nodosExistentes.clear();

        if (nombreRaiz != null) {
            nodosExistentes.add(nombreRaiz);
        }

        for (String[] relacion : relaciones) {
            if (!nodosExistentes.contains(relacion[0])) {
                nodosExistentes.add(relacion[0]);
            }
            if (!nodosExistentes.contains(relacion[1])) {
                nodosExistentes.add(relacion[1]);
            }
        }
    }

    private void actualizarPadresCombo() {
        padreCombo.setItems(FXCollections.observableArrayList(nodosExistentes));
    }

    private void mostrarInformacion() {
        StringBuilder sb = new StringBuilder();

        sb.append("Cantidad de nodos: ").append(arbol.contarNodos()).append("\n");
        sb.append("Cantidad de aristas: ").append(arbol.contarAristas()).append("\n\n");

        sb.append("Aristas:\n");
        List<String> aristas = arbol.obtenerAristasComoTexto();
        for (String a : aristas) {
            sb.append(a).append("\n");
        }

        sb.append("\nNiveles:\n");
        List<String> niveles = arbol.obtenerNivelesComoTexto();
        for (String n : niveles) {
            sb.append(n).append("\n");
        }

        sb.append("\nHojas:\n");
        arbol.obtenerHojas().forEach(h -> sb.append(h.getNombre()).append("\n"));

        sb.append("\n").append(arbol.centroOBicentroComoTexto());

        infoArea.setText(sb.toString());
    }

    private void mostrarInformacionTemporal(String mensaje) {
        infoArea.setText(mensaje);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}