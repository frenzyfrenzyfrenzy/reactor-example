package com.svintsov;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
@RequiredArgsConstructor
public class CustomSubscriber implements Subscriber<String> {

    private final String name;
    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription s) {
        log.info("{} just subscribed", name);
        subscription = s;
        subscription.request(Integer.MAX_VALUE);
    }

    @Override
    public void onNext(String s) {
        log.info("{} received the next value {}", name, s);
    }

    @Override
    public void onError(Throwable t) {
        log.info("{} received the error}", name, t);
    }

    @Override
    public void onComplete() {
        log.info("{} just completed", name);
    }

}
