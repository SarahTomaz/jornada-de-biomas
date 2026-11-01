package jogo.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import jogo.Config;

public final class BarraStatus {
    private final int x;
    private final int y;
    private final int largura;
    private final int altura;
    private final Color cor;
    private final Color corPiscar;
    private int valor;
    private int maximo;
    private double valorExibido;
    private double valorInicial;
    private double tempoAnimacaoMs;
    private double tempoPiscarMs;

    public BarraStatus(int x, int y, int largura, int altura, Color cor, int valor, int maximo) {
        if (largura < 3 || altura < 3 || cor == null) {
            throw new IllegalArgumentException("Geometria ou cor da barra inválida.");
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.cor = cor;
        corPiscar = new Color(cor.getRed() + (255 - cor.getRed()) / 8,
                cor.getGreen() + (255 - cor.getGreen()) / 8,
                cor.getBlue() + (255 - cor.getBlue()) / 8, cor.getAlpha());
        this.maximo = Math.max(1, maximo);
        this.valor = Math.max(0, Math.min(valor, this.maximo));
        valorExibido = this.valor;
        valorInicial = valorExibido;
        tempoAnimacaoMs = Config.DURACAO_BARRA_MS;
    }

    public void setValores(int valor, int maximo) {
        int novoMaximo = Math.max(1, maximo);
        int novoValor = Math.max(0, Math.min(valor, novoMaximo));
        if (this.valor == novoValor && this.maximo == novoMaximo) {
            return;
        }
        this.maximo = novoMaximo;
        this.valor = novoValor;
        valorExibido = Math.min(valorExibido, novoMaximo);
        valorInicial = valorExibido;
        tempoAnimacaoMs = 0;
    }

    public void setValor(int valor) {
        setValores(valor, maximo);
    }

    public void atualizar(double dt) {
        if (!Double.isFinite(dt) || dt <= 0) {
            return;
        }
        tempoAnimacaoMs = Math.min(Config.DURACAO_BARRA_MS, tempoAnimacaoMs + dt * 1000);
        double progresso = tempoAnimacaoMs / Config.DURACAO_BARRA_MS;
        valorExibido = valorInicial + (valor - valorInicial) * progresso;
        tempoPiscarMs = (tempoPiscarMs + dt * 1000) % (Config.INTERVALO_CURSOR_MS * 2);
    }

    public int getValor() { return valor; }
    public int getMaximo() { return maximo; }
    public double getValorExibido() { return valorExibido; }

    public void desenhar(Graphics2D grafico) {
        Graphics2D desenho = (Graphics2D) grafico.create();
        Botao.desenharCaixa(desenho, x, y, largura, altura, Paleta.FUNDO_BARRA);
        boolean devePiscar = valorExibido / maximo < Config.LIMIAR_BARRA_BAIXA
                && tempoPiscarMs >= Config.INTERVALO_CURSOR_MS;
        desenho.setColor(devePiscar ? corPiscar : cor);
        int preenchimento = (int) Math.round((largura - 2) * valorExibido / maximo);
        desenho.fillRect(x + 1, y + 1, preenchimento, altura - 2);
        desenho.dispose();
    }
}
