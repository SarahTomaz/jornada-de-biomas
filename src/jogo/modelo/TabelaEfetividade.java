package jogo.modelo;

/**
 * Matriz dos multiplicadores por ferramenta e tipo de animal.
 * Cumpre o checklist de matriz e orienta os turnos de batalha.
 */
public class TabelaEfetividade {
    private final double[][] multiplicadores = {
        {1.5, 0.0, 1.5, 0.5},
        {1.5, 1.0, 1.0, 0.0},
        {1.0, 1.0, 0.0, 1.5},
        {0.0, 1.5, 1.0, 1.5},
        {1.0, 1.0, 1.0, 1.0}
    };

    public double getMultiplicador(TipoFerramenta ferramenta, TipoAnimal animal) {
        return multiplicadores[ferramenta.ordinal()][animal.ordinal()];
    }
}
