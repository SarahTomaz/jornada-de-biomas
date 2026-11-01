package jogo.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import jogo.Config;

public final class CampoTexto {
    private final int x;
    private final int y;
    private final int largura;
    private final int altura;
    private final Font fonte;
    private final int limite;
    private String texto = "";
    private boolean focado;
    private boolean somenteDigitos;
    private boolean textoLivre;
    private double tempoCursorMs;

    public CampoTexto(int x, int y, int largura, int altura, Font fonte, int limite) {
        if (largura < 3 || altura < 3 || fonte == null || limite < 1) {
            throw new IllegalArgumentException("Geometria, fonte ou limite do campo inválido.");
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.fonte = fonte;
        this.limite = limite;
    }

    public void setFocado(boolean focado) {
        if (this.focado != focado) {
            tempoCursorMs = 0;
        }
        this.focado = focado;
    }

    public boolean isFocado() {
        return focado;
    }

    public void aoClicar(int posicaoX, int posicaoY) {
        setFocado(contem(posicaoX, posicaoY));
    }

    public boolean contem(int posicaoX, int posicaoY) {
        return posicaoX >= x && posicaoX < x + largura && posicaoY >= y && posicaoY < y + altura;
    }

    public void aoTeclar(KeyEvent evento) {
        if (!focado || evento == null) {
            return;
        }
        if (evento.getID() == KeyEvent.KEY_PRESSED && evento.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!texto.isEmpty()) {
                texto = texto.substring(0, texto.offsetByCodePoints(texto.length(), -1));
                tempoCursorMs = 0;
            }
            return;
        }
        if (evento.getID() != KeyEvent.KEY_TYPED
                || ((evento.isControlDown() || evento.isAltDown() || evento.isMetaDown())
                && !evento.isAltGraphDown())) {
            return;
        }
        char caractere = evento.getKeyChar();
        if (texto.length() < limite && caracterePermitido(caractere)) {
            texto += caractere;
            tempoCursorMs = 0;
        }
    }

    public void setSomenteDigitos(boolean somenteDigitos) {
        this.somenteDigitos = somenteDigitos;
        setTexto(texto);
    }

    public void setTextoLivre(boolean textoLivre) {
        this.textoLivre = textoLivre;
        setTexto(texto);
    }

    public void setTexto(String texto) {
        String entrada = texto == null ? "" : texto;
        StringBuilder filtrado = new StringBuilder();
        for (int indice = 0; indice < entrada.length() && filtrado.length() < limite; indice++) {
            char caractere = entrada.charAt(indice);
            if (caracterePermitido(caractere)) {
                filtrado.append(caractere);
            }
        }
        this.texto = filtrado.toString();
        tempoCursorMs = 0;
    }

    public String getTexto() {
        return texto;
    }

    public void atualizar(double dt) {
        if (Double.isFinite(dt) && dt > 0 && focado) {
            tempoCursorMs = (tempoCursorMs + dt * 1000) % (Config.INTERVALO_CURSOR_MS * 2);
        }
    }

    public void desenhar(Graphics2D grafico) {
        Graphics2D desenho = (Graphics2D) grafico.create();
        desenho.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        desenho.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        Botao.desenharCaixa(desenho, x, y, largura, altura, Paleta.CAIXA);
        if (focado) {
            desenho.setColor(Paleta.DESTAQUE);
            desenho.drawRect(x + 2, y + 2, largura - 5, altura - 5);
        }
        desenho.clipRect(x + 3, y + 2, largura - 6, altura - 4);
        desenho.setFont(fonte);
        desenho.setColor(Paleta.CONTORNO);
        FontMetrics medidas = desenho.getFontMetrics();
        int inicio = 0;
        while (inicio < texto.length() && medidas.stringWidth(texto.substring(inicio)) > largura - 10) {
            inicio = texto.offsetByCodePoints(inicio, 1);
        }
        String trecho = texto.substring(inicio);
        int linhaBase = y + (altura - medidas.getHeight()) / 2 + medidas.getAscent();
        desenho.drawString(trecho, x + 4, linhaBase);
        if (focado && tempoCursorMs < Config.INTERVALO_CURSOR_MS) {
            desenho.fillRect(x + 4 + medidas.stringWidth(trecho), linhaBase - medidas.getAscent(),
                    1, medidas.getAscent() + medidas.getDescent());
        }
        desenho.dispose();
    }

    private boolean caracterePermitido(char caractere) {
        if (somenteDigitos) {
            return Character.isDigit(caractere);
        }
        return textoLivre ? caractere != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(caractere)
                : Character.isLetterOrDigit(caractere) || caractere == ' ';
    }
}
