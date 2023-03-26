package com.svintsov;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {


    public static void main(String[] args) {
        CustomHttpServer server = new CustomHttpServer();
        server.init();
        server.start();
    }


}
