package utilities;

import java.util.List;
import java.util.Map;

public class ResultadoDistanciaVertices {

    private int[][] matrizDistancias;
    private Map<String, Integer> excentricidades;
    private int radio;
    private int diametro;
    private List<String> centro;

    public ResultadoDistanciaVertices(
            int[][] matrizDistancias,
            Map<String, Integer> excentricidades,
            int radio,
            int diametro,
            List<String> centro) {

        this.matrizDistancias = matrizDistancias;
        this.excentricidades = excentricidades;
        this.radio = radio;
        this.diametro = diametro;
        this.centro = centro;
    }

    public int[][] getMatrizDistancias() {
        return matrizDistancias;
    }

    public Map<String, Integer> getExcentricidades() {
        return excentricidades;
    }

    public int getRadio() {
        return radio;
    }

    public int getDiametro() {
        return diametro;
    }

    public List<String> getCentro() {
        return centro;
    }
}