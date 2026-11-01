package jogo;

import jogo.modelo.Animal;
import jogo.modelo.AtaqueAnimal;
import jogo.modelo.Bioma;
import jogo.modelo.Fase;
import jogo.modelo.Ferramenta;
import jogo.modelo.Personagem;
import jogo.modelo.TabelaEfetividade;
import jogo.modelo.TipoAnimal;
import jogo.modelo.TipoFerramenta;

public final class TesteModelo {
    private static int verificacoes;

    private TesteModelo() {
    }

    public static void main(String[] argumentos) {
        testarEfetividade();
        testarCopiasECrescimento();
        testarRecuperacaoCoragem();
        testarUsosERecargas();
        testarRecompensasDardos();
        System.out.println("Modelo: " + verificacoes + " verificações passaram.");
    }

    private static void testarEfetividade() {
        Personagem personagem = new Personagem("Sarah");
        TabelaEfetividade tabela = new TabelaEfetividade();
        Animal mamifero = animal(TipoAnimal.MAMIFERO, 1000);
        Animal reptil = animal(TipoAnimal.REPTIL, 1000);
        verificar(personagem.usarFerramenta(0, mamifero, tabela) == 15,
                "Flauta deve ter poder 15 contra mamífero.");
        verificar(mamifero.getAgitacao() == 985,
                "A ferramenta deve aplicar a calma uma única vez.");
        verificar(personagem.usarFerramenta(0, reptil, tabela) == 0,
                "Flauta não deve reduzir a agitação de réptil.");
        verificar(reptil.getAgitacao() == 1000,
                "Multiplicador zero deve preservar a agitação.");
        personagem.evoluir();
        verificar(personagem.usarFerramenta(0, mamifero, tabela) == 16,
                "O poder 11 vezes 1,5 deve ser truncado de 16,5 para 16.");
        verificar(personagem.usarFerramenta(0, reptil, tabela) == 0,
                "O bônus de poder não deve alterar o multiplicador zero.");
    }

    private static void testarCopiasECrescimento() {
        Animal base = animal(TipoAnimal.MAMIFERO, 65);
        Fase fase = new Fase(new Bioma("Floresta", "Mata úmida.",
                TipoAnimal.MAMIFERO, "floresta"), base);
        Animal cicloUm = fase.gerarInimigo(1);
        Animal cicloDois = fase.gerarInimigo(2);
        verificar(cicloUm != base && cicloDois != base && cicloUm != cicloDois,
                "Cada geração deve criar um animal independente.");
        verificar(cicloUm.getAgitacao() == 85 && cicloUm.getAgitacaoMaxima() == 85,
                "Agitação 65 no ciclo interno 1 deve arredondar 84,5 para 85.");
        verificar(cicloDois.getAgitacao() == 104 && cicloDois.getAgitacaoMaxima() == 104,
                "Ciclo interno 2 deve acrescentar 60% à agitação base.");
        verificar(danosIguais(cicloUm, 4, 6, 12),
                "Danos no ciclo interno 1 devem crescer 20% e arredondar.");
        verificar(danosIguais(cicloDois, 4, 7, 14),
                "Danos no ciclo interno 2 devem crescer 40% sobre a base.");
        AtaqueAnimal[] ataquesBase = base.getAtaques();
        AtaqueAnimal[] ataquesUm = cicloUm.getAtaques();
        AtaqueAnimal[] ataquesDois = cicloDois.getAtaques();
        boolean ataquesIndependentes = true;
        for (int indice = 0; indice < ataquesBase.length; indice++) {
            ataquesIndependentes &= ataquesBase[indice] != ataquesUm[indice]
                    && ataquesBase[indice] != ataquesDois[indice]
                    && ataquesUm[indice] != ataquesDois[indice];
        }
        verificar(ataquesIndependentes, "As cópias também devem conter novos ataques.");
        cicloUm.receberCalma(7);
        verificar(cicloUm.getAgitacao() == 78 && cicloDois.getAgitacao() == 104
                && base.getAgitacao() == 65 && base.getAgitacaoMaxima() == 65,
                "Acalmar uma cópia não deve alterar outra cópia nem o animal-base.");
        verificar(danosIguais(base, 3, 5, 10),
                "Os ataques do animal-base não devem sofrer crescimento.");
        cicloUm.escalar(2);
        verificar(cicloUm.getAgitacao() == 104 && danosIguais(cicloUm, 4, 7, 14),
                "Escalar novamente deve partir dos valores base.");
        cicloUm.escalar(2);
        verificar(cicloUm.getAgitacao() == 104 && danosIguais(cicloUm, 4, 7, 14),
                "Repetir a escala do mesmo ciclo não deve acumular crescimento.");
        verificar("animal".equals(cicloUm.getSprite())
                && "floresta".equals(fase.getBioma().getCenario()),
                "Os identificadores da arte devem ser preservados.");
    }

    private static void testarRecuperacaoCoragem() {
        Personagem personagem = new Personagem("Ágata");
        verificar(personagem.getCoragem() == 100 && personagem.getCoragemMaxima() == 100
                && personagem.getVitorias() == 0 && personagem.getBonusPoder() == 0,
                "Personagem deve começar com os valores previstos.");
        int recuperada = personagem.evoluir();
        verificar(recuperada == 5 && personagem.getCoragem() == 105
                && personagem.getCoragemMaxima() == 105,
                "Com coragem cheia, a recompensa efetiva deve ser apenas 5.");
        personagem.receberSusto(50);
        recuperada = personagem.evoluir();
        verificar(recuperada == 20 && personagem.getCoragem() == 75
                && personagem.getCoragemMaxima() == 110,
                "Com espaço suficiente, a recuperação efetiva deve ser 20.");
        verificar(personagem.getVitorias() == 2 && personagem.getBonusPoder() == 2,
                "Cada evolução deve acrescentar uma vitória e um ponto de poder.");
        personagem.receberSusto(1000);
        verificar(personagem.getCoragem() == 0 && !personagem.temCoragem(),
                "Um susto maior que a coragem deve zerá-la sem ficar negativa.");
    }

    private static void testarUsosERecargas() {
        Personagem personagem = new Personagem("Teste");
        TabelaEfetividade tabela = new TabelaEfetividade();
        Animal alvo = animal(TipoAnimal.MAMIFERO, 1000);
        Ferramenta[] ferramentas = personagem.getFerramentas();
        verificar(ferramentas[1].getUsosRestantes() == 3
                && ferramentas[3].getUsosRestantes() == 3
                && ferramentas[4].getUsosRestantes() == 3,
                "Isca, Repelente e Dardo devem começar com três usos.");
        verificar(personagem.usarFerramenta(4, alvo, tabela) == 40
                && ferramentas[4].getUsosRestantes() == 2,
                "O uso de Dardo deve gastar apenas um uso do estoque da partida.");
        for (int repeticao = 0; repeticao < 3; repeticao++) {
            personagem.usarFerramenta(1, alvo, tabela);
            personagem.usarFerramenta(3, alvo, tabela);
        }
        verificar(!ferramentas[1].temUsos() && !ferramentas[3].temUsos(),
                "Isca e Repelente devem esgotar após três usos.");
        int agitacaoAnterior = alvo.getAgitacao();
        verificar(personagem.usarFerramenta(1, alvo, tabela) == 0
                && alvo.getAgitacao() == agitacaoAnterior
                && ferramentas[1].getUsosRestantes() == 0,
                "Uma ferramenta esgotada não deve ser usada pelo modelo.");
        personagem.prepararParaBatalha();
        verificar(ferramentas[1].getUsosRestantes() == 3
                && ferramentas[3].getUsosRestantes() == 3,
                "Uma nova batalha deve recarregar Isca e Repelente.");
        verificar(ferramentas[4].getUsosRestantes() == 2,
                "Uma nova batalha não deve recarregar Dardo.");
        for (int repeticao = 0; repeticao < 10; repeticao++) {
            personagem.usarFerramenta(0, alvo, tabela);
            personagem.usarFerramenta(2, alvo, tabela);
        }
        verificar(ferramentas[0].temUsos() && ferramentas[2].temUsos()
                && ferramentas[0].getUsosRestantes() == -1
                && ferramentas[2].getUsosRestantes() == -1,
                "Flauta e Lanterna devem continuar ilimitadas após vários usos.");
    }

    private static void testarRecompensasDardos() {
        Personagem personagem = new Personagem("Dardos");
        Animal alvo = animal(TipoAnimal.MAMIFERO, 1000);
        TabelaEfetividade tabela = new TabelaEfetividade();
        Ferramenta dardo = personagem.getFerramentas()[TipoFerramenta.DARDO.ordinal()];
        for (int repeticao = 0; repeticao < 3; repeticao++) {
            personagem.usarFerramenta(4, alvo, tabela);
        }
        verificar(dardo.getUsosRestantes() == 0 && !dardo.temUsos(),
                "Dardos da partida devem poder ser esgotados.");
        vencer(personagem, 4);
        verificar(dardo.getUsosRestantes() == 0, "Quatro vitórias ainda não devem dar Dardo.");
        vencer(personagem, 1);
        verificar(dardo.getUsosRestantes() == 1, "A quinta vitória deve dar um Dardo.");
        vencer(personagem, 4);
        verificar(dardo.getUsosRestantes() == 1, "Vitórias seis a nove não devem dar outro Dardo.");
        vencer(personagem, 1);
        verificar(dardo.getUsosRestantes() == 2, "A décima vitória deve dar outro Dardo.");
        vencer(personagem, 15);
        verificar(dardo.getUsosRestantes() == 5, "O estoque deve chegar a cinco Dardos.");
        vencer(personagem, 5);
        verificar(dardo.getUsosRestantes() == 5, "O estoque não deve ultrapassar cinco Dardos.");
        personagem.usarFerramenta(4, alvo, tabela);
        personagem.prepararParaBatalha();
        verificar(dardo.getUsosRestantes() == 4,
                "Dardo usado depois de alcançar o limite não deve recarregar na batalha.");
        vencer(personagem, 4);
        verificar(dardo.getUsosRestantes() == 4,
                "O uso de um Dardo não deve antecipar a próxima recompensa.");
        vencer(personagem, 1);
        verificar(dardo.getUsosRestantes() == 5,
                "A próxima quinta vitória deve restaurar o Dardo usado, respeitando o limite.");
    }

    private static Animal animal(TipoAnimal tipo, int agitacao) {
        AtaqueAnimal[] ataques = {
            new AtaqueAnimal("Básico", 3),
            new AtaqueAnimal("Investida", 5),
            new AtaqueAnimal("Especial", 10)
        };
        return new Animal("Animal", tipo, agitacao, ataques, "animal");
    }

    private static boolean danosIguais(Animal animal, int basico, int investida, int especial) {
        AtaqueAnimal[] ataques = animal.getAtaques();
        return ataques[0].getDanoCoragem() == basico && ataques[1].getDanoCoragem() == investida
                && ataques[2].getDanoCoragem() == especial;
    }

    private static void vencer(Personagem personagem, int quantidade) {
        for (int repeticao = 0; repeticao < quantidade; repeticao++) {
            personagem.evoluir();
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        verificacoes++;
    }
}
