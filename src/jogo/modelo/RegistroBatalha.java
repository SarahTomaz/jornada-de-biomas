package jogo.modelo;

public class RegistroBatalha {
    private final String animal;
    private final String bioma;
    private final int ciclo;
    private final boolean acalmou;

    public RegistroBatalha(String animal, String bioma, int ciclo, boolean acalmou) {
        this.animal = animal;
        this.bioma = bioma;
        this.ciclo = ciclo;
        this.acalmou = acalmou;
    }

    public String getAnimal() {
        return animal;
    }

    public String getBioma() {
        return bioma;
    }

    public int getCiclo() {
        return ciclo;
    }

    public boolean isAcalmou() {
        return acalmou;
    }

    @Override
    public String toString() {
        return "Ciclo " + ciclo + " - " + bioma + " - " + animal + ": "
                + (acalmou ? "acalmada" : "não acalmada");
    }
}
