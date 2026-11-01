package jogo;

import java.io.IOException;
import java.util.Random;
import jogo.arquivos.GerenciadorArquivos;
import jogo.modelo.Batalha;
import jogo.modelo.ResultadoTurno;

/** Simulação de combate independente da entrada gráfica, preservada para testes. */
public final class SimulacaoConsole {
    private SimulacaoConsole() { }

    public static void main(String[] argumentos) {
        try {
            Jogo jogo = new Jogo(new GerenciadorArquivos(), new Random(0));
            if (!jogo.getMensagemArquivo().isEmpty()) {
                System.out.println(jogo.getMensagemArquivo());
            }
            jogo.novaPartida("Teste");
            Batalha batalha = jogo.iniciarBatalhaAtual();
            System.out.println("Simulação da etapa 1 — Ciclo " + (jogo.getCiclo() + 1)
                    + " | " + batalha.getFase().getBioma().getNome()
                    + " | " + batalha.getInimigo().getNome());
            while (!batalha.terminou()) {
                int turno = batalha.getTurno();
                ResultadoTurno resultado = batalha.jogarTurno(0);
                System.out.println("Turno " + turno + ": " + resultado.getNomeFerramenta()
                        + ". " + resultado.getMensagemEfetividade()
                        + " Agitação -" + resultado.getDanoCausado() + ".");
                if (resultado.getAtaqueUsado() != null) {
                    System.out.println("  " + resultado.getAtaqueUsado().getNome()
                            + ": Coragem -" + resultado.getDanoSofrido() + ".");
                }
            }
            jogo.registrarResultadoBatalha(batalha);
            System.out.println(batalha.gerarRegistro());
            if (batalha.venceu()) {
                String mensagem = "O animal relaxou e foi descansar.";
                if (jogo.getCoragemRecuperada() > 0) {
                    mensagem += " Você ganhou +" + jogo.getCoragemRecuperada() + " de Coragem!";
                }
                System.out.println(mensagem);
            }
            System.out.println("Coragem: " + jogo.getJogador().getCoragem()
                    + "/" + jogo.getJogador().getCoragemMaxima()
                    + " | Animais acalmados: " + jogo.getJogador().getVitorias());
            System.out.println("Histórico: " + jogo.getHistorico().pop());
            System.out.println("Próximo bioma: " + jogo.getFaseAtual().getBioma().getNome());
        } catch (IOException erro) {
            System.out.println(erro.getMessage());
            System.exit(1);
        }
    }
}
