package jogo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import jogo.arquivos.GerenciadorArquivos;
import jogo.estruturas.ListaEncadeada;
import jogo.estruturas.Pilha;
import jogo.modelo.Batalha;
import jogo.modelo.Bioma;
import jogo.modelo.Fase;
import jogo.modelo.Personagem;
import jogo.modelo.Ranking;
import jogo.modelo.RegistroBatalha;
import jogo.modelo.RegistroRanking;
import jogo.modelo.TabelaEfetividade;
import jogo.modelo.TipoAnimal;
import jogo.modelo.TipoFerramenta;
import jogo.modelo.ValidadorNome;

public final class Jogo {
    private enum EstadoPartida {
        SEM_PARTIDA, AGUARDANDO_BATALHA, EM_BATALHA, ENCERRADA, FINALIZADA
    }

    private final ListaEncadeada<Fase> fases;
    private final Ranking ranking;
    private final GerenciadorArquivos gerenciadorArquivos;
    private final TabelaEfetividade tabela = new TabelaEfetividade();
    private final Random sorteador;
    private EstadoPartida estado = EstadoPartida.SEM_PARTIDA;
    private Personagem jogador;
    private Pilha<RegistroBatalha> historico;
    private RegistroBatalha[] historicoFinal;
    private Batalha batalhaAtual;
    private Batalha ultimaBatalhaRegistrada;
    private RegistroRanking ultimoRegistro;
    private Bioma biomaFinal;
    private int cicloFinal;
    private int coragemRecuperada;
    private int posicaoObtida = -1;
    private String mensagemArquivo = "";
    private boolean[][] efetividadesDescobertas = criarMatrizDescobertas();

    public Jogo() throws IOException {
        this(new GerenciadorArquivos(), new Random());
    }

    public Jogo(GerenciadorArquivos gerenciadorArquivos, Random sorteador) throws IOException {
        if (gerenciadorArquivos == null || sorteador == null) {
            throw new IllegalArgumentException("Dados do jogo inválidos.");
        }
        this.gerenciadorArquivos = gerenciadorArquivos;
        this.sorteador = sorteador;
        fases = gerenciadorArquivos.carregarFases();
        Ranking rankingCarregado;
        try {
            rankingCarregado = gerenciadorArquivos.carregarRanking();
        } catch (IOException erro) {
            rankingCarregado = new Ranking();
            mensagemArquivo = "Não foi possível carregar o ranking. Será usado um ranking vazio. "
                    + erro.getMessage();
        }
        ranking = rankingCarregado;
    }

    public void novaPartida(String nome) {
        String nomeValidado = ValidadorNome.validar(nome, "Informe um nome com 1 a "
                + Config.TAMANHO_MAXIMO_NOME + " caracteres.");
        for (int indice = 0; indice < nomeValidado.length(); indice++) {
            char caractere = nomeValidado.charAt(indice);
            if (!Character.isLetterOrDigit(caractere) && caractere != ' ') {
                throw new IllegalArgumentException("O nome aceita letras, números, espaços e acentos.");
            }
        }
        jogador = new Personagem(nomeValidado);
        historico = new Pilha<>();
        historicoFinal = null;
        efetividadesDescobertas = criarMatrizDescobertas();
        fases.reiniciar();
        batalhaAtual = null;
        ultimaBatalhaRegistrada = null;
        ultimoRegistro = null;
        biomaFinal = null;
        cicloFinal = 0;
        coragemRecuperada = 0;
        posicaoObtida = -1;
        estado = EstadoPartida.AGUARDANDO_BATALHA;
    }

    public Batalha iniciarBatalhaAtual() {
        if (estado == EstadoPartida.EM_BATALHA) {
            return batalhaAtual;
        }
        if (estado != EstadoPartida.AGUARDANDO_BATALHA) {
            throw new IllegalStateException("Inicie uma partida antes de começar uma batalha.");
        }
        Fase fase = getFaseAtual();
        int ciclo = getCiclo();
        batalhaAtual = new Batalha(jogador, fase.gerarInimigo(ciclo), tabela,
                fase, ciclo, sorteador, efetividadesDescobertas);
        batalhaAtual.iniciar();
        estado = EstadoPartida.EM_BATALHA;
        return batalhaAtual;
    }

    public void registrarResultadoBatalha(Batalha batalha) {
        if (batalha != null && batalha == ultimaBatalhaRegistrada) {
            return;
        }
        if (estado != EstadoPartida.EM_BATALHA || batalha == null || batalha != batalhaAtual
                || !batalha.terminou()) {
            throw new IllegalStateException("Só é possível registrar a batalha atual depois de terminada.");
        }
        historico.push(batalha.gerarRegistro());
        ultimaBatalhaRegistrada = batalha;
        coragemRecuperada = 0;
        if (batalha.venceu()) {
            coragemRecuperada = jogador.evoluir();
            fases.avancar();
            estado = EstadoPartida.AGUARDANDO_BATALHA;
        } else {
            biomaFinal = batalha.getFase().getBioma();
            cicloFinal = batalha.getCiclo();
            estado = EstadoPartida.ENCERRADA;
        }
    }

    public int finalizarPartida() {
        if (estado == EstadoPartida.FINALIZADA) {
            return posicaoObtida;
        }
        if (estado != EstadoPartida.ENCERRADA) {
            throw new IllegalStateException("A partida só pode ser finalizada após derrota ou desistência.");
        }
        if (jogador.getVitorias() >= 1) {
            ultimoRegistro = new RegistroRanking(jogador.getNome(), jogador.getVitorias(),
                    cicloFinal + 1, LocalDate.now().toString());
            ranking.adicionar(ultimoRegistro);
            posicaoObtida = ranking.posicaoDe(ultimoRegistro);
            try {
                gerenciadorArquivos.salvarRanking(ranking);
                mensagemArquivo = "";
            } catch (IOException erro) {
                mensagemArquivo = "Não foi possível salvar o ranking. O resultado continua nesta sessão. "
                        + erro.getMessage();
            }
        }
        estado = EstadoPartida.FINALIZADA;
        return posicaoObtida;
    }

    public Fase getFaseAtual() { return fases.getAtual(); }
    public int getCiclo() { return jogador == null ? 0 : jogador.getVitorias() / fases.getTamanho(); }
    public Personagem getJogador() { return jogador; }
    public Ranking getRanking() { return ranking; }
    public Pilha<RegistroBatalha> getHistorico() { return historico; }
    public Batalha getBatalhaAtual() { return batalhaAtual; }
    public RegistroRanking getUltimoRegistro() { return ultimoRegistro; }
    public Bioma getBiomaFinal() { return biomaFinal; }
    public int getCicloFinal() { return cicloFinal; }
    public int getCoragemRecuperada() { return coragemRecuperada; }
    public String getMensagemArquivo() { return mensagemArquivo; }
    public int getPosicaoObtida() { return posicaoObtida; }

    public boolean partidaFinalizada() {
        return estado == EstadoPartida.FINALIZADA;
    }

    public RegistroBatalha[] getHistoricoFinal() {
        if (!partidaFinalizada()) {
            throw new IllegalStateException("Finalize a partida antes de obter o histórico final.");
        }
        if (historicoFinal == null) {
            historicoFinal = new RegistroBatalha[historico.getTamanho()];
            for (int indice = 0; indice < historicoFinal.length; indice++) {
                historicoFinal[indice] = historico.pop();
            }
        }
        return historicoFinal.clone();
    }

    private static boolean[][] criarMatrizDescobertas() {
        return new boolean[TipoFerramenta.values().length][TipoAnimal.values().length];
    }

    public boolean efetividadeDescoberta(TipoFerramenta ferramenta, TipoAnimal animal) {
        return ferramenta != null && animal != null
                && efetividadesDescobertas[ferramenta.ordinal()][animal.ordinal()];
    }

    public boolean[][] getEfetividadesDescobertas() {
        boolean[][] copia = new boolean[efetividadesDescobertas.length][];
        for (int ferramenta = 0; ferramenta < efetividadesDescobertas.length; ferramenta++) {
            copia[ferramenta] = new boolean[efetividadesDescobertas[ferramenta].length];
            for (int animal = 0; animal < efetividadesDescobertas[ferramenta].length; animal++) {
                copia[ferramenta][animal] = efetividadesDescobertas[ferramenta][animal];
            }
        }
        return copia;
    }

    public boolean partidaTerminou() {
        return estado == EstadoPartida.ENCERRADA || estado == EstadoPartida.FINALIZADA;
    }
}
