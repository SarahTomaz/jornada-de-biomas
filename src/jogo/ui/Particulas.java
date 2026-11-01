package jogo.ui;

import java.awt.Graphics2D;
import jogo.Config;

/** Efeitos visuais sincronizados com o relógio da tela de batalha. */
public final class Particulas {
    // Posições horizontais de origem dos cinco corações.
    private static final int[] POSICOES_CORACOES = {208, 221, 234, 247, 258};
    // Posições de origem das três estrelas sobre o animal.
    private static final int[][] POSICOES_ESTRELAS = {{201, 49}, {231, 48}, {260, 52}};
    // Fração do voo concluída antes de aparecerem estrelas.
    private static final double FRACAO_INICIO_ESTRELAS = 0.7;
    // Duração de cada alternância do brilho, em segundos.
    private static final double PERIODO_BRILHO_SEGUNDOS = 0.25;
    // Converte o tempo do brilho em deslocamento de pixels.
    private static final int VELOCIDADE_BRILHO = 8;
    // Duração de uma subida completa dos corações, em segundos.
    private static final double PERIODO_CORACOES_SEGUNDOS = 1.2;
    // Atraso entre corações consecutivos, em segundos.
    private static final double DEFASAGEM_CORACOES_SEGUNDOS = 0.17;
    // Uma volta completa da oscilação corresponde a dois pi.
    private static final int FATOR_VOLTA_COMPLETA = 2;
    // Amplitude horizontal da oscilação dos corações, em pixels.
    private static final int AMPLITUDE_BALANCO_CORACOES = 2;
    // Linha inicial dos corações no canvas lógico.
    private static final int ALTURA_INICIAL_CORACOES = 76;
    // Distância vertical percorrida por cada coração, em pixels.
    private static final int DISTANCIA_SUBIDA_CORACOES = 34;
    private final Recursos recursos;

    public Particulas(Recursos recursos) {
        if (recursos == null) {
            throw new IllegalArgumentException("Os recursos das partículas são obrigatórios.");
        }
        this.recursos = recursos;
    }

    public void desenharEstrelas(Graphics2D desenho, double tempoEtapa, double tempoTotal) {
        double duracao = Config.DURACAO_ACAO_MS / 1000.0;
        if (!tempoValido(tempoEtapa) || !tempoValido(tempoTotal)
                || tempoEtapa < duracao * FRACAO_INICIO_ESTRELAS) {
            return;
        }
        int brilho = (int) ((tempoTotal % PERIODO_BRILHO_SEGUNDOS) * VELOCIDADE_BRILHO);
        desenho.drawImage(recursos.getEstrela(), POSICOES_ESTRELAS[0][0],
                POSICOES_ESTRELAS[0][1] + brilho, null);
        desenho.drawImage(recursos.getEstrela(), POSICOES_ESTRELAS[1][0],
                POSICOES_ESTRELAS[1][1] + (1 - brilho), null);
        if (brilho == 0) {
            desenho.drawImage(recursos.getEstrela(), POSICOES_ESTRELAS[2][0],
                    POSICOES_ESTRELAS[2][1], null);
        }
    }

    public void desenharCoracoes(Graphics2D desenho, double tempoEtapa) {
        if (!tempoValido(tempoEtapa)) {
            return;
        }
        for (int indice = 0; indice < POSICOES_CORACOES.length; indice++) {
            double progresso = ((tempoEtapa % PERIODO_CORACOES_SEGUNDOS)
                    + indice * DEFASAGEM_CORACOES_SEGUNDOS)
                    % PERIODO_CORACOES_SEGUNDOS / PERIODO_CORACOES_SEGUNDOS;
            int x = POSICOES_CORACOES[indice]
                    + (int) Math.round(Math.sin(progresso * Math.PI * FATOR_VOLTA_COMPLETA)
                            * AMPLITUDE_BALANCO_CORACOES);
            int y = (int) Math.round(ALTURA_INICIAL_CORACOES
                    - progresso * DISTANCIA_SUBIDA_CORACOES);
            desenho.drawImage(recursos.getCoracao(), x, y, null);
        }
    }

    private static boolean tempoValido(double tempo) {
        return Double.isFinite(tempo) && tempo >= 0;
    }
}
