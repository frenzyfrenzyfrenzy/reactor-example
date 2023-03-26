package com.svintsov;

import static com.svintsov.SocketUtils.getClientPort;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CustomHttpServer {

    private static final int MAX_CLIENTS = 5;
    private ServerSocketChannel serverSocketChannel;
    private Selector serverSelector;
    private Selector clientSelector;
    private Map<Integer, SocketChannel> clientSockets = new HashMap<>();

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

                serverSelector = Selector.open();
                clientSelector = Selector.open();

                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress("localhost", 8080));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                log.error("Error starting the server", e);
                throw new RuntimeException(e);
            }
        }

    }

    public void start() {

        while (true) {

            try {
                log.info("Polling...");
                Set<SelectionKey> selectedKeys;
                try {
                    selectedKeys = pollForClients();
                } catch (FailedIterationException failedIterationException) {
                    log.info("Skipping an iteration...");
                    continue;
                }

                ServerSocketChannel readyServerChannel = validateAndGetServerSelectionKey(selectedKeys);
                SocketChannel clientSocketChannel = readyServerChannel.accept();
                log.info("New client arrived: {}", getClientPort(clientSocketChannel));
                clientSocketChannel.configureBlocking(false);

                if (clientSockets.size() >= MAX_CLIENTS) {
                    log.warn("Maximum amount of pending client requests reached");
                    writeToClient(format("Sorry %s, cannot accept you", getClientPort(clientSocketChannel)),
                            HttpResponse.STATUS_SERVICE_UNAVAILABLE, clientSocketChannel);
                    clientSocketChannel.close();
                    continue;
                }

                clientSocketChannel.register(clientSelector, SelectionKey.OP_READ);
                int remotePort = getClientPort(clientSocketChannel);
                clientSockets.put(remotePort, clientSocketChannel);
                writeToClient(format("Hello %s", remotePort), HttpResponse.STATUS_OK, clientSocketChannel);
            } catch (Exception e) {
                log.error("An error occurred when listening for clients connections", e);
                throw new RuntimeException(e);
            }
        }
    }

    private ServerSocketChannel validateAndGetServerSelectionKey(Set<SelectionKey> selectedKeys) {
        SelectionKey selectionKey = new ArrayList<>(selectedKeys).get(0);
        if (isNull(selectionKey)) {
            throw new RuntimeException("Selection key is null, cannot proceed");
        }

        if (!selectionKey.isAcceptable()) {
            throw new RuntimeException("Client connection is not acceptable, cannot proceed");
        }

        return (ServerSocketChannel) selectionKey.channel();
    }

    private Set<SelectionKey> pollForClients() throws IOException, FailedIterationException {

        int readyChannelsCount = serverSelector.select();
        if (readyChannelsCount > 1) {
            log.error("More then one server {} is ready to accept connections, this is a " +
                    "misconfiguration issue, the program will be stopped", readyChannelsCount);
            throw new RuntimeException("More then one server  is ready to accept connections, this is a " +
                    "misconfiguration issue, the program will be stopped");
        }

        Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
        if (selectedKeys.isEmpty()) {
            log.error("Selection keys are empty, cannot proceed");
            throw new FailedIterationException();
        }

        return selectedKeys;
    }

    private boolean writeToClient(String response, String status, SocketChannel socketChannel) {
        log.info("Responding to client: {}", getClientPort(socketChannel));
        HttpResponse httpResponse = HttpResponse.builder()
                .status(status)
                .body(response)
                .contentLength(ofNullable(response).map(String::getBytes).map(bytes -> bytes.length).orElse(0))
                .contentType("text/plain")
                .build();
        try {
            int bytesWritten = socketChannel.write(ByteBuffer.wrap(httpResponse.toResponse().getBytes(StandardCharsets.UTF_8)));
            log.info("{} bytes written to client {}", bytesWritten, getClientPort(socketChannel));
            return true;
        } catch (IOException e) {

            log.error("Cannot respond to client ");
            return false;
        }
    }

}
