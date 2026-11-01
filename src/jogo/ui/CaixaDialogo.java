package jogo.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import jogo.Config;

public final class CaixaDialogo {
    private final int x;
    private final int y;
    private final int largura;
    private final int altura;
    private final Font fonte;
    private final FontMetrics medidas;
    private final int linhasPorPagina;
    private String texto = "";
    private int caracteresVisiveis;
    private double tempoTextoMs;
    private int[] iniciosLinhas = new int[0];
    private int[] finsLinhas = new int[0];
    private int[] proximosInicios = new int[0];
    private int quantidadeLinhas;
    private int primeiraLinhaPagina;

    public CaixaDialogo(int x, int y, int largura, int altura, Font fonte) {
        if (largura < 3 || altura < 3 || fonte == null) {
            throw new IllegalArgumentException("Geometria ou fonte da caixa inválida.");
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.fonte = fonte;
        BufferedImage medidor = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D desenho = medidor.createGraphics();
        DesenhoPixel.configurarTexto(desenho);
        desenho.setFont(fonte);
        medidas = desenho.getFontMetrics();
        desenho.dispose();
        linhasPorPagina = Math.max(1, (altura - 8) / medidas.getHeight());
    }

    public void setTexto(String texto) {
        this.texto = texto == null ? "" : texto;
        caracteresVisiveis = 0;
        tempoTextoMs = 0;
        primeiraLinhaPagina = 0;
        quebrarLinhas();
    }

    public void atualizar(double dt) {
        if (!Double.isFinite(dt) || dt <= 0 || caracteresVisiveis >= getFimPagina()) {
            return;
        }
        tempoTextoMs += dt * 1000;
        caracteresVisiveis = (int) Math.min(getFimPagina(), tempoTextoMs / Config.MS_POR_LETRA);
    }

    public void completar() {
        int fimPagina = getFimPagina();
        if (caracteresVisiveis < fimPagina) {
            caracteresVisiveis = fimPagina;
        } else if (primeiraLinhaPagina + linhasPorPagina < quantidadeLinhas) {
            primeiraLinhaPagina += linhasPorPagina;
            caracteresVisiveis = iniciosLinhas[primeiraLinhaPagina];
        }
        tempoTextoMs = (double) caracteresVisiveis * Config.MS_POR_LETRA;
    }

    public boolean estaCompleta() {
        return caracteresVisiveis >= texto.length();
    }

    public String getTexto() {
        return texto;
    }

    public String getTextoVisivel() {
        return texto.substring(0, caracteresVisiveis);
    }

    public int getIndicePagina() {
        return primeiraLinhaPagina / linhasPorPagina;
    }

    public int getQuantidadePaginas() {
        return Math.max(1, (quantidadeLinhas + linhasPorPagina - 1) / linhasPorPagina);
    }

    public boolean contem(int posicaoX, int posicaoY) {
        return posicaoX >= x && posicaoX < x + largura && posicaoY >= y && posicaoY < y + altura;
    }

    public void desenhar(Graphics2D grafico) {
        Graphics2D desenho = (Graphics2D) grafico.create();
        DesenhoPixel.configurarTexto(desenho);
        Botao.desenharCaixa(desenho, x, y, largura, altura, Paleta.CAIXA);
        desenho.clipRect(x + 3, y + 3, largura - 6, altura - 6);
        desenho.setFont(fonte);
        desenho.setColor(Paleta.CONTORNO);
        desenharLinhas(desenho);
        desenho.dispose();
    }

    private void desenharLinhas(Graphics2D desenho) {
        int fimPagina = Math.min(quantidadeLinhas, primeiraLinhaPagina + linhasPorPagina);
        for (int indice = primeiraLinhaPagina; indice < fimPagina; indice++) {
            int inicio = iniciosLinhas[indice];
            if (inicio >= caracteresVisiveis) {
                break;
            }
            int fim = Math.min(finsLinhas[indice], caracteresVisiveis);
            desenho.drawString(texto.substring(inicio, fim), x + 4,
                    y + 4 + medidas.getAscent()
                    + (indice - primeiraLinhaPagina) * medidas.getHeight());
        }
        if (caracteresVisiveis >= getFimPagina() && getFimPagina() < texto.length()) {
            desenho.fillRect(x + largura - 7, y + altura - 5, 3, 1);
            desenho.fillRect(x + largura - 6, y + altura - 4, 1, 1);
        }
    }

    private void quebrarLinhas() {
        iniciosLinhas = new int[texto.length() + 1];
        finsLinhas = new int[texto.length() + 1];
        proximosInicios = new int[texto.length() + 1];
        quantidadeLinhas = 0;
        int larguraDisponivel = Math.max(1, largura - 8);
        int inicio = 0;
        while (inicio < texto.length()) {
            while (inicio < texto.length() && texto.charAt(inicio) == ' ') {
                inicio++;
            }
            if (inicio >= texto.length()) {
                break;
            }
            int fim = inicio;
            int ultimoEspaco = -1;
            while (fim < texto.length() && texto.charAt(fim) != '\n'
                    && medidas.stringWidth(texto.substring(inicio, fim + 1)) <= larguraDisponivel) {
                if (texto.charAt(fim) == ' ') {
                    ultimoEspaco = fim;
                }
                fim++;
            }
            int proximo;
            if (fim < texto.length() && texto.charAt(fim) == '\n') {
                proximo = fim + 1;
            } else if (fim < texto.length() && ultimoEspaco >= inicio) {
                proximo = ultimoEspaco + 1;
                fim = ultimoEspaco;
            } else {
                if (fim == inicio) {
                    fim++;
                }
                proximo = fim;
            }
            iniciosLinhas[quantidadeLinhas] = inicio;
            finsLinhas[quantidadeLinhas] = fim;
            proximosInicios[quantidadeLinhas] = proximo;
            quantidadeLinhas++;
            inicio = proximo;
        }
        if (quantidadeLinhas > 0) {
            proximosInicios[quantidadeLinhas - 1] = texto.length();
        }
    }

    private int getFimPagina() {
        if (quantidadeLinhas == 0) {
            return texto.length();
        }
        int ultimaLinha = Math.min(quantidadeLinhas, primeiraLinhaPagina + linhasPorPagina) - 1;
        return proximosInicios[ultimaLinha];
    }

}
