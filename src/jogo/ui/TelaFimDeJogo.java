package jogo.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import jogo.Config;
import jogo.Jogo;
import jogo.modelo.RegistroBatalha;
import jogo.modelo.RegistroRanking;

public final class TelaFimDeJogo implements Tela {
    private static final int LINHAS_VISIVEIS = 6;
    private final PainelJogo painel;
    private final Recursos recursos;
    private final Jogo jogo;
    private final Runnable sair;
    private final Font fontePequena;
    private final Font fonteNormal;
    private final Font fonteGrande;
    private final boolean disposicaoCompacta;
    private final int passoHistorico;
    private final int topoHistorico;
    private final int primeiraLinhaHistorico;
    private final Botao botaoRanking;
    private final Botao botaoMenu;
    private final Botao[] botoes;
    private RegistroBatalha[] historico = new RegistroBatalha[0];
    private RegistroRanking ultimoRegistro;
    private boolean carregada;
    private int nivel;
    private int ciclo;
    private int inicioHistorico;
    private int indiceSelecionado = 1;
    private String bioma = "";
    private String cenario = "menu";
    private String mensagemNivel = "";
    private String mensagemRanking = "";
    private String avisoArquivo = "";
    private String quadroPersonagem = "assustado";

    public TelaFimDeJogo(PainelJogo painel, Recursos recursos, Jogo jogo, Runnable sair) {
        if (painel == null || recursos == null || jogo == null || sair == null) {
            throw new IllegalArgumentException("A tela final precisa de painel, recursos, jogo e saída.");
        }
        this.painel = painel;
        this.recursos = recursos;
        this.jogo = jogo;
        this.sair = sair;
        fontePequena = recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA);
        fonteNormal = recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA);
        fonteGrande = recursos.getFonte(Config.TAMANHO_FONTE_GRANDE);
        FontRenderContext contexto = new FontRenderContext(null, false, false);
        java.awt.Rectangle tinta = fontePequena.createGlyphVector(contexto, "ÁÉÍÓÚãõçgjpq")
                .getPixelBounds(contexto, 0, 0);
        passoHistorico = Math.max(12, tinta.height);
        disposicaoCompacta = passoHistorico > 12;
        topoHistorico = disposicaoCompacta ? 156 - LINHAS_VISIVEIS * passoHistorico : 84;
        primeiraLinhaHistorico = disposicaoCompacta ? topoHistorico + 1 - tinta.y : 94;
        botaoRanking = new Botao(48, 160, 112, 16, "Ver ranking", fontePequena, recursos);
        botaoMenu = new Botao(176, 160, 112, 16, "Menu", fontePequena, recursos);
        botoes = new Botao[] {botaoRanking, botaoMenu};
        botaoRanking.setAcao(this::abrirRanking);
        botaoMenu.setAcao(() -> painel.trocarTela(new TelaMenu(painel, recursos, jogo, sair)));
    }

    @Override
    public void aoEntrar() {
        if (!jogo.partidaTerminou()) {
            throw new IllegalStateException("Encerre a partida antes de abrir o fim da jornada.");
        }
        if (!carregada) {
            if (!jogo.partidaFinalizada()) {
                jogo.finalizarPartida();
            }
            nivel = jogo.getJogador().getVitorias();
            ciclo = jogo.getCicloFinal() + 1;
            bioma = jogo.getBiomaFinal().getNome();
            cenario = jogo.getBiomaFinal().getCenario();
            historico = jogo.getHistoricoFinal();
            ultimoRegistro = jogo.getUltimoRegistro();
            mensagemNivel = nivel == 0 ? "Você ainda não acalmou nenhum animal"
                    : "Nível alcançado: " + nivel;
            int posicao = jogo.getPosicaoObtida();
            mensagemRanking = nivel == 0 ? "" : posicao >= 1
                    ? "Você ficou em " + posicao + "º lugar!" : "Dessa vez não entrou no Top 10";
            quadroPersonagem = jogo.getBatalhaAtual().desistiu() ? "feliz" : "assustado";
            String mensagemArquivo = jogo.getMensagemArquivo();
            if (!mensagemArquivo.isEmpty()) {
                avisoArquivo = mensagemArquivo.startsWith("Não foi possível salvar")
                        ? "Ranking não salvo em arquivo."
                        : "Ranking indisponível nesta sessão.";
            }
            carregada = true;
        }
        for (Botao botao : botoes) {
            botao.aoSoltarMouse(-1, -1);
        }
        selecionar(indiceSelecionado);
    }

    @Override
    public void atualizar(double dt) {
    }

    @Override
    public void desenhar(Graphics2D grafico) {
        Graphics2D desenho = (Graphics2D) grafico.create();
        desenho.drawImage(recursos.getCenario(cenario), 0, 0,
                Config.LARGURA_LOGICA, Config.ALTURA_LOGICA, null);
        Botao.desenharCaixa(desenho, 4, 4, 312, 172, Paleta.CAIXA);
        desenho.setColor(Paleta.CONTORNO);
        desenho.setFont(fonteGrande);
        String titulo = "Fim da jornada";
        desenho.drawString(titulo,
                (Config.LARGURA_LOGICA - desenho.getFontMetrics().stringWidth(titulo)) / 2, 21);

        desenho.setFont(fontePequena);
        FontMetrics medidas = desenho.getFontMetrics();
        if (disposicaoCompacta) {
            desenho.drawString(Botao.ajustarTexto("Nível " + nivel + " | Ciclo " + ciclo
                    + " | " + bioma, medidas, 252), 12, 36);
        } else {
            desenho.drawString(Botao.ajustarTexto("Nível: " + nivel + " | Ciclo: " + ciclo,
                    medidas, 252), 12, 35);
            desenho.drawString(Botao.ajustarTexto("Bioma: " + bioma, medidas, 252), 12, 48);
        }
        desenho.drawImage(recursos.getPersonagem(quadroPersonagem), 274,
                disposicaoCompacta ? 5 : 22, 32, 32, null);
        desenho.setFont(fonteNormal);
        desenho.drawString(nivel == 0 ? mensagemNivel : mensagemRanking, 12,
                disposicaoCompacta ? 53 : 65);
        desenho.setFont(fontePequena);
        String cabecalho = avisoArquivo.isEmpty() ? "Histórico de batalhas" : avisoArquivo;
        int linhaCabecalho = disposicaoCompacta ? topoHistorico - 3 : 80;
        desenho.drawString(Botao.ajustarTexto(cabecalho, medidas, 240), 12, linhaCabecalho);
        if (historico.length > LINHAS_VISIVEIS) {
            String intervalo = (inicioHistorico + 1) + "-"
                    + Math.min(inicioHistorico + LINHAS_VISIVEIS, historico.length)
                    + "/" + historico.length;
            desenho.drawString(intervalo, 306 - medidas.stringWidth(intervalo), linhaCabecalho);
        }
        desenharHistorico(desenho);
        for (Botao botao : botoes) {
            botao.desenhar(desenho);
        }
        Botao selecionado = botoes[indiceSelecionado];
        int x = selecionado.getX() - 7;
        int y = selecionado.getY() + selecionado.getAltura() / 2;
        desenho.setColor(Paleta.CONTORNO);
        desenho.fillRect(x, y - 3, 1, 7);
        desenho.fillRect(x + 1, y - 2, 1, 5);
        desenho.fillRect(x + 2, y - 1, 1, 3);
        desenho.fillRect(x + 3, y, 1, 1);
        desenho.dispose();
    }

    private void desenharHistorico(Graphics2D desenho) {
        int alturaHistorico = 158 - topoHistorico;
        Botao.desenharCaixa(desenho, 8, topoHistorico, 304, alturaHistorico, Paleta.CAIXA);
        Graphics2D linhas = (Graphics2D) desenho.create();
        linhas.clipRect(10, topoHistorico + 1, 290, alturaHistorico - 2);
        linhas.setFont(fontePequena);
        linhas.setColor(Paleta.CONTORNO);
        FontMetrics medidas = linhas.getFontMetrics();
        if (historico.length == 0) {
            linhas.drawString("Nenhuma batalha registrada.", 14, primeiraLinhaHistorico);
        } else {
            for (int linha = 0; linha < LINHAS_VISIVEIS; linha++) {
                int indice = inicioHistorico + linha;
                if (indice >= historico.length) {
                    break;
                }
                RegistroBatalha registro = historico[indice];
                int linhaBase = primeiraLinhaHistorico + linha * passoHistorico;
                linhas.drawString(Botao.ajustarTexto("C" + registro.getCiclo(), medidas, 28),
                        14, linhaBase);
                linhas.drawString(Botao.ajustarTexto(registro.getBioma(), medidas, 68),
                        46, linhaBase);
                linhas.drawString(Botao.ajustarTexto(registro.getAnimal(), medidas, 110),
                        118, linhaBase);
                linhas.drawString(registro.isAcalmou() ? "Calmo" : "Agitado", 237, linhaBase);
            }
        }
        linhas.dispose();
        if (historico.length > LINHAS_VISIVEIS) {
            desenho.setColor(Paleta.CONTORNO);
            if (inicioHistorico > 0) {
                desenho.fillRect(304, topoHistorico + 3, 1, 1);
                desenho.fillRect(303, topoHistorico + 4, 3, 1);
                desenho.fillRect(302, topoHistorico + 5, 5, 1);
            }
            if (inicioHistorico < historico.length - LINHAS_VISIVEIS) {
                desenho.fillRect(302, 152, 5, 1);
                desenho.fillRect(303, 153, 3, 1);
                desenho.fillRect(304, 154, 1, 1);
            }
        }
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
            Botao botao = botoes[indice];
            botao.aoMoverMouse(x, y);
            if (botao.contem(x, y)) {
                indiceSelecionado = indice;
            }
        }
    }

    @Override
    public void aoPressionarMouse(int x, int y) {
        for (int indice = 0; indice < botoes.length; indice++) {
            Botao botao = botoes[indice];
            botao.aoPressionarMouse(x, y);
            if (botao.contem(x, y)) {
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
    public void aoRolarMouse(int rotacao) {
        if (!painel.estaEmTransicao()) {
            int maximo = Math.max(0, historico.length - LINHAS_VISIVEIS);
            inicioHistorico = (int) Math.max(0L, Math.min(maximo, (long) inicioHistorico + rotacao));
        }
    }

    @Override
    public void aoTeclar(KeyEvent evento) {
        if (evento.getID() != KeyEvent.KEY_PRESSED || painel.estaEmTransicao()) {
            return;
        }
        switch (evento.getKeyCode()) {
            case KeyEvent.VK_UP -> aoRolarMouse(-1);
            case KeyEvent.VK_DOWN -> aoRolarMouse(1);
            case KeyEvent.VK_TAB -> selecionar(1 - indiceSelecionado);
            case KeyEvent.VK_ENTER -> botoes[indiceSelecionado].ativar();
            case KeyEvent.VK_ESCAPE -> botaoMenu.ativar();
            default -> {
                return;
            }
        }
        evento.consume();
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
        painel.trocarTela(new TelaRanking(painel, recursos, jogo, ultimoRegistro,
                () -> painel.trocarTela(this)));
    }

    public int getNivel() { return nivel; }
    public int getCiclo() { return ciclo; }
    public String getBioma() { return bioma; }
    public String getMensagemRanking() { return mensagemRanking; }
    public String getMensagemNivel() { return mensagemNivel; }
    public String getQuadroPersonagem() { return quadroPersonagem; }
    public int getInicioHistorico() { return inicioHistorico; }
    public Botao getBotaoRanking() { return botaoRanking; }
    public Botao getBotaoMenu() { return botaoMenu; }

    public RegistroBatalha[] getHistorico() {
        RegistroBatalha[] copia = new RegistroBatalha[historico.length];
        for (int indice = 0; indice < historico.length; indice++) {
            copia[indice] = historico[indice];
        }
        return copia;
    }
}
