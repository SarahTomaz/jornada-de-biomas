package jogo.ui;

import java.awt.Color;
import jogo.modelo.TipoAnimal;

public final class Paleta {
    public static final Color CONTORNO = new Color(0x4A3F55);
    public static final Color CAIXA = new Color(0xFFF4E0);
    public static final Color BOTAO_NORMAL = new Color(0xF7C6D0);
    public static final Color BOTAO_HOVER = new Color(0xFFD9E1);
    public static final Color BOTAO_DESABILITADO = new Color(0xD9D2DC);
    public static final Color CORAGEM = new Color(0xFF6F91);
    public static final Color AGITACAO = new Color(0xFFB347);
    public static final Color FUNDO_BARRA = new Color(0xE8DDEB);
    public static final Color DESTAQUE = new Color(0xCDB4DB);
    public static final Color MARCA_EFICAZ = new Color(0x287447);
    public static final Color MARCA_INEFICAZ = new Color(0xB43B52);
    public static final Color FLORESTA = new Color(0xBFE3C0);
    public static final Color DESERTO = new Color(0xFFD6A5);
    public static final Color MONTANHA = new Color(0xA8D8EA);
    public static final Color PANTANO = new Color(0xC9E4C5);
    public static final Color PANTANO_CHAO = new Color(0x8FB996);
    public static final Color GELO = new Color(0xE3F2FD);

    private Paleta() {
    }

    public static Color getCorBioma(String nome) {
        if (nome == null) {
            return DESTAQUE;
        }
        String bioma = nome.trim();
        if (bioma.equalsIgnoreCase("floresta")) {
            return FLORESTA;
        }
        if (bioma.equalsIgnoreCase("deserto")) {
            return DESERTO;
        }
        if (bioma.equalsIgnoreCase("montanha")) {
            return MONTANHA;
        }
        if (bioma.equalsIgnoreCase("pantano") || bioma.equalsIgnoreCase("pântano")) {
            return PANTANO;
        }
        if (bioma.equalsIgnoreCase("gelo")) {
            return GELO;
        }
        return DESTAQUE;
    }

    public static Color getCorTipo(TipoAnimal tipo) {
        if (tipo == null) {
            return DESTAQUE;
        }
        return switch (tipo) {
            case MAMIFERO -> DESERTO;
            case REPTIL -> PANTANO_CHAO;
            case AVE -> MONTANHA;
            case ARACNIDEO -> DESTAQUE;
        };
    }
}
