package jogo;

import jogo.algoritmos.Busca;
import jogo.algoritmos.Ordenacao;
import jogo.estruturas.ListaEncadeada;
import jogo.estruturas.No;
import jogo.estruturas.Pilha;
import jogo.modelo.Ranking;
import jogo.modelo.RegistroBatalha;
import jogo.modelo.RegistroRanking;

public final class TesteEstruturasRanking {
    private static int verificacoes;
    private TesteEstruturasRanking() {
    }

    public static void main(String[] argumentos) {
        testarNosELista();
        testarPilhaEHistorico();
        testarOrdenacaoEBuscas();
        testarRanking();
        System.out.println("Estruturas e ranking: " + verificacoes + " verificações passaram");
    }

    private static void testarNosELista() {
        No<String> primeiro = new No<>("inicial");
        No<String> segundo = new No<>("seguinte");
        primeiro.setDado("alterado");
        primeiro.setProximo(segundo);
        verificar("alterado".equals(primeiro.getDado()), "O nó deve guardar seu dado.");
        verificar(primeiro.getProximo() == segundo, "O nó deve guardar o próximo nó.");

        ListaEncadeada<String> lista = new ListaEncadeada<>();
        verificar(lista.estaVazia() && lista.getTamanho() == 0, "Lista deve iniciar vazia.");
        lista.avancar();
        lista.reiniciar();
        verificar(lista.getAtual() == null, "Lista vazia não deve ter elemento atual.");
        lista.inserirNoFim("Floresta");
        lista.avancar();
        verificar("Floresta".equals(lista.getAtual()), "Um único nó também deve ser circular.");
        lista.inserirNoFim("Deserto");
        lista.inserirNoFim("Montanha");
        verificar(lista.getTamanho() == 3 && !lista.estaVazia(), "Lista deve contar as fases.");
        lista.avancar();
        verificar("Deserto".equals(lista.getAtual()), "Lista deve preservar a ordem de inserção.");
        lista.avancar();
        verificar("Montanha".equals(lista.getAtual()), "Lista deve alcançar a última fase.");
        lista.avancar();
        verificar("Floresta".equals(lista.getAtual()), "Última fase deve voltar à primeira.");
        lista.avancar();
        lista.reiniciar();
        verificar("Floresta".equals(lista.getAtual()), "Reiniciar deve voltar ao início.");
    }

    private static void testarPilhaEHistorico() {
        Pilha<RegistroBatalha> pilha = new Pilha<>();
        verificar(pilha.vazia() && pilha.pop() == null && pilha.peek() == null,
                "Pilha vazia deve devolver null sem alterar seu tamanho.");
        RegistroBatalha primeira = new RegistroBatalha("Onça-pintada", "Floresta", 1, true);
        RegistroBatalha ultima = new RegistroBatalha("Escorpião", "Deserto", 2, false);
        pilha.push(primeira);
        pilha.push(ultima);
        verificar(pilha.peek() == ultima && pilha.getTamanho() == 2,
                "Consultar o topo não deve retirar o registro.");
        verificar(pilha.pop() == ultima && pilha.pop() == primeira,
                "Histórico deve sair da última batalha para a primeira.");
        verificar(pilha.vazia() && pilha.getTamanho() == 0 && pilha.pop() == null,
                "Desempilhar tudo deve deixar a pilha vazia.");
        verificar(primeira.isAcalmou() && !ultima.isAcalmou(), "Registro deve guardar o resultado.");
        verificar(primeira.getCiclo() == 1 && ultima.getCiclo() == 2,
                "Ciclo do registro já deve ser exibível, sem soma adicional.");
        verificar("Ciclo 1 - Floresta - Onça-pintada: acalmada".equals(primeira.toString()),
                "Histórico deve manter acentos e o ciclo informado.");
    }

    private static void testarOrdenacaoEBuscas() {
        RegistroRanking empateAnterior = registro("Sarah", 7);
        RegistroRanking empatePosterior = registro("sarah", 7);
        RegistroRanking foraDaQuantidade = registro("Fora", 99);
        RegistroRanking[] vetor = {
            registro("Menor", 2), empateAnterior, registro("Maior", 10), empatePosterior,
            foraDaQuantidade
        };
        Ordenacao.insertionSortDecrescente(vetor, 4);
        verificar(vetor[0].getNivelAlcancado() == 10 && vetor[3].getNivelAlcancado() == 2,
                "Ordenação deve ser decrescente.");
        verificar(vetor[1] == empateAnterior && vetor[2] == empatePosterior,
                "Ordenação deve preservar a ordem dos empates.");
        verificar(vetor[4] == foraDaQuantidade, "Ordenação deve respeitar a quantidade informada.");
        verificar(Busca.sequencialPorNome(vetor, 4, "SARAH") == 1,
                "Busca por nome deve ignorar caixa e devolver a primeira ocorrência.");
        verificar(Busca.sequencialPorNome(vetor, 4, "Fora") == -1,
                "Busca por nome deve respeitar a quantidade informada.");
        verificar(Busca.binariaPorNivel(vetor, 4, 7) == 1,
                "Busca binária deve devolver a primeira posição entre níveis iguais.");
        verificar(Busca.binariaPorNivel(vetor, 4, 10) == 0
                && Busca.binariaPorNivel(vetor, 4, 2) == 3,
                "Busca binária deve alcançar as duas extremidades.");
        verificar(Busca.binariaPorNivel(vetor, 4, 8) == -1
                && Busca.binariaPorNivel(vetor, 4, 11) == -1
                && Busca.binariaPorNivel(vetor, 4, 1) == -1,
                "Busca binária deve detectar níveis ausentes dentro e fora do intervalo.");
        verificar(Busca.binariaPorNivel(vetor, 0, 10) == -1
                && Busca.sequencialPorNome(vetor, 0, "Sarah") == -1,
                "Buscas devem aceitar ranking vazio.");
        Integer[] numeros = {3, 1, 5, 3};
        Ordenacao.insertionSortDecrescente(numeros, numeros.length);
        verificar(numeros[0] == 5 && numeros[1] == 3 && numeros[2] == 3 && numeros[3] == 1,
                "Ordenação genérica deve funcionar com outro tipo Comparable.");
        verificar(registro("Máximo", Integer.MAX_VALUE).compareTo(registro("Mínimo", 0)) > 0,
                "Comparação de nível não deve estourar um inteiro.");
    }

    private static void testarRanking() {
        Ranking ranking = new Ranking();
        verificar(ranking.getQuantidade() == 0 && ranking.getRegistros().length == 10,
                "Ranking deve iniciar vazio com capacidade dez.");
        verificar(ranking.buscarPorNome("Sarah") == -1 && ranking.buscarPorNivel(1) == -1,
                "Ranking vazio não deve encontrar registros.");
        RegistroRanking[] inseridos = new RegistroRanking[10];
        for (int indice = 0; indice < inseridos.length; indice++) {
            inseridos[indice] = registro("Pessoa " + indice, 10 - indice);
            ranking.adicionar(inseridos[indice]);
        }
        RegistroRanking empateNoLimite = registro("Empate", 1);
        ranking.adicionar(empateNoLimite);
        ranking.adicionar(registro("Abaixo", 0));
        verificar(ranking.getQuantidade() == 10 && ranking.getRegistros()[9] == inseridos[9],
                "Ranking cheio deve rejeitar nível menor ou igual ao último.");
        RegistroRanking empateNovo = registro("Pessoa 0", 10);
        ranking.adicionar(empateNovo);
        verificar(ranking.getQuantidade() == 10 && ranking.getRegistros()[0] == inseridos[0]
                && ranking.getRegistros()[1] == empateNovo,
                "Novo registro aceito deve ficar depois do empate anterior.");
        verificar(ranking.buscarPorNome("PESSOA 0") == 0 && ranking.buscarPorNivel(10) == 0,
                "Ranking deve usar as buscas de primeira ocorrência.");
        verificar(ranking.buscarPorNome("Pessoa 9") == -1 && ranking.buscarPorNome("Empate") == -1,
                "Registro substituído ou rejeitado não deve aparecer no ranking.");
        verificar("2026-10-08".equals(empateNovo.getData()) && empateNovo.getCiclo() == 1,
                "Registro deve preservar data e ciclo recebidos.");
        for (int indice = 1; indice < ranking.getQuantidade(); indice++) {
            verificar(ranking.getRegistros()[indice - 1].getNivelAlcancado()
                    >= ranking.getRegistros()[indice].getNivelAlcancado(),
                    "Ranking deve continuar ordenado após substituição.");
        }
    }

    private static RegistroRanking registro(String nome, int nivel) {
        return new RegistroRanking(nome, nivel, 1, "2026-10-08");
    }

    private static void verificar(boolean condicao, String mensagem) {
        verificacoes++;
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
