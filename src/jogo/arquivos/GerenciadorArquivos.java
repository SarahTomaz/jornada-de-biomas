package jogo.arquivos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jogo.estruturas.ListaEncadeada;
import jogo.modelo.Animal;
import jogo.modelo.AtaqueAnimal;
import jogo.modelo.Bioma;
import jogo.modelo.Fase;
import jogo.modelo.Ranking;
import jogo.modelo.RegistroRanking;
import jogo.modelo.TipoAnimal;

/**
 * Lê as fases do CSV e carrega ou salva o ranking em arquivo.
 */
public final class GerenciadorArquivos {
    private static final int COLUNAS_ANIMAIS = 13;
    private static final int COL_BIOMA = 0;
    private static final int COL_DESCRICAO = 1;
    private static final int COL_ANIMAL = 2;
    private static final int COL_TIPO = 3;
    private static final int COL_AGITACAO = 4;
    private static final int COL_ATAQUE_1 = 5;
    private static final int COL_DANO_1 = 6;
    private static final int COL_ATAQUE_2 = 7;
    private static final int COL_DANO_2 = 8;
    private static final int COL_ATAQUE_3 = 9;
    private static final int COL_DANO_3 = 10;
    private static final int COL_SPRITE = 11;
    private static final int COL_CENARIO = 12;
    private static final String CAMINHO_PADRAO_ANIMAIS = "dados/animais.csv";
    private static final String CAMINHO_PADRAO_RANKING = "dados/ranking.txt";
    private final String caminhoAnimais;
    private final String caminhoRanking;

    public GerenciadorArquivos() {
        this(CAMINHO_PADRAO_ANIMAIS, CAMINHO_PADRAO_RANKING);
    }

    public GerenciadorArquivos(String caminhoAnimais, String caminhoRanking) {
        this.caminhoAnimais = caminhoAnimais;
        this.caminhoRanking = caminhoRanking;
    }

    public ListaEncadeada<Fase> carregarFases() throws IOException {
        ListaEncadeada<Fase> fases = new ListaEncadeada<>();
        if (!new File(caminhoAnimais).isFile()) {
            throw erroLinha(1, "arquivo obrigatório não encontrado");
        }
        try (BufferedReader leitor = new BufferedReader(
                new FileReader(caminhoAnimais, StandardCharsets.UTF_8))) {
            String cabecalho = leitor.readLine();
            if (cabecalho == null) {
                throw erroLinha(1, "arquivo vazio; cabeçalho e animais são obrigatórios");
            }
            int quantidadeColunas = cabecalho.split(";", -1).length;
            if (quantidadeColunas != COLUNAS_ANIMAIS) {
                throw erroLinha(1, "esperadas " + COLUNAS_ANIMAIS + " colunas no cabeçalho, encontradas "
                        + quantidadeColunas);
            }
            String linha;
            int numeroLinha = 1;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                fases.inserirNoFim(lerFase(linha, numeroLinha));
            }
        }
        if (fases.estaVazia()) {
            throw erroLinha(2, "nenhum animal encontrado depois do cabeçalho");
        }
        return fases;
    }

    public Ranking carregarRanking() throws IOException {
        Ranking ranking = new Ranking();
        if (!new File(caminhoRanking).exists()) {
            return ranking;
        }
        try (BufferedReader leitor = new BufferedReader(
                new FileReader(caminhoRanking, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                RegistroRanking registro = lerRegistroRanking(linha);
                if (registro != null) {
                    ranking.adicionar(registro);
                }
            }
        }
        return ranking;
    }

    public void salvarRanking(Ranking ranking) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(caminhoRanking, StandardCharsets.UTF_8))) {
            RegistroRanking[] registros = ranking.getRegistros();
            for (int indice = 0; indice < ranking.getQuantidade(); indice++) {
                RegistroRanking registro = registros[indice];
                escritor.write(registro.getNome() + ";" + registro.getNivelAlcancado()
                        + ";" + registro.getCiclo() + ";" + registro.getData());
                escritor.newLine();
            }
        }
    }

    private Fase lerFase(String linha, int numeroLinha) throws IOException {
        String[] campos = linha.split(";", -1);
        if (campos.length != COLUNAS_ANIMAIS) {
            throw erroLinha(numeroLinha, "esperadas " + COLUNAS_ANIMAIS
                    + " colunas, encontradas " + campos.length);
        }
        for (int indice = 0; indice < campos.length; indice++) {
            campos[indice] = campos[indice].trim();
        }
        validarNome(campos[COL_BIOMA], "bioma", numeroLinha);
        validarNome(campos[COL_ANIMAL], "animal", numeroLinha);
        validarNome(campos[COL_ATAQUE_1], "ataque1", numeroLinha);
        validarNome(campos[COL_ATAQUE_2], "ataque2", numeroLinha);
        validarNome(campos[COL_ATAQUE_3], "ataque3", numeroLinha);
        TipoAnimal tipo;
        try {
            tipo = TipoAnimal.valueOf(campos[COL_TIPO]);
        } catch (IllegalArgumentException erro) {
            throw erroLinha(numeroLinha, "tipo de animal inválido: '" + campos[COL_TIPO] + "'");
        }
        int agitacao = lerInteiroPositivo(campos[COL_AGITACAO], "agitacao", numeroLinha);
        AtaqueAnimal[] ataques = {
            new AtaqueAnimal(campos[COL_ATAQUE_1], lerInteiroPositivo(campos[COL_DANO_1], "dano1", numeroLinha)),
            new AtaqueAnimal(campos[COL_ATAQUE_2], lerInteiroPositivo(campos[COL_DANO_2], "dano2", numeroLinha)),
            new AtaqueAnimal(campos[COL_ATAQUE_3], lerInteiroPositivo(campos[COL_DANO_3], "dano3", numeroLinha))
        };
        Bioma bioma = new Bioma(campos[COL_BIOMA], campos[COL_DESCRICAO], tipo, campos[COL_CENARIO]);
        Animal animal = new Animal(campos[COL_ANIMAL], tipo, agitacao, ataques, campos[COL_SPRITE]);
        return new Fase(bioma, animal);
    }

    private void validarNome(String valor, String campo, int numeroLinha) throws IOException {
        if (valor.isEmpty()) {
            throw erroLinha(numeroLinha, "nome vazio no campo '" + campo + "'");
        }
    }

    private int lerInteiroPositivo(String valor, String campo, int numeroLinha) throws IOException {
        int numero;
        try {
            numero = Integer.parseInt(valor);
        } catch (NumberFormatException erro) {
            throw erroLinha(numeroLinha, "o campo '" + campo + "' deve ser um número inteiro");
        }
        if (numero < 1) {
            throw erroLinha(numeroLinha, "o campo '" + campo + "' deve ser maior ou igual a 1");
        }
        return numero;
    }

    private IOException erroLinha(int numeroLinha, String motivo) {
        return new IOException("Arquivo " + caminhoAnimais + " inválido na linha "
                + numeroLinha + ": " + motivo + ".");
    }

    private RegistroRanking lerRegistroRanking(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length != 4 || campos[0].trim().isEmpty()) {
            return null;
        }
        try {
            int nivel = Integer.parseInt(campos[1].trim());
            int ciclo = Integer.parseInt(campos[2].trim());
            if (nivel < 1 || ciclo < 1) {
                return null;
            }
            return new RegistroRanking(campos[0].trim(), nivel, ciclo, campos[3]);
        } catch (NumberFormatException erro) {
            return null;
        }
    }
}
