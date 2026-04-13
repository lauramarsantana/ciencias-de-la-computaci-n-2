package utilities;

// Estructura básica para el Nodo de Huffman
public class NodoHuffman implements Comparable<NodoHuffman> {
    public char letra;
    public int frecuencia;
    public NodoHuffman izquierdo, derecho;

    // Este es el constructor que te falta:
    public NodoHuffman(char letra, int frecuencia) {
        this.letra = letra;
        this.frecuencia = frecuencia;
    }

    @Override
    public int compareTo(NodoHuffman otro) {
        return Integer.compare(this.frecuencia, otro.frecuencia);
    }
}