/**
 * Representa o estado geral do jogo Ouri.
 *
 * Esta classe contém a lógica de alternância de turnos e delega as jogadas para a classe {@link com.meu.ourigame.model.Jogada}.
 */
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

    public ResultadoJogada jogar(int casaEscolhida) {
        Jogada jogada = new Jogada(jogadorAtual, casaEscolhida);
        ResultadoJogada resultado = jogada.aplicar(tabuleiro, score);

        if (resultado.isValida()) {
            trocarJogador();
        }

        return resultado;
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

    public int getIndiceVencedor() {
        int pontosJ1 = tabuleiro.getDeposito(0);
        int pontosJ2 = tabuleiro.getDeposito(1);

        if (pontosJ1 > pontosJ2) {
            return 0;
        } else if (pontosJ2 > pontosJ1) {
            return 1;
        } else {
            return -1;
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