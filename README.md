# Jornada dos Biomas

Jogo em Java e Swing em que você percorre biomas para acalmar animais e registrar sua jornada no ranking. Requer JDK 17 ou superior e não usa bibliotecas externas.

Escolha **Jogar**, informe seu nome e use as ferramentas para reduzir a agitação do animal. Com o mouse ou as teclas **1 a 5**, escolha uma ferramenta; **0** ou **Esc** abre a desistência. Clique ou pressione **Enter** para ler e avançar as mensagens.

A efetividade depende da ferramenta e do tipo de animal. Uma combinação ineficaz causa contraefeito: reduz a coragem e o animal pode atacar no mesmo turno. A coragem zerada encerra a partida. Cada animal acalmado aumenta o nível; após cinco biomas, começa um ciclo mais difícil. Partidas com ao menos uma vitória entram no ranking, limitado a dez registros.

## Compilar e executar

No Windows, dê dois cliques em `Jogar.bat` para compilar e abrir o jogo.

No PowerShell, dentro da pasta `jornada-dos-biomas`:

```powershell
$fontes = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac --release 17 -Xlint:all -encoding UTF-8 -d out $fontes
java '-Dfile.encoding=UTF-8' -cp out jogo.Main
```

Para compilar e executar os sete programas em `testes/`:

```powershell
$testes = @(Get-ChildItem testes -Recurse -Filter *.java)
javac --release 17 -Xlint:all -encoding UTF-8 -cp out -d out-testes ($testes.FullName)
foreach ($teste in $testes) { java '-Dfile.encoding=UTF-8' -cp 'out;out-testes' ('jogo.' + $teste.BaseName) }
```

## Pastas

```text
assets/        imagens e fonte
dados/         animais.csv e ranking.txt quando criado
src/jogo/      Main, Jogo, Config, modelo/, estruturas/, algoritmos/, arquivos/ e ui/
testes/jogo/   sete programas de teste
```

Os 39 PNGs estão em `assets/cenarios/`, `personagem/`, `animais/`, `icones/` e `ui/`; todos são necessários para abrir o jogo. A fonte Silkscreen está em `assets/fontes/fonte_pixel.ttf`, junto da sua licença; se ausente, é usada uma fonte padrão.

## Checklist da disciplina

| Item | Classe | Teste em `testes/jogo/` |
|---|---|---|
| Vetor | `Personagem`, `Animal`, `Ranking` | `TesteModelo`, `TesteEstruturasRanking` |
| Matriz | `TabelaEfetividade` | `TesteModelo`, `TesteContraefeito` |
| Generics | `No`, `ListaEncadeada`, `Pilha`, `Ordenacao` | `TesteEstruturasRanking` |
| Arquivos | `GerenciadorArquivos` | `TesteArquivos`, `TesteRegras` |
| Pilha | `Pilha`, `Jogo` | `TesteEstruturasRanking`, `TesteRegras` |
| Lista encadeada | `ListaEncadeada` | `TesteEstruturasRanking` |
| Busca | `Busca`, `Ranking` | `TesteEstruturasRanking` |
| Ordenação | `Ordenacao`, `Ranking` | `TesteEstruturasRanking` |

`TesteDescobertas` verifica a memória das combinações por partida; `SimulacaoConsole` executa uma batalha de demonstração. O ranking é criado ao final de uma partida com vitória.
