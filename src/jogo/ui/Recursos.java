package jogo.ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;
import jogo.Config;
import jogo.modelo.TipoAnimal;
import jogo.modelo.TipoFerramenta;

public final class Recursos {
    private static final String[] CENARIOS = {
        "floresta", "deserto", "montanha", "pantano", "gelo", "menu"
    };
    private static final String[] QUADROS_PERSONAGEM = {
        "idle_1", "idle_2", "usando", "assustado", "feliz"
    };
    private static final String[] SPRITES_ANIMAIS = {"onca", "escorpiao", "aguia", "jacare", "urso"};
    private static final String[] QUADROS_ANIMAIS = {"idle_1", "idle_2", "agitado", "calmo"};
    private static final String[] FERRAMENTAS = {"flauta", "isca", "lanterna", "repelente", "dardo"};
    private static final int QUANTIDADE_IMAGENS = 39;

    private final File pastaAssets;
    private final Font fonteBase;
    private final String[] caminhosImagens = new String[QUANTIDADE_IMAGENS];
    private final BufferedImage[] imagens = new BufferedImage[QUANTIDADE_IMAGENS];

    public Recursos() {
        this("assets");
    }

    public Recursos(String pastaAssets) {
        this.pastaAssets = new File(pastaAssets == null ? "assets" : pastaAssets);
        fonteBase = carregarFonte();
        carregarImagens();
    }

    public BufferedImage getImagem(String caminhoRelativo) {
        String caminho = caminhoRelativo == null ? "imagem-desconhecida.png"
                : caminhoRelativo.replace('\\', '/');
        for (int indice = 0; indice < caminhosImagens.length; indice++) {
            if (caminho.equals(caminhosImagens[indice])) {
                return imagens[indice];
            }
        }
        return carregarImagem(caminho);
    }

    public Font getFonte(float tamanho) {
        return fonteBase.deriveFont(tamanho > 0 && Float.isFinite(tamanho)
                ? tamanho : Config.TAMANHO_FONTE_PEQUENA);
    }

    public BufferedImage getCenario(String identificador) {
        return getImagem("cenarios/" + identificador + ".png");
    }

    public BufferedImage getPersonagem(String quadro) {
        return getImagem("personagem/" + quadro + ".png");
    }

    public BufferedImage getAnimal(String sprite, String quadro, TipoAnimal tipo, String nome) {
        return getImagem("animais/" + sprite + "_" + quadro + ".png");
    }

    public BufferedImage getIconeFerramenta(TipoFerramenta tipo) {
        return tipo == null ? getImagem("icones/ferramenta_desconhecida.png")
                : imagens[32 + tipo.ordinal()];
    }

    public BufferedImage getTitulo() { return imagens[6]; }
    public BufferedImage getCoracao() { return imagens[37]; }
    public BufferedImage getEstrela() { return imagens[38]; }
    public int getQuantidadeImagens() { return imagens.length; }
    public String getCaminhoImagem(int indice) { return caminhosImagens[indice]; }

    private void carregarImagens() {
        int indice = 0;
        for (String cenario : CENARIOS) {
            registrarImagem(indice++, "cenarios/" + cenario + ".png");
        }
        registrarImagem(indice++, "ui/titulo.png");
        for (String quadro : QUADROS_PERSONAGEM) {
            registrarImagem(indice++, "personagem/" + quadro + ".png");
        }
        for (String animal : SPRITES_ANIMAIS) {
            for (String quadro : QUADROS_ANIMAIS) {
                registrarImagem(indice++, "animais/" + animal + "_" + quadro + ".png");
            }
        }
        for (String ferramenta : FERRAMENTAS) {
            registrarImagem(indice++, "icones/ferramenta_" + ferramenta + ".png");
        }
        registrarImagem(indice++, "icones/coracao.png");
        registrarImagem(indice, "icones/estrela.png");
    }

    private void registrarImagem(int indice, String caminho) {
        caminhosImagens[indice] = caminho;
        imagens[indice] = carregarImagem(caminho);
    }

    private BufferedImage carregarImagem(String caminho) {
        File arquivo = new File(pastaAssets, caminho);
        try {
            if (!arquivo.isFile()) {
                throw new IOException("arquivo ausente");
            }
            BufferedImage imagem = ImageIO.read(arquivo);
            if (imagem == null) {
                throw new IOException("imagem ilegível ou formato inválido");
            }
            return imagem;
        } catch (IOException | RuntimeException erro) {
            IOException causa = erro instanceof IOException ? (IOException) erro : new IOException(erro);
            throw new UncheckedIOException("Não foi possível carregar a imagem: " + arquivo.getPath(), causa);
        }
    }

    private Font carregarFonte() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File(pastaAssets, "fontes/fonte_pixel.ttf"));
        } catch (FontFormatException | IOException | RuntimeException erro) {
            return new Font(Font.MONOSPACED, Font.BOLD, (int) Config.TAMANHO_FONTE_PEQUENA);
        }
    }
}
