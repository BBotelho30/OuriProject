package com.meu.ourigame.model;

public class JogoOuri {
    private final Tabuleiro tabuleiro;
    private final int[] score;
    private int jogadorAtual;

    public JogoOuri() {
        this.tabuleiro = new Tabuleiro();
        this.score = new int[2];
        this.jogadorAtual = 0;
    }

    public boolean jogar(int casaEscolhida) {
        Jogada jogada = new Jogada(jogadorAtual, casaEscolhida);
        boolean jogadaValida = jogada.aplicar(tabuleiro, score);

        if (jogadaValida) {
            trocarJogador();
        }

        return jogadaValida;
    }

    private void trocarJogador() {
        jogadorAtual = jogadorAtual == 0 ? 1 : 0;
    }

    public String getVencedor() {
        int pontosJ1 = tabuleiro.getDeposito(0);
        int pontosJ2 = tabuleiro.getDeposito(1);

        if (pontosJ1 > pontosJ2) {
            return "Jogador 1 venceu!";
        } else if (pontosJ2 > pontosJ1) {
            return "Jogador 2 venceu!";
        } else {
            return "Empate!";
        }
    }

    public boolean isFimDeJogo() {
        boolean linha0Vazia = true;
        boolean linha1Vazia = true;

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            if (tabuleiro.getCasa(0, i) > 0) linha0Vazia = false;
            if (tabuleiro.getCasa(1, i) > 0) linha1Vazia = false;
        }

        return (linha0Vazia && linha1Vazia)
                || tabuleiro.getDeposito(0) >= 25
                || tabuleiro.getDeposito(1) >= 25;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public int[] getScore() {
        return score;
    }

    public int getJogadorAtual() {
        return jogadorAtual;
    }

    public void setJogadorAtual(int jogadorAtual) {
        this.jogadorAtual = jogadorAtual;
    }
}