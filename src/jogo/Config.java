package jogo;

public final class Config {
    // Regras do jogo
    public static final int CORAGEM_INICIAL = 100;
    public static final int TAMANHO_RANKING = 10;
    public static final double CRESCIMENTO_AGITACAO = 0.30;
    public static final double CRESCIMENTO_DANO = 0.20;
    public static final int RECUPERACAO_CORAGEM = 20;
    public static final int AUMENTO_CORAGEM_MAXIMA = 5;
    public static final int AUMENTO_BONUS_PODER = 1;
    public static final int VITORIAS_POR_DARDO = 5;
    public static final int MAXIMO_DARDOS = 5;
    public static final int INTERVALO_ESPECIAL = 3;
    public static final int CONTRAEFEITO_BASE = 6;
    public static final double CONTRAEFEITO_POR_CICLO = 0.20;
    public static final double MULTIPLICADOR_MUITO_EFICAZ = 1.5;
    public static final double MULTIPLICADOR_POUCO_EFICAZ = 0.5;
    public static final int TAMANHO_MAXIMO_NOME = 15;

    // Ferramentas e animais
    public static final int QUANTIDADE_FERRAMENTAS = 5;
    public static final int QUANTIDADE_ATAQUES_ANIMAL = 3;
    public static final int INDICE_ATAQUE_ESPECIAL = 2;
    public static final int QUANTIDADE_ATAQUES_COMUNS = 2;
    public static final int USOS_ILIMITADOS = -1;
    public static final int PODER_FLAUTA = 10;
    public static final int PODER_ISCA = 20;
    public static final int PODER_LANTERNA = 14;
    public static final int PODER_REPELENTE = 20;
    public static final int PODER_DARDO = 40;
    public static final int USOS_ISCA_POR_BATALHA = 3;
    public static final int USOS_REPELENTE_POR_BATALHA = 3;
    public static final int DARDOS_INICIAIS = 3;

    // Interface
    public static final int LARGURA_LOGICA = 320;
    public static final int ALTURA_LOGICA = 180;
    public static final int ESCALA = 3;
    public static final int LARGURA_JANELA = LARGURA_LOGICA * ESCALA;
    public static final int ALTURA_JANELA = ALTURA_LOGICA * ESCALA;
    public static final int FPS = 30;
    public static final float TAMANHO_FONTE_PEQUENA = 8f;
    public static final float TAMANHO_FONTE_NORMAL = TAMANHO_FONTE_PEQUENA;
    public static final float TAMANHO_FONTE_GRANDE = 12f;
    public static final float TAMANHO_FONTE_TITULO_GRANDE = 16f;
    public static final int LINHA_CHAO_BATALHA = 112;
    public static final int MS_POR_LETRA = 25;
    public static final int DURACAO_FADE_MS = 300;
    public static final int DURACAO_TREMOR_MS = 250;
    public static final int DURACAO_BARRA_MS = 400;
    public static final int DURACAO_ACAO_MS = 400;
    public static final int INTERVALO_CLIQUES_MS = 200;
    public static final int INTERVALO_CURSOR_MS = 500;
    public static final double DURACAO_TRANSICAO_BIOMA_SEGUNDOS = 2;
    public static final double INTERVALO_IDLE_SEGUNDOS = 0.5;
    public static final double LIMIAR_BARRA_BAIXA = 0.25;
    public static final int RECUO_ATAQUE_PIXELS = 8;
    public static final int PERSONAGEM_BATALHA_X = 50;
    public static final int PERSONAGEM_BATALHA_Y = 82;
    public static final int ANIMAL_BATALHA_X = 200;
    public static final int ANIMAL_BATALHA_Y = 54;

    private Config() {
    }
}
