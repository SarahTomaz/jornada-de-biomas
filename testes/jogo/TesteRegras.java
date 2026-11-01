package jogo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import jogo.arquivos.GerenciadorArquivos;
import jogo.modelo.Animal;
import jogo.modelo.AtaqueAnimal;
import jogo.modelo.Batalha;
import jogo.modelo.Bioma;
import jogo.modelo.Fase;
import jogo.modelo.Personagem;
import jogo.modelo.RegistroBatalha;
import jogo.modelo.RegistroRanking;
import jogo.modelo.ResultadoTurno;
import jogo.modelo.TabelaEfetividade;
import jogo.modelo.TipoAnimal;

public final class TesteRegras {
    private static int verificacoes;

    private TesteRegras() { }

    public static void main(String[] argumentos) throws IOException {
        testarTurnos();
        testarFinaisDeBatalha();
        testarPartidas();
        System.out.println("Regras e partidas: " + verificacoes + " verificações passaram.");
    }

    private static void testarTurnos() {
        Personagem jogador = new Personagem("Teste");
        Fase fase = criarFase(500, 3, 5, 10);
        Batalha batalha = criarBatalha(jogador, fase, 0);
        verificar(batalha.jogarTurno(0) == null, "Não agir antes de iniciar.");
        batalha.iniciar();
        verificar(batalha.getTurno() == 1, "O primeiro turno deve ser 1.");
        int agitacao = batalha.getInimigo().getAgitacao();
        verificar(batalha.jogarTurno(-1) == null && batalha.jogarTurno(5) == null,
                "Índices inválidos devem devolver null.");
        verificar(batalha.getTurno() == 1 && batalha.getInimigo().getAgitacao() == agitacao
                && jogador.getCoragem() == 100 && jogador.getFerramentas()[1].getUsosRestantes() == 3,
                "Índices inválidos não podem alterar o combate.");
        ResultadoTurno primeiro = batalha.jogarTurno(1);
        verificar(primeiro.getDanoCausado() == 30 && primeiro.getMultiplicador() == 1.5
                && primeiro.getMensagemEfetividade().equals("Foi muito eficaz!"),
                "A matriz deve influenciar o dano e a mensagem.");
        verificar(batalha.getInimigo().getAgitacao() == 470
                && jogador.getFerramentas()[1].getUsosRestantes() == 2,
                "Aplicar a ferramenta e gastar o uso exatamente uma vez.");
        verificar(!primeiro.getAtaqueUsado().getNome().equals("Especial"),
                "O primeiro ataque não pode ser especial.");
        ResultadoTurno segundo = batalha.jogarTurno(1);
        verificar(!segundo.getAtaqueUsado().getNome().equals("Especial"),
                "O segundo ataque não pode ser especial.");
        ResultadoTurno terceiro = batalha.jogarTurno(1);
        verificar(terceiro.getAtaqueUsado().getNome().equals("Especial")
                && terceiro.getDanoSofrido() == 10 && batalha.getTurno() == 4,
                "O especial deve ocorrer no terceiro turno válido.");
        agitacao = batalha.getInimigo().getAgitacao();
        int coragem = jogador.getCoragem();
        verificar(batalha.jogarTurno(1) == null && batalha.getTurno() == 4
                && batalha.getInimigo().getAgitacao() == agitacao && jogador.getCoragem() == coragem,
                "Ferramenta esgotada não pode causar resposta nem avançar turno.");
        batalha.iniciar();
        verificar(batalha.getTurno() == 4 && jogador.getFerramentas()[1].getUsosRestantes() == 0,
                "Iniciar novamente não pode recarregar ou reiniciar a batalha.");
        verificar(batalha.jogarTurno(0) != null,
                "Uma ferramenta ilimitada deve continuar disponível.");
    }

    private static void testarFinaisDeBatalha() {
        Personagem jogador = new Personagem("Teste");
        Batalha vitoria = criarBatalha(jogador, criarFase(5, 200, 200, 200), 2);
        vitoria.iniciar();
        ResultadoTurno resultado = vitoria.jogarTurno(0);
        verificar(resultado.getAnimalCalmou() && resultado.getAtaqueUsado() == null
                && resultado.getDanoSofrido() == 0 && jogador.getCoragem() == 100,
                "Animal acalmado não responde, mesmo se seu ataque seria fatal.");
        verificar(vitoria.terminou() && vitoria.venceu() && !vitoria.desistiu(),
                "A batalha deve registrar vitória.");
        verificar(jogador.getVitorias() == 0,
                "A recompensa só é aplicada pelo registro da batalha no Jogo.");
        verificar(vitoria.gerarRegistro().getCiclo() == 3,
                "O histórico deve converter o ciclo interno para exibível.");
        verificar(vitoria.jogarTurno(0) == null && vitoria.desistir() == null && vitoria.venceu(),
                "Batalha encerrada não deve aceitar novas ações.");

        jogador = new Personagem("Teste");
        Batalha derrota = criarBatalha(jogador, criarFase(500, 200, 200, 200), 0);
        derrota.iniciar();
        resultado = derrota.jogarTurno(0);
        verificar(resultado.getSemCoragem() && jogador.getCoragem() == 0
                && derrota.terminou() && !derrota.venceu() && !derrota.desistiu(),
                "Coragem deve chegar a zero e encerrar em derrota.");

        jogador = new Personagem("Teste");
        Batalha desistencia = criarBatalha(jogador, criarFase(500, 3, 5, 10), 0);
        desistencia.iniciar();
        resultado = desistencia.desistir();
        verificar(desistencia.desistiu() && desistencia.terminou() && !desistencia.venceu(),
                "Desistência deve encerrar sem vitória.");
        verificar(resultado.getNomeFerramenta().isEmpty() && resultado.getMensagemEfetividade().isEmpty()
                && resultado.getDanoCausado() == 0 && resultado.getMultiplicador() == 0
                && resultado.getAtaqueUsado() == null && resultado.getDanoSofrido() == 0
                && !resultado.getAnimalCalmou() && !resultado.getSemCoragem(),
                "A desistência deve devolver um ResultadoTurno vazio.");
        verificar(desistencia.getTurno() == 1 && jogador.getCoragem() == 100,
                "Desistir não deve causar ataque ou avançar o turno.");
    }

    private static void testarPartidas() throws IOException {
        File pasta = new File("testes/temporarios/regras");
        if (!pasta.isDirectory() && !pasta.mkdirs()) {
            throw new IOException("Não foi possível criar a pasta de testes.");
        }
        File animais = new File(pasta, "animais.csv");
        File ranking = new File(pasta, "ranking.txt");
        File semVitorias = new File(pasta, "sem-vitorias.txt");
        File rejeitado = new File(pasta, "rejeitado.txt");
        File[] temporarios = {animais, ranking, semVitorias, rejeitado};
        try {
            for (File arquivo : temporarios) {
                limpar(arquivo);
            }
            gravarFases(animais);
            GerenciadorArquivos gerenciador = new GerenciadorArquivos(animais.getPath(), ranking.getPath());
            Jogo jogo = new Jogo(gerenciador, new Random(0));
            String[] nomesInvalidos = {null, "", "   ", "1234567890123456", "Nome;inválido"};
            for (String nome : nomesInvalidos) {
                try {
                    jogo.novaPartida(nome);
                    throw new AssertionError("Nome inválido deveria ser rejeitado.");
                } catch (IllegalArgumentException esperado) {
                    verificacoes++;
                }
            }
            jogo.novaPartida("  Sárah  ");
            verificar(jogo.getJogador().getNome().equals("Sárah"), "Preservar acentos e aparar espaços.");
            String[] biomas = {"Floresta", "Deserto", "Montanha", "Pântano", "Gelo"};
            for (int indice = 0; indice < biomas.length; indice++) {
                verificar(jogo.getFaseAtual().getBioma().getNome().equals(biomas[indice]),
                        "As fases devem seguir a ordem do CSV.");
                Batalha batalha = jogo.iniciarBatalhaAtual();
                verificar(jogo.iniciarBatalhaAtual() == batalha,
                        "Iniciar a mesma batalha novamente deve manter a instância.");
                acalmar(batalha);
                jogo.registrarResultadoBatalha(batalha);
                int coragem = jogo.getJogador().getCoragem();
                jogo.registrarResultadoBatalha(batalha);
                verificar(jogo.getJogador().getVitorias() == indice + 1
                        && jogo.getHistorico().getTamanho() == indice + 1
                        && jogo.getJogador().getCoragem() == coragem,
                        "Registrar novamente não pode duplicar a vitória, recompensa ou histórico.");
            }
            verificar(jogo.getCiclo() == 1 && jogo.getFaseAtual().getBioma().getNome().equals("Floresta"),
                    "Depois do quinto bioma, voltar à Floresta no segundo ciclo.");
            Batalha finalizada = jogo.iniciarBatalhaAtual();
            verificar(finalizada.getInimigo().getAgitacaoMaxima() == 13,
                    "O segundo ciclo deve aumentar a Agitação em 30%.");
            finalizada.desistir();
            jogo.registrarResultadoBatalha(finalizada);
            verificar(jogo.partidaTerminou() && jogo.getBiomaFinal().getNome().equals("Floresta")
                    && jogo.getCicloFinal() == 1, "Guardar o bioma e ciclo onde terminou.");
            jogo.getRanking().adicionar(new RegistroRanking("Sárah", 5, 2, "2026-10-08"));
            verificar(jogo.finalizarPartida() == 2,
                    "A posição deve identificar o novo registro, mesmo com nome e nível repetidos.");
            verificar(jogo.getUltimoRegistro() == jogo.getRanking().getRegistros()[1]
                    && jogo.getUltimoRegistro().getCiclo() == 2,
                    "Guardar o registro recém-criado e seu ciclo exibível.");
            verificar(jogo.finalizarPartida() == 2 && jogo.getRanking().getQuantidade() == 2
                    && gerenciador.carregarRanking().getQuantidade() == 2,
                    "Finalizar novamente não deve duplicar a pontuação gravada.");
            RegistroBatalha ultimo = jogo.getHistorico().pop();
            verificar(ultimo.getBioma().equals("Floresta") && ultimo.getCiclo() == 2 && !ultimo.isAcalmou(),
                    "A desistência deve aparecer primeiro no histórico.");
            for (int indice = biomas.length - 1; indice >= 0; indice--) {
                verificar(jogo.getHistorico().pop().getBioma().equals(biomas[indice]),
                        "Histórico deve desempilhar da última vitória para a primeira.");
            }

            Jogo zero = new Jogo(new GerenciadorArquivos(animais.getPath(), semVitorias.getPath()), new Random(0));
            zero.novaPartida("Teste");
            Batalha primeira = zero.iniciarBatalhaAtual();
            primeira.desistir();
            zero.registrarResultadoBatalha(primeira);
            verificar(zero.finalizarPartida() == -1 && zero.getUltimoRegistro() == null
                    && zero.getRanking().getQuantidade() == 0 && !semVitorias.exists(),
                    "Partida sem vitórias não deve criar pontuação ou arquivo.");

            Jogo fora = new Jogo(new GerenciadorArquivos(animais.getPath(), rejeitado.getPath()), new Random(0));
            for (int nivel = 2; nivel <= 11; nivel++) {
                fora.getRanking().adicionar(new RegistroRanking("Pessoa " + nivel, nivel, 1, "2026-10-08"));
            }
            terminarComUmaVitoria(fora);
            verificar(fora.finalizarPartida() == -1 && fora.getRanking().getQuantidade() == 10
                    && fora.getUltimoRegistro().getNivelAlcancado() == 1,
                    "Resultado abaixo do Top 10 devolve -1 sem substituir os anteriores.");

            Jogo falha = new Jogo(new GerenciadorArquivos(animais.getPath(),
                    new File(pasta, "pasta-ausente/ranking.txt").getPath()), new Random(0));
            terminarComUmaVitoria(falha);
            verificar(falha.finalizarPartida() == 1 && !falha.getMensagemArquivo().isEmpty()
                    && falha.getRanking().getQuantidade() == 1,
                    "Falha ao salvar deve manter o resultado em memória e informar a interface.");
            Jogo falhaLeitura = new Jogo(new GerenciadorArquivos(animais.getPath(), pasta.getPath()), new Random(0));
            verificar(falhaLeitura.getRanking().getQuantidade() == 0
                    && !falhaLeitura.getMensagemArquivo().isEmpty(),
                    "Falha ao carregar ranking não deve impedir o jogo.");
        } finally {
            for (File arquivo : temporarios) {
                limpar(arquivo);
            }
            if (!pasta.delete() && pasta.exists()) {
                throw new IOException("Não foi possível limpar a pasta de testes.");
            }
        }
    }

    private static Fase criarFase(int agitacao, int danoBasico, int danoInvestida, int danoEspecial) {
        AtaqueAnimal[] ataques = {
            new AtaqueAnimal("Básico", danoBasico), new AtaqueAnimal("Investida", danoInvestida),
            new AtaqueAnimal("Especial", danoEspecial)
        };
        return new Fase(new Bioma("Floresta", "Mata.", TipoAnimal.MAMIFERO, "floresta"),
                new Animal("Onça-pintada", TipoAnimal.MAMIFERO, agitacao, ataques, "onca"));
    }

    private static Batalha criarBatalha(Personagem jogador, Fase fase, int ciclo) {
        return new Batalha(jogador, fase.gerarInimigo(ciclo), new TabelaEfetividade(), fase, ciclo, new Random(0));
    }

    private static void acalmar(Batalha batalha) {
        while (!batalha.terminou()) {
            batalha.jogarTurno(batalha.getInimigo().getTipo() == TipoAnimal.REPTIL ? 2 : 0);
        }
        verificar(batalha.venceu(), "A batalha de teste deve terminar em vitória.");
    }

    private static void terminarComUmaVitoria(Jogo jogo) {
        jogo.novaPartida("Teste");
        Batalha batalha = jogo.iniciarBatalhaAtual();
        acalmar(batalha);
        jogo.registrarResultadoBatalha(batalha);
        batalha = jogo.iniciarBatalhaAtual();
        batalha.desistir();
        jogo.registrarResultadoBatalha(batalha);
    }

    private static void gravarFases(File animais) throws IOException {
        String[] biomas = {"Floresta", "Deserto", "Montanha", "Pântano", "Gelo"};
        String[] tipos = {"MAMIFERO", "ARACNIDEO", "AVE", "REPTIL", "MAMIFERO"};
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(animais, StandardCharsets.UTF_8))) {
            escritor.write("bioma;descricaoBioma;animal;tipo;agitacao;ataque1;dano1;ataque2;dano2;ataque3;dano3;sprite;cenario\n");
            for (int indice = 0; indice < biomas.length; indice++) {
                escritor.write(biomas[indice] + ";Descrição.;Animal;" + tipos[indice]
                        + ";10;Básico;1;Investida;1;Especial;1;animal;cenario\n");
            }
        }
    }

    private static void limpar(File arquivo) throws IOException {
        if (arquivo.exists() && !arquivo.delete()) {
            throw new IOException("Não foi possível limpar " + arquivo + ".");
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        verificacoes++;
    }
}
