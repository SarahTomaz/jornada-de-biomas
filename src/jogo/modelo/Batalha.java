package jogo.modelo;

import java.util.Random;
import jogo.Config;

public final class Batalha {
    private final Personagem jogador;
    private final Animal inimigo;
    private final TabelaEfetividade tabela;
    private final Fase fase;
    private final int ciclo;
    private final Random sorteador;
    private final boolean[][] descobertas;
    private int turno;
    private boolean iniciada;
    private boolean terminou;
    private boolean venceu;
    private boolean desistiu;

    public Batalha(Personagem jogador, Animal inimigo, TabelaEfetividade tabela,
            Fase fase, int ciclo) {
        this(jogador, inimigo, tabela, fase, ciclo, new Random());
    }

    public Batalha(Personagem jogador, Animal inimigo, TabelaEfetividade tabela,
            Fase fase, int ciclo, Random sorteador) {
        this(jogador, inimigo, tabela, fase, ciclo, sorteador, null);
    }

    public Batalha(Personagem jogador, Animal inimigo, TabelaEfetividade tabela,
            Fase fase, int ciclo, Random sorteador, boolean[][] descobertas) {
        if (jogador == null || inimigo == null || tabela == null || fase == null
                || sorteador == null || ciclo < 0) {
            throw new IllegalArgumentException("Dados da batalha inválidos.");
        }
        if (descobertas != null) {
            if (descobertas.length != Config.QUANTIDADE_FERRAMENTAS) {
                throw new IllegalArgumentException("A matriz de descobertas deve ter cinco linhas.");
            }
            for (int indice = 0; indice < descobertas.length; indice++) {
                if (descobertas[indice] == null
                        || descobertas[indice].length != TipoAnimal.values().length) {
                    throw new IllegalArgumentException("Cada linha de descobertas deve ter quatro colunas.");
                }
            }
        }
        this.jogador = jogador;
        this.inimigo = inimigo;
        this.tabela = tabela;
        this.fase = fase;
        this.ciclo = ciclo;
        this.sorteador = sorteador;
        this.descobertas = descobertas;
    }

    public void iniciar() {
        if (iniciada) {
            return;
        }
        jogador.prepararParaBatalha();
        turno = 1;
        iniciada = true;
    }

    public ResultadoTurno jogarTurno(int indiceFerramenta) {
        if (!iniciada || terminou || indiceFerramenta < 0
                || indiceFerramenta >= Config.QUANTIDADE_FERRAMENTAS) {
            return null;
        }
        Ferramenta ferramenta = jogador.getFerramentas()[indiceFerramenta];
        if (!ferramenta.temUsos()) {
            return null;
        }

        double multiplicador = tabela.getMultiplicador(ferramenta.getTipo(), inimigo.getTipo());
        int danoCausado = jogador.usarFerramenta(indiceFerramenta, inimigo, tabela);
        registrarDescoberta(ferramenta, multiplicador);
        boolean contraefeito = multiplicador == 0;
        int danoContraefeito = calcularContraefeito(contraefeito);
        if (contraefeito) {
            jogador.receberSusto(danoContraefeito);
        }
        AtaqueAnimal ataqueUsado = null;
        int danoSofrido = 0;
        venceu = inimigo.estaCalmo();
        if (!venceu && jogador.temCoragem()) {
            ataqueUsado = inimigo.escolherAtaque(turno, sorteador);
            danoSofrido = ataqueUsado.getDanoCoragem();
            jogador.receberSusto(danoSofrido);
        }
        terminou = venceu || !jogador.temCoragem();
        turno++;

        String mensagemEfetividade = montarMensagemEfetividade(multiplicador, danoContraefeito);
        return new ResultadoTurno(ferramenta.getNome(), danoCausado, multiplicador,
                mensagemEfetividade, venceu, ataqueUsado, danoSofrido, !jogador.temCoragem(),
                contraefeito, danoContraefeito);
    }

    private void registrarDescoberta(Ferramenta ferramenta, double multiplicador) {
        if (descobertas != null
                && (multiplicador == 0 || multiplicador >= Config.MULTIPLICADOR_MUITO_EFICAZ)) {
            descobertas[ferramenta.getTipo().ordinal()][inimigo.getTipo().ordinal()] = true;
        }
    }

    private int calcularContraefeito(boolean contraefeito) {
        return contraefeito ? (int) Math.round(Config.CONTRAEFEITO_BASE
                * (1 + Config.CONTRAEFEITO_POR_CICLO * ciclo)) : 0;
    }

    private String montarMensagemEfetividade(double multiplicador, int danoContraefeito) {
        String mensagemEfetividade;
        if (multiplicador == 0) {
            mensagemEfetividade = "Não funcionou... " + inimigo.getNome()
                    + " não gostou e ficou irritado! (-" + danoContraefeito + " Coragem)";
        } else if (multiplicador >= Config.MULTIPLICADOR_MUITO_EFICAZ) {
            mensagemEfetividade = "Foi muito eficaz!";
        } else if (multiplicador == Config.MULTIPLICADOR_POUCO_EFICAZ) {
            mensagemEfetividade = "Quase não fez efeito...";
        } else {
            mensagemEfetividade = "Funcionou.";
        }
        return mensagemEfetividade;
    }

    public ResultadoTurno desistir() {
        if (!iniciada || terminou) {
            return null;
        }
        desistiu = true;
        terminou = true;
        return new ResultadoTurno();
    }

    public RegistroBatalha gerarRegistro() {
        if (!terminou) {
            throw new IllegalStateException("A batalha ainda não terminou.");
        }
        return new RegistroBatalha(inimigo.getNome(), fase.getBioma().getNome(), ciclo + 1, venceu);
    }

    public boolean terminou() { return terminou; }
    public boolean venceu() { return venceu; }
    public boolean desistiu() { return desistiu; }
    public int getTurno() { return turno; }
    public Personagem getJogador() { return jogador; }
    public Animal getInimigo() { return inimigo; }
    public Fase getFase() { return fase; }
    public int getCiclo() { return ciclo; }
}
