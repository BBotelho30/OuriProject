
package com.meu.ourigame.network;

import java.util.function.Consumer;

public interface NetworkConnection {
    void start();
    void send(String message);
    void setOnMessage(Consumer<String> onMessage);
    void close();
}