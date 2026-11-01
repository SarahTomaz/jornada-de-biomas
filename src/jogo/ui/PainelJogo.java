package jogo.ui;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;
import jogo.Config;

public final class PainelJogo extends JPanel {
    private static final long serialVersionUID = 1L;

    // Recursos gráficos, telas e relógio não fazem parte de dados persistidos.
    private final transient Recursos recursos;
    private final transient BufferedImage canvas;
    private final transient Timer temporizador;
    private transient Tela telaAtual;
    private transient Tela proximaTela;
    private double tempoTransicao;
    private boolean entrouNaProxima;
    private boolean mousePressionado;
    private final boolean[] teclasPressionadas = new boolean[256];
    private long ultimoInstante;

    public PainelJogo(Recursos recursos) {
        if (recursos == null) {
            throw new IllegalArgumentException("Os recursos da interface são obrigatórios.");
        }
        this.recursos = recursos;
        canvas = new BufferedImage(Config.LARGURA_LOGICA, Config.ALTURA_LOGICA,
                BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(Config.LARGURA_JANELA, Config.ALTURA_JANELA));
        setBackground(Paleta.CAIXA);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        temporizador = new Timer(Math.max(1, 1000 / Config.FPS), evento -> atualizarPeloRelogio());
        configurarEntrada();
    }

    public void trocarTela(Tela novaTela) {
        if (novaTela == null) {
            throw new IllegalArgumentException("A tela não pode ser nula.");
        }
        if (estaEmTransicao() || novaTela == telaAtual) {
            return;
        }
        cancelarPressaoMouse();
        if (telaAtual == null) {
            telaAtual = novaTela;
            telaAtual.aoEntrar();
        } else {
            proximaTela = novaTela;
            tempoTransicao = 0;
            entrouNaProxima = false;
        }
        repaint();
    }

    public Tela getTelaAtual() {
        return telaAtual;
    }

    public boolean estaEmTransicao() {
        return proximaTela != null;
    }

    public void atualizar(double dt) {
        if (!Double.isFinite(dt) || dt < 0) {
            return;
        }
        Tela destinoAnterior = proximaTela;
        if (telaAtual != null) {
            telaAtual.atualizar(dt);
        }
        // Uma troca pedida durante a atualização começa a contar no próximo quadro.
        if (destinoAnterior != null && proximaTela == destinoAnterior) {
            tempoTransicao += dt;
            double duracao = Config.DURACAO_FADE_MS / 1000.0;
            if (!entrouNaProxima && tempoTransicao >= duracao / 2) {
                telaAtual = proximaTela;
                entrouNaProxima = true;
                telaAtual.aoEntrar();
            }
            if (tempoTransicao >= duracao) {
                proximaTela = null;
                tempoTransicao = 0;
                entrouNaProxima = false;
            }
        }
        repaint();
    }

    public void iniciar() {
        if (!temporizador.isRunning()) {
            ultimoInstante = System.nanoTime();
            temporizador.start();
        }
    }

    public void parar() {
        temporizador.stop();
        limparTeclas();
        cancelarPressaoMouse();
    }

    public boolean estaRodando() {
        return temporizador.isRunning();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        iniciar();
        requestFocusInWindow();
    }

    @Override
    public void removeNotify() {
        parar();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics desenho) {
        super.paintComponent(desenho);
        Graphics2D desenhoLogico = canvas.createGraphics();
        try {
            configurarPixels(desenhoLogico);
            desenhoLogico.setColor(Paleta.CAIXA);
            desenhoLogico.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            desenhoLogico.setFont(recursos.getFonte(Config.TAMANHO_FONTE_PEQUENA));
            if (telaAtual != null) {
                Graphics2D desenhoTela = (Graphics2D) desenhoLogico.create();
                try {
                    telaAtual.desenhar(desenhoTela);
                } finally {
                    desenhoTela.dispose();
                }
            }
            if (estaEmTransicao()) {
                double metade = Config.DURACAO_FADE_MS / 2000.0;
                double opacidade = tempoTransicao <= metade
                        ? tempoTransicao / metade : (metade * 2 - tempoTransicao) / metade;
                float intensidade = (float) Math.max(0, Math.min(1, opacidade));
                desenhoLogico.setComposite(AlphaComposite.SrcOver.derive(intensidade));
                desenhoLogico.setColor(Paleta.CONTORNO);
                desenhoLogico.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        } finally {
            desenhoLogico.dispose();
        }

        Graphics2D desenhoAmpliado = (Graphics2D) desenho.create();
        try {
            configurarPixels(desenhoAmpliado);
            desenhoAmpliado.drawImage(canvas, 0, 0, Config.LARGURA_JANELA,
                    Config.ALTURA_JANELA, null);
        } finally {
            desenhoAmpliado.dispose();
        }
    }

    private void configurarPixels(Graphics2D desenho) {
        DesenhoPixel.configurarPixels(desenho);
        desenho.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    private void atualizarPeloRelogio() {
        long agora = System.nanoTime();
        double dt = (agora - ultimoInstante) / 1_000_000_000.0;
        ultimoInstante = agora;
        atualizar(dt);
    }

    private void configurarEntrada() {
        configurarMouse();
        configurarTeclado();
        configurarFoco();
    }

    private void configurarMouse() {
        MouseAdapter entradaMouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evento) {
                encaminharPressaoMouse(evento);
            }

            @Override
            public void mouseReleased(MouseEvent evento) {
                encaminharSolturaMouse(evento);
            }

            @Override
            public void mouseMoved(MouseEvent evento) {
                encaminharMovimento(evento);
            }

            @Override
            public void mouseDragged(MouseEvent evento) {
                encaminharMovimento(evento);
            }

            @Override
            public void mouseEntered(MouseEvent evento) {
                encaminharMovimento(evento);
            }

            @Override
            public void mouseExited(MouseEvent evento) {
                if (aceitaEntrada()) {
                    telaAtual.aoMoverMouse(-1, -1);
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent evento) {
                if (aceitaEntrada()) {
                    telaAtual.aoRolarMouse(evento.getWheelRotation());
                }
            }
        };
        addMouseListener(entradaMouse);
        addMouseMotionListener(entradaMouse);
        addMouseWheelListener(entradaMouse);
    }

    private void encaminharPressaoMouse(MouseEvent evento) {
        if (evento.getButton() == MouseEvent.BUTTON1 && aceitaEntrada()) {
            requestFocusInWindow();
            mousePressionado = true;
            telaAtual.aoPressionarMouse(coordenadaLogica(evento.getX()),
                    coordenadaLogica(evento.getY()));
        }
    }

    private void encaminharSolturaMouse(MouseEvent evento) {
        if (evento.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        boolean podeClicar = mousePressionado && aceitaEntrada();
        mousePressionado = false;
        if (podeClicar) {
            Tela destinataria = telaAtual;
            int x = coordenadaLogica(evento.getX());
            int y = coordenadaLogica(evento.getY());
            destinataria.aoClicar(x, y, evento.getClickCount());
            destinataria.aoSoltarMouse(x, y);
        }
    }

    private void configurarTeclado() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evento) {
                encaminharTecla(evento);
            }

            @Override
            public void keyTyped(KeyEvent evento) {
                encaminharTecla(evento);
            }

            @Override
            public void keyReleased(KeyEvent evento) {
                encaminharTecla(evento);
            }
        });
    }

    private void configurarFoco() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evento) {
                limparTeclas();
                cancelarPressaoMouse();
                if (telaAtual != null) {
                    telaAtual.aoPerderFoco();
                }
            }
        });
    }

    private void encaminharMovimento(MouseEvent evento) {
        if (aceitaEntrada()) {
            telaAtual.aoMoverMouse(coordenadaLogica(evento.getX()),
                    coordenadaLogica(evento.getY()));
        }
    }

    private void encaminharTecla(KeyEvent evento) {
        int codigo = evento.getKeyCode();
        boolean repetida = false;
        if (codigo >= 0 && codigo < teclasPressionadas.length) {
            if (evento.getID() == KeyEvent.KEY_PRESSED) {
                repetida = teclasPressionadas[codigo];
                teclasPressionadas[codigo] = true;
            } else if (evento.getID() == KeyEvent.KEY_RELEASED) {
                teclasPressionadas[codigo] = false;
            }
        }
        if (!aceitaEntrada()) {
            evento.consume();
            return;
        }
        if (repetida && !telaAtual.aceitaTeclaRepetida(codigo)) {
            evento.consume();
            return;
        }
        telaAtual.aoTeclar(evento);
    }

    private boolean aceitaEntrada() {
        return telaAtual != null && !estaEmTransicao();
    }

    private int coordenadaLogica(int coordenada) {
        return Math.floorDiv(coordenada, Config.ESCALA);
    }

    private void cancelarPressaoMouse() {
        if (mousePressionado && telaAtual != null) {
            telaAtual.aoSoltarMouse(-1, -1);
        }
        mousePressionado = false;
    }

    private void limparTeclas() {
        for (int indice = 0; indice < teclasPressionadas.length; indice++) {
            teclasPressionadas[indice] = false;
        }
    }
}
