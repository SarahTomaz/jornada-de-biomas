package jogo.modelo;

public class Bioma {
    private final String nome;
    private final String descricao;
    private final TipoAnimal tipoPredominante;
    private final String cenario;

    public Bioma(String nome, String descricao, TipoAnimal tipoPredominante, String cenario) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipoPredominante = tipoPredominante;
        this.cenario = cenario;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoAnimal getTipoPredominante() {
        return tipoPredominante;
    }

    public String getCenario() {
        return cenario;
    }
}
