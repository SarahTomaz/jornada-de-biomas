package jogo.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import jogo.Config;

/** Operações comuns de desenho, preservando as posições inteiras dos pixels. */
final class DesenhoPixel {
    private DesenhoPixel() {
    }

    static void preencherCantosCortados(Graphics2D desenho, int x, int y,
            int largura, int altura, Color cor) {
        desenho.setColor(cor);
        desenho.fillRect(x + 1, y, largura - 2, altura);
        desenho.fillRect(x, y + 1, largura, altura - 2);
    }

    static void escreverCentralizado(Graphics2D desenho, String texto, int linhaBase) {
        // A largura restante é dividida depois da subtração, como nas telas.
        desenho.drawString(texto, (Config.LARGURA_LOGICA - desenho.getFontMetrics().stringWidth(texto)) / 2,
                linhaBase);
    }

    static void escreverCentralizado(Graphics2D desenho, String texto, int centro, int linhaBase) {
        // O centro explícito preserva o arredondamento dos placeholders.
        desenho.drawString(texto, centro - desenho.getFontMetrics().stringWidth(texto) / 2, linhaBase);
    }

    static void configurarTexto(Graphics2D desenho) {
        desenho.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        desenho.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    static void configurarPixels(Graphics2D desenho) {
        desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        configurarTexto(desenho);
    }
}
