package jogo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jogo.arquivos.GerenciadorArquivos;
import jogo.estruturas.ListaEncadeada;
import jogo.modelo.Fase;
import jogo.modelo.Ranking;
import jogo.modelo.RegistroRanking;

public final class TesteArquivos {
    private static final String CABECALHO = "bioma;descricaoBioma;animal;tipo;agitacao;"
            + "ataque1;dano1;ataque2;dano2;ataque3;dano3;sprite;cenario";
    private static final String LINHA_VALIDA = "Floresta;Mata úmida.;Onça-pintada;MAMIFERO;"
            + "60;Rugido;3;Bote;5;Salto;10;onca;floresta";
    private static int verificacoes;

    private TesteArquivos() {
    }

    public static void main(String[] argumentos) throws IOException {
        File pasta = new File("testes/temporarios/arquivos");
        if (!pasta.isDirectory() && !pasta.mkdirs()) {
            throw new IOException("Não foi possível criar a pasta temporária de testes.");
        }
        File animais = new File(pasta, "animais.csv");
        File ranking = new File(pasta, "ranking.txt");
        GerenciadorArquivos gerenciador = new GerenciadorArquivos(
                animais.getPath(), ranking.getPath());
        try {
            limparArquivo(animais);
            limparArquivo(ranking);
            esperarErroCsv(gerenciador, 1);
            verificar(gerenciador.carregarRanking().getQuantidade() == 0,
                    "Ranking ausente deve começar vazio.");
            testarCsv(gerenciador, animais);
            testarRanking(gerenciador, ranking);
            System.out.println("Arquivos: " + verificacoes + " verificações passaram.");
        } finally {
            limparArquivo(animais);
            limparArquivo(ranking);
            if (!pasta.delete() && pasta.exists()) {
                throw new IOException("Não foi possível remover a pasta temporária de testes.");
            }
        }
    }

    private static void testarCsv(GerenciadorArquivos gerenciador, File animais)
            throws IOException {
        gravar(animais, CABECALHO + "\n" + LINHA_VALIDA + "\n"
                + "Gelo;;Urso-polar;MAMIFERO;115;Rugido;5;Investida;8;Pose;15;;\n");
        ListaEncadeada<Fase> fases = gerenciador.carregarFases();
        verificar(fases.getTamanho() == 2, "As duas fases válidas devem ser carregadas.");
        verificar(fases.getAtual().getBioma().getNome().equals("Floresta"),
                "A ordem do CSV deve ser preservada.");
        fases.avancar();
        verificar(fases.getAtual().getBioma().getNome().equals("Gelo"),
                "Campos opcionais vazios não invalidam a fase.");

        gravar(animais, "");
        esperarErroCsv(gerenciador, 1);
        gravar(animais, "bioma;animal\n" + LINHA_VALIDA);
        esperarErroCsv(gerenciador, 1);
        gravar(animais, CABECALHO);
        esperarErroCsv(gerenciador, 2);
        gravar(animais, CABECALHO + "\n" + LINHA_VALIDA + ";extra");
        esperarErroCsv(gerenciador, 2);
        gravar(animais, CABECALHO + "\n"
                + LINHA_VALIDA.substring(0, LINHA_VALIDA.lastIndexOf(';')));
        esperarErroCsv(gerenciador, 2);
        gravar(animais, CABECALHO + "\n" + LINHA_VALIDA + "\n\n");
        esperarErroCsv(gerenciador, 3);

        int[] camposNumericos = {4, 6, 8, 10};
        for (int indice : camposNumericos) {
            String[] valoresInvalidos = {"1.5", "zero", "0", "-1", "2147483648", ""};
            for (String valor : valoresInvalidos) {
                gravar(animais, CABECALHO + "\n" + trocarCampo(indice, valor));
                esperarErroCsv(gerenciador, 2);
            }
        }
        gravar(animais, CABECALHO + "\n" + trocarCampo(3, "PEIXE"));
        esperarErroCsv(gerenciador, 2);
        gravar(animais, CABECALHO + "\n" + trocarCampo(3, "mamifero"));
        esperarErroCsv(gerenciador, 2);
        int[] camposDeNome = {0, 2, 5, 7, 9};
        for (int indice : camposDeNome) {
            gravar(animais, CABECALHO + "\n" + trocarCampo(indice, "   "));
            esperarErroCsv(gerenciador, 2);
        }
    }

    private static void testarRanking(GerenciadorArquivos gerenciador, File ranking)
            throws IOException {
        gravar(ranking, "Sarah;4;1;2026-10-08\n"
                + "João;4;1;\n"
                + " ;1;1;2026-10-08\n"
                + "Campos;1;1\n"
                + "Campos;1;1;data;extra\n"
                + "Nivel;0;1;data\n"
                + "Ciclo;1;0;data\n"
                + "Texto;um;1;data\n"
                + "Fração;1;1.5;data\n"
                + "Overflow;2147483648;1;data\n"
                + "Maria;6;2;data sem validação\n");
        Ranking carregado = gerenciador.carregarRanking();
        verificar(carregado.getQuantidade() == 3, "Linhas inválidas devem ser ignoradas.");
        verificar(carregado.getRegistros()[0].getNome().equals("Maria"),
                "Ranking deve ser ordenado pelo nível decrescente.");
        verificar(carregado.getRegistros()[1].getNome().equals("Sarah")
                && carregado.getRegistros()[2].getNome().equals("João"),
                "Empates devem preservar a ordem do arquivo.");
        verificar(carregado.getRegistros()[2].getData().isEmpty(),
                "Data vazia é válida conforme os critérios definidos.");
        verificar(carregado.getRegistros()[0].getData().equals("data sem validação"),
                "A data deve ser preservada sem validação adicional.");

        StringBuilder conteudo = new StringBuilder();
        for (int nivel = 1; nivel <= 12; nivel++) {
            conteudo.append("Jogador ").append(nivel).append(';').append(nivel)
                    .append(";1;2026-10-08\n");
        }
        conteudo.append("Empatado;12;1;2026-10-08\n");
        gravar(ranking, conteudo.toString());
        carregado = gerenciador.carregarRanking();
        verificar(carregado.getQuantidade() == 10, "Ranking deve conter no máximo 10 registros.");
        verificar(carregado.getRegistros()[0].getNome().equals("Jogador 12")
                && carregado.getRegistros()[1].getNome().equals("Empatado"),
                "Top 10 deve preservar o empate na maior pontuação.");
        verificar(carregado.getRegistros()[9].getNivelAlcancado() == 4,
                "O Top 10 deve guardar as dez maiores pontuações.");
        gerenciador.salvarRanking(carregado);
        Ranking recarregado = gerenciador.carregarRanking();
        verificar(recarregado.getQuantidade() == carregado.getQuantidade(),
                "Quantidade de registros deve sobreviver à gravação.");
        for (int indice = 0; indice < carregado.getQuantidade(); indice++) {
            RegistroRanking antes = carregado.getRegistros()[indice];
            RegistroRanking depois = recarregado.getRegistros()[indice];
            verificar(antes.getNome().equals(depois.getNome())
                    && antes.getNivelAlcancado() == depois.getNivelAlcancado()
                    && antes.getCiclo() == depois.getCiclo()
                    && antes.getData().equals(depois.getData()),
                    "Registro deve sobreviver à gravação na mesma posição.");
        }
        carregado = new Ranking();
        carregado.adicionar(new RegistroRanking("Ágata", 7, 2, "2026-10-08"));
        gerenciador.salvarRanking(carregado);
        verificar(gerenciador.carregarRanking().getRegistros()[0].getNome().equals("Ágata"),
                "A gravação e leitura devem preservar UTF-8.");
        gerenciador.salvarRanking(new Ranking());
        verificar(gerenciador.carregarRanking().getQuantidade() == 0,
                "Gravação deve substituir o arquivo inteiro.");
    }

    private static String trocarCampo(int indiceAlterado, String novoValor) {
        String[] campos = LINHA_VALIDA.split(";", -1);
        campos[indiceAlterado] = novoValor;
        StringBuilder linha = new StringBuilder();
        for (int indice = 0; indice < campos.length; indice++) {
            if (indice > 0) {
                linha.append(';');
            }
            linha.append(campos[indice]);
        }
        return linha.toString();
    }

    private static void gravar(File arquivo, String conteudo) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(arquivo, StandardCharsets.UTF_8))) {
            escritor.write(conteudo);
        }
    }

    private static void esperarErroCsv(GerenciadorArquivos gerenciador, int numeroLinha)
            throws IOException {
        try {
            gerenciador.carregarFases();
            throw new AssertionError("CSV inválido deveria ter sido rejeitado.");
        } catch (IOException erro) {
            verificar(erro.getMessage().contains("linha " + numeroLinha + ":"),
                    "Erro deve identificar a linha " + numeroLinha + ": " + erro.getMessage());
        }
    }

    private static void limparArquivo(File arquivo) throws IOException {
        if (arquivo.exists() && !arquivo.delete()) {
            throw new IOException("Não foi possível limpar o arquivo de teste " + arquivo + ".");
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        verificacoes++;
    }
}
