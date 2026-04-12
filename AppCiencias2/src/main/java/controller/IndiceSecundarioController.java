package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;

public class IndiceSecundarioController {

    @FXML private TextField registrosField;
    @FXML private TextField bloqueField;
    @FXML private TextField registroDatoField;
    @FXML private TextField registroIndiceField;

    @FXML private Label bfrLabel;
    @FXML private Label bLabel;
    @FXML private Label bfriLabel;
    @FXML private Label biLabel;
    @FXML private Label accesosLabel;
    @FXML private Label explicacionLabel;
    @FXML private Label resultadoLabel;

    @FXML private VBox indiceBox;
    @FXML private VBox datosBox;

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

        int bfr = B / R;
        int b = (int) Math.ceil((double) r / bfr);
        int bfri = B / Ri;
        int bi = (int) Math.ceil((double) r / bfri);
        int accesos = (int) Math.ceil(log2(bi)) + 1;

        bfrLabel.setText("Registros por bloque de datos (bfr) = " + bfr);
        bLabel.setText("Bloques para datos (b) = " + b);
        bfriLabel.setText("Registros índice por bloque (bfri) = " + bfri);
        biLabel.setText("Bloques por registros índice (bi) = " + bi);
        accesosLabel.setText("Accesos con índice secundario = " + accesos);

        explicacionLabel.setText(
                "Cada bloque del índice secundario contiene entradas densas. "
              + "La vista muestra el primer bloque, uno intermedio y el último."
        );

        construirIndiceSecundario(bi, bfri, r);
        construirDatos(b, bfr);

        resultadoLabel.setText("Estructura secundaria generada correctamente.");
    }

    private void construirIndiceSecundario(int bi, int bfri, int totalRegistros) {
        indiceBox.getChildren().clear();

        agregarBloqueIndiceSecundario(1, bfri, totalRegistros);

        if (bi > 2) {
            indiceBox.getChildren().add(new Label("..."));

            int medio = bi / 2;
            agregarBloqueIndiceSecundario(medio, bfri, totalRegistros);

            indiceBox.getChildren().add(new Label("..."));
        }

        if (bi > 1) {
            agregarBloqueIndiceSecundario(bi, bfri, totalRegistros);
        }
    }

    private void agregarBloqueIndiceSecundario(int i, int bfri, int totalRegistros) {
        int inicio = (i - 1) * bfri + 1;
        int fin = i * bfri;

        if (inicio > totalRegistros) inicio = totalRegistros;
        if (fin > totalRegistros) fin = totalRegistros;

        String texto = "B" + i + " → " + inicio + " a " + fin;

        Label l = new Label(texto);
        l.setStyle("-fx-border-color: black; -fx-padding: 6;");
        indiceBox.getChildren().add(l);
    }

    private void construirDatos(int b, int bfr) {
        datosBox.getChildren().clear();

        agregarBloqueDato(1, bfr);

        if (b > 2) {
            datosBox.getChildren().add(new Label("..."));

            int medio = b / 2;
            agregarBloqueDato(medio, bfr);

            datosBox.getChildren().add(new Label("..."));
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
        l.setStyle("-fx-border-color: black; -fx-padding: 6;");
        datosBox.getChildren().add(l);
    }

    @FXML
    private void limpiarEstructura() {
        indiceBox.getChildren().clear();
        datosBox.getChildren().clear();
        resultadoLabel.setText("Limpio.");
    }

    @FXML
    private void guardarEstructura() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar índice secundario");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Índice Secundario (*.isec)", "*.isec")
            );

            File file = fc.showSaveDialog(indiceBox.getScene().getWindow());
            if (file == null) return;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("TIPO=INDICE_SECUNDARIO");
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
            fc.setTitle("Cargar índice secundario");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Índice Secundario (*.isec)", "*.isec")
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

            if (rTemp <= 0 || BTemp <= 0 || RTemp <= 0 || RiTemp <= 0) {
                resultadoLabel.setText("Archivo inválido.");
                return;
            }

            this.r = rTemp;
            this.B = BTemp;
            this.R = RTemp;
            this.Ri = RiTemp;

            registrosField.setText(String.valueOf(r));
            bloqueField.setText(String.valueOf(B));
            registroDatoField.setText(String.valueOf(R));
            registroIndiceField.setText(String.valueOf(Ri));

            crearEstructura();
            resultadoLabel.setText("Estructura cargada correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error al cargar.");
        }
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