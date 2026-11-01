package jogo.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import jogo.Config;
import jogo.modelo.Animal;
import jogo.modelo.Batalha;
import jogo.modelo.Ferramenta;

final class VisualBatalha {
    private static final int[] DESLOCAMENTOS_TREMOR = {2, -2, 1, -1, 2, -1, 0, 1};
    private final Recursos recursos;
    private final Particulas particulas;
    private final Batalha batalha;
    private final BarraStatus coragem;
    private final BarraStatus agitacao;
    private final CaixaDialogo dialogo;
    private final Botao[] botoes;
    private final Botao sim;
    private final Botao nao;
    private final Font fonte;
    private final BufferedImage fundo;

    VisualBatalha(Recursos recursos, Batalha batalha, BarraStatus coragem,
            BarraStatus agitacao, CaixaDialogo dialogo, Botao[] botoes,
            Botao sim, Botao nao) {
        this.recursos = recursos;
        particulas = new Particulas(recursos);
        this.batalha = batalha;
        this.coragem = coragem;
        this.agitacao = agitacao;
        this.dialogo = dialogo;
        this.botoes = botoes;
        this.sim = sim;
        this.nao = nao;
        fonte = recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA);
        fundo = recursos.getCenario(batalha.getFase().getBioma().getCenario());
    }

    void desenhar(Graphics2D grafico, TelaBatalha.Estado estado,
            double tempoEtapa, double tempoTotal, int indiceFerramenta,
            boolean confirmar, boolean ferramentaIneficaz, boolean contraefeito) {
        Graphics2D desenho = (Graphics2D) grafico.create();
        try {
            aplicarTremor(desenho, estado, tempoEtapa, contraefeito);
            desenho.drawImage(fundo, 0, 0, Config.LARGURA_LOGICA,
                    Config.ALTURA_LOGICA, null);
            desenharFiguras(desenho, estado, tempoEtapa, tempoTotal, contraefeito);
            if (estado == TelaBatalha.Estado.ANIMANDO_JOGADOR && !contraefeito) {
                desenharFerramenta(desenho, tempoEtapa, tempoTotal, indiceFerramenta, !ferramentaIneficaz);
            } else if (estado == TelaBatalha.Estado.VITORIA) {
                particulas.desenharCoracoes(desenho, tempoEtapa);
            }
            desenharHud(desenho);
            Botao.desenharCaixa(desenho, 0, 118, 320, 62, Paleta.CAIXA);
            dialogo.desenhar(desenho);
            for (Botao botao : botoes) {
                botao.desenhar(desenho);
            }
            if (confirmar) {
                desenharConfirmacao(desenho);
            }
        } finally {
            desenho.dispose();
        }
    }

    private void aplicarTremor(Graphics2D desenho, TelaBatalha.Estado estado,
            double tempoEtapa, boolean contraefeito) {
        if ((estado != TelaBatalha.Estado.ANIMANDO_ANIMAL && !contraefeito)
                || tempoEtapa >= Config.DURACAO_TREMOR_MS / 1000.0) {
            return;
        }
        int quadro = (int) (tempoEtapa * Config.FPS * 2);
        int deslocamento = DESLOCAMENTOS_TREMOR[quadro % DESLOCAMENTOS_TREMOR.length];
        desenho.translate(deslocamento, quadro % 2 == 0 ? 1 : -1);
    }

    private void desenharFiguras(Graphics2D desenho, TelaBatalha.Estado estado,
            double tempoEtapa, double tempoTotal, boolean contraefeito) {
        int quadroIdle = (int) (tempoTotal / Config.INTERVALO_IDLE_SEGUNDOS) % 2;
        String idle = quadroIdle == 0 ? "idle_1" : "idle_2";
        String quadroPersonagem = switch (estado) {
            case ANIMANDO_JOGADOR -> "usando";
            case ANIMANDO_ANIMAL, DERROTA -> "assustado";
            case VITORIA -> "feliz";
            default -> idle;
        };
        String quadroAnimal = switch (estado) {
            case ANIMANDO_ANIMAL, DERROTA -> "agitado";
            case VITORIA -> "calmo";
            default -> idle;
        };
        if (contraefeito) {
            quadroPersonagem = "assustado";
            quadroAnimal = "agitado";
        }
        int deslocamentoAnimal = 0;
        if (estado == TelaBatalha.Estado.ANIMANDO_ANIMAL) {
            double progresso = Math.min(1, tempoEtapa / (Config.DURACAO_ACAO_MS / 1000.0));
            deslocamentoAnimal = -(int) Math.round(Config.RECUO_ATAQUE_PIXELS
                    * (1 - Math.abs(2 * progresso - 1)));
        }
        int balancoPersonagem = quadroPersonagem.equals(idle) ? quadroIdle : 0;
        int balancoAnimal = quadroAnimal.equals(idle) ? quadroIdle : 0;
        if (contraefeito) {
            balancoAnimal = (int) Math.round(Math.sin(tempoEtapa * 18));
        }
        BufferedImage personagem = recursos.getPersonagem(quadroPersonagem);
        desenho.drawImage(personagem, Config.PERSONAGEM_BATALHA_X,
                Config.PERSONAGEM_BATALHA_Y + balancoPersonagem, null);
        Animal animal = batalha.getInimigo();
        BufferedImage figuraAnimal = recursos.getAnimal(animal.getSprite(), quadroAnimal,
                animal.getTipo(), animal.getNome());
        desenho.drawImage(figuraAnimal, Config.ANIMAL_BATALHA_X + deslocamentoAnimal,
                Config.ANIMAL_BATALHA_Y + balancoAnimal, null);
    }

    private void desenharFerramenta(Graphics2D desenho, double tempoEtapa,
            double tempoTotal, int indiceFerramenta, boolean exibirEstrelas) {
        Ferramenta[] ferramentas = batalha.getJogador().getFerramentas();
        if (indiceFerramenta < 0 || indiceFerramenta >= ferramentas.length) {
            return;
        }
        double duracao = Config.DURACAO_ACAO_MS / 1000.0;
        double progresso = Math.min(1, tempoEtapa / duracao);
        if (tempoEtapa <= duracao) {
            int x = (int) Math.round(60 + (226 - 60) * progresso);
            int y = (int) Math.round(94 + (68 - 94) * progresso
                    - 24 * Math.sin(Math.PI * progresso));
            desenho.drawImage(recursos.getIconeFerramenta(ferramentas[indiceFerramenta].getTipo()),
                    x, y, null);
        }
        if (exibirEstrelas) {
            particulas.desenharEstrelas(desenho, tempoEtapa, tempoTotal);
        }
    }

    private void desenharHud(Graphics2D desenho) {
        Botao.desenharCaixa(desenho, 4, 4, 156, 32, Paleta.CAIXA);
        Botao.desenharCaixa(desenho, 208, 4, 108, 42, Paleta.CAIXA);
        coragem.desenhar(desenho);
        agitacao.desenhar(desenho);
        desenho.setColor(Paleta.CONTORNO);
        desenho.setFont(fonte);
        desenho.drawString(Math.round(coragem.getValorExibido()) + "/" + coragem.getMaximo(),
                108, 28);
        desenho.drawImage(recursos.getCoracao(), 8, 7, null);
        desenho.drawString("Coragem", 22, 15);
        Animal animal = batalha.getInimigo();
        FontMetrics medidas = desenho.getFontMetrics();
        desenho.drawString(Botao.ajustarTexto(animal.getNome(), medidas, 100), 212, 28);
        desenho.drawString("(" + animal.getTipo().getNomeExibicao() + ")", 212, 41);
    }

    private void desenharConfirmacao(Graphics2D desenho) {
        desenho.setColor(new Color(Paleta.CONTORNO.getRed(), Paleta.CONTORNO.getGreen(),
                Paleta.CONTORNO.getBlue(), 110));
        desenho.fillRect(0, 0, Config.LARGURA_LOGICA, Config.ALTURA_LOGICA);
        Botao.desenharCaixa(desenho, 72, 62, 176, 70, Paleta.CAIXA);
        desenho.setFont(fonte);
        desenho.setColor(Paleta.CONTORNO);
        String pergunta = "Voltar para casa?";
        desenho.drawString(pergunta, (Config.LARGURA_LOGICA
                - desenho.getFontMetrics().stringWidth(pergunta)) / 2, 84);
        sim.desenhar(desenho);
        nao.desenhar(desenho);
    }
}
