package jogo.modelo;

public class RegistroRanking implements Comparable<RegistroRanking> {
    private final String nome;
    private final int nivelAlcancado;
    private final int ciclo;
    private final String data;

    public RegistroRanking(String nome, int nivelAlcancado, int ciclo, String data) {
        this.nome = nome;
        this.nivelAlcancado = nivelAlcancado;
        this.ciclo = ciclo;
        this.data = data;
    }

    public String getNome() {
        return nome;
    }

    public int getNivelAlcancado() {
        return nivelAlcancado;
    }

    public int getCiclo() {
        return ciclo;
    }

    public String getData() {
        return data;
    }

    @Override
    public int compareTo(RegistroRanking outro) {
        return Integer.compare(nivelAlcancado, outro.nivelAlcancado);
    }
}
