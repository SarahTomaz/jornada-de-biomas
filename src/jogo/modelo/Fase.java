package jogo.modelo;

public class Fase {
    private final Bioma bioma;
    private final Animal animalBase;

    public Fase(Bioma bioma, Animal animalBase) {
        this.bioma = bioma;
        this.animalBase = animalBase;
    }

    public Bioma getBioma() {
        return bioma;
    }

    public Animal getAnimalBase() {
        return animalBase;
    }

    public Animal gerarInimigo(int ciclo) {
        Animal inimigo = new Animal(animalBase.getNome(), animalBase.getTipo(),
                animalBase.getAgitacaoMaxima(), animalBase.getAtaques(), animalBase.getSprite());
        inimigo.escalar(ciclo);
        return inimigo;
    }
}
