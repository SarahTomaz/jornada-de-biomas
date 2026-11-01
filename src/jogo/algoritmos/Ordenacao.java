package jogo.algoritmos;

/**
 * Insertion sort decrescente para vetores de elementos comparáveis.
 */
public final class Ordenacao {
    private Ordenacao() {
    }

    public static <T extends Comparable<T>> void insertionSortDecrescente(
            T[] vetor, int quantidade) {
        for (int indice = 1; indice < quantidade; indice++) {
            T escolhido = vetor[indice];
            int anterior = indice - 1;
            // Só desloca valores menores; os empates mantêm sua ordem.
            while (anterior >= 0 && vetor[anterior].compareTo(escolhido) < 0) {
                vetor[anterior + 1] = vetor[anterior];
                anterior--;
            }
            vetor[anterior + 1] = escolhido;
        }
    }
}
