package com.meu.ourigame.network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class Server implements NetworkConnection {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessage;

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

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (onMessage != null) onMessage.accept(msg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void send(String message) {
        if (out != null) {
            out.println(message);
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