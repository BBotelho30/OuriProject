/**
 * Representa o resultado de uma tentativa de jogada.
 *
 * A classe indica se a jogada foi válida e fornece uma mensagem de erro quando necessário.
 */
package com.meu.ourigame.model;

public class ResultadoJogada {
    private final boolean valida;
    private final String mensagem;

    public ResultadoJogada(boolean valida, String mensagem) {
        this.valida = valida;
        this.mensagem = mensagem;
    }

    public boolean isValida() {
        return valida;
    }

    public String getMensagem() {
        return mensagem;
    }

    public static ResultadoJogada sucesso() {
        return new ResultadoJogada(true, "");
    }

    public static ResultadoJogada erro(String mensagem) {
        return new ResultadoJogada(false, mensagem);
    }
}