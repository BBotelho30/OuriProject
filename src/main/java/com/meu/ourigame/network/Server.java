package com.meu.ourigame.network;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Server implements NetworkConnection {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessage;

    private final List<String> mensagensPendentes = new ArrayList<>();

    @Override
    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(5000));

                System.out.println("Servidor à espera na porta 5000...");

                socket = serverSocket.accept();

                System.out.println("Cliente ligado!");

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                for (String mensagem : mensagensPendentes) {
                    out.println(mensagem);
                    System.out.println("Mensagem pendente enviada: " + mensagem);
                }

                mensagensPendentes.clear();

                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Servidor recebeu: " + msg);

                    if (onMessage != null) {
                        onMessage.accept(msg);
                    }
                }

            } catch (IOException e) {
                System.out.println("Servidor terminou ou falhou a ligação.");
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void send(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Servidor enviou: " + message);
        } else {
            mensagensPendentes.add(message);
            System.out.println("Servidor guardou mensagem pendente: " + message);
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
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}