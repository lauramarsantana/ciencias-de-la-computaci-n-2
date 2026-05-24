package utilities;

import java.util.List;
import java.util.Map;

public class ResultadoCaminoMinimo {

    private List<String> pasos;
    private Map<String, Integer> distancias;
    private Map<String, String> predecesores;
    private int[][] matriz;
    private boolean hayCicloNegativo;

    public ResultadoCaminoMinimo(List<String> pasos,
                                 Map<String, Integer> distancias,
                                 Map<String, String> predecesores,
                                 int[][] matriz,
                                 boolean hayCicloNegativo) {
        this.pasos = pasos;
        this.distancias = distancias;
        this.predecesores = predecesores;
        this.matriz = matriz;
        this.hayCicloNegativo = hayCicloNegativo;
    }

    public List<String> getPasos() {
        return pasos;
    }

    public Map<String, Integer> getDistancias() {
        return distancias;
    }

    public Map<String, String> getPredecesores() {
        return predecesores;
    }

    public int[][] getMatriz() {
        return matriz;
    }

    public boolean isHayCicloNegativo() {
        return hayCicloNegativo;
    }
}