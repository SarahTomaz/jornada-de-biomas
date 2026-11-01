package jogo.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import jogo.Config;
import jogo.Jogo;
import jogo.modelo.Ranking;
import jogo.modelo.RegistroRanking;

public final class TelaRanking implements Tela {
    private static final int LINHAS_BUSCANDO = 4;

    // O modo seleciona o campo de consulta e o algoritmo, sem alterar a ordem da tabela.
    private enum ModoBusca {
        NOME, NIVEL
    }

    private final PainelJogo painel;
    private final Jogo jogo;
    private final RegistroRanking registroDestacado;
    private final Runnable voltar;
    private final BufferedImage fundo;
    private final Font fontePequena;
    private final Font fonteNormal;
    private final Font fonteGrande;
    private final int passoTabela;
    private final int primeiraLinhaTabela;
    private final int linhaCabecalhoTabela;
    private final int linhasTabelaCompleta;
    private final int linhasTabelaBuscando;
    private final Botao[] botoes;
    private final Botao buscar;
    private final CaixaDialogo avisoArquivo;
    private RegistroRanking[] registros;
    private CampoTexto campoBusca;
    private ModoBusca modoBusca;
    private String resultadoBusca = "";
    private int indiceDestacado = -1;
    private int indiceFoco;
    private int inicioTabela;
    private boolean encaminhou;

    public TelaRanking(PainelJogo painel, Recursos recursos, Jogo jogo,
            RegistroRanking destaque, Runnable voltar) {
        if (painel == null || recursos == null || jogo == null || voltar == null) {
            throw new IllegalArgumentException("Os dados da tela de ranking são obrigatórios.");
        }
        this.painel = painel;
        this.jogo = jogo;
        registroDestacado = destaque;
        this.voltar = voltar;
        fundo = recursos.getCenario("menu");
        fontePequena = recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA);
        fonteNormal = recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA);
        fonteGrande = recursos.getFonte(Config.TAMANHO_FONTE_GRANDE);
        FontRenderContext contexto = new FontRenderContext(null, false, false);
        java.awt.Rectangle tinta = fontePequena.createGlyphVector(contexto, "ÁÉÍÓÚãõçgjpq")
                .getPixelBounds(contexto, 0, 0);
        passoTabela = Math.max(12, tinta.height);
        boolean compacta = passoTabela > 12;
        primeiraLinhaTabela = compacta ? 40 - tinta.y : 44;
        linhaCabecalhoTabela = compacta ? 36 : 32;
        linhasTabelaCompleta = compacta ? Math.min(10, Math.max(1, 115 / passoTabela)) : 10;
        linhasTabelaBuscando = compacta ? Math.min(LINHAS_BUSCANDO, Math.max(1, 43 / passoTabela))
                : LINHAS_BUSCANDO;
        botoes = new Botao[] {
            new Botao(8, 160, 114, 16, "Buscar por nome", fontePequena, recursos),
            new Botao(126, 160, 120, 16, "Buscar por nível", fontePequena, recursos),
            new Botao(250, 160, 62, 16, "Voltar", fontePequena, recursos)
        };
        buscar = new Botao(220, 106, 92, 20, "Buscar", fontePequena, recursos);
        botoes[0].setAcao(() -> abrirBusca(ModoBusca.NOME));
        botoes[1].setAcao(() -> abrirBusca(ModoBusca.NIVEL));
        botoes[2].setAcao(this::voltar);
        buscar.setAcao(this::buscar);
        avisoArquivo = new CaixaDialogo(8, 86, 304, 70, fonteNormal);
        atualizarTabela();
        campoBusca = criarCampo(ModoBusca.NOME);
        buscar.setHabilitado(false);
    }

    @Override
    public void aoEntrar() {
        atualizarTabela();
        encaminhou = false;
        modoBusca = null;
        resultadoBusca = "";
        inicioTabela = 0;
        campoBusca = criarCampo(ModoBusca.NOME);
        for (Botao botao : botoes) {
            botao.setHabilitado(true);
            botao.aoSoltarMouse(-1, -1);
        }
        buscar.setHabilitado(false);
        avisoArquivo.setTexto(jogo.getMensagemArquivo());
        avisoArquivo.completar();
        focar(0);
    }

    @Override
    public void atualizar(double dt) {
        campoBusca.atualizar(dt);
    }

    @Override
    public void desenhar(Graphics2D desenho) {
        desenho.drawImage(fundo, 0, 0, Config.LARGURA_LOGICA, Config.ALTURA_LOGICA, null);
        Botao.desenharCaixa(desenho, 4, 2, 312, 154, Paleta.CAIXA);
        desenho.setColor(Paleta.CONTORNO);
        desenho.setFont(fonteGrande);
        desenho.drawString("Ranking", (Config.LARGURA_LOGICA
                - desenho.getFontMetrics().stringWidth("Ranking")) / 2, passoTabela > 12 ? 18 : 19);
        desenharTabela(desenho);
        if (registros.length > getLinhasVisiveis()) {
            desenho.setFont(fontePequena);
            String intervalo = (inicioTabela + 1) + "-"
                    + Math.min(inicioTabela + getLinhasVisiveis(), registros.length)
                    + "/" + registros.length;
            desenho.drawString(intervalo, 308 - desenho.getFontMetrics().stringWidth(intervalo), 18);
        }
        for (Botao botao : botoes) {
            botao.desenhar(desenho);
        }
        if (modoBusca != null) {
            Botao.desenharCaixa(desenho, 4, 86, 312, 70, Paleta.CAIXA);
            desenho.setFont(fonteNormal);
            desenho.setColor(Paleta.CONTORNO);
            desenho.drawString(modoBusca == ModoBusca.NOME ? "Nome do jogador:" : "Nível alcançado:",
                    9, 100);
            campoBusca.desenhar(desenho);
            buscar.desenhar(desenho);
            desenho.setFont(fonteNormal);
            desenho.setColor(Paleta.CONTORNO);
            desenho.drawString(Botao.ajustarTexto(resultadoBusca, desenho.getFontMetrics(), 304),
                    8, 145);
        } else if (registros.length == 0 && !avisoArquivo.getTexto().isEmpty()) {
            avisoArquivo.desenhar(desenho);
        } else if (registros.length <= Math.min(7, getLinhasVisiveis() - 2)) {
            desenho.setFont(fontePequena);
            desenho.setColor(Paleta.CONTORNO);
            String mensagem = avisoArquivo.getTexto().isEmpty() ? "Escolha uma busca abaixo."
                    : "Aviso: ranking indisponível em arquivo.";
            desenho.drawString(Botao.ajustarTexto(mensagem, desenho.getFontMetrics(), 296), 12, 148);
        }
    }

    @Override
    public void aoRolarMouse(int rotacao) {
        if (aceitaEntrada()) {
            int maximo = Math.max(0, registros.length - getLinhasVisiveis());
            inicioTabela = (int) Math.max(0L, Math.min(maximo, (long) inicioTabela + rotacao));
        }
    }

    @Override
    public void aoPressionarMouse(int x, int y) {
        if (!aceitaEntrada()) {
            return;
        }
        for (int indice = 0; indice < botoes.length; indice++) {
            if (botoes[indice].contem(x, y)) {
                focar(indice);
            }
            botoes[indice].aoPressionarMouse(x, y);
        }
        if (modoBusca != null) {
            if (campoBusca.contem(x, y)) {
                focar(3);
            } else if (buscar.contem(x, y)) {
                focar(4);
            }
            buscar.aoPressionarMouse(x, y);
        }
    }

    @Override
    public void aoClicar(int x, int y) {
        if (!aceitaEntrada()) {
            return;
        }
        if (modoBusca != null) {
            campoBusca.aoClicar(x, y);
            if (campoBusca.isFocado()) {
                indiceFoco = 3;
            }
        }
        for (Botao botao : botoes) {
            botao.aoClicar(x, y);
        }
        if (modoBusca != null && !encaminhou) {
            buscar.aoClicar(x, y);
        }
    }

    @Override
    public void aoSoltarMouse(int x, int y) {
        for (Botao botao : botoes) {
            botao.aoSoltarMouse(x, y);
        }
        buscar.aoSoltarMouse(x, y);
    }

    @Override
    public void aoMoverMouse(int x, int y) {
        if (encaminhou) {
            return;
        }
        for (Botao botao : botoes) {
            botao.aoMoverMouse(x, y);
        }
        if (modoBusca != null) {
            buscar.aoMoverMouse(x, y);
        }
    }

    @Override
    public void aoTeclar(KeyEvent evento) {
        if (evento == null || !aceitaEntrada()) {
            return;
        }
        if (evento.getID() == KeyEvent.KEY_PRESSED) {
            switch (evento.getKeyCode()) {
                case KeyEvent.VK_UP -> aoRolarMouse(-1);
                case KeyEvent.VK_DOWN -> aoRolarMouse(1);
                case KeyEvent.VK_ESCAPE -> botoes[2].ativar();
                case KeyEvent.VK_TAB -> {
                    int quantidade = modoBusca == null ? 3 : 5;
                    int direcao = evento.isShiftDown() ? -1 : 1;
                    focar((indiceFoco + direcao + quantidade) % quantidade);
                }
                case KeyEvent.VK_ENTER -> {
                    if (modoBusca != null && campoBusca.isFocado()) {
                        buscar.ativar();
                    } else if (indiceFoco < botoes.length) {
                        botoes[indiceFoco].ativar();
                    } else if (indiceFoco == 4) {
                        buscar.ativar();
                    }
                }
                default -> {
                    campoBusca.aoTeclar(evento);
                    return;
                }
            }
            evento.consume();
            return;
        }
        campoBusca.aoTeclar(evento);
    }

    @Override
    public boolean estaDigitando() {
        return !encaminhou && modoBusca != null && campoBusca.isFocado();
    }

    public RegistroRanking getRegistroDestacado() { return registroDestacado; }
    public int getIndiceDestacado() { return indiceDestacado; }
    public String getResultadoBusca() { return resultadoBusca; }
    public CampoTexto getCampoBusca() { return campoBusca; }
    public Botao getBotaoBuscaNome() { return botoes[0]; }
    public Botao getBotaoBuscaNivel() { return botoes[1]; }
    public Botao getBotaoVoltar() { return botoes[2]; }
    public Botao getBotaoBuscar() { return buscar; }
    public int getInicioTabela() { return inicioTabela; }
    public int getLinhasVisiveis() { return modoBusca == null ? linhasTabelaCompleta : linhasTabelaBuscando; }

    private void atualizarTabela() {
        Ranking ranking = jogo.getRanking();
        registros = new RegistroRanking[ranking.getQuantidade()];
        indiceDestacado = -1;
        for (int indice = 0; indice < registros.length; indice++) {
            registros[indice] = ranking.getRegistros()[indice];
            if (registros[indice] == registroDestacado) {
                indiceDestacado = indice;
            }
        }
    }

    private void desenharTabela(Graphics2D grafico) {
        Graphics2D desenho = (Graphics2D) grafico.create();
        try {
            int altura = modoBusca == null ? 132 : 60;
            Botao.desenharCaixa(desenho, 4, 24, 312, altura, Paleta.CAIXA);
            desenho.setFont(fontePequena);
            desenho.setColor(Paleta.CONTORNO);
            desenho.drawString("#", 10, linhaCabecalhoTabela);
            desenho.drawString("Nome", 38, linhaCabecalhoTabela);
            desenho.drawString("Nível", 152, linhaCabecalhoTabela);
            desenho.drawString("Ciclo", 192, linhaCabecalhoTabela);
            desenho.drawString("Data", 236, linhaCabecalhoTabela);
            if (registros.length == 0) {
                String mensagem = "Ninguém jogou ainda. Seja o primeiro!";
                desenho.setFont(fonteNormal);
                desenho.drawString(mensagem, (Config.LARGURA_LOGICA
                        - desenho.getFontMetrics().stringWidth(mensagem)) / 2, 65);
                return;
            }
            FontMetrics medidas = desenho.getFontMetrics();
            int visiveis = getLinhasVisiveis();
            for (int linha = 0; linha < visiveis && inicioTabela + linha < registros.length; linha++) {
                int indice = inicioTabela + linha;
                RegistroRanking registro = registros[indice];
                int linhaBase = primeiraLinhaTabela + linha * passoTabela;
                if (indice == indiceDestacado) {
                    desenho.setColor(Paleta.DESTAQUE);
                    int topoDestaque = passoTabela > 12 ? 40 + linha * passoTabela : linhaBase - 9;
                    desenho.fillRect(6, topoDestaque, 308, passoTabela);
                    desenho.setColor(Paleta.CONTORNO);
                }
                desenho.drawString((indice + 1) + "º", 10, linhaBase);
                desenho.drawString(Botao.ajustarTexto(registro.getNome(), medidas, 110), 38, linhaBase);
                desenho.drawString(Botao.ajustarTexto(Integer.toString(registro.getNivelAlcancado()),
                        medidas, 36), 152, linhaBase);
                desenho.drawString(Botao.ajustarTexto(Integer.toString(registro.getCiclo()),
                        medidas, 36), 192, linhaBase);
                desenho.drawString(Botao.ajustarTexto(registro.getData(), medidas, 72), 236, linhaBase);
            }
            if (registros.length > visiveis) {
                desenho.setColor(Paleta.CONTORNO);
                if (inicioTabela > 0) {
                    desenho.fillRect(312, 37, 1, 1);
                    desenho.fillRect(311, 38, 3, 1);
                    desenho.fillRect(310, 39, 5, 1);
                }
                if (inicioTabela < registros.length - visiveis) {
                    int base = modoBusca == null ? 150 : 78;
                    desenho.fillRect(310, base - 2, 5, 1);
                    desenho.fillRect(311, base - 1, 3, 1);
                    desenho.fillRect(312, base, 1, 1);
                }
            }
        } finally {
            desenho.dispose();
        }
    }

    private CampoTexto criarCampo(ModoBusca modo) {
        int limite = 10;
        if (modo == ModoBusca.NOME) {
            limite = Config.TAMANHO_MAXIMO_NOME;
            for (RegistroRanking registro : registros) {
                limite = Math.max(limite, registro.getNome().length());
            }
        }
        CampoTexto campo = new CampoTexto(8, 106, 204, 20, fonteNormal, limite);
        campo.setSomenteDigitos(modo == ModoBusca.NIVEL);
        campo.setTextoLivre(modo == ModoBusca.NOME);
        return campo;
    }

    private void abrirBusca(ModoBusca modo) {
        if (!aceitaEntrada()) {
            return;
        }
        modoBusca = modo;
        campoBusca = criarCampo(modo);
        resultadoBusca = "";
        buscar.setHabilitado(true);
        buscar.aoSoltarMouse(-1, -1);
        focar(3);
    }

    private void buscar() {
        if (!aceitaEntrada() || modoBusca == null) {
            return;
        }
        String entrada = campoBusca.getTexto().trim();
        int indice = -1;
        if (modoBusca == ModoBusca.NOME && !entrada.isEmpty()) {
            indice = jogo.getRanking().buscarPorNome(entrada);
        } else if (modoBusca == ModoBusca.NIVEL) {
            try {
                int nivel = Integer.parseInt(entrada);
                if (nivel >= 1) {
                    indice = jogo.getRanking().buscarPorNivel(nivel);
                }
            } catch (NumberFormatException erro) {
                indice = -1;
            }
        }
        if (indice < 0) {
            resultadoBusca = "Nenhum registro encontrado";
        } else {
            RegistroRanking registro = jogo.getRanking().getRegistros()[indice];
            resultadoBusca = "Encontrado: " + (indice + 1) + "º lugar, " + registro.getNome()
                    + ", nível " + registro.getNivelAlcancado();
        }
    }

    private void focar(int indice) {
        indiceFoco = indice;
        campoBusca.setFocado(modoBusca != null && indice == 3);
        for (int posicao = 0; posicao < botoes.length; posicao++) {
            Botao botao = botoes[posicao];
            botao.aoMoverMouse(posicao == indice ? botao.getX() + 1 : -1,
                    posicao == indice ? botao.getY() + 1 : -1);
        }
        buscar.aoMoverMouse(indice == 4 ? buscar.getX() + 1 : -1,
                indice == 4 ? buscar.getY() + 1 : -1);
    }

    private void voltar() {
        if (!aceitaEntrada()) {
            return;
        }
        encaminhou = true;
        campoBusca.setFocado(false);
        for (Botao botao : botoes) {
            botao.setHabilitado(false);
        }
        buscar.setHabilitado(false);
        voltar.run();
    }

    private boolean aceitaEntrada() {
        return !encaminhou && !painel.estaEmTransicao();
    }
}
