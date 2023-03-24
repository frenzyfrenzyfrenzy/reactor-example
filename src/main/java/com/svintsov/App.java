package com.svintsov;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class App {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    public static void main(String[] args) {
        new App().doStuff();
    }

    @SneakyThrows
    private void doStuff() {
        Scheduler subscribeOnScheduler = Schedulers.fromExecutorService(createExecutor("subscribeOnScheduler"));
        Scheduler publishOnScheduler = Schedulers.fromExecutorService(createExecutor("publishOnScheduler"));

        Mono<String> coldMono = createMono();

        Subscriber<String> firstSubscriber = new CustomSubscriber("firstSubscriber");
        coldMono
                .subscribeOn(subscribeOnScheduler)
                .publishOn(publishOnScheduler)
                .subscribe(firstSubscriber);

/*        Subscriber<String> secondSubscriber = new CustomSubscriber("secondSubscriber");
        coldMono
                .subscribe(secondSubscriber);*/

        log.info("At the end of the main program");
    }

    private Mono<String> createMono() {
        return Mono.create(monoSink -> {
            long randomNumber = random.nextLong(1000);
            log.info("Emitting a value and finishing the mono with the value {}", randomNumber);
            monoSink.success(String.valueOf(randomNumber));
        });
    }

    private ExecutorService createExecutor(String threadNamePrefix) {
        return new ThreadPoolExecutor(0, 5, 500, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(true), new NamedThreadFactory(threadNamePrefix));
    }
}
