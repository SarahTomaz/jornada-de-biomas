package jogo;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import jogo.ui.JanelaPrincipal;

public final class Main {
    private Main() { }

    public static void main(String[] argumentos) {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue() {
            @Override
            protected void dispatchEvent(AWTEvent evento) {
                try {
                    super.dispatchEvent(evento);
                } catch (UncheckedIOException erro) {
                    encerrarPorErro(erro);
                }
            }
        });
        SwingUtilities.invokeLater(() -> {
            try {
                JanelaPrincipal janela = new JanelaPrincipal();
                janela.setVisible(true);
                janela.getPainel().requestFocusInWindow();
            } catch (IOException | UncheckedIOException erro) {
                encerrarPorErro(erro);
            }
        });
    }

    private static void encerrarPorErro(Exception erro) {
        JOptionPane.showMessageDialog(null, erro.getMessage(),
                "Não foi possível abrir a jornada", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
