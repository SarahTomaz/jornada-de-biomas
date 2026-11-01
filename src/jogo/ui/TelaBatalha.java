package jogo.ui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import jogo.Config;
import jogo.Jogo;
import jogo.modelo.Batalha;
import jogo.modelo.Ferramenta;
import jogo.modelo.ResultadoTurno;
import jogo.modelo.TabelaEfetividade;

public final class TelaBatalha implements Tela {
    // Somente ESCOLHENDO aceita ferramentas; os demais estados apresentam o resultado.
    public enum Estado {
        ESCOLHENDO, ANIMANDO_JOGADOR, ANIMANDO_ANIMAL, VITORIA, DERROTA
    }

    private static final String[] NOMES_FERRAMENTAS = {
        "Flauta", "Isca", "Lanterna", "Repelente", "Dardo"
    };
    private final PainelJogo painel;
    private final Recursos recursos;
    private final Jogo jogo;
    private final Runnable sair;
    private final TabelaEfetividade tabelaEfetividade = new TabelaEfetividade();
    private final Botao[] botoes = new Botao[6];
    private final Botao sim;
    private final Botao nao;
    private final CaixaDialogo dialogo;
    private final boolean[] teclasPressionadas = new boolean[256];
    private Batalha batalha;
    private BarraStatus barraCoragem;
    private BarraStatus barraAgitacao;
    private VisualBatalha visual;
    private Estado estado = Estado.ESCOLHENDO;
    private ResultadoTurno resultado;
    private int indiceFerramenta = -1;
    private int ferramentaSobMouse = -1;
    private int coragemAntesDoTurno;
    private double tempoEtapa;
    private double tempoTotal;
    private double ultimoClique = Double.NEGATIVE_INFINITY;
    private boolean inicializada;
    private boolean confirmaDesistencia;
    private boolean avancoSolicitado;
    private boolean resultadoRegistrado;
    private boolean encaminhou;
    private boolean animandoContraefeito;

    public TelaBatalha(PainelJogo painel, Recursos recursos, Jogo jogo, Runnable sair) {
        if (painel == null || recursos == null || jogo == null || sair == null) {
            throw new IllegalArgumentException("Os dados da tela de batalha são obrigatórios.");
        }
        this.painel = painel;
        this.recursos = recursos;
        this.jogo = jogo;
        this.sair = sair;
        dialogo = new CaixaDialogo(6, 124, 150, 50,
                recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA));
        for (int indice = 0; indice < botoes.length; indice++) {
            String nome = indice < 5 ? NOMES_FERRAMENTAS[indice] : "Desistir";
            botoes[indice] = new Botao(indice % 2 == 0 ? 164 : 242,
                    122 + (indice / 2) * 18, 74, 16, nome,
                    recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA), recursos);
            if (indice < 5) {
                int escolha = indice;
                botoes[indice].setAcao(() -> jogar(escolha));
            } else {
                botoes[indice].setAcao(this::pedirDesistencia);
            }
        }
        sim = new Botao(88, 102, 56, 16, "Sim",
                recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA), recursos);
        nao = new Botao(176, 102, 56, 16, "Não",
                recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA), recursos);
        sim.setAcao(this::confirmarDesistencia);
        nao.setAcao(this::cancelarDesistencia);
    }

    @Override
    public void aoEntrar() {
        if (inicializada) {
            return;
        }
        batalha = jogo.iniciarBatalhaAtual();
        barraCoragem = new BarraStatus(8, 20, 96, 8, Paleta.CORAGEM,
                batalha.getJogador().getCoragem(), batalha.getJogador().getCoragemMaxima());
        barraAgitacao = new BarraStatus(212, 8, 100, 8, Paleta.AGITACAO,
                batalha.getInimigo().getAgitacao(), batalha.getInimigo().getAgitacaoMaxima());
        visual = new VisualBatalha(recursos, batalha, barraCoragem, barraAgitacao,
                dialogo, botoes, sim, nao);
        inicializada = true;
        mudarEstado(Estado.ESCOLHENDO, mensagemEscolha());
    }

    @Override
    public void atualizar(double dt) {
        if (!inicializada || !Double.isFinite(dt) || dt < 0) {
            return;
        }
        tempoTotal += dt;
        tempoEtapa += dt;
        barraCoragem.atualizar(dt);
        barraAgitacao.atualizar(dt);
        dialogo.atualizar(dt);
        if (avancoSolicitado && podeReceberEntrada() && animacaoTerminou()) {
            avancarEtapa();
        }
    }

    @Override
    public void desenhar(Graphics2D desenho) {
        if (visual != null) {
            visual.desenhar(desenho, estado, tempoEtapa, tempoTotal,
                    indiceFerramenta, confirmaDesistencia,
                    resultado != null && resultado.getContraefeito(), animandoContraefeito);
        }
    }

    @Override
    public void aoClicar(int x, int y) {
        aoClicar(x, y, 1);
    }

    @Override
    public void aoClicar(int x, int y, int quantidadeCliques) {
        if (!podeReceberEntrada() || x < 0 || x >= Config.LARGURA_LOGICA
                || y < 0 || y >= Config.ALTURA_LOGICA) {
            return;
        }
        double intervalo = tempoTotal - ultimoClique;
        ultimoClique = tempoTotal;
        if (confirmaDesistencia) {
            sim.aoClicar(x, y);
            nao.aoClicar(x, y);
        } else if (estado != Estado.ESCOLHENDO) {
            if (dialogo.contem(x, y)) {
                avancarMensagem();
            }
        } else if (dialogo.contem(x, y)) {
            dialogo.completar();
        } else if (quantidadeCliques <= 1 && intervalo >= Config.INTERVALO_CLIQUES_MS / 1000.0) {
            for (Botao botao : botoes) {
                botao.aoClicar(x, y);
            }
        }
    }

    @Override
    public void aoPressionarMouse(int x, int y) {
        if (!podeReceberEntrada()) {
            return;
        }
        if (confirmaDesistencia) {
            sim.aoPressionarMouse(x, y);
            nao.aoPressionarMouse(x, y);
        } else if (estado == Estado.ESCOLHENDO) {
            for (Botao botao : botoes) {
                botao.aoPressionarMouse(x, y);
            }
        }
    }

    @Override
    public void aoSoltarMouse(int x, int y) {
        for (Botao botao : botoes) {
            botao.aoSoltarMouse(x, y);
        }
        sim.aoSoltarMouse(x, y);
        nao.aoSoltarMouse(x, y);
    }

    @Override
    public void aoMoverMouse(int x, int y) {
        if (!podeReceberEntrada()) {
            return;
        }
        if (confirmaDesistencia) {
            sim.aoMoverMouse(x, y);
            nao.aoMoverMouse(x, y);
            return;
        }
        for (Botao botao : botoes) {
            botao.aoMoverMouse(x, y);
        }
        if (estado != Estado.ESCOLHENDO) {
            return;
        }
        int novoIndice = -1;
        for (int indice = 0; indice < 5; indice++) {
            if (botoes[indice].contem(x, y)) {
                novoIndice = indice;
                break;
            }
        }
        if (novoIndice != ferramentaSobMouse) {
            ferramentaSobMouse = novoIndice;
            if (novoIndice < 0) {
                dialogo.setTexto(mensagemEscolha());
            } else {
                Ferramenta ferramenta = batalha.getJogador().getFerramentas()[novoIndice];
                dialogo.setTexto("Poder: " + (ferramenta.getPoder() + batalha.getJogador().getBonusPoder()));
            }
        }
    }

    @Override
    public void aoTeclar(KeyEvent evento) {
        if (evento == null) {
            return;
        }
        int codigo = evento.getKeyCode();
        if (evento.getID() == KeyEvent.KEY_RELEASED) {
            if (codigo >= 0 && codigo < teclasPressionadas.length) {
                teclasPressionadas[codigo] = false;
            }
            return;
        }
        if (evento.getID() != KeyEvent.KEY_PRESSED) {
            return;
        }
        if (codigo >= 0 && codigo < teclasPressionadas.length) {
            boolean repetida = teclasPressionadas[codigo];
            teclasPressionadas[codigo] = true;
            if (repetida && !aceitaTeclaRepetida(codigo)) {
                return;
            }
        }
        if (!podeReceberEntrada()) {
            return;
        }
        if (confirmaDesistencia) {
            if (codigo == KeyEvent.VK_ESCAPE) {
                nao.ativar();
            } else if (codigo == KeyEvent.VK_ENTER) {
                sim.ativar();
            }
            evento.consume();
            return;
        }
        if (codigo == KeyEvent.VK_ENTER) {
            if (estado == Estado.ESCOLHENDO) {
                dialogo.completar();
            } else {
                avancarMensagem();
            }
            evento.consume();
        } else if (estado == Estado.ESCOLHENDO) {
            if (codigo == KeyEvent.VK_0 || codigo == KeyEvent.VK_NUMPAD0 || codigo == KeyEvent.VK_ESCAPE) {
                botoes[5].ativar();
                evento.consume();
            } else {
                int indice = codigo >= KeyEvent.VK_1 && codigo <= KeyEvent.VK_5 ? codigo - KeyEvent.VK_1
                        : codigo >= KeyEvent.VK_NUMPAD1 && codigo <= KeyEvent.VK_NUMPAD5
                        ? codigo - KeyEvent.VK_NUMPAD1 : -1;
                if (indice >= 0) {
                    botoes[indice].ativar();
                    evento.consume();
                }
            }
        }
    }

    @Override
    public boolean aceitaTeclaRepetida(int codigo) {
        return !(codigo >= KeyEvent.VK_0 && codigo <= KeyEvent.VK_5
                || codigo >= KeyEvent.VK_NUMPAD0 && codigo <= KeyEvent.VK_NUMPAD5
                || codigo == KeyEvent.VK_ENTER || codigo == KeyEvent.VK_ESCAPE);
    }

    @Override
    public void aoPerderFoco() {
        for (int indice = 0; indice < teclasPressionadas.length; indice++) {
            teclasPressionadas[indice] = false;
        }
        aoSoltarMouse(-1, -1);
    }

    private void jogar(int indice) {
        if (!podeReceberEntrada() || estado != Estado.ESCOLHENDO || confirmaDesistencia) {
            return;
        }
        int coragemAnterior = batalha.getJogador().getCoragem();
        ResultadoTurno novoResultado = batalha.jogarTurno(indice);
        if (novoResultado == null) {
            atualizarBotoes();
            return;
        }
        resultado = novoResultado;
        coragemAntesDoTurno = coragemAnterior;
        animandoContraefeito = false;
        indiceFerramenta = indice;
        barraAgitacao.setValor(batalha.getInimigo().getAgitacao());
        String artigo = indice < 3 ? "a " : "o ";
        String mensagem = "Você usou " + artigo + resultado.getNomeFerramenta() + ".";
        if (!resultado.getContraefeito()) {
            mensagem += " " + resultado.getMensagemEfetividade();
        }
        mudarEstado(Estado.ANIMANDO_JOGADOR, mensagem);
    }

    private void avancarMensagem() {
        if (!dialogo.estaCompleta()) {
            dialogo.completar();
            return;
        }
        avancoSolicitado = true;
        if (animacaoTerminou()) {
            avancarEtapa();
        }
    }

    private boolean animacaoTerminou() {
        return tempoEtapa >= Config.DURACAO_ACAO_MS / 1000.0;
    }

    private void avancarEtapa() {
        avancoSolicitado = false;
        // Apresenta voo, possível contraefeito e resposta do animal sem jogar outro turno.
        switch (estado) {
            case ANIMANDO_JOGADOR -> {
                if (resultado.getContraefeito() && !animandoContraefeito) {
                    animandoContraefeito = true;
                    barraCoragem.setValor(Math.max(0,
                            coragemAntesDoTurno - resultado.getDanoContraefeito()));
                    mudarEstado(Estado.ANIMANDO_JOGADOR, resultado.getMensagemEfetividade());
                } else if (resultado.getContraefeito() && resultado.getSemCoragem()
                        && resultado.getAtaqueUsado() == null) {
                    iniciarDerrota();
                } else if (resultado.getAnimalCalmou()) {
                    registrarBatalha();
                    barraCoragem.setValores(batalha.getJogador().getCoragem(),
                            batalha.getJogador().getCoragemMaxima());
                    mudarEstado(Estado.VITORIA, mensagemVitoria(batalha.getInimigo().getNome(),
                            jogo.getCoragemRecuperada()));
                } else {
                    barraCoragem.setValor(batalha.getJogador().getCoragem());
                    mudarEstado(Estado.ANIMANDO_ANIMAL, batalha.getInimigo().getNome() + " usa "
                            + resultado.getAtaqueUsado().getNome()
                            + "! Você levou um susto! (-" + resultado.getDanoSofrido() + " Coragem)");
                }
            }
            case ANIMANDO_ANIMAL -> {
                // Depois do susto, volta à escolha ou apresenta o encerramento.
                if (resultado.getSemCoragem()) {
                    iniciarDerrota();
                } else {
                    mudarEstado(Estado.ESCOLHENDO, mensagemEscolha());
                }
            }
            // A recompensa já foi registrada antes de exibir a mensagem de vitória.
            case VITORIA -> encaminhar(new TelaTransicaoBioma(painel, recursos, jogo, sair));
            case DERROTA -> abrirFimDaJornada();
            case ESCOLHENDO -> { }
        }
    }

    public static String mensagemVitoria(String nomeAnimal, int coragemRecuperada) {
        String mensagem = nomeAnimal + " relaxou e foi descansar.";
        if (coragemRecuperada > 0) {
            mensagem += " Você ganhou +" + coragemRecuperada + " de Coragem!";
        }
        return mensagem;
    }

    private void iniciarDerrota() {
        registrarBatalha();
        mudarEstado(Estado.DERROTA,
                "Sua coragem acabou... você precisa descansar um pouco.");
    }

    private void mudarEstado(Estado novoEstado, String mensagem) {
        estado = novoEstado;
        if (novoEstado != Estado.ANIMANDO_JOGADOR) {
            animandoContraefeito = false;
        }
        tempoEtapa = 0;
        avancoSolicitado = false;
        ferramentaSobMouse = -1;
        dialogo.setTexto(mensagem);
        atualizarBotoes();
    }

    private String mensagemEscolha() {
        return "Escolha uma ferramenta para acalmar " + batalha.getInimigo().getNome() + ".";
    }

    private void atualizarBotoes() {
        boolean podeEscolher = estado == Estado.ESCOLHENDO && !confirmaDesistencia && !encaminhou;
        Ferramenta[] ferramentas = batalha.getJogador().getFerramentas();
        for (int indice = 0; indice < 5; indice++) {
            Ferramenta ferramenta = ferramentas[indice];
            String usos = !ferramenta.temUsos() ? "esgotada"
                    : ferramenta.getUsosRestantes() == Config.USOS_ILIMITADOS
                    ? "∞" : Integer.toString(ferramenta.getUsosRestantes());
            botoes[indice].setTexto(NOMES_FERRAMENTAS[indice] + " " + usos);
            botoes[indice].setIcone(recursos.getIconeFerramenta(ferramenta.getTipo()));
            botoes[indice].setHabilitado(podeEscolher && ferramenta.temUsos());
            int marca = 0;
            if (jogo.efetividadeDescoberta(ferramenta.getTipo(), batalha.getInimigo().getTipo())) {
                double multiplicador = tabelaEfetividade.getMultiplicador(
                        ferramenta.getTipo(), batalha.getInimigo().getTipo());
                marca = multiplicador == 0 ? -1
                        : multiplicador >= Config.MULTIPLICADOR_MUITO_EFICAZ ? 1 : 0;
            }
            botoes[indice].setMarcaEfetividade(marca);
        }
        botoes[5].setHabilitado(podeEscolher);
        sim.setHabilitado(confirmaDesistencia && !encaminhou);
        nao.setHabilitado(confirmaDesistencia && !encaminhou);
    }

    private void pedirDesistencia() {
        if (!podeReceberEntrada() || estado != Estado.ESCOLHENDO || confirmaDesistencia) {
            return;
        }
        confirmaDesistencia = true;
        atualizarBotoes();
        sim.aoMoverMouse(116, 110);
    }

    private void cancelarDesistencia() {
        if (!confirmaDesistencia || !podeReceberEntrada()) {
            return;
        }
        confirmaDesistencia = false;
        aoSoltarMouse(-1, -1);
        atualizarBotoes();
    }

    private void confirmarDesistencia() {
        if (!confirmaDesistencia || !podeReceberEntrada()) {
            return;
        }
        if (batalha.desistir() == null) {
            return;
        }
        confirmaDesistencia = false;
        registrarBatalha();
        estado = Estado.DERROTA;
        abrirFimDaJornada();
    }

    private void registrarBatalha() {
        if (!resultadoRegistrado) {
            jogo.registrarResultadoBatalha(batalha);
            resultadoRegistrado = true;
        }
    }

    private void abrirFimDaJornada() {
        encaminhar(new TelaFimDeJogo(painel, recursos, jogo, sair));
    }

    private void encaminhar(Tela destino) {
        if (!podeReceberEntrada()) {
            return;
        }
        encaminhou = true;
        atualizarBotoes();
        painel.trocarTela(destino);
    }

    private boolean podeReceberEntrada() {
        return inicializada && !encaminhou && !painel.estaEmTransicao();
    }

    public Estado getEstado() { return estado; }
    public Batalha getBatalha() { return batalha; }
    public CaixaDialogo getDialogo() { return dialogo; }
    public BarraStatus getBarraCoragem() { return barraCoragem; }
    public BarraStatus getBarraAgitacao() { return barraAgitacao; }
    public Botao getBotao(int indice) { return botoes[indice]; }
    public boolean isConfirmandoDesistencia() { return confirmaDesistencia; }
    public double getTempoEtapa() { return tempoEtapa; }
    public boolean isAnimandoContraefeito() { return animandoContraefeito; }
    public ResultadoTurno getResultadoTurno() { return resultado; }
}
