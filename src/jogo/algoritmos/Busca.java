package jogo.algoritmos;

import jogo.modelo.RegistroRanking;

/**
 * Buscas sequencial por nome e binária por nível no vetor do ranking.
 */
public final class Busca {
    private Busca() {
    }

    public static int sequencialPorNome(RegistroRanking[] vetor, int quantidade, String nome) {
        for (int indice = 0; indice < quantidade; indice++) {
            if (vetor[indice].getNome().equalsIgnoreCase(nome)) {
                return indice;
            }
        }
        return -1;
    }

    public static int binariaPorNivel(RegistroRanking[] vetor, int quantidade, int nivel) {
        int inicio = 0;
        int fim = quantidade - 1;
        while (inicio <= fim) {
            int meio = inicio + (fim - inicio) / 2;
            int nivelAtual = vetor[meio].getNivelAlcancado();
            if (nivelAtual == nivel) {
                // Recua entre os empates para devolver a primeira posição.
                while (meio > 0 && vetor[meio - 1].getNivelAlcancado() == nivel) {
                    meio--;
                }
                return meio;
            }
            // O vetor é decrescente: níveis maiores ficam à esquerda.
            if (nivelAtual < nivel) {
                fim = meio - 1;
            } else {
                inicio = meio + 1;
            }
        }
        return -1;
    }
}
