package com.meu.ourigame.network;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Client implements NetworkConnection {
    private final String ip;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessage;

    private final List<String> mensagensPendentes = new ArrayList<>();

    public Client(String ip) {
        this.ip = ip;
    }

    @Override
    public void start() {
        new Thread(() -> {
            try {
                socket = new Socket(ip, 5000);

                System.out.println("Ligado ao servidor!");

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                for (String mensagem : mensagensPendentes) {
                    out.println(mensagem);
                    System.out.println("Mensagem pendente enviada: " + mensagem);
                }

                mensagensPendentes.clear();

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Cliente recebeu: " + msg);

                    if (onMessage != null) {
                        onMessage.accept(msg);
                    }
                }

            } catch (IOException e) {
                System.out.println("Cliente não conseguiu ligar ao servidor.");
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void send(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Cliente enviou: " + message);
        } else {
            mensagensPendentes.add(message);
            System.out.println("Cliente guardou mensagem pendente: " + message);
        }
    }

    @Override
    public void setOnMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    @Override
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}