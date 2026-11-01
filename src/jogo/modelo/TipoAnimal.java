package jogo.modelo;

public enum TipoAnimal {
    MAMIFERO("Mamífero"),
    REPTIL("Réptil"),
    AVE("Ave"),
    ARACNIDEO("Aracnídeo");

    private final String nomeExibicao;

    TipoAnimal(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }
}
