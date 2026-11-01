package jogo.modelo;

public enum TipoFerramenta {
    FLAUTA("Flauta calmante"),
    ISCA("Isca de comida"),
    LANTERNA("Lanterna"),
    REPELENTE("Repelente"),
    DARDO("Dardo de sono");

    private final String nomeExibicao;

    TipoFerramenta(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }
}
