package jogo.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import jogo.Jogo;

public final class JanelaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    // A janela não persiste recursos nem componentes da interface.
    private final transient Recursos recursos;
    private final transient PainelJogo painel;

    public JanelaPrincipal() throws IOException {
        this(new Jogo());
    }

    public JanelaPrincipal(Jogo jogo) {
        this(new Recursos(), null);
        if (jogo == null) {
            throw new IllegalArgumentException("O jogo é obrigatório.");
        }
        painel.trocarTela(new TelaMenu(painel, recursos, jogo, this::dispose));
    }

    public JanelaPrincipal(Recursos recursos, Tela telaInicial) {
        super("Jornada dos Biomas");
        if (recursos == null) {
            throw new IllegalArgumentException("Os recursos da interface são obrigatórios.");
        }
        this.recursos = recursos;
        painel = new PainelJogo(recursos);
        setContentPane(painel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setIconImage(recursos.getPersonagem("idle_1"));
        if (telaInicial != null) {
            painel.trocarTela(telaInicial);
        }
        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent evento) {
                painel.requestFocusInWindow();
            }
        });
    }

    public PainelJogo getPainel() {
        return painel;
    }

    @Override
    public void dispose() {
        painel.parar();
        super.dispose();
    }
}
