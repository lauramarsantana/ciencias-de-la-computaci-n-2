package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import utilities.SlotIndice;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;

public class IndicePrimarioController {

    @FXML private TextField registrosField;
    @FXML private TextField bloqueField;
    @FXML private TextField registroDatoField;
    @FXML private TextField registroIndiceField;

    @FXML private Label bfrLabel;
    @FXML private Label bLabel;
    @FXML private Label comparacionesLabel;
    @FXML private Label logDatosLabel;
    @FXML private Label bfriLabel;
    @FXML private Label biLabel;
    @FXML private Label logIndiceLabel;

    @FXML private VBox indiceBox;
    @FXML private VBox datosBox;

    @FXML private Label resultadoLabel;

    private int r, B, R, Ri;

    @FXML
    private void crearEstructura() {

        Integer rVal = leerEntero(registrosField);
        Integer bVal = leerEntero(bloqueField);
        Integer rDatoVal = leerEntero(registroDatoField);
        Integer rIndVal = leerEntero(registroIndiceField);

        if (rVal == null || bVal == null || rDatoVal == null || rIndVal == null) {
            resultadoLabel.setText("Datos inválidos.");
            return;
        }

        this.r = rVal;
        this.B = bVal;
        this.R = rDatoVal;
        this.Ri = rIndVal;

        // --- CÁLCULOS ---
        int bfr = B / R;
        int b = (int) Math.ceil((double) r / bfr);
        int comparaciones = b / 2;
        int accesosDatos = (int) Math.ceil(log2(b)) + 1;

        int bfri = B / Ri;
        int bi = (int) Math.ceil((double) b / bfri);
        int accesosIndice = (int) Math.ceil(log2(bi)) + 1;

        // --- MOSTRAR RESULTADOS ---
        bfrLabel.setText("Registros por bloque de datos (bfr) = " + bfr);
        bLabel.setText("Bloques de datos necesarios (b) = " + b);
        comparacionesLabel.setText("Comparaciones promedio en búsqueda lineal (b/2) = " + comparaciones);
        logDatosLabel.setText("Accesos en búsqueda binaria sobre datos = " + accesosDatos);
        bfriLabel.setText("Entradas de índice por bloque (bfri) = " + bfri);
        biLabel.setText("Bloques de índice necesarios (bi) = " + bi);
        logIndiceLabel.setText("Accesos con índice primario = " + accesosIndice);

        // --- CONSTRUIR CUADROS ---
        construirDatos(b, bfr);
        construirIndice(b, bi, bfri);

        resultadoLabel.setText("Estructura generada correctamente.");
    }

    private void construirDatos(int b, int bfr) {
    datosBox.getChildren().clear();

    agregarBloqueDato(1, bfr);

    if (b > 2) {
        Label puntos1 = new Label("...");
        datosBox.getChildren().add(puntos1);

        int medio = b / 2;
        agregarBloqueDato(medio, bfr);

        Label puntos2 = new Label("...");
        datosBox.getChildren().add(puntos2);
    }

    if (b > 1) {
        agregarBloqueDato(b, bfr);
    }
    }

    private void agregarBloqueDato(int i, int bfr) {
        int inicio = (i - 1) * bfr + 1;
        int fin = i * bfr;

        String texto = "B" + i + " : " + inicio + " - " + fin;

        Label l = new Label(texto);
        l.setStyle("-fx-border-color: black; -fx-padding: 3;");
        datosBox.getChildren().add(l);
    }

        private void construirIndice(int b, int bi, int bfri) {
        indiceBox.getChildren().clear();

        agregarBloqueIndice(1, bfri, b);

        if (bi > 2) {
            Label puntos1 = new Label("...");
            indiceBox.getChildren().add(puntos1);

            int medio = bi / 2;
            agregarBloqueIndice(medio, bfri, b);

            Label puntos2 = new Label("...");
            indiceBox.getChildren().add(puntos2);
        }

        if (bi > 1) {
            agregarBloqueIndice(bi, bfri, b);
        }
    }

    private void agregarBloqueIndice(int i, int bfri, int totalBloquesDatos) {

    int inicio = (i - 1) * bfri + 1;
    int fin = i * bfri;

    if (inicio > totalBloquesDatos) inicio = totalBloquesDatos;
    if (fin > totalBloquesDatos) fin = totalBloquesDatos;

    String texto = "B" + i + " → B" + inicio + " a B" + fin;

    Label l = new Label(texto);
    l.setStyle("-fx-border-color: black; -fx-padding: 3;");

    indiceBox.getChildren().add(l);
    }
    
    @FXML
    private void guardarEstructura() {

        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar índice primario");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Índice (*.idx)", "*.idx")
            );

            File file = fc.showSaveDialog(indiceBox.getScene().getWindow());
            if (file == null) return;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

                bw.write("TIPO=INDICE_PRIMARIO");
                bw.newLine();
                bw.write("R=" + r);
                bw.newLine();
                bw.write("B=" + B);
                bw.newLine();
                bw.write("RD=" + R);
                bw.newLine();
                bw.write("RI=" + Ri);
                bw.newLine();

            }

            resultadoLabel.setText("Estructura guardada correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error al guardar.");
        }
    }
    
    @FXML
    private void cargarEstructura() {

        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Cargar índice primario");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Índice (*.idx)", "*.idx")
            );

            File file = fc.showOpenDialog(indiceBox.getScene().getWindow());
            if (file == null) return;

            int rTemp = 0, BTemp = 0, RTemp = 0, RiTemp = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                String line;

                while ((line = br.readLine()) != null) {

                    if (line.startsWith("R=")) {
                        rTemp = Integer.parseInt(line.substring(2));
                    } else if (line.startsWith("B=")) {
                        BTemp = Integer.parseInt(line.substring(2));
                    } else if (line.startsWith("RD=")) {
                        RTemp = Integer.parseInt(line.substring(3));
                    } else if (line.startsWith("RI=")) {
                        RiTemp = Integer.parseInt(line.substring(3));
                    }
                }
            }

            // Asignar a los campos
            this.r = rTemp;
            this.B = BTemp;
            this.R = RTemp;
            this.Ri = RiTemp;

            // Mostrar en la interfaz
            registrosField.setText(String.valueOf(r));
            bloqueField.setText(String.valueOf(B));
            registroDatoField.setText(String.valueOf(R));
            registroIndiceField.setText(String.valueOf(Ri));

            // Reconstruir estructura
            crearEstructura();

            resultadoLabel.setText("Estructura cargada correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error al cargar.");
        }
    }

    @FXML
    private void limpiarEstructura() {
        indiceBox.getChildren().clear();
        datosBox.getChildren().clear();
        resultadoLabel.setText("Limpio.");
    }

    private Integer leerEntero(TextField tf) {
        try {
            return Integer.parseInt(tf.getText().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}