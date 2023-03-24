package com.svintsov;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class NamedThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final AtomicInteger threadCounter = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        int threadNumber = threadCounter.getAndIncrement();
        Thread thread = new Thread(r);
        thread.setName(String.format("%s #%d", namePrefix, threadNumber));
        thread.setDaemon(false);
        return thread;
    }

}
