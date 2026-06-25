package com.meu.ourigame.network;

import java.util.function.Consumer;

/**
 * Abstração de ligação de rede para servidor e cliente.
 */
public interface NetworkConnection {
    /**
     * Inicia a conexão de rede em segundo plano.
     */
    void start();

    /**
     * Envia uma mensagem para o outro jogador.
     *
     * @param message mensagem a enviar.
     */
    void send(String message);

    /**
     * Regista um callback para mensagens recebidas.
     *
     * @param onMessage ação executada quando uma mensagem chega.
     */
    void setOnMessage(Consumer<String> onMessage);

    /**
     * Fecha a conexão e liberta recursos associados.
     */
    void close();
}