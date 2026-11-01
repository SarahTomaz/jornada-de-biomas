package jogo.ui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import jogo.Config;
import jogo.Jogo;

public final class TelaMenu implements Tela {
    private final PainelJogo painel;
    private final Recursos recursos;
    private final Jogo jogo;
    private final Runnable sair;
    private final Botao[] botoes = new Botao[3];
    private int indiceSelecionado;
    private double tempoAnimacao;

    public TelaMenu(PainelJogo painel, Recursos recursos, Jogo jogo, Runnable sair) {
        if (painel == null || recursos == null || jogo == null || sair == null) {
            throw new IllegalArgumentException("A tela de menu precisa de painel, recursos, jogo e saída.");
        }
        this.painel = painel;
        this.recursos = recursos;
        this.jogo = jogo;
        this.sair = sair;
        botoes[0] = new Botao(112, 84, 96, 20, "Jogar",
                recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA), recursos);
        botoes[1] = new Botao(112, 108, 96, 20, "Ranking",
                recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA), recursos);
        botoes[2] = new Botao(112, 132, 96, 20, "Sair",
                recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA), recursos);
        botoes[0].setAcao(() -> painel.trocarTela(new TelaNome(painel, recursos, jogo, sair)));
        botoes[1].setAcao(this::abrirRanking);
        botoes[2].setAcao(sair);
    }

    @Override
    public void aoEntrar() {
        tempoAnimacao = 0;
        selecionar(0);
    }

    @Override
    public void atualizar(double dt) {
        if (Double.isFinite(dt) && dt >= 0) {
            tempoAnimacao = (tempoAnimacao + dt) % (Config.INTERVALO_IDLE_SEGUNDOS * 2);
        }
    }

    @Override
    public void desenhar(Graphics2D desenho) {
        desenho.drawImage(recursos.getCenario("menu"), 0, 0,
                Config.LARGURA_LOGICA, Config.ALTURA_LOGICA, null);
        desenho.drawImage(recursos.getTitulo(), 60, 14, 200, 48, null);
        desenho.drawImage(recursos.getPersonagem(getQuadroPersonagem()), 20, 130, 32, 32, null);
        for (Botao botao : botoes) {
            botao.desenhar(desenho);
        }
        int centroSeta = botoes[indiceSelecionado].getY()
                + botoes[indiceSelecionado].getAltura() / 2;
        desenho.setColor(Paleta.CONTORNO);
        desenho.fillRect(105, centroSeta - 3, 1, 7);
        desenho.fillRect(106, centroSeta - 2, 1, 5);
        desenho.fillRect(107, centroSeta - 1, 1, 3);
        desenho.fillRect(108, centroSeta, 1, 1);
    }

    @Override
    public void aoClicar(int x, int y) {
        if (!painel.estaEmTransicao()) {
            for (Botao botao : botoes) {
                botao.aoClicar(x, y);
            }
        }
    }

    @Override
    public void aoMoverMouse(int x, int y) {
        for (int indice = 0; indice < botoes.length; indice++) {
            botoes[indice].aoMoverMouse(x, y);
            if (botoes[indice].contem(x, y)) {
                indiceSelecionado = indice;
            }
        }
    }

    @Override
    public void aoPressionarMouse(int x, int y) {
        for (int indice = 0; indice < botoes.length; indice++) {
            botoes[indice].aoPressionarMouse(x, y);
            if (botoes[indice].contem(x, y)) {
                indiceSelecionado = indice;
            }
        }
    }

    @Override
    public void aoSoltarMouse(int x, int y) {
        for (Botao botao : botoes) {
            botao.aoSoltarMouse(x, y);
        }
    }

    @Override
    public void aoTeclar(KeyEvent evento) {
        if (evento.getID() != KeyEvent.KEY_PRESSED || painel.estaEmTransicao()) {
            return;
        }
        switch (evento.getKeyCode()) {
            case KeyEvent.VK_UP -> selecionar((indiceSelecionado + botoes.length - 1) % botoes.length);
            case KeyEvent.VK_DOWN -> selecionar((indiceSelecionado + 1) % botoes.length);
            case KeyEvent.VK_ENTER -> botoes[indiceSelecionado].ativar();
            default -> {
                return;
            }
        }
        evento.consume();
    }

    public Botao getBotao(int indice) {
        return botoes[indice];
    }

    public int getIndiceSelecionado() {
        return indiceSelecionado;
    }

    public String getQuadroPersonagem() {
        return tempoAnimacao < Config.INTERVALO_IDLE_SEGUNDOS ? "idle_1" : "idle_2";
    }

    private void selecionar(int indice) {
        indiceSelecionado = indice;
        for (int posicao = 0; posicao < botoes.length; posicao++) {
            Botao botao = botoes[posicao];
            botao.aoMoverMouse(posicao == indice ? botao.getX() + botao.getLargura() / 2 : -1,
                    posicao == indice ? botao.getY() + botao.getAltura() / 2 : -1);
        }
    }

    private void abrirRanking() {
        painel.trocarTela(new TelaRanking(painel, recursos, jogo, null,
                () -> painel.trocarTela(new TelaMenu(painel, recursos, jogo, sair))));
    }
}
