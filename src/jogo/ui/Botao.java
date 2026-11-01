package jogo.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public final class Botao {
    private final int x;
    private final int y;
    private final int largura;
    private final int altura;
    private final Font fonte;
    private String texto;
    private BufferedImage icone;
    private int marcaEfetividade;
    private Runnable acao;
    private boolean habilitado = true;
    private boolean sobre;
    private boolean pressionado;

    public Botao(int x, int y, int largura, int altura, String texto,
            Font fonte, Recursos recursos) {
        if (largura < 3 || altura < 3 || fonte == null) {
            throw new IllegalArgumentException("Geometria ou fonte do botão inválida.");
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.fonte = fonte;
        setTexto(texto);
    }

    public void setAcao(Runnable acao) {
        this.acao = acao;
    }

    public void setTexto(String texto) {
        this.texto = texto == null ? "" : texto;
    }

    public String getTexto() {
        return texto;
    }

    public void setIcone(BufferedImage icone) {
        this.icone = icone;
    }

    public BufferedImage getIcone() {
        return icone;
    }

    public void setMarcaEfetividade(int marcaEfetividade) {
        if (marcaEfetividade < -1 || marcaEfetividade > 1) {
            throw new IllegalArgumentException("A marca de efetividade deve ser -1, 0 ou 1.");
        }
        this.marcaEfetividade = marcaEfetividade;
    }

    public int getMarcaEfetividade() {
        return marcaEfetividade;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
        if (!habilitado) {
            sobre = false;
            pressionado = false;
        }
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void aoMoverMouse(int posicaoX, int posicaoY) {
        sobre = habilitado && contem(posicaoX, posicaoY);
    }

    public void aoPressionarMouse(int posicaoX, int posicaoY) {
        aoMoverMouse(posicaoX, posicaoY);
        pressionado = habilitado && contem(posicaoX, posicaoY);
    }

    public void aoSoltarMouse(int posicaoX, int posicaoY) {
        pressionado = false;
        aoMoverMouse(posicaoX, posicaoY);
    }

    public void aoClicar(int posicaoX, int posicaoY) {
        boolean deveAtivar = habilitado && pressionado && contem(posicaoX, posicaoY);
        pressionado = false;
        aoMoverMouse(posicaoX, posicaoY);
        if (deveAtivar) {
            ativar();
        }
    }

    public void ativar() {
        if (!habilitado) {
            return;
        }
        if (acao != null) {
            acao.run();
        }
    }

    public boolean contem(int posicaoX, int posicaoY) {
        return posicaoX >= x && posicaoX < x + largura && posicaoY >= y && posicaoY < y + altura;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getLargura() { return largura; }
    public int getAltura() { return altura; }

    public void desenhar(Graphics2D grafico) {
        Graphics2D desenho = (Graphics2D) grafico.create();
        DesenhoPixel.configurarTexto(desenho);
        int deslocamento = pressionado && sobre ? 1 : sobre ? -1 : 0;
        int topo = y + deslocamento;
        Color cor = !habilitado ? Paleta.BOTAO_DESABILITADO
                : sobre ? Paleta.BOTAO_HOVER : Paleta.BOTAO_NORMAL;
        desenharCaixa(desenho, x, topo, largura, altura, cor);
        desenho.setFont(fonte);
        desenho.setColor(Paleta.CONTORNO);
        FontMetrics medidas = desenho.getFontMetrics();
        if (icone != null || texto.indexOf('∞') >= 0) {
            desenharFerramenta(desenho, topo, medidas);
        } else {
            desenho.clipRect(x + 2, topo + 1, largura - 4, altura - 2);
            String rotulo = ajustarTexto(texto, medidas, largura - 8);
            Rectangle tinta = medirTinta(desenho, rotulo);
            int linhaBase = topo + (altura - tinta.height) / 2 - tinta.y;
            desenho.drawString(rotulo, x + (largura - medidas.stringWidth(rotulo)) / 2, linhaBase);
        }
        desenho.dispose();
    }

    private void desenharFerramenta(Graphics2D desenho, int topo, FontMetrics medidas) {
        if (icone != null) {
            desenho.drawImage(icone, x + 1, topo + (altura - icone.getHeight()) / 2, null);
            desenharMarcaEfetividade(desenho, topo);
        }
        int inicioTexto = icone == null ? x + 4 : x + icone.getWidth() + 3;
        int larguraDisponivel = x + largura - 2 - inicioTexto;
        if (larguraDisponivel <= 0) {
            return;
        }
        int separador = texto.indexOf(' ');
        if (icone != null && separador > 0
                && medirRotulo(texto.substring(separador + 1), medidas) > larguraDisponivel) {
            inicioTexto = x + 1 + icone.getWidth();
            larguraDisponivel = x + largura - 1 - inicioTexto;
        }
        desenho.clipRect(inicioTexto, topo + 1, larguraDisponivel, altura - 2);
        if (medirRotulo(texto, medidas) > larguraDisponivel && separador > 0) {
            String nome = texto.substring(0, separador);
            String usos = texto.substring(separador + 1);
            Rectangle tintaNome = medirTinta(desenho, nome);
            Rectangle tintaUsos = medirTinta(desenho, usos);
            int alturaTexto = tintaNome.height + 1 + tintaUsos.height;
            if (alturaTexto <= altura - 2) {
                int cimaTexto = topo + (altura - alturaTexto) / 2;
                int linhaBase = cimaTexto - tintaNome.y;
                desenharRotulo(desenho, nome, medidas, inicioTexto, larguraDisponivel, linhaBase);
                desenharRotulo(desenho, usos, medidas, inicioTexto, larguraDisponivel,
                        cimaTexto + tintaNome.height + 1 - tintaUsos.y);
                return;
            }
            int larguraNome = larguraDisponivel - medirRotulo(" " + usos, medidas);
            String nomeCurto = nome.substring(0, 1) + ".";
            String rotulo = medirRotulo(nomeCurto, medidas) <= larguraNome
                    ? nomeCurto + " " + usos : usos;
            Rectangle tinta = medirTinta(desenho, rotulo);
            int linhaBase = topo + (altura - tinta.height) / 2 - tinta.y;
            desenharRotulo(desenho, rotulo, medidas, inicioTexto, larguraDisponivel, linhaBase);
            return;
        }
        Rectangle tinta = medirTinta(desenho, texto);
        int linhaBase = topo + (altura - tinta.height) / 2 - tinta.y;
        desenharRotulo(desenho, texto, medidas, inicioTexto, larguraDisponivel, linhaBase);
    }

    private static Rectangle medirTinta(Graphics2D desenho, String rotulo) {
        Rectangle tinta = desenho.getFont().createGlyphVector(desenho.getFontRenderContext(),
                rotulo.replace('∞', ' ')).getPixelBounds(desenho.getFontRenderContext(), 0, 0);
        if (rotulo.indexOf('∞') >= 0) {
            int fim = Math.max(tinta.y + tinta.height, -1);
            tinta.y = Math.min(tinta.y, -4);
            tinta.height = fim - tinta.y;
        }
        return tinta;
    }

    private static void desenharRotulo(Graphics2D desenho, String rotulo, FontMetrics medidas,
            int inicioTexto, int larguraDisponivel, int linhaBase) {
        String ajustado = medirRotulo(rotulo, medidas) <= larguraDisponivel
                ? rotulo : ajustarTexto(rotulo, medidas, larguraDisponivel);
        int posicao = inicioTexto + Math.max(0,
                (larguraDisponivel - medirRotulo(ajustado, medidas)) / 2);
        int inicio = 0;
        for (int indice = 0; indice < ajustado.length(); indice++) {
            if (ajustado.charAt(indice) == '∞') {
                String trecho = ajustado.substring(inicio, indice);
                desenho.drawString(trecho, posicao, linhaBase);
                posicao += medidas.stringWidth(trecho);
                desenharInfinito(desenho, posicao, linhaBase - 4);
                posicao += 7;
                inicio = indice + 1;
            }
        }
        desenho.drawString(ajustado.substring(inicio), posicao, linhaBase);
    }

    private static int medirRotulo(String rotulo, FontMetrics medidas) {
        int largura = 0;
        int inicio = 0;
        for (int indice = 0; indice < rotulo.length(); indice++) {
            if (rotulo.charAt(indice) == '∞') {
                largura += medidas.stringWidth(rotulo.substring(inicio, indice)) + 7;
                inicio = indice + 1;
            }
        }
        return largura + medidas.stringWidth(rotulo.substring(inicio));
    }

    private void desenharMarcaEfetividade(Graphics2D desenho, int topo) {
        if (marcaEfetividade == 0) {
            return;
        }
        int esquerda = x + 1 + icone.getWidth() - 5;
        int cima = topo + (altura - icone.getHeight()) / 2;
        desenho.setColor(Paleta.CAIXA);
        desenho.fillRect(esquerda, cima, 5, 5);
        if (marcaEfetividade < 0) {
            desenho.setColor(Paleta.MARCA_INEFICAZ);
            for (int pixel = 0; pixel < 5; pixel++) {
                desenho.fillRect(esquerda + pixel, cima + pixel, 1, 1);
                desenho.fillRect(esquerda + 4 - pixel, cima + pixel, 1, 1);
            }
        } else {
            desenho.setColor(Paleta.MARCA_EFICAZ);
            desenho.fillRect(esquerda + 2, cima, 1, 5);
            desenho.fillRect(esquerda + 1, cima + 1, 3, 1);
            desenho.fillRect(esquerda, cima + 2, 1, 1);
            desenho.fillRect(esquerda + 4, cima + 2, 1, 1);
        }
        desenho.setColor(Paleta.CONTORNO);
    }

    private static void desenharInfinito(Graphics2D desenho, int x, int y) {
        desenho.fillRect(x + 1, y, 2, 1);
        desenho.fillRect(x + 4, y, 2, 1);
        desenho.fillRect(x, y + 1, 1, 1);
        desenho.fillRect(x + 3, y + 1, 1, 1);
        desenho.fillRect(x + 6, y + 1, 1, 1);
        desenho.fillRect(x + 1, y + 2, 2, 1);
        desenho.fillRect(x + 4, y + 2, 2, 1);
    }

    static void desenharCaixa(Graphics2D desenho, int x, int y,
            int largura, int altura, Color preenchimento) {
        DesenhoPixel.preencherCantosCortados(desenho, x, y + 1, largura, altura, Paleta.CONTORNO.darker());
        DesenhoPixel.preencherCantosCortados(desenho, x, y, largura, altura, Paleta.CONTORNO);
        desenho.setColor(preenchimento);
        desenho.fillRect(x + 1, y + 1, largura - 2, altura - 2);
    }

    static String ajustarTexto(String texto, FontMetrics medidas, int larguraDisponivel) {
        if (larguraDisponivel <= 0) {
            return "";
        }
        if (medidas.stringWidth(texto) <= larguraDisponivel) {
            return texto;
        }
        String sufixo = "...";
        if (medidas.stringWidth(sufixo) > larguraDisponivel) {
            return "";
        }
        int fim = texto.length();
        while (fim > 0 && medidas.stringWidth(texto.substring(0, fim) + sufixo) > larguraDisponivel) {
            fim--;
        }
        return texto.substring(0, fim) + sufixo;
    }
}
