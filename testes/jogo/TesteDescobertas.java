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
import jogo.modelo.ResultadoTurno;
import jogo.modelo.TabelaEfetividade;
import jogo.modelo.TipoAnimal;
import jogo.modelo.TipoFerramenta;

public final class TesteDescobertas {
    private static int verificacoes;

    private TesteDescobertas() {
    }

    public static void main(String[] argumentos) throws IOException {
        testarTiposDeEfetividade();
        testarAcoesInvalidas();
        testarMemoriaDaPartida();
        System.out.println("Descobertas: " + verificacoes + " verificações passaram.");
    }

    private static void testarTiposDeEfetividade() {
        TabelaEfetividade tabela = new TabelaEfetividade();
        boolean encontrouMeio = false;
        boolean encontrouZero = false;
        for (TipoFerramenta ferramenta : TipoFerramenta.values()) {
            for (TipoAnimal tipo : TipoAnimal.values()) {
                boolean[][] descobertas = matrizVazia();
                Batalha batalha = criarBatalha(tipo, 1000, descobertas);
                batalha.iniciar();
                ResultadoTurno resultado = batalha.jogarTurno(ferramenta.ordinal());
                double multiplicador = tabela.getMultiplicador(ferramenta, tipo);
                boolean deveDescobrir = multiplicador == 0 || multiplicador >= 1.5;
                verificar(resultado != null && resultado.getMultiplicador() == multiplicador,
                        "A descoberta deve usar o multiplicador do turno executado.");
                verificar(descobertas[ferramenta.ordinal()][tipo.ordinal()] == deveDescobrir,
                        "Descobrir somente efeito zero ou muito eficaz: " + ferramenta + "/" + tipo);
                verificar(contarDescobertas(descobertas) == (deveDescobrir ? 1 : 0),
                        "Um turno só pode revelar o par de ferramenta e tipo realmente utilizado.");
                encontrouMeio |= multiplicador == 0.5;
                encontrouZero |= multiplicador == 0;
            }
        }
        verificar(encontrouMeio && encontrouZero, "O teste deve cobrir tanto 0,5 quanto zero.");
    }

    private static void testarAcoesInvalidas() {
        boolean[][] descobertas = matrizVazia();
        Batalha batalha = criarBatalha(TipoAnimal.MAMIFERO, 1000, descobertas);
        verificar(batalha.jogarTurno(0) == null && contarDescobertas(descobertas) == 0,
                "Ação antes de iniciar não deve revelar um par eficaz.");
        batalha.iniciar();
        verificar(batalha.jogarTurno(-1) == null && batalha.jogarTurno(5) == null
                && contarDescobertas(descobertas) == 0, "Índices inválidos não devem revelar nada.");
        for (int uso = 0; uso < 3; uso++) {
            batalha.getJogador().getFerramentas()[TipoFerramenta.ISCA.ordinal()].usar();
        }
        verificar(batalha.jogarTurno(TipoFerramenta.ISCA.ordinal()) == null
                && contarDescobertas(descobertas) == 0 && batalha.getTurno() == 1,
                "Ferramenta esgotada não deve descobrir efetividade nem avançar o turno.");
        batalha.desistir();
        verificar(contarDescobertas(descobertas) == 0,
                "O ResultadoTurno vazio da desistência não pode ser interpretado como descoberta zero.");

        descobertas = matrizVazia();
        batalha = criarBatalha(TipoAnimal.MAMIFERO, 1, descobertas);
        batalha.iniciar();
        batalha.jogarTurno(TipoFerramenta.FLAUTA.ordinal());
        verificar(batalha.venceu() && descobertas[TipoFerramenta.FLAUTA.ordinal()]
                [TipoAnimal.MAMIFERO.ordinal()], "A vitória também deve registrar a descoberta eficaz.");
        verificar(batalha.jogarTurno(TipoFerramenta.REPELENTE.ordinal()) == null
                && !descobertas[TipoFerramenta.REPELENTE.ordinal()][TipoAnimal.MAMIFERO.ordinal()],
                "Uma ação após o fim não deve revelar outro par.");
    }

    private static void testarMemoriaDaPartida() throws IOException {
        File pasta = new File("testes/temporarios/descobertas");
        if (!pasta.isDirectory() && !pasta.mkdirs()) {
            throw new IOException("Não foi possível criar a pasta de teste de descobertas.");
        }
        File animais = new File(pasta, "animais.csv");
        File ranking = new File(pasta, "ranking.txt");
        try {
            gravarFases(animais);
            Jogo jogo = new Jogo(new GerenciadorArquivos(animais.getPath(), ranking.getPath()), new Random(0));
            verificar(contarDescobertas(jogo.getEfetividadesDescobertas()) == 0,
                    "Um jogo recém-carregado deve começar sem descobertas.");
            jogo.novaPartida("Descobertas");
            boolean[][] copia = jogo.getEfetividadesDescobertas();
            verificar(copia.length == 5 && copia[0].length == 4,
                    "A memória deve ter cinco ferramentas por quatro tipos de animal.");
            copia[0][0] = true;
            copia[1] = new boolean[] {true, true, true, true};
            verificar(contarDescobertas(jogo.getEfetividadesDescobertas()) == 0,
                    "Alterar elementos ou linhas do snapshot não deve revelar nada na partida.");

            Batalha batalha = jogo.iniciarBatalhaAtual();
            verificar(batalha.jogarTurno(TipoFerramenta.LANTERNA.ordinal()).getMultiplicador() == 1
                    && !jogo.efetividadeDescoberta(TipoFerramenta.LANTERNA, TipoAnimal.MAMIFERO),
                    "Efeito normal não deve produzir descoberta.");
            batalha.jogarTurno(TipoFerramenta.REPELENTE.ordinal());
            verificar(jogo.efetividadeDescoberta(TipoFerramenta.REPELENTE, TipoAnimal.MAMIFERO),
                    "Uma ferramenta sem efeito deve ser descoberta imediatamente no Jogo.");
            batalha.jogarTurno(TipoFerramenta.FLAUTA.ordinal());
            verificar(jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.MAMIFERO),
                    "Uma ferramenta muito eficaz deve ser descoberta imediatamente no Jogo.");
            verificar(!jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.REPTIL),
                    "Descobrir em um tipo não pode revelar a ferramenta em outro tipo.");
            copia = jogo.getEfetividadesDescobertas();
            copia[TipoFerramenta.FLAUTA.ordinal()][TipoAnimal.MAMIFERO.ordinal()] = false;
            verificar(jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.MAMIFERO),
                    "O snapshot também deve impedir que uma descoberta seja apagada de fora.");
            acalmar(batalha, TipoFerramenta.FLAUTA);
            jogo.registrarResultadoBatalha(batalha);

            batalha = jogo.iniciarBatalhaAtual();
            verificar(jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.MAMIFERO),
                    "Avançar o bioma deve preservar a memória dos tipos anteriores.");
            verificar(batalha.jogarTurno(TipoFerramenta.FLAUTA.ordinal()).getMultiplicador() == 0.5
                    && !jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.ARACNIDEO),
                    "Efeito reduzido de 0,5 não deve receber marca de zero.");
            acalmar(batalha, TipoFerramenta.LANTERNA);
            jogo.registrarResultadoBatalha(batalha);
            batalha = jogo.iniciarBatalhaAtual();
            acalmar(batalha, TipoFerramenta.FLAUTA);
            jogo.registrarResultadoBatalha(batalha);
            batalha = jogo.iniciarBatalhaAtual();
            acalmar(batalha, TipoFerramenta.LANTERNA);
            jogo.registrarResultadoBatalha(batalha);
            batalha = jogo.iniciarBatalhaAtual();
            verificar(jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.MAMIFERO)
                    && jogo.efetividadeDescoberta(TipoFerramenta.REPELENTE, TipoAnimal.MAMIFERO),
                    "Outro animal do mesmo tipo deve herdar ambas as descobertas.");
            acalmar(batalha, TipoFerramenta.FLAUTA);
            jogo.registrarResultadoBatalha(batalha);
            verificar(jogo.getCiclo() == 1
                    && jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.MAMIFERO),
                    "A passagem para o segundo ciclo deve manter as descobertas da partida.");

            try {
                jogo.novaPartida(" ");
                throw new AssertionError("Nome inválido deveria ser rejeitado.");
            } catch (IllegalArgumentException esperado) {
                verificar(jogo.efetividadeDescoberta(TipoFerramenta.FLAUTA, TipoAnimal.MAMIFERO),
                        "Nome rejeitado não deve apagar a partida atual.");
            }
            jogo.novaPartida("Outra pessoa");
            verificar(contarDescobertas(jogo.getEfetividadesDescobertas()) == 0,
                    "Uma nova partida deve reiniciar todas as descobertas.");
            Batalha antiga = jogo.iniciarBatalhaAtual();
            jogo.novaPartida("Nova partida");
            antiga.jogarTurno(TipoFerramenta.FLAUTA.ordinal());
            verificar(contarDescobertas(jogo.getEfetividadesDescobertas()) == 0,
                    "Uma batalha antiga não pode alterar a nova matriz da partida seguinte.");
            Jogo reaberto = new Jogo(new GerenciadorArquivos(animais.getPath(), ranking.getPath()),
                    new Random(0));
            verificar(contarDescobertas(reaberto.getEfetividadesDescobertas()) == 0 && !ranking.exists(),
                    "Descobertas não são persistidas e o teste não deve gravar ranking.");
        } finally {
            if (animais.exists() && !animais.delete()) {
                throw new IOException("Não foi possível remover o CSV temporário de descobertas.");
            }
            if (!pasta.delete() && pasta.exists()) {
                throw new IOException("Não foi possível remover a pasta temporária de descobertas.");
            }
        }
    }

    private static Batalha criarBatalha(TipoAnimal tipo, int agitacao, boolean[][] descobertas) {
        AtaqueAnimal[] ataques = {new AtaqueAnimal("Básico", 1), new AtaqueAnimal("Investida", 1),
            new AtaqueAnimal("Especial", 1)};
        Animal animal = new Animal("Animal", tipo, agitacao, ataques, "animal");
        Fase fase = new Fase(new Bioma("Bioma", "Teste.", tipo, "cenario"), animal);
        return new Batalha(new Personagem("Teste"), fase.gerarInimigo(0),
                new TabelaEfetividade(), fase, 0, new Random(0), descobertas);
    }

    private static boolean[][] matrizVazia() {
        return new boolean[TipoFerramenta.values().length][TipoAnimal.values().length];
    }

    private static int contarDescobertas(boolean[][] descobertas) {
        int quantidade = 0;
        for (boolean[] linha : descobertas) {
            for (boolean descoberta : linha) {
                if (descoberta) {
                    quantidade++;
                }
            }
        }
        return quantidade;
    }

    private static void acalmar(Batalha batalha, TipoFerramenta ferramenta) {
        for (int turno = 0; !batalha.terminou() && turno < 100; turno++) {
            verificar(batalha.jogarTurno(ferramenta.ordinal()) != null,
                    "A ferramenta ilimitada de teste deve permanecer utilizável.");
        }
        verificar(batalha.venceu(), "A batalha de teste deve terminar em vitória.");
    }

    private static void gravarFases(File animais) throws IOException {
        String[] biomas = {"Floresta", "Deserto", "Montanha", "Pântano", "Gelo"};
        String[] tipos = {"MAMIFERO", "ARACNIDEO", "AVE", "REPTIL", "MAMIFERO"};
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(animais, StandardCharsets.UTF_8))) {
            escritor.write("bioma;descricaoBioma;animal;tipo;agitacao;ataque1;dano1;ataque2;dano2;"
                    + "ataque3;dano3;sprite;cenario\n");
            for (int indice = 0; indice < biomas.length; indice++) {
                escritor.write(biomas[indice] + ";Descrição.;Animal;" + tipos[indice]
                        + ";60;Básico;1;Investida;1;Especial;1;animal;cenario\n");
            }
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        verificacoes++;
    }
}
