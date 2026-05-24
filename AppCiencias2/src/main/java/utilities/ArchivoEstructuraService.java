package utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import utilities.GrafoPonderado;
import utilities.AristaPonderada;

public class ArchivoEstructuraService {

    public static void guardarArbolSimple(
            File file,
            String nombreRaiz,
            List<String[]> relaciones
    ) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8))) {

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
        }
    }

    public static void guardarGrafoGenerador(
            File file,
            GrafoPonderado grafo
    ) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8))) {

            bw.write("TIPO=GRAFO_GENERADOR");
            bw.newLine();

            bw.write("VERTICES=");
            bw.write(String.join(",", grafo.getVertices()));
            bw.newLine();

            bw.write("ARISTAS");
            bw.newLine();

            for (AristaPonderada a : grafo.getAristas()) {
                bw.write(a.getOrigen() + "|" +
                         a.getDestino() + "|" +
                         a.getPeso());

                bw.newLine();
            }

            bw.write("END");
            bw.newLine();
        }
    }

    public static void guardarDistanciaArboles(
            File file,
            String vertices1,
            String aristas1,
            String vertices2,
            String aristas2
    ) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8))) {

            bw.write("TIPO=DISTANCIA_ARBOLES");
            bw.newLine();

            bw.write("VERTICES1=" + vertices1);
            bw.newLine();

            bw.write("ARISTAS1=" + aristas1);
            bw.newLine();

            bw.write("VERTICES2=" + vertices2);
            bw.newLine();

            bw.write("ARISTAS2=" + aristas2);
            bw.newLine();

            bw.write("END");
            bw.newLine();
        }
    }
    public static void guardarGrafoOrdinal(
        File file,
        List<String[]> vertices,
        List<String[]> aristas
) throws IOException {

    try (BufferedWriter bw = new BufferedWriter(
            new OutputStreamWriter(
                    new FileOutputStream(file),
                    StandardCharsets.UTF_8))) {

        bw.write("TIPO=GRAFO_ORDINAL");
        bw.newLine();

        bw.write("VERTICES");
        bw.newLine();

        for (String[] vertice : vertices) {
            bw.write(vertice[0] + "|" + vertice[1] + "|" + vertice[2]);
            bw.newLine();
        }

        bw.write("ARISTAS");
        bw.newLine();

        for (String[] arista : aristas) {
            bw.write(arista[0] + "|" + arista[1]);
            bw.newLine();
        }

        bw.write("END");
        bw.newLine();
    }
}
    public static void guardarGrafoCaminos(
        File file,
        GrafoCamino grafo
) throws IOException {

    try (BufferedWriter bw = new BufferedWriter(
            new OutputStreamWriter(
                    new FileOutputStream(file),
                    StandardCharsets.UTF_8))) {

        bw.write("TIPO=GRAFO_CAMINOS");
        bw.newLine();

        bw.write("VERTICES=");

        StringBuilder verticesTexto = new StringBuilder();

        for (int i = 0; i < grafo.getVertices().size(); i++) {

            if (i > 0) {
                verticesTexto.append(",");
            }

            verticesTexto.append(
                    grafo.getVertices().get(i).getNombre()
            );
        }

        bw.write(verticesTexto.toString());
        bw.newLine();

        bw.write("ARISTAS");
        bw.newLine();

        for (AristaCamino a : grafo.getAristas()) {

            bw.write(
                    a.getOrigen()
                    + "|"
                    + a.getDestino()
                    + "|"
                    + a.getPeso()
            );

            bw.newLine();
        }

        bw.write("END");
        bw.newLine();
    }
}
    
    public static DatosArchivo cargarArchivo(File file) throws IOException {

    DatosArchivo datos = new DatosArchivo();

    try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

        String linea;

        boolean leyendoRelaciones = false;
        boolean leyendoAristas = false;
        boolean leyendoVerticesSeccion = false;

        while ((linea = br.readLine()) != null) {

            linea = linea.trim();

            if (linea.isEmpty()) {
                continue;
            }

            if (linea.startsWith("TIPO=")) {
                datos.setTipo(linea.substring("TIPO=".length()).trim());
            }

            else if (linea.startsWith("RAIZ=")) {
                datos.setRaiz(linea.substring("RAIZ=".length()).trim());
            }

            else if (linea.equals("RELACIONES")) {
                leyendoRelaciones = true;
                leyendoAristas = false;
                leyendoVerticesSeccion = false;
            }

            else if (linea.equals("VERTICES")) {
                leyendoVerticesSeccion = true;
                leyendoRelaciones = false;
                leyendoAristas = false;
            }

            else if (linea.startsWith("VERTICES=")) {
                String contenido = linea.substring("VERTICES=".length()).trim();

                if (!contenido.isEmpty()) {
                    String[] vertices = contenido.split(",");

                    for (String v : vertices) {
                        datos.getVertices().add(v.trim());
                    }
                }
            }

            else if (linea.equals("ARISTAS")) {
                leyendoAristas = true;
                leyendoRelaciones = false;
                leyendoVerticesSeccion = false;
            }

            else if (linea.startsWith("VERTICES1=")) {
                datos.setVertices1(linea.substring("VERTICES1=".length()).trim());
            }

            else if (linea.startsWith("ARISTAS1=")) {
                datos.setAristas1(linea.substring("ARISTAS1=".length()).trim());
            }

            else if (linea.startsWith("VERTICES2=")) {
                datos.setVertices2(linea.substring("VERTICES2=".length()).trim());
            }

            else if (linea.startsWith("ARISTAS2=")) {
                datos.setAristas2(linea.substring("ARISTAS2=".length()).trim());
            }

            else if (linea.equals("END")) {
                break;
            }

            else {

                if (leyendoVerticesSeccion) {
                    String[] partes = linea.split("\\|");

                    if (partes.length >= 1) {
                        String nombre = partes[0].trim();

                        if (!nombre.isEmpty()) {
                            datos.getVertices().add(nombre);
                        }
                    }
                }

                else if (leyendoRelaciones) {
                    String[] partes = linea.split("\\|");

                    if (partes.length >= 2) {
                        datos.getRelaciones().add(new String[]{
                                partes[0].trim(),
                                partes[1].trim()
                        });
                    }
                }

                else if (leyendoAristas) {
                    String[] partes = linea.split("\\|");

                    if (partes.length >= 3) {
                        String origen = partes[0].trim();
                        String destino = partes[1].trim();
                        int peso = Integer.parseInt(partes[2].trim());

                        datos.getAristas().add(new AristaPonderada(origen, destino, peso));
                    }

                    else if (partes.length == 2) {
                        datos.getRelaciones().add(new String[]{
                                partes[0].trim(),
                                partes[1].trim()
                        });
                    }
                }
            }
        }
    }

    return datos;
}
}