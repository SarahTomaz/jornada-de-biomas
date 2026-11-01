package jogo;

import java.util.Random;
import jogo.modelo.Animal;
import jogo.modelo.AtaqueAnimal;
import jogo.modelo.Batalha;
import jogo.modelo.Bioma;
import jogo.modelo.Fase;
import jogo.modelo.Ferramenta;
import jogo.modelo.Personagem;
import jogo.modelo.ResultadoTurno;
import jogo.modelo.TabelaEfetividade;
import jogo.modelo.TipoAnimal;
import jogo.modelo.TipoFerramenta;

public final class TesteContraefeito {
    private static int verificacoes;

    private TesteContraefeito() {
    }

    public static void main(String[] argumentos) {
        testarMatriz();
        testarCalmaEUsos();
        testarContraefeitos();
        testarEspecial();
        testarContraefeitoFatal();
        testarEscala();
        testarMensagens();
        testarDescobertas();
        testarAcoesInvalidas();
        testarCompatibilidade();
        System.out.println("Contraefeitos: " + verificacoes + " verificações passaram.");
    }

    private static void testarMatriz() {
        double[][] esperado = {
            {1.5, 0.0, 1.5, 0.5},
            {1.5, 1.0, 1.0, 0.0},
            {1.0, 1.0, 0.0, 1.5},
            {0.0, 1.5, 1.0, 1.5},
            {1.0, 1.0, 1.0, 1.0}
        };
        TabelaEfetividade tabela = new TabelaEfetividade();
        for (TipoFerramenta ferramenta : TipoFerramenta.values()) {
            for (TipoAnimal animal : TipoAnimal.values()) {
                verificar(tabela.getMultiplicador(ferramenta, animal)
                        == esperado[ferramenta.ordinal()][animal.ordinal()],
                        "A matriz deve preservar todos os vinte multiplicadores definidos.");
            }
        }
    }

    private static void testarCalmaEUsos() {
        int[] indices = {0, 1, 2, 3};
        TipoAnimal[] tipos = {
            TipoAnimal.REPTIL, TipoAnimal.ARACNIDEO, TipoAnimal.AVE, TipoAnimal.MAMIFERO
        };
        TabelaEfetividade tabela = new TabelaEfetividade();
        for (int indice = 0; indice < indices.length; indice++) {
            Personagem jogador = new Personagem("Teste");
            Animal animal = fase(tipos[indice], 500).gerarInimigo(0);
            Ferramenta ferramenta = jogador.getFerramentas()[indices[indice]];
            int usos = ferramenta.getUsosRestantes();
            verificar(jogador.usarFerramenta(indices[indice], animal, tabela) == 0
                    && animal.getAgitacao() == 500 && jogador.getCoragem() == 100,
                    "Usar ferramenta com multiplicador zero deve causar zero calma.");
            verificar(ferramenta.getUsosRestantes() == (usos == -1 ? -1 : usos - 1),
                    "A escolha sem efeito ainda deve gastar seu uso limitado.");
        }
        Personagem jogador = new Personagem("Teste");
        Animal aracnideo = fase(TipoAnimal.ARACNIDEO, 500).gerarInimigo(0);
        verificar(jogador.usarFerramenta(0, aracnideo, tabela) == 5,
                "A flauta contra aracnídeo deve preservar a efetividade 0,5.");
        jogador.evoluir();
        verificar(jogador.usarFerramenta(0, aracnideo, tabela) == 5,
                "Onze vezes 0,5 deve continuar truncando 5,5 para cinco.");
        jogador = new Personagem("Teste");
        jogador.getFerramentas()[0] = new Ferramenta("Flauta", 1, -1, -1, TipoFerramenta.FLAUTA);
        verificar(jogador.usarFerramenta(0, aracnideo, tabela) == 1,
                "O mínimo de um ponto deve valer quando o multiplicador é positivo.");
        Animal reptil = fase(TipoAnimal.REPTIL, 500).gerarInimigo(0);
        verificar(jogador.usarFerramenta(0, reptil, tabela) == 0,
                "O mínimo de um ponto não deve transformar multiplicador zero em calma.");
    }

    private static void testarContraefeitos() {
        TipoAnimal[] tipos = {
            TipoAnimal.REPTIL, TipoAnimal.ARACNIDEO, TipoAnimal.AVE, TipoAnimal.MAMIFERO
        };
        for (int indice = 0; indice < tipos.length; indice++) {
            Personagem jogador = new Personagem("Teste");
            SorteadorContado sorteador = new SorteadorContado();
            Batalha batalha = batalha(jogador, tipos[indice], 0, sorteador, null);
            batalha.iniciar();
            ResultadoTurno resultado = batalha.jogarTurno(indice);
            verificar(resultado.getContraefeito() && resultado.getDanoContraefeito() == 6
                    && resultado.getMultiplicador() == 0 && resultado.getDanoCausado() == 0,
                    "Cada um dos quatro pares zero deve devolver o contraefeito separado.");
            verificar(batalha.getInimigo().getAgitacao() == 500
                    && resultado.getDanoSofrido() == 3 && jogador.getCoragem() == 91,
                    "O susto do contraefeito e o ataque comum devem somar suas perdas de coragem.");
            verificar(resultado.getAtaqueUsado().getNome().equals("Básico")
                    && sorteador.chamadas == 1 && batalha.getTurno() == 2
                    && !batalha.terminou() && !resultado.getSemCoragem(),
                    "Contraefeito sobrevivido deve permitir um único ataque e consumir um turno.");
        }
    }

    private static void testarEspecial() {
        Personagem jogador = new Personagem("Teste");
        SorteadorContado sorteador = new SorteadorContado();
        Batalha batalha = batalha(jogador, TipoAnimal.REPTIL, 0, sorteador, null);
        batalha.iniciar();
        batalha.jogarTurno(0);
        batalha.jogarTurno(0);
        ResultadoTurno terceiro = batalha.jogarTurno(0);
        verificar(terceiro.getAtaqueUsado().getNome().equals("Especial")
                && terceiro.getDanoSofrido() == 10 && terceiro.getDanoContraefeito() == 6,
                "O terceiro turno deve manter o ataque especial após o contraefeito.");
        verificar(batalha.getTurno() == 4 && sorteador.chamadas == 2
                && jogador.getCoragem() == 66,
                "Cada turno zero deve gastar coragem e avançar uma vez; o especial não sorteia.");
    }

    private static void testarContraefeitoFatal() {
        Personagem jogador = new Personagem("Teste");
        jogador.receberSusto(95);
        boolean[][] descobertas = new boolean[5][4];
        SorteadorContado sorteador = new SorteadorContado();
        Batalha batalha = batalha(jogador, TipoAnimal.ARACNIDEO, 0, sorteador, descobertas);
        batalha.iniciar();
        ResultadoTurno resultado = batalha.jogarTurno(1);
        verificar(resultado.getContraefeito() && resultado.getDanoContraefeito() == 6
                && resultado.getSemCoragem() && jogador.getCoragem() == 0,
                "O contraefeito fatal deve zerar a coragem sem produzir valor negativo.");
        verificar(resultado.getAtaqueUsado() == null && resultado.getDanoSofrido() == 0
                && sorteador.chamadas == 0,
                "Depois do contraefeito fatal o animal não deve escolher ataque nem consumir RNG.");
        verificar(batalha.terminou() && !batalha.venceu() && !batalha.desistiu()
                && batalha.getTurno() == 2 && jogador.getFerramentas()[1].getUsosRestantes() == 2,
                "A escolha fatal ainda é um turno válido e gasta exatamente uma carga.");
        verificar(descobertas[1][TipoAnimal.ARACNIDEO.ordinal()],
                "O par sem efeito deve ser descoberto mesmo quando o contraefeito encerra a batalha.");
        verificar(batalha.jogarTurno(0) == null && batalha.desistir() == null
                && sorteador.chamadas == 0 && batalha.getTurno() == 2,
                "A derrota por contraefeito deve bloquear qualquer ação seguinte.");
    }

    private static void testarEscala() {
        int[] esperados = {6, 7, 8, 10, 11, 12};
        for (int ciclo = 0; ciclo < esperados.length; ciclo++) {
            Personagem jogador = new Personagem("Teste");
            Batalha batalha = batalha(jogador, TipoAnimal.REPTIL, ciclo, new SorteadorContado(), null);
            batalha.iniciar();
            ResultadoTurno resultado = batalha.jogarTurno(0);
            verificar(resultado.getDanoContraefeito() == esperados[ciclo],
                    "O contraefeito deve crescer por ciclo com arredondamento, sem acumular a escala.");
            verificar(jogador.getCoragem() == 100 - esperados[ciclo] - resultado.getDanoSofrido(),
                    "Os danos separados devem explicar a coragem restante em cada ciclo.");
        }
    }

    private static void testarMensagens() {
        Batalha zero = batalha(new Personagem("Teste"), TipoAnimal.REPTIL,
                0, new SorteadorContado(), null);
        zero.iniciar();
        verificar(zero.jogarTurno(0).getMensagemEfetividade().equals(
                "Não funcionou... Animal não gostou e ficou irritado! (-6 Coragem)"),
                "A mensagem zero deve incluir o animal e a perda exata do contraefeito.");
        Batalha fraco = batalha(new Personagem("Teste"), TipoAnimal.ARACNIDEO,
                0, new SorteadorContado(), null);
        fraco.iniciar();
        ResultadoTurno resultado = fraco.jogarTurno(0);
        verificar(resultado.getMensagemEfetividade().equals("Quase não fez efeito...")
                && !resultado.getContraefeito() && resultado.getDanoContraefeito() == 0,
                "A efetividade 0,5 deve manter a mensagem fraca sem contraefeito.");
        Batalha normal = batalha(new Personagem("Teste"), TipoAnimal.MAMIFERO,
                0, new SorteadorContado(), null);
        normal.iniciar();
        verificar(normal.jogarTurno(2).getMensagemEfetividade().equals("Funcionou."),
                "A efetividade normal deve manter sua mensagem.");
        Batalha eficaz = batalha(new Personagem("Teste"), TipoAnimal.MAMIFERO,
                0, new SorteadorContado(), null);
        eficaz.iniciar();
        verificar(eficaz.jogarTurno(0).getMensagemEfetividade().equals("Foi muito eficaz!"),
                "A efetividade 1,5 deve manter sua mensagem.");
    }

    private static void testarDescobertas() {
        TabelaEfetividade tabela = new TabelaEfetividade();
        for (TipoFerramenta ferramenta : TipoFerramenta.values()) {
            for (TipoAnimal tipo : TipoAnimal.values()) {
                boolean[][] descobertas = new boolean[5][4];
                Batalha batalha = batalha(new Personagem("Teste"), tipo,
                        0, new SorteadorContado(), descobertas);
                batalha.iniciar();
                batalha.jogarTurno(ferramenta.ordinal());
                double multiplicador = tabela.getMultiplicador(ferramenta, tipo);
                boolean deveDescobrir = multiplicador == 0 || multiplicador >= 1.5;
                verificar(descobertas[ferramenta.ordinal()][tipo.ordinal()] == deveDescobrir
                        && quantidadeDescobertas(descobertas) == (deveDescobrir ? 1 : 0),
                        "Só o par efetivo ou sem efeito escolhido deve ser registrado como descoberto.");
            }
        }
        Personagem jogador = new Personagem("Teste");
        Fase fase = fase(TipoAnimal.MAMIFERO, 1);
        boolean[][] descobertas = new boolean[5][4];
        SorteadorContado sorteador = new SorteadorContado();
        Batalha vitoria = new Batalha(jogador, fase.gerarInimigo(0), tabela,
                fase, 0, sorteador, descobertas);
        vitoria.iniciar();
        ResultadoTurno resultado = vitoria.jogarTurno(0);
        verificar(resultado.getAnimalCalmou() && descobertas[0][0] && sorteador.chamadas == 0,
                "Um golpe eficaz que vence sem ataque também deve marcar sua descoberta.");
    }

    private static void testarAcoesInvalidas() {
        Personagem jogador = new Personagem("Teste");
        boolean[][] descobertas = new boolean[5][4];
        SorteadorContado sorteador = new SorteadorContado();
        Batalha batalha = batalha(jogador, TipoAnimal.ARACNIDEO, 0, sorteador, descobertas);
        verificar(batalha.jogarTurno(1) == null && quantidadeDescobertas(descobertas) == 0,
                "Escolhas antes de iniciar não devem revelar efetividades.");
        batalha.iniciar();
        jogador.getFerramentas()[1].usar();
        jogador.getFerramentas()[1].usar();
        jogador.getFerramentas()[1].usar();
        verificar(batalha.jogarTurno(-1) == null && batalha.jogarTurno(5) == null
                && batalha.jogarTurno(1) == null && quantidadeDescobertas(descobertas) == 0,
                "Índices inválidos e ferramentas esgotadas não devem marcar descobertas.");
        verificar(jogador.getCoragem() == 100 && batalha.getTurno() == 1 && sorteador.chamadas == 0,
                "Escolhas inválidas não devem aplicar contraefeito, atacar ou avançar o turno.");
        ResultadoTurno desistir = batalha.desistir();
        verificar(quantidadeDescobertas(descobertas) == 0 && !desistir.getContraefeito()
                && desistir.getDanoContraefeito() == 0 && desistir.getAtaqueUsado() == null,
                "Desistir deve manter o resultado vazio e não revelar efetividades.");
    }

    private static void testarCompatibilidade() {
        ResultadoTurno vazio = new ResultadoTurno();
        ResultadoTurno antigo = new ResultadoTurno("Flauta", 15, 1.5,
                "Foi muito eficaz!", false, new AtaqueAnimal("Básico", 3), 3, false);
        verificar(!vazio.getContraefeito() && vazio.getDanoContraefeito() == 0
                && !antigo.getContraefeito() && antigo.getDanoContraefeito() == 0,
                "Os construtores antigos devem continuar produzindo zero contraefeito.");
        Personagem jogador = new Personagem("Teste");
        Fase fase = fase(TipoAnimal.MAMIFERO, 1);
        Batalha batalhaAntiga = new Batalha(jogador, fase.gerarInimigo(0),
                new TabelaEfetividade(), fase, 0);
        batalhaAntiga.iniciar();
        verificar(batalhaAntiga.jogarTurno(0).getAnimalCalmou(),
                "O construtor antigo de batalha deve continuar funcionando sem matriz opcional.");
        boolean[][][] invalidas = {new boolean[4][4], new boolean[5][3], new boolean[5][]};
        for (boolean[][] invalida : invalidas) {
            try {
                batalha(new Personagem("Teste"), TipoAnimal.MAMIFERO,
                        0, new SorteadorContado(), invalida);
                throw new AssertionError("Matriz de descobertas inválida deveria ser rejeitada.");
            } catch (IllegalArgumentException esperado) {
                verificacoes++;
            }
        }
    }

    private static Batalha batalha(Personagem jogador, TipoAnimal tipo, int ciclo,
            Random sorteador, boolean[][] descobertas) {
        Fase fase = fase(tipo, 500);
        return new Batalha(jogador, fase.gerarInimigo(ciclo), new TabelaEfetividade(),
                fase, ciclo, sorteador, descobertas);
    }

    private static Fase fase(TipoAnimal tipo, int agitacao) {
        AtaqueAnimal[] ataques = {
            new AtaqueAnimal("Básico", 3), new AtaqueAnimal("Investida", 5),
            new AtaqueAnimal("Especial", 10)
        };
        return new Fase(new Bioma("Bioma", "Descrição.", tipo, "floresta"),
                new Animal("Animal", tipo, agitacao, ataques, "animal"));
    }

    private static int quantidadeDescobertas(boolean[][] descobertas) {
        int quantidade = 0;
        for (boolean[] linha : descobertas) {
            for (boolean descoberta : linha) {
                if (descoberta) {
                    quantidade++;
                }
            }
        }
        return quantidade;
    }

    private static final class SorteadorContado extends Random {
        private static final long serialVersionUID = 1L;
        private int chamadas;

        @Override
        public int nextInt(int limite) {
            if (limite != 2) {
                throw new AssertionError("O sorteio do ataque deve escolher entre básico e investida.");
            }
            chamadas++;
            return 0;
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        verificacoes++;
    }
}
