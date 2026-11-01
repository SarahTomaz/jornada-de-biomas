package jogo.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import jogo.Config;
import jogo.Jogo;

public final class TelaNome implements Tela {
    private final PainelJogo painel;
    private final Recursos recursos;
    private final Jogo jogo;
    private final Runnable sair;
    private final BufferedImage fundo;
    private final Font fontePequena;
    private final Font fonteNormal;
    private final Font fonteGrande;
    private final CampoTexto campoNome;
    private final Botao comecar;
    private final Botao voltar;
    private String mensagem = "";
    private boolean encaminhou;

    public TelaNome(PainelJogo painel, Recursos recursos, Jogo jogo, Runnable sair) {
        if (painel == null || recursos == null || jogo == null || sair == null) {
            throw new IllegalArgumentException("Os dados da tela de nome são obrigatórios.");
        }
        this.painel = painel;
        this.recursos = recursos;
        this.jogo = jogo;
        this.sair = sair;
        fundo = recursos.getCenario("menu");
        fontePequena = recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA);
        fonteNormal = recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA);
        fonteGrande = recursos.getFonte(Config.TAMANHO_FONTE_GRANDE);
        campoNome = new CampoTexto(50, 62, 220, 24, fonteNormal, Config.TAMANHO_MAXIMO_NOME);
        comecar = new Botao(56, 134, 96, 22, "Começar", fontePequena, recursos);
        voltar = new Botao(168, 134, 96, 22, "Voltar", fontePequena, recursos);
        comecar.setAcao(this::confirmarNome);
        voltar.setAcao(this::voltarAoMenu);
    }

    @Override
    public void aoEntrar() {
        encaminhou = false;
        mensagem = "";
        campoNome.setTexto("");
        campoNome.setFocado(true);
        comecar.setHabilitado(true);
        voltar.setHabilitado(true);
    }

    @Override
    public void atualizar(double dt) {
        campoNome.atualizar(dt);
    }

    @Override
    public void desenhar(Graphics2D desenho) {
        desenho.drawImage(fundo, 0, 0, null);
        Botao.desenharCaixa(desenho, 16, 18, 288, 148, Paleta.CAIXA);
        desenho.setColor(Paleta.CONTORNO);
        desenho.setFont(fonteGrande);
        DesenhoPixel.escreverCentralizado(desenho, "Qual é o seu nome?", 47);
        campoNome.desenhar(desenho);
        if (!mensagem.isEmpty()) {
            desenho.setFont(fonteNormal);
            desenharMensagem(desenho);
        }
        comecar.desenhar(desenho);
        voltar.desenhar(desenho);
    }

    @Override
    public void aoPressionarMouse(int x, int y) {
        if (encaminhou) {
            return;
        }
        comecar.aoPressionarMouse(x, y);
        voltar.aoPressionarMouse(x, y);
    }

    @Override
    public void aoClicar(int x, int y) {
        if (encaminhou) {
            return;
        }
        campoNome.aoClicar(x, y);
        comecar.aoClicar(x, y);
        voltar.aoClicar(x, y);
    }

    @Override
    public void aoSoltarMouse(int x, int y) {
        comecar.aoSoltarMouse(x, y);
        voltar.aoSoltarMouse(x, y);
    }

    @Override
    public void aoMoverMouse(int x, int y) {
        comecar.aoMoverMouse(x, y);
        voltar.aoMoverMouse(x, y);
    }

    @Override
    public void aoTeclar(KeyEvent evento) {
        if (evento == null || encaminhou) {
            return;
        }
        if (evento.getID() == KeyEvent.KEY_PRESSED) {
            if (evento.getKeyCode() == KeyEvent.VK_ENTER) {
                comecar.ativar();
                evento.consume();
                return;
            }
            if (evento.getKeyCode() == KeyEvent.VK_ESCAPE) {
                voltar.ativar();
                evento.consume();
                return;
            }
        }
        String textoAnterior = campoNome.getTexto();
        campoNome.aoTeclar(evento);
        if (!textoAnterior.equals(campoNome.getTexto())) {
            mensagem = "";
        }
    }

    @Override
    public boolean estaDigitando() {
        return !encaminhou && campoNome.isFocado();
    }

    public CampoTexto getCampoNome() {
        return campoNome;
    }

    public String getMensagem() {
        return mensagem;
    }

    private void confirmarNome() {
        if (encaminhou || painel.estaEmTransicao()) {
            return;
        }
        String nome = campoNome.getTexto().trim();
        if (nome.isEmpty()) {
            mensagem = "Digite seu nome para começar.";
            campoNome.setFocado(true);
            return;
        }
        try {
            jogo.novaPartida(nome);
        } catch (IllegalArgumentException erro) {
            mensagem = erro.getMessage();
            campoNome.setFocado(true);
            return;
        }
        bloquearEntrada();
        painel.trocarTela(new TelaTransicaoBioma(painel, recursos, jogo, sair));
    }

    private void voltarAoMenu() {
        if (encaminhou || painel.estaEmTransicao()) {
            return;
        }
        bloquearEntrada();
        painel.trocarTela(new TelaMenu(painel, recursos, jogo, sair));
    }

    private void bloquearEntrada() {
        encaminhou = true;
        campoNome.setFocado(false);
        comecar.setHabilitado(false);
        voltar.setHabilitado(false);
    }

    private void desenharMensagem(Graphics2D desenho) {
        FontMetrics medidas = desenho.getFontMetrics();
        int inicio = 0;
        int linhaBase = 104;
        while (inicio < mensagem.length()) {
            while (inicio < mensagem.length() && mensagem.charAt(inicio) == ' ') {
                inicio++;
            }
            if (inicio >= mensagem.length()) {
                break;
            }
            int fim = inicio;
            int ultimoEspaco = -1;
            while (fim < mensagem.length()
                    && medidas.stringWidth(mensagem.substring(inicio, fim + 1)) <= 264) {
                if (mensagem.charAt(fim) == ' ') {
                    ultimoEspaco = fim;
                }
                fim++;
            }
            if (fim < mensagem.length() && ultimoEspaco > inicio) {
                fim = ultimoEspaco;
            }
            if (fim == inicio) {
                fim++;
            }
            DesenhoPixel.escreverCentralizado(desenho, mensagem.substring(inicio, fim), linhaBase);
            inicio = fim;
            linhaBase += medidas.getHeight();
        }
    }
}
