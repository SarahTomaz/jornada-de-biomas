package jogo.ui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import jogo.Config;
import jogo.Jogo;
import jogo.modelo.Bioma;
import jogo.modelo.Fase;

public final class TelaTransicaoBioma implements Tela {
    private final PainelJogo painel;
    private final Recursos recursos;
    private final Jogo jogo;
    private final Runnable sair;
    private final Font fonteTitulo;
    private final Font fonteTituloGrande;
    private final CaixaDialogo descricao;
    private Bioma bioma;
    private BufferedImage fundo;
    private int cicloExibido;
    private double tempo;
    private boolean encaminhou;

    public TelaTransicaoBioma(PainelJogo painel, Recursos recursos, Jogo jogo, Runnable sair) {
        if (painel == null || recursos == null || jogo == null || sair == null) {
            throw new IllegalArgumentException("Os dados da transição de bioma são obrigatórios.");
        }
        this.painel = painel;
        this.recursos = recursos;
        this.jogo = jogo;
        this.sair = sair;
        fonteTitulo = recursos.getFonte(Config.TAMANHO_FONTE_GRANDE);
        fonteTituloGrande = recursos.getFonte(Config.TAMANHO_FONTE_TITULO_GRANDE);
        descricao = new CaixaDialogo(16, 102, 288, 48,
                recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA));
    }

    @Override
    public void aoEntrar() {
        Fase fase = jogo.getFaseAtual();
        if (jogo.getJogador() == null || fase == null) {
            throw new IllegalStateException("Inicie uma partida antes de apresentar o bioma.");
        }
        bioma = fase.getBioma();
        fundo = recursos.getCenario(bioma.getCenario());
        cicloExibido = jogo.getCiclo() + 1;
        descricao.setTexto(bioma.getDescricao());
        descricao.completar();
        tempo = 0;
        encaminhou = false;
    }

    @Override
    public void atualizar(double dt) {
        if (!Double.isFinite(dt) || dt < 0 || encaminhou) {
            return;
        }
        tempo += dt;
        if (tempo >= Config.DURACAO_TRANSICAO_BIOMA_SEGUNDOS) {
            seguir();
        }
    }

    @Override
    public void desenhar(Graphics2D desenho) {
        if (fundo == null || bioma == null) {
            return;
        }
        desenho.drawImage(fundo, 0, 0, null);
        Botao.desenharCaixa(desenho, 8, 24, 304, 136, Paleta.CAIXA);
        desenho.setColor(Paleta.CONTORNO);
        desenho.setFont(fonteTitulo);
        DesenhoPixel.escreverCentralizado(desenho, "Ciclo " + cicloExibido, 50);
        desenho.setFont(fonteTituloGrande);
        DesenhoPixel.escreverCentralizado(desenho, bioma.getNome(), 87);
        descricao.desenhar(desenho);
    }

    @Override
    public void aoClicar(int x, int y) {
        if (x >= 0 && x < Config.LARGURA_LOGICA && y >= 0 && y < Config.ALTURA_LOGICA) {
            seguir();
        }
    }

    @Override
    public void aoMoverMouse(int x, int y) {
    }

    @Override
    public void aoTeclar(KeyEvent evento) {
        if (evento != null && evento.getID() == KeyEvent.KEY_PRESSED
                && evento.getKeyCode() == KeyEvent.VK_ENTER) {
            seguir();
            evento.consume();
        }
    }

    private void seguir() {
        if (encaminhou || bioma == null || painel.estaEmTransicao()) {
            return;
        }
        encaminhou = true;
        painel.trocarTela(new TelaBatalha(painel, recursos, jogo, sair));
    }

}
