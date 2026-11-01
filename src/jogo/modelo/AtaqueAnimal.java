package jogo.modelo;

import jogo.Config;

public class AtaqueAnimal {
    private final String nome;
    private final int danoCoragem;

    public AtaqueAnimal(String nome, int danoCoragem) {
        this.nome = nome;
        this.danoCoragem = danoCoragem;
    }

    public String getNome() {
        return nome;
    }

    public int getDanoCoragem() {
        return danoCoragem;
    }

    public AtaqueAnimal copiaEscalada(int ciclo) {
        int danoEscalado = Math.max(1,
                (int) Math.round(danoCoragem * (1 + Config.CRESCIMENTO_DANO * ciclo)));
        return new AtaqueAnimal(nome, danoEscalado);
    }
}
