package com.meu.ourigame.model;
import static com.meu.ourigame.model.Tabuleiro.NUM_CASAS;
import java.util.*;


public class Jogada {
  private final int jogador;
  private final int casaEscolhida;
  public Jogada(int jogador, int casaEscolhida) {
    this.jogador = jogador;
    this.casaEscolhida = casaEscolhida;
  }

  public boolean aplicar(Tabuleiro tabuleiro, int[] score) {
    // Percurso anti-horário
    List<int[]> percurso = Arrays.asList(
      new int[]{0,0}, new int[]{0,1}, new int[]{0,2}, new int[]{0,3}, new int[]{0,4}, new int[]{0,5},
      new int[]{1,0}, new int[]{1,1}, new int[]{1,2}, new int[]{1,3}, new int[]{1,4}, new int[]{1,5}
    );

    int sementes = tabuleiro.getCasa(jogador, casaEscolhida);
    if (sementes == 0) return false; // Jogada inválida
    if (sementes == 1) return false; // Jogada inválida, não se pode jogar com 1 semente
    tabuleiro.setCasa(jogador, casaEscolhida, 0);

    // Encontra o índice da casa de origem no percurso
    int origemIdx = -1;
    for (int i = 0; i < percurso.size(); i++) {
      int[] casa = percurso.get(i);
      if (casa[0] == jogador && casa[1] == casaEscolhida) {
        origemIdx = i;
        break;
      }
    }

    int idx = origemIdx;

    // Distribuição das sementes
    while (sementes > 0) {
      idx = (idx + 1) % percurso.size();
      // Salta a casa de origem
      if (idx == origemIdx) {
        idx = (idx + 1) % percurso.size();
      }
      int[] casa = percurso.get(idx);
      tabuleiro.setCasa(casa[0], casa[1], tabuleiro.getCasa(casa[0], casa[1]) + 1);
      sementes--;
    }

    // Captura: retrocede no percurso no campo do adversário
    int adversario = (jogador == 0) ? 1 : 0;
    for (int i = 0; i < NUM_CASAS; i++) {
      int valor = tabuleiro.getCasa(adversario, i);
      if (valor >= 2 && valor <= 3) {
        tabuleiro.addAoDeposito(jogador, valor);
        tabuleiro.setCasa(adversario, i, 0); // Limpa as sementes capturadas
      }
    }

    boolean noPossiblePlay = true;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < NUM_CASAS; j++) {
        if (tabuleiro.getCasa(i,j) <= 1) {
          noPossiblePlay = false;
          break;
        }
      }
    }
    if (noPossiblePlay) {
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < NUM_CASAS; j++) {
          tabuleiro.addAoDeposito(i, tabuleiro.getCasa(i,j));
          tabuleiro.setCasa(i, j, 0); // Limpa as casas
        }
      }
    }

    return true;
  }
}
