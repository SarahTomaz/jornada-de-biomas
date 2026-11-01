package jogo.modelo;

import jogo.Config;

/** Normalização e limite de nome compartilhados pelo modelo e pela partida. */
public final class ValidadorNome {
    private ValidadorNome() { }

    public static String validar(String nome, String mensagemTamanho) {
        if (nome == null || nome.trim().isEmpty()
                || nome.trim().length() > Config.TAMANHO_MAXIMO_NOME) {
            throw new IllegalArgumentException(mensagemTamanho);
        }
        return nome.trim();
    }
}
