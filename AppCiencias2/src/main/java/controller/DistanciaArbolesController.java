    package controller;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Set;
    import javafx.fxml.FXML;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Label;
    import javafx.scene.control.TextArea;
    import javafx.scene.control.TextField;
    import javafx.scene.layout.Pane;
    import utilities.AristaPonderada;
    import utilities.ArbolDistanciaVisual;
    import utilities.ArbolGeneradorVisual;
    import utilities.DistanciaArbolesService;
    import utilities.GrafoPonderado;
    import javafx.stage.FileChooser;
    import java.io.*;
    import java.nio.charset.StandardCharsets;
    import utilities.ArchivoEstructuraService;  
    import utilities.DatosArchivo;
    import java.util.LinkedHashSet;
    import java.util.Set;

    public class DistanciaArbolesController {

        @FXML
        private TextField verticesField1;

        @FXML
        private TextField aristasField1;

        @FXML
        private TextField verticesField2;

        @FXML
        private TextField aristasField2;

        @FXML
        private Label infoArbol1;

        @FXML
        private Label infoArbol2;

        @FXML
        private Label infoResultado;

        @FXML
        private TextArea infoArea;

        @FXML
        private Pane panelArbol1;

        @FXML
        private Pane panelArbol2;

        @FXML
        private Pane panelResultado;

        private LayoutController layoutController;
        private GrafoPonderado arbol1;
        private GrafoPonderado arbol2;

        public void setLayoutController(LayoutController layoutController) {
            this.layoutController = layoutController;
        }

        @FXML
        private void mostrarArbol1() {
            try {
                arbol1 = construirArbol(verticesField1.getText(), aristasField1.getText());

                ArbolGeneradorVisual.dibujarGrafoCompleto(arbol1, panelArbol1);
                infoArbol1.setText("Árbol 1");

                StringBuilder sb = new StringBuilder();
                sb.append("Árbol 1\n\n");
                sb.append("Vértices:\n");
                for (String v : arbol1.getVertices()) {
                    sb.append("- ").append(v).append("\n");
                }

                sb.append("\nAristas:\n");
                for (AristaPonderada a : arbol1.getAristas()) {
                    sb.append(a).append("\n");
                }

                infoArea.setText(sb.toString());

            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage());
            }
        }

        @FXML
        private void mostrarArbol2() {
            try {
                arbol2 = construirArbol(verticesField2.getText(), aristasField2.getText());

                ArbolGeneradorVisual.dibujarGrafoCompleto(arbol2, panelArbol2);
                infoArbol2.setText("Árbol 2");

                StringBuilder sb = new StringBuilder();
                sb.append("Árbol 2\n\n");
                sb.append("Vértices:\n");
                for (String v : arbol2.getVertices()) {
                    sb.append("- ").append(v).append("\n");
                }

                sb.append("\nAristas:\n");
                for (AristaPonderada a : arbol2.getAristas()) {
                    sb.append(a).append("\n");
                }

                infoArea.setText(sb.toString());

            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage());
            }
        }

        @FXML
        private void calcularDistancia() {
            try {
                arbol1 = construirArbol(verticesField1.getText(), aristasField1.getText());
                arbol2 = construirArbol(verticesField2.getText(), aristasField2.getText());



                // Mostrar cada árbol si aún no se ha dibujado
                ArbolGeneradorVisual.dibujarGrafoCompleto(arbol1, panelArbol1);
                ArbolGeneradorVisual.dibujarGrafoCompleto(arbol2, panelArbol2);

                GrafoPonderado grafoBase = construirGrafoUnion(arbol1, arbol2);

                ArbolDistanciaVisual.dibujarComparacion(
                        grafoBase,
                        arbol1.getAristas(),
                        arbol2.getAristas(),
                        panelResultado
                );

                infoResultado.setText("Resultado / Comparación");

                double distancia = DistanciaArbolesService.calcularDistancia(
                        arbol1.getAristas(),
                        arbol2.getAristas()
                );

                Set<String> comunes = DistanciaArbolesService.obtenerComunes(
                        arbol1.getAristas(), arbol2.getAristas()
                );

                Set<String> solo1 = DistanciaArbolesService.obtenerSoloArbol1(
                        arbol1.getAristas(), arbol2.getAristas()
                );

                Set<String> solo2 = DistanciaArbolesService.obtenerSoloArbol2(
                        arbol1.getAristas(), arbol2.getAristas()
                );
                
                int rango = DistanciaArbolesService.calcularRango(arbol2.getVertices());

                int nulidad = DistanciaArbolesService.calcularNulidad(
                        arbol2.getVertices(),
                        arbol2.getAristas()
                );

                StringBuilder sb = new StringBuilder();
                sb.append("Comparación de árboles\n\n");

                sb.append("Aristas del Árbol 1:\n");
                for (AristaPonderada a : arbol1.getAristas()) {
                    sb.append(a).append("\n");
                }

                sb.append("\nAristas del Árbol 2:\n");
                for (AristaPonderada a : arbol2.getAristas()) {
                    sb.append(a).append("\n");
                }

                sb.append("\nAristas comunes (IDs): ").append(comunes).append("\n");
                sb.append("Solo en Árbol 1 (IDs): ").append(solo1).append("\n");
                sb.append("Solo en Árbol 2 (IDs): ").append(solo2).append("\n");
                sb.append("\nRango = ").append(rango);
                sb.append("\nNulidad = ").append(nulidad);
                sb.append("\nDistancia = ").append(distancia);

                infoArea.setText(sb.toString());

            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage());
            }
        }

        @FXML
private void guardarEstructura() {
    try {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar distancia entre árboles");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Distancia entre Árboles (*.arb)", "*.arb")
        );

        File file = fc.showSaveDialog(panelResultado.getScene().getWindow());
        if (file == null) return;

        ArchivoEstructuraService.guardarDistanciaArboles(
                file,
                verticesField1.getText().trim(),
                aristasField1.getText().trim(),
                verticesField2.getText().trim(),
                aristasField2.getText().trim()
        );

        infoArea.setText("Estructura guardada correctamente: " + file.getName());

    } catch (Exception e) {
        mostrarAlerta("Error", "Error guardando: " + e.getMessage());
    }
}

        @FXML
private void cargarEstructura() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Cargar distancia entre árboles");
    fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Estructuras (*.arb, *.gra)", "*.arb", "*.gra")
    );

    File file = fc.showOpenDialog(panelResultado.getScene().getWindow());
    if (file == null) return;

    try {
        DatosArchivo datos = ArchivoEstructuraService.cargarArchivo(file);

        if ("DISTANCIA_ARBOLES".equals(datos.getTipo())) {

            verticesField1.setText(datos.getVertices1());
            aristasField1.setText(datos.getAristas1());
            verticesField2.setText(datos.getVertices2());
            aristasField2.setText(datos.getAristas2());

        } else if ("ARBOL".equals(datos.getTipo())) {

            Set<String> vertices = new LinkedHashSet<>();
            StringBuilder aristas = new StringBuilder();

            if (datos.getRaiz() != null && !datos.getRaiz().isEmpty()) {
                vertices.add(datos.getRaiz());
            }

            for (String[] relacion : datos.getRelaciones()) {
                String padre = relacion[0].trim();
                String hijo = relacion[1].trim();

                vertices.add(padre);
                vertices.add(hijo);

                if (aristas.length() > 0) {
                    aristas.append(", ");
                }

                aristas.append(padre)
                        .append("-")
                        .append(hijo);
            }

            verticesField1.setText(String.join(",", vertices));
            aristasField1.setText(aristas.toString());

            verticesField2.clear();
            aristasField2.clear();

        } else if ("GRAFO_GENERADOR".equals(datos.getTipo())) {

            StringBuilder aristas = new StringBuilder();

            for (AristaPonderada a : datos.getAristas()) {
                if (aristas.length() > 0) {
                    aristas.append(", ");
                }

                aristas.append(a.getOrigen())
                        .append("-")
                        .append(a.getDestino());
            }

            verticesField1.setText(String.join(",", datos.getVertices()));
            aristasField1.setText(aristas.toString());

            verticesField2.clear();
            aristasField2.clear();

        } else {
            mostrarAlerta("Error", "Este archivo no se puede cargar en distancia entre árboles.");
            return;
        }

        arbol1 = null;
        arbol2 = null;

        panelArbol1.getChildren().clear();
        panelArbol2.getChildren().clear();
        panelResultado.getChildren().clear();

        infoArbol1.setText("Árbol 1");
        infoArbol2.setText("Árbol 2");
        infoResultado.setText("Resultado / Comparación");

        infoArea.setText("Estructura cargada correctamente: " + file.getName());

    } catch (Exception e) {
        mostrarAlerta("Error", "Error cargando: " + e.getMessage());
    }
}

        @FXML
        private void limpiarEstructura() {
            arbol1 = null;
            arbol2 = null;

            verticesField1.clear();
            aristasField1.clear();
            verticesField2.clear();
            aristasField2.clear();

            infoArbol1.setText("Árbol 1");
            infoArbol2.setText("Árbol 2");
            infoResultado.setText("Resultado / Comparación");

            infoArea.setText("Estructura limpiada.");

            panelArbol1.getChildren().clear();
            panelArbol2.getChildren().clear();
            panelResultado.getChildren().clear();
        }

        private GrafoPonderado construirArbol(String textoVertices, String textoAristas) {
            GrafoPonderado g = new GrafoPonderado();

            textoVertices = textoVertices.trim();
            if (textoVertices.isEmpty()) {
                throw new IllegalArgumentException("Debes ingresar los vértices.");
            }

            String[] vertices = textoVertices.split(",");
            for (String v : vertices) {
                String limpio = v.trim();
                if (!limpio.isEmpty()) {
                    g.agregarVertice(limpio);
                }
            }

            textoAristas = textoAristas.trim();
            if (textoAristas.isEmpty()) {
                throw new IllegalArgumentException("Debes ingresar las aristas.");
            }

            String[] aristas = textoAristas.split(",");
            for (String a : aristas) {
                String[] partes = a.trim().split("-");

                if (partes.length != 3) {
                    throw new IllegalArgumentException(
                            "Formato inválido en arista: " + a + ". Usa origen-destino-id"
                    );
                }

                String origen = partes[0].trim();
                String destino = partes[1].trim();
                int id;

                try {
                    id = Integer.parseInt(partes[2].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("ID inválido en la arista: " + a);
                }

                g.agregarArista(origen, destino, id);
            }

            return g;
        }



        private GrafoPonderado construirGrafoUnion(GrafoPonderado a1, GrafoPonderado a2) {
        GrafoPonderado g = new GrafoPonderado();

        // Agregar vértices del árbol 1
        for (String v : a1.getVertices()) {
            g.agregarVertice(v);
        }

        // Agregar vértices del árbol 2
        for (String v : a2.getVertices()) {
            if (!g.getVertices().contains(v)) {
                g.agregarVertice(v);
            }
        }

        // Agregar aristas del árbol 1
        for (AristaPonderada a : a1.getAristas()) {
            g.agregarArista(a.getOrigen(), a.getDestino(), a.getPeso());
        }

        // Agregar aristas del árbol 2 si no existe ya una con el mismo ID
        for (AristaPonderada a : a2.getAristas()) {
            boolean existe = false;

            for (AristaPonderada b : g.getAristas()) {
                if (b.getPeso() == a.getPeso()) {
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                g.agregarArista(a.getOrigen(), a.getDestino(), a.getPeso());
            }
        }

        return g;
    }

        private void mostrarAlerta(String titulo, String mensaje) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
        
        
    }