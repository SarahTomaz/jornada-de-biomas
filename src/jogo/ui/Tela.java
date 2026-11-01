package jogo.ui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public interface Tela {
    void aoEntrar();

    void atualizar(double dt);

    void desenhar(Graphics2D desenho);

    void aoClicar(int x, int y);

    default void aoClicar(int x, int y, int quantidadeCliques) {
        aoClicar(x, y);
    }

    void aoMoverMouse(int x, int y);

    void aoTeclar(KeyEvent evento);

    default void aoRolarMouse(int rotacao) {
    }

    default void aoPressionarMouse(int x, int y) {
    }

    default void aoSoltarMouse(int x, int y) {
    }

    default boolean estaDigitando() {
        return false;
    }

    default boolean aceitaTeclaRepetida(int codigo) {
        return codigo != KeyEvent.VK_ENTER && codigo != KeyEvent.VK_ESCAPE;
    }

    default void aoPerderFoco() {
    }
}
