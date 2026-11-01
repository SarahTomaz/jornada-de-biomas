package jogo.estruturas;

/**
 * Lista encadeada genérica das fases carregadas dos arquivos.
 */
public class ListaEncadeada<T> {
    private No<T> inicio;
    private No<T> fim;
    private No<T> atual;
    private int tamanho;

    public void inserirNoFim(T dado) {
        No<T> novo = new No<>(dado);
        if (inicio == null) {
            inicio = novo;
            atual = novo;
        } else {
            fim.setProximo(novo);
        }
        fim = novo;
        // O último nó sempre aponta para o primeiro, fechando o círculo.
        fim.setProximo(inicio);
        tamanho++;
    }

    public T getAtual() {
        return atual == null ? null : atual.getDado();
    }

    public void avancar() {
        if (atual != null) {
            atual = atual.getProximo();
        }
    }

    public void reiniciar() {
        atual = inicio;
    }

    public int getTamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }
}
