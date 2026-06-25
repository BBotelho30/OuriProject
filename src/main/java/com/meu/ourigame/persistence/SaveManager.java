package com.meu.ourigame.persistence;

import com.meu.ourigame.model.JogoOuri;
import com.meu.ourigame.model.Tabuleiro;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SaveManager {

    private static final String FICHEIRO = "ouri_estado.txt";

    public static void guardar(JogoOuri jogo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FICHEIRO))) {
            writer.println(serializar(jogo));
        }
    }

    public static void carregar(JogoOuri jogo) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(FICHEIRO))) {
            String linha = reader.readLine();

            if (linha != null) {
                aplicarEstado(jogo, linha);
            }
        }
    }

    public static String serializar(JogoOuri jogo) {
        Tabuleiro tabuleiro = jogo.getTabuleiro();

        StringBuilder sb = new StringBuilder();

        sb.append(jogo.getJogadorAtual()).append(";");

        sb.append(tabuleiro.getDeposito(0)).append(",");
        sb.append(tabuleiro.getDeposito(1)).append(";");

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            sb.append(tabuleiro.getCasa(0, i));

            if (i < Tabuleiro.NUM_CASAS - 1) {
                sb.append(",");
            }
        }

        sb.append(";");

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            sb.append(tabuleiro.getCasa(1, i));

            if (i < Tabuleiro.NUM_CASAS - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    public static void aplicarEstado(JogoOuri jogo, String estado) {
        String[] partes = estado.split(";");

        if (partes.length != 4) {
            return;
        }

        int jogadorAtual = Integer.parseInt(partes[0]);

        String[] depositos = partes[1].split(",");
        String[] casasJ1 = partes[2].split(",");
        String[] casasJ2 = partes[3].split(",");

        Tabuleiro tabuleiro = jogo.getTabuleiro();

        tabuleiro.setDeposito(0, Integer.parseInt(depositos[0]));
        tabuleiro.setDeposito(1, Integer.parseInt(depositos[1]));

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            tabuleiro.setCasa(0, i, Integer.parseInt(casasJ1[i]));
            tabuleiro.setCasa(1, i, Integer.parseInt(casasJ2[i]));
        }

        jogo.setJogadorAtual(jogadorAtual);
    }
}