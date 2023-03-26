package com.svintsov;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Monos {

    @SneakyThrows
    private void doStuff() {
        Scheduler subscribeOnScheduler = Schedulers.fromExecutorService(createExecutor("subscribeOnScheduler"));
        Scheduler publishOnScheduler = Schedulers.fromExecutorService(createExecutor("publishOnScheduler"));

        Mono<String> sinkMono = createSinkMono();
        Mono<String> supplierMono = createSupplierMono();

        Subscriber<String> firstSubscriber = new CustomSubscriber("firstSubscriber");
        Subscriber<String> secondSubscriber = new CustomSubscriber("secondSubscriber");

        supplierMono
                .subscribeOn(subscribeOnScheduler)
                .publishOn(publishOnScheduler)
                .subscribe(firstSubscriber);

        supplierMono
                .subscribeOn(subscribeOnScheduler)
                .publishOn(publishOnScheduler)
                .subscribe(secondSubscriber);

        log.info("At the end of the main program");
    }

    private Mono<String> createSinkMono() {
        return Mono.create(monoSink -> {
            long randomNumber = ThreadLocalRandom.current().nextLong(1000);
            log.info("Emitting a value and finishing the mono with the value {}", randomNumber);
            monoSink.success(String.valueOf(randomNumber));
        });
    }

    private Mono<String> createSupplierMono() {
        return Mono.defer(() -> {
            long randomValue = ThreadLocalRandom.current().nextLong(1000);
            log.info("Emitting a value: {}", randomValue);
            return Mono.just(String.valueOf(randomValue));
        });
    }

    private ExecutorService createExecutor(String threadNamePrefix) {
        return new ThreadPoolExecutor(0, 5, 500, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(true), new NamedThreadFactory(threadNamePrefix));
    }

}
