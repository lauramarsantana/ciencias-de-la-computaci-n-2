package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class IndiceMultinivelController {

    @FXML private ChoiceBox<String> tipoIndiceChoice;

    @FXML private TextField registrosField;
    @FXML private TextField bloqueField;
    @FXML private TextField registroDatoField;
    @FXML private TextField registroIndiceField;

    @FXML private Label bfrLabel;
    @FXML private Label bLabel;
    @FXML private Label bfriLabel;
    @FXML private Label nivel1Label;
    @FXML private Label nivel2Label;
    @FXML private Label nivel3Label;
    @FXML private Label accesosLabel;
    @FXML private Label explicacionLabel;
    @FXML private Label resultadoLabel;

    @FXML private VBox externosBox;
    @FXML private VBox internosBox;
    @FXML private VBox datosBox;

    private int r;
    private int B;
    private int R;
    private int Ri;

    @FXML
    public void initialize() {
        tipoIndiceChoice.getItems().addAll("Primario", "Secundario");
        tipoIndiceChoice.setValue("Primario");
    }

    @FXML
    private void crearEstructura() {

        String tipo = tipoIndiceChoice.getValue();

        Integer rVal = leerEntero(registrosField);
        Integer bVal = leerEntero(bloqueField);
        Integer rDatoVal = leerEntero(registroDatoField);
        Integer rIndVal = leerEntero(registroIndiceField);

        if (tipo == null || rVal == null || bVal == null || rDatoVal == null || rIndVal == null) {
            resultadoLabel.setText("Debes ingresar datos válidos.");
            return;
        }

        if (rVal <= 0 || bVal <= 0 || rDatoVal <= 0 || rIndVal <= 0) {
            resultadoLabel.setText("Todos los valores deben ser mayores que cero.");
            return;
        }

        this.r = rVal;
        this.B = bVal;
        this.R = rDatoVal;
        this.Ri = rIndVal;

        int bfr = B / R;
        if (bfr <= 0) {
            resultadoLabel.setText("El tamaño del bloque debe ser mayor que el del registro de datos.");
            return;
        }

        int b = (int) Math.ceil((double) r / bfr);

        int bfri = B / Ri;
        if (bfri <= 0) {
            resultadoLabel.setText("El tamaño del bloque debe ser mayor que el del registro índice.");
            return;
        }

        // Construcción de niveles
        List<Integer> niveles = new ArrayList<>();

        int nivelBaseIndice;
        if ("Primario".equals(tipo)) {
            nivelBaseIndice = (int) Math.ceil((double) b / bfri);
        } else {
            nivelBaseIndice = (int) Math.ceil((double) r / bfri);
        }

        niveles.add(nivelBaseIndice);

        while (niveles.get(niveles.size() - 1) > 1) {
            int actual = niveles.get(niveles.size() - 1);
            int siguiente = (int) Math.ceil((double) actual / bfri);
            niveles.add(siguiente);
        }

        int accesos = niveles.size() + 1;

        // Mostrar resultados
        bfrLabel.setText("Registros por bloque de datos (bfr) = " + bfr);
        bLabel.setText("Bloques de datos necesarios (b) = " + b);
        bfriLabel.setText("Entradas de índice por bloque (bfri) = " + bfri);

        nivel1Label.setText("Bloques de índice nivel 1 = " + obtenerNivelTexto(niveles, 0));
        nivel2Label.setText("Bloques de índice nivel 2 = " + obtenerNivelTexto(niveles, 1));
        nivel3Label.setText("Bloques de índice nivel 3 = " + obtenerNivelTexto(niveles, 2));

        accesosLabel.setText("Accesos con índice multinivel = " + accesos);

        if ("Primario".equals(tipo)) {
            explicacionLabel.setText(
                "Multinivel primario: el primer nivel del índice se construye sobre los bloques de datos. "
              + "La vista resume la estructura en índices externos, índices internos y datos."
            );
        } else {
            explicacionLabel.setText(
                "Multinivel secundario: el primer nivel del índice es denso y se construye sobre los registros. "
              + "La vista resume la estructura en índices externos, índices internos y datos."
            );
        }

        // Dibujar cuadros
        construirDatos(b, bfr);
        construirMultinivel(tipo, niveles, b, bfri, bfr);

        resultadoLabel.setText("Estructura multinivel generada correctamente.");
    }

    private void construirMultinivel(String tipo, List<Integer> niveles, int bloquesDatos, int bfri, int bfr) {
        externosBox.getChildren().clear();
        internosBox.getChildren().clear();

        if (niveles.isEmpty()) {
            return;
        }

        if ("Primario".equals(tipo)) {
            construirPrimarioMultinivel(niveles, bloquesDatos, bfri);
        } else {
            construirSecundarioMultinivel(niveles, bloquesDatos, bfri, bfr);
        }
    }

    private void construirPrimarioMultinivel(List<Integer> niveles, int bloquesDatos, int bfri) {

        int nivel1 = niveles.get(0);

        // Internos = nivel 1
        construirResumenInternoPrimario(nivel1, bloquesDatos, bfri);

        // Externos = último nivel si existe, si no el mismo resumido
        int nivelExterno = niveles.get(niveles.size() - 1);

        if (niveles.size() >= 2) {
            int nivelDebajo = niveles.get(niveles.size() - 2);
            construirResumenExterno(nivelExterno, nivelDebajo, bfri);
        } else {
            construirResumenExterno(nivelExterno, nivel1, bfri);
        }
    }

    private void construirSecundarioMultinivel(List<Integer> niveles, int bloquesDatos, int bfri, int bfr) {

        int nivel1 = niveles.get(0);

        // Internos = nivel 1 denso
        construirResumenInternoSecundario(nivel1, bfri, r);

        // Externos = último nivel si existe, si no el mismo resumido
        int nivelExterno = niveles.get(niveles.size() - 1);

        if (niveles.size() >= 2) {
            int nivelDebajo = niveles.get(niveles.size() - 2);
            construirResumenExterno(nivelExterno, nivelDebajo, bfri);
        } else {
            construirResumenExterno(nivelExterno, nivel1, bfri);
        }
    }

    private void construirResumenInternoPrimario(int cantidadBloquesIndice, int totalBloquesDatos, int bfri) {
        internosBox.getChildren().clear();

        agregarBloqueInternoPrimario(1, totalBloquesDatos, bfri);

        if (cantidadBloquesIndice > 2) {
            internosBox.getChildren().add(crearLabelPuntos());

            int medio = cantidadBloquesIndice / 2;
            agregarBloqueInternoPrimario(medio, totalBloquesDatos, bfri);

            internosBox.getChildren().add(crearLabelPuntos());
        }

        if (cantidadBloquesIndice > 1) {
            agregarBloqueInternoPrimario(cantidadBloquesIndice, totalBloquesDatos, bfri);
        }
    }

    private void agregarBloqueInternoPrimario(int i, int totalBloquesDatos, int bfri) {
        int inicio = (i - 1) * bfri + 1;
        int fin = i * bfri;

        if (inicio > totalBloquesDatos) inicio = totalBloquesDatos;
        if (fin > totalBloquesDatos) fin = totalBloquesDatos;

        String texto = "B" + i + " \u2192 B" + inicio + " a B" + fin;

        internosBox.getChildren().add(crearLabelBloque(texto));
    }

    private void construirResumenInternoSecundario(int cantidadBloquesIndice, int bfri, int totalRegistros) {
        internosBox.getChildren().clear();

        agregarBloqueInternoSecundario(1, bfri, totalRegistros);

        if (cantidadBloquesIndice > 2) {
            internosBox.getChildren().add(crearLabelPuntos());

            int medio = cantidadBloquesIndice / 2;
            agregarBloqueInternoSecundario(medio, bfri, totalRegistros);

            internosBox.getChildren().add(crearLabelPuntos());
        }

        if (cantidadBloquesIndice > 1) {
            agregarBloqueInternoSecundario(cantidadBloquesIndice, bfri, totalRegistros);
        }
    }

    private void agregarBloqueInternoSecundario(int i, int bfri, int totalRegistros) {
        int inicio = (i - 1) * bfri + 1;
        int fin = i * bfri;

        if (inicio > totalRegistros) inicio = totalRegistros;
        if (fin > totalRegistros) fin = totalRegistros;

        String texto = "B" + i + " \u2192 " + inicio + " a " + fin;

        internosBox.getChildren().add(crearLabelBloque(texto));
    }

    private void construirResumenExterno(int cantidadBloquesExternos, int totalBloquesDelNivelInferior, int bfri) {
        externosBox.getChildren().clear();

        agregarBloqueExterno(1, totalBloquesDelNivelInferior, bfri);

        if (cantidadBloquesExternos > 2) {
            externosBox.getChildren().add(crearLabelPuntos());

            int medio = cantidadBloquesExternos / 2;
            agregarBloqueExterno(medio, totalBloquesDelNivelInferior, bfri);

            externosBox.getChildren().add(crearLabelPuntos());
        }

        if (cantidadBloquesExternos > 1) {
            agregarBloqueExterno(cantidadBloquesExternos, totalBloquesDelNivelInferior, bfri);
        }
    }

    private void agregarBloqueExterno(int i, int totalBloquesInferiores, int bfri) {
        int inicio = (i - 1) * bfri + 1;
        int fin = i * bfri;

        if (inicio > totalBloquesInferiores) inicio = totalBloquesInferiores;
        if (fin > totalBloquesInferiores) fin = totalBloquesInferiores;

        String texto = "B" + i + " \u2192 B" + inicio + " a B" + fin;

        externosBox.getChildren().add(crearLabelBloque(texto));
    }

    private void construirDatos(int b, int bfr) {
        datosBox.getChildren().clear();

        agregarBloqueDato(1, bfr);

        if (b > 2) {
            datosBox.getChildren().add(crearLabelPuntos());

            int medio = b / 2;
            agregarBloqueDato(medio, bfr);

            datosBox.getChildren().add(crearLabelPuntos());
        }

        if (b > 1) {
            agregarBloqueDato(b, bfr);
        }
    }

    private void agregarBloqueDato(int i, int bfr) {
        int inicio = (i - 1) * bfr + 1;
        int fin = i * bfr;

        if (fin > r) fin = r;

        String texto = "B" + i + " : " + inicio + " - " + fin;
        datosBox.getChildren().add(crearLabelBloque(texto));
    }

    @FXML
    private void limpiarEstructura() {
        externosBox.getChildren().clear();
        internosBox.getChildren().clear();
        datosBox.getChildren().clear();

        bfrLabel.setText("Registros por bloque de datos (bfr) = ");
        bLabel.setText("Bloques de datos necesarios (b) = ");
        bfriLabel.setText("Entradas de índice por bloque (bfri) = ");
        nivel1Label.setText("Bloques de índice nivel 1 = ");
        nivel2Label.setText("Bloques de índice nivel 2 = ");
        nivel3Label.setText("Bloques de índice nivel 3 = ");
        accesosLabel.setText("Accesos con índice multinivel = ");
        explicacionLabel.setText("");
        resultadoLabel.setText("Limpio.");
    }

    @FXML
    private void guardarEstructura() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar índice multinivel");
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Índice Multinivel (*.imulti)", "*.imulti")
            );

            File file = fc.showSaveDialog(externosBox.getScene().getWindow());
            if (file == null) return;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("TIPO=INDICE_MULTINIVEL");
                bw.newLine();
                bw.write("CLASE=" + tipoIndiceChoice.getValue());
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
            fc.setTitle("Cargar índice multinivel");
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Índice Multinivel (*.imulti)", "*.imulti")
            );

            File file = fc.showOpenDialog(externosBox.getScene().getWindow());
            if (file == null) return;

            String tipo = "Primario";
            int rTemp = 0, BTemp = 0, RTemp = 0, RiTemp = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("CLASE=")) {
                        tipo = line.substring(6).trim();
                    } else if (line.startsWith("R=")) {
                        rTemp = Integer.parseInt(line.substring(2).trim());
                    } else if (line.startsWith("B=")) {
                        BTemp = Integer.parseInt(line.substring(2).trim());
                    } else if (line.startsWith("RD=")) {
                        RTemp = Integer.parseInt(line.substring(3).trim());
                    } else if (line.startsWith("RI=")) {
                        RiTemp = Integer.parseInt(line.substring(3).trim());
                    }
                }
            }

            if (rTemp <= 0 || BTemp <= 0 || RTemp <= 0 || RiTemp <= 0) {
                resultadoLabel.setText("Archivo inválido.");
                return;
            }

            tipoIndiceChoice.setValue(tipo);
            registrosField.setText(String.valueOf(rTemp));
            bloqueField.setText(String.valueOf(BTemp));
            registroDatoField.setText(String.valueOf(RTemp));
            registroIndiceField.setText(String.valueOf(RiTemp));

            crearEstructura();
            resultadoLabel.setText("Estructura cargada correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            resultadoLabel.setText("Error al cargar.");
        }
    }

    private Label crearLabelBloque(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-border-color: black; -fx-padding: 6;");
        return l;
    }

    private Label crearLabelPuntos() {
        return new Label("...");
    }

    private String obtenerNivelTexto(List<Integer> niveles, int indice) {
        if (indice < niveles.size()) {
            return String.valueOf(niveles.get(indice));
        }
        return "No aplica";
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

    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}