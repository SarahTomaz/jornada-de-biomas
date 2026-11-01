package jogo.modelo;

import jogo.Config;

public class Ferramenta {
    private final String nome;
    private final int poder;
    private int usosRestantes;
    private final int usosPorBatalha;
    private final TipoFerramenta tipo;

    public Ferramenta(String nome, int poder, int usosRestantes,
            int usosPorBatalha, TipoFerramenta tipo) {
        this.nome = nome;
        this.poder = poder;
        this.usosRestantes = usosRestantes;
        this.usosPorBatalha = usosPorBatalha;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public int getPoder() {
        return poder;
    }

    public int getUsosRestantes() {
        return usosRestantes;
    }

    public TipoFerramenta getTipo() {
        return tipo;
    }

    public void usar() {
        if (usosRestantes > 0) {
            usosRestantes--;
        }
    }

    public boolean temUsos() {
        return usosRestantes == Config.USOS_ILIMITADOS || usosRestantes > 0;
    }

    public void recarregar() {
        if (usosPorBatalha > 0) {
            usosRestantes = usosPorBatalha;
        }
    }

    public void recuperarUso(int limite) {
        if (usosRestantes >= 0 && usosRestantes < limite) {
            usosRestantes++;
        }
    }
}
