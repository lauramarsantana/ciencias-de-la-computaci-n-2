package utilities;

public class CodigoLetra {
    private final String letra;
    private final int valorAlfabeto;
    private final String binario;

    public CodigoLetra(String letra, int valorAlfabeto, String binario) {
        this.letra = letra;
        this.valorAlfabeto = valorAlfabeto;
        this.binario = binario;
    }

    public String getLetra() { return letra; }
    public int getValorAlfabeto() { return valorAlfabeto; }
    public String getBinario() { return binario; }
}
