package jogo.modelo;

import jogo.Config;
import jogo.algoritmos.Busca;
import jogo.algoritmos.Ordenacao;

/**
 * Ranking dos melhores registros em um vetor de tamanho limitado.
 * Cumpre o checklist de vetor e usa ordenação e busca na tela de ranking.
 */
public class Ranking {
    private final RegistroRanking[] registros;
    private int quantidade;

    public Ranking() {
        registros = new RegistroRanking[Config.TAMANHO_RANKING];
    }

    public void adicionar(RegistroRanking novo) {
        if (quantidade < registros.length) {
            registros[quantidade] = novo;
            quantidade++;
        } else if (novo.getNivelAlcancado()
                > registros[quantidade - 1].getNivelAlcancado()) {
            registros[quantidade - 1] = novo;
        } else {
            return;
        }
        Ordenacao.insertionSortDecrescente(registros, quantidade);
    }

    public int buscarPorNome(String nome) {
        return Busca.sequencialPorNome(registros, quantidade, nome);
    }

    public int buscarPorNivel(int nivel) {
        return Busca.binariaPorNivel(registros, quantidade, nivel);
    }

    public int posicaoDe(RegistroRanking registro) {
        for (int indice = 0; indice < quantidade; indice++) {
            if (registros[indice] == registro) {
                return indice + 1;
            }
        }
        return -1;
    }

    public RegistroRanking[] getRegistros() {
        return registros;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
