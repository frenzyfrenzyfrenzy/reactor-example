package com.svintsov;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

@Slf4j
public class SocketUtils {

    public static int getClientPort(SocketChannel socketChannel) {
        try {
             return ((InetSocketAddress) socketChannel.getRemoteAddress()).getPort();
        } catch (IOException e) {
            log.error("Cannot determine remote client's address for: {}. Error: {}", socketChannel, e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
