package jogo.modelo;

import jogo.Config;

public class Personagem {
    private final String nome;
    private int coragem;
    private int coragemMaxima;
    private int bonusPoder;
    private final Ferramenta[] ferramentas;
    private int vitorias;

    public Personagem(String nome) {
        this.nome = ValidadorNome.validar(nome, "O nome deve ter de 1 a "
                + Config.TAMANHO_MAXIMO_NOME + " caracteres.");
        coragem = Config.CORAGEM_INICIAL;
        coragemMaxima = Config.CORAGEM_INICIAL;
        ferramentas = new Ferramenta[Config.QUANTIDADE_FERRAMENTAS];
        criarFerramenta(TipoFerramenta.FLAUTA, Config.PODER_FLAUTA,
                Config.USOS_ILIMITADOS, Config.USOS_ILIMITADOS);
        criarFerramenta(TipoFerramenta.ISCA, Config.PODER_ISCA,
                Config.USOS_ISCA_POR_BATALHA, Config.USOS_ISCA_POR_BATALHA);
        criarFerramenta(TipoFerramenta.LANTERNA, Config.PODER_LANTERNA,
                Config.USOS_ILIMITADOS, Config.USOS_ILIMITADOS);
        criarFerramenta(TipoFerramenta.REPELENTE, Config.PODER_REPELENTE,
                Config.USOS_REPELENTE_POR_BATALHA, Config.USOS_REPELENTE_POR_BATALHA);
        // Zero impede a recarga do estoque de dardos entre batalhas.
        criarFerramenta(TipoFerramenta.DARDO, Config.PODER_DARDO, Config.DARDOS_INICIAIS, 0);
    }

    private void criarFerramenta(TipoFerramenta tipo, int poder, int usos, int usosPorBatalha) {
        ferramentas[tipo.ordinal()] = new Ferramenta(tipo.getNomeExibicao(), poder,
                usos, usosPorBatalha, tipo);
    }

    public String getNome() {
        return nome;
    }

    public int getCoragem() {
        return coragem;
    }

    public int getCoragemMaxima() {
        return coragemMaxima;
    }

    public int getBonusPoder() {
        return bonusPoder;
    }

    public Ferramenta[] getFerramentas() {
        return ferramentas;
    }

    public int getVitorias() {
        return vitorias;
    }

    public int usarFerramenta(int indice, Animal alvo, TabelaEfetividade tabela) {
        if (indice < 0 || indice >= ferramentas.length || !ferramentas[indice].temUsos()) {
            return 0;
        }
        Ferramenta ferramenta = ferramentas[indice];
        double multiplicador = tabela.getMultiplicador(ferramenta.getTipo(), alvo.getTipo());
        int dano = multiplicador > 0
                ? Math.max(1, (int) ((ferramenta.getPoder() + bonusPoder) * multiplicador)) : 0;
        ferramenta.usar();
        alvo.receberCalma(dano);
        return dano;
    }

    public void receberSusto(int dano) {
        coragem = Math.max(0, coragem - Math.max(0, dano));
    }

    public boolean temCoragem() {
        return coragem > 0;
    }

    public int evoluir() {
        int coragemAnterior = coragem;
        vitorias++;
        coragemMaxima += Config.AUMENTO_CORAGEM_MAXIMA;
        coragem = Math.min(coragem + Config.RECUPERACAO_CORAGEM, coragemMaxima);
        bonusPoder += Config.AUMENTO_BONUS_PODER;
        if (vitorias % Config.VITORIAS_POR_DARDO == 0) {
            ferramentas[TipoFerramenta.DARDO.ordinal()].recuperarUso(Config.MAXIMO_DARDOS);
        }
        return coragem - coragemAnterior;
    }

    public void prepararParaBatalha() {
        for (int indice = 0; indice < ferramentas.length; indice++) {
            ferramentas[indice].recarregar();
        }
    }
}
