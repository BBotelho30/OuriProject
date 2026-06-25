/**
 * Representa uma jogada escolhida por um jogador.
 *
 * A lógica de distribuição de sementes e captura é realizada nesta classe.
 */
package com.meu.ourigame.model;

import java.util.Arrays;
import java.util.List;

import static com.meu.ourigame.model.Tabuleiro.NUM_CASAS;

public class Jogada {
    private final int jogador;
    private final int casaEscolhida;

    public Jogada(int jogador, int casaEscolhida) {
        this.jogador = jogador;
        this.casaEscolhida = casaEscolhida;
    }

    public ResultadoJogada aplicar(Tabuleiro tabuleiro, int[] score){
        int sementes = tabuleiro.getCasa(jogador, casaEscolhida);

        if (sementes <= 0) {
            return ResultadoJogada.erro("Essa cavidade está vazia.");
        }
        if (sementes == 1 && existeCasaComMaisDeUmaSemente(tabuleiro, jogador)) {
            return ResultadoJogada.erro("Não podes jogar uma casa com 1 semente enquanto tens outra com 2 ou mais.");
        }

        int[][] backupCasas = copiarCasas(tabuleiro);
        int depositoJ1Antes = tabuleiro.getDeposito(0);
        int depositoJ2Antes = tabuleiro.getDeposito(1);

        List<int[]> percurso = Arrays.asList(
                new int[]{0, 5},
                new int[]{0, 4},
                new int[]{0, 3},
                new int[]{0, 2},
                new int[]{0, 1},
                new int[]{0, 0},
                new int[]{1, 0},
                new int[]{1, 1},
                new int[]{1, 2},
                new int[]{1, 3},
                new int[]{1, 4},
                new int[]{1, 5}
        );

        int origemIdx = encontrarIndice(percurso, jogador, casaEscolhida);

        tabuleiro.setCasa(jogador, casaEscolhida, 0);

        int idx = origemIdx;
        int ultimaLinha = -1;
        int ultimaCasa = -1;

        while (sementes > 0) {
            idx = (idx + 1) % percurso.size();

            if (idx == origemIdx) {
                idx = (idx + 1) % percurso.size();
            }

            int[] posicao = percurso.get(idx);
            int linha = posicao[0];
            int casa = posicao[1];

            tabuleiro.setCasa(linha, casa, tabuleiro.getCasa(linha, casa) + 1);

            ultimaLinha = linha;
            ultimaCasa = casa;

            sementes--;
        }

        capturar(tabuleiro, percurso, idx, ultimaLinha, ultimaCasa);

        int adversario = jogador == 0 ? 1 : 0;

        if (linhaVazia(tabuleiro, adversario)) {
            if (existeJogadaParaAlimentar(tabuleiro, jogador, percurso)) {
                restaurar(tabuleiro, backupCasas, depositoJ1Antes, depositoJ2Antes);
                return ResultadoJogada.erro("Essa jogada deixaria o adversário sem sementes.");
            } else {
                recolherRestantes(tabuleiro);
                return ResultadoJogada.sucesso();
            }
        }

        if (fimSemJogadasPossiveis(tabuleiro)) {
            recolherRestantes(tabuleiro);
        }

        return ResultadoJogada.sucesso();
    }

    private int encontrarIndice(List<int[]> percurso, int jogador, int casaEscolhida) {
        for (int i = 0; i < percurso.size(); i++) {
            int[] posicao = percurso.get(i);

            if (posicao[0] == jogador && posicao[1] == casaEscolhida) {
                return i;
            }
        }

        return -1;
    }

    private void capturar(Tabuleiro tabuleiro, List<int[]> percurso, int ultimaIdx, int ultimaLinha, int ultimaCasa) {
        int adversario = jogador == 0 ? 1 : 0;

        if (ultimaLinha != adversario) {
            return;
        }

        int valorUltimaCasa = tabuleiro.getCasa(ultimaLinha, ultimaCasa);

        if (valorUltimaCasa != 2 && valorUltimaCasa != 3) {
            return;
        }

        int idx = ultimaIdx;

        while (true) {
            int[] posicao = percurso.get(idx);
            int linha = posicao[0];
            int casa = posicao[1];

            if (linha != adversario) {
                break;
            }

            int valor = tabuleiro.getCasa(linha, casa);

            if (valor == 2 || valor == 3) {
                tabuleiro.addAoDeposito(jogador, valor);
                tabuleiro.setCasa(linha, casa, 0);
            } else {
                break;
            }

            idx--;

            if (idx < 0) {
                idx = percurso.size() - 1;
            }
        }
    }

    private boolean linhaVazia(Tabuleiro tabuleiro, int jogador) {
      for (int i = 0; i < NUM_CASAS; i++) {
          if (tabuleiro.getCasa(jogador, i) > 0) {
              return false;
          }
      }

      return true;
  }

    private boolean fimSemJogadasPossiveis(Tabuleiro tabuleiro) {
        boolean jogador0TemSementes = false;
        boolean jogador1TemSementes = false;

        for (int i = 0; i < NUM_CASAS; i++) {
            if (tabuleiro.getCasa(0, i) > 0) {
                jogador0TemSementes = true;
            }

            if (tabuleiro.getCasa(1, i) > 0) {
                jogador1TemSementes = true;
            }
        }

        return !jogador0TemSementes || !jogador1TemSementes;
    }

    private void recolherRestantes(Tabuleiro tabuleiro) {
        for (int jogador = 0; jogador < 2; jogador++) {
            for (int casa = 0; casa < NUM_CASAS; casa++) {
                int sementes = tabuleiro.getCasa(jogador, casa);

                if (sementes > 0) {
                    tabuleiro.addAoDeposito(jogador, sementes);
                    tabuleiro.setCasa(jogador, casa, 0);
                }
            }
        }
    }

    private int[][] copiarCasas(Tabuleiro tabuleiro) {
        int[][] copia = new int[2][NUM_CASAS];

        for (int jogador = 0; jogador < 2; jogador++) {
            for (int casa = 0; casa < NUM_CASAS; casa++) {
                copia[jogador][casa] = tabuleiro.getCasa(jogador, casa);
            }
        }

        return copia;
    }

    private void restaurar(Tabuleiro tabuleiro, int[][] backupCasas, int depositoJ1, int depositoJ2) {
        for (int jogador = 0; jogador < 2; jogador++) {
            for (int casa = 0; casa < NUM_CASAS; casa++) {
                tabuleiro.setCasa(jogador, casa, backupCasas[jogador][casa]);
            }
        }

        tabuleiro.setDeposito(0, depositoJ1);
        tabuleiro.setDeposito(1, depositoJ2);
    }

    private boolean existeCasaComMaisDeUmaSemente(Tabuleiro tabuleiro, int jogador) {
      for (int i = 0; i < NUM_CASAS; i++) {
          if (tabuleiro.getCasa(jogador, i) > 1) {
              return true;
          }
      }

      return false;
  }

    private boolean existeJogadaParaAlimentar(Tabuleiro tabuleiro, int jogador, List<int[]> percurso) {
      int adversario = jogador == 0 ? 1 : 0;

      for (int casa = 0; casa < NUM_CASAS; casa++) {
          int sementes = tabuleiro.getCasa(jogador, casa);

          if (sementes <= 0) {
              continue;
          }

          if (sementes == 1 && existeCasaComMaisDeUmaSemente(tabuleiro, jogador)) {
              continue;
          }

          int origemIdx = encontrarIndice(percurso, jogador, casa);
          int idx = origemIdx;
          int sementesParaDistribuir = sementes;

          while (sementesParaDistribuir > 0) {
              idx = (idx + 1) % percurso.size();

              if (idx == origemIdx) {
                  idx = (idx + 1) % percurso.size();
              }

              int[] posicao = percurso.get(idx);

              if (posicao[0] == adversario) {
                  return true;
              }

              sementesParaDistribuir--;
          }
      }

      return false;
  }
}