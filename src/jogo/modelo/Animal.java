package jogo.modelo;

import java.util.Random;
import jogo.Config;

public class Animal {
    private final String nome;
    private final TipoAnimal tipo;
    private int agitacao;
    private int agitacaoMaxima;
    private final AtaqueAnimal[] ataques;
    private final String sprite;
    private final int agitacaoBase;
    private final AtaqueAnimal[] ataquesBase;

    public Animal(String nome, TipoAnimal tipo, int agitacao,
            AtaqueAnimal[] ataques, String sprite) {
        if (ataques == null || ataques.length != Config.QUANTIDADE_ATAQUES_ANIMAL) {
            throw new IllegalArgumentException("O animal deve ter exatamente três ataques.");
        }
        this.nome = nome;
        this.tipo = tipo;
        this.agitacao = agitacao;
        agitacaoMaxima = agitacao;
        agitacaoBase = agitacao;
        this.sprite = sprite;
        this.ataques = new AtaqueAnimal[ataques.length];
        ataquesBase = new AtaqueAnimal[ataques.length];
        for (int indice = 0; indice < ataques.length; indice++) {
            ataquesBase[indice] = new AtaqueAnimal(
                    ataques[indice].getNome(), ataques[indice].getDanoCoragem());
            this.ataques[indice] = ataquesBase[indice].copiaEscalada(0);
        }
    }

    public String getNome() {
        return nome;
    }

    public TipoAnimal getTipo() {
        return tipo;
    }

    public int getAgitacao() {
        return agitacao;
    }

    public int getAgitacaoMaxima() {
        return agitacaoMaxima;
    }

    public AtaqueAnimal[] getAtaques() {
        return ataques.clone();
    }

    public String getSprite() {
        return sprite;
    }

    public void escalar(int ciclo) {
        agitacaoMaxima = Math.max(1,
                (int) Math.round(agitacaoBase * (1 + Config.CRESCIMENTO_AGITACAO * ciclo)));
        agitacao = agitacaoMaxima;
        for (int indice = 0; indice < ataques.length; indice++) {
            ataques[indice] = ataquesBase[indice].copiaEscalada(ciclo);
        }
    }

    public AtaqueAnimal escolherAtaque(int turno, Random sorteador) {
        if (turno % Config.INTERVALO_ESPECIAL == 0) {
            return ataques[Config.INDICE_ATAQUE_ESPECIAL];
        }
        return ataques[sorteador.nextInt(Config.QUANTIDADE_ATAQUES_COMUNS)];
    }

    public void receberCalma(int dano) {
        agitacao = Math.max(0, agitacao - Math.max(0, dano));
    }

    public boolean estaCalmo() {
        return agitacao == 0;
    }
}
