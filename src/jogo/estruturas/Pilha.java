package jogo.estruturas;

/**
 * Pilha genérica LIFO que guarda o histórico de batalhas em Jogo.
 */
public class Pilha<T> {
    private No<T> topo;
    private int tamanho;

    public void push(T dado) {
        No<T> novo = new No<>(dado);
        // O novo nó passa a ser o topo e aponta para o topo anterior.
        novo.setProximo(topo);
        topo = novo;
        tamanho++;
    }

    public T pop() {
        if (topo == null) {
            return null;
        }
        T dado = topo.getDado();
        topo = topo.getProximo();
        tamanho--;
        return dado;
    }

    public T peek() {
        return topo == null ? null : topo.getDado();
    }

    public boolean vazia() {
        return tamanho == 0;
    }

    public int getTamanho() {
        return tamanho;
    }
}
