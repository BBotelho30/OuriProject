package com.meu.ourigame.model;

public class Tabuleiro {
  public static final int NUM_CASAS = 6;
  private final int[][] casas;
  private final int[] depositos;

  public Tabuleiro() {
    casas = new int[2][NUM_CASAS];
    depositos = new int[2];
    reset();
  }

  public void reset() {
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < NUM_CASAS; j++)
        casas[i][j] = 4;
    depositos[0] = 0;
    depositos[1] = 0;
  }

  public int getCasa(int jogador, int indice) {
    return casas[jogador][indice];
  }

  public void setCasa(int jogador, int indice, int valor) {
    casas[jogador][indice] = valor;
  }

  public int getDeposito(int jogador) {
    return depositos[jogador];
  }

  public void addAoDeposito(int jogador, int valor) {
    depositos[jogador] += valor;
  }
  
}
