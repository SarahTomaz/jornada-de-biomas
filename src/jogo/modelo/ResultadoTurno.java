package jogo.modelo;

public final class ResultadoTurno {
    private final String nomeFerramenta;
    private final int danoCausado;
    private final double multiplicador;
    private final String mensagemEfetividade;
    private final boolean animalCalmou;
    private final AtaqueAnimal ataqueUsado;
    private final int danoSofrido;
    private final boolean semCoragem;
    private final boolean contraefeito;
    private final int danoContraefeito;

    public ResultadoTurno() {
        this("", 0, 0.0, "", false, null, 0, false);
    }

    public ResultadoTurno(String nomeFerramenta, int danoCausado, double multiplicador,
            String mensagemEfetividade, boolean animalCalmou, AtaqueAnimal ataqueUsado,
            int danoSofrido, boolean semCoragem) {
        this(nomeFerramenta, danoCausado, multiplicador, mensagemEfetividade,
                animalCalmou, ataqueUsado, danoSofrido, semCoragem, false, 0);
    }

    public ResultadoTurno(String nomeFerramenta, int danoCausado, double multiplicador,
            String mensagemEfetividade, boolean animalCalmou, AtaqueAnimal ataqueUsado,
            int danoSofrido, boolean semCoragem, boolean contraefeito, int danoContraefeito) {
        this.nomeFerramenta = nomeFerramenta;
        this.danoCausado = danoCausado;
        this.multiplicador = multiplicador;
        this.mensagemEfetividade = mensagemEfetividade;
        this.animalCalmou = animalCalmou;
        this.ataqueUsado = ataqueUsado;
        this.danoSofrido = danoSofrido;
        this.semCoragem = semCoragem;
        this.contraefeito = contraefeito;
        this.danoContraefeito = danoContraefeito;
    }

    public String getNomeFerramenta() { return nomeFerramenta; }
    public int getDanoCausado() { return danoCausado; }
    public double getMultiplicador() { return multiplicador; }
    public String getMensagemEfetividade() { return mensagemEfetividade; }
    public boolean getAnimalCalmou() { return animalCalmou; }
    public AtaqueAnimal getAtaqueUsado() { return ataqueUsado; }
    public int getDanoSofrido() { return danoSofrido; }
    public boolean getSemCoragem() { return semCoragem; }
    public boolean getContraefeito() { return contraefeito; }
    public int getDanoContraefeito() { return danoContraefeito; }
}
