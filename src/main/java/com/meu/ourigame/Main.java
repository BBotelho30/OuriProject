package com.meu.ourigame;

import javafx.application.Application;

/**
 * Classe de entrada da aplicação Ouri.
 *
 * Esta classe contém apenas o main que inicia a aplicação JavaFX.
 */
public class Main {
    /**
     * Inicia a aplicação JavaFX.
     *
     * @param args argumentos de linha de comando não utilizados.
     */
    public static void main(String[] args) {
        Application.launch(OuriGameUI.class, args);
    }
}