package com.svintsov;

import static java.util.Objects.nonNull;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class CustomHttpServer {

    private ServerSocketChannel serverSocketChannel;

    public void init() {

        if (nonNull(serverSocketChannel)) {
            log.warn("Server is already started, doing nothing...");
            return;
        }

        synchronized (this) {
            if (nonNull(serverSocketChannel)) {
                log.warn("Server is already started, doing nothing...");
                return;
            }
            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress("localhost", 8080));
                serverSocketChannel.configureBlocking(true);
            } catch (IOException e) {
                log.error("Error starting the server", e);
                throw new RuntimeException(e);
            }
        }

    }

    public void start() {
        while (true) {
            try {
                SocketChannel clientSocket = serverSocketChannel.accept();
                log.info("Client connection accepted: {}", clientSocket);
                clientSocket.configureBlocking(true);
            } catch (IOException e) {
                log.error("Error while listening for connection", e);
                throw new RuntimeException(e);
            }
        }
    }

}
