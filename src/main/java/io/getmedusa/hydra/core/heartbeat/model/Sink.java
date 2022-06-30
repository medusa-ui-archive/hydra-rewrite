package io.getmedusa.hydra.core.heartbeat.model;

import reactor.core.publisher.Flux;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sink {

    interface EventListener<T> {
        void onDataChunk(T chunk);
        void processComplete();
    }

    interface EventProcessor {
        void register(EventListener<Heartbeat> eventListener);

        void dataChunk(Heartbeat value);
        void processComplete();
    }

    private final EventProcessor eventProcessor = new EventProcessor() {

        private EventListener<Heartbeat> eventListener;
        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        @Override
        public void register(EventListener<Heartbeat> eventListener) {
            this.eventListener = eventListener;
        }

        @Override
        public void dataChunk(Heartbeat value) {
            executor.schedule(() -> eventListener.onDataChunk(value), 500, TimeUnit.MILLISECONDS);
        }

        @Override
        public void processComplete() {
            executor.schedule(() -> eventListener.processComplete(), 500, TimeUnit.MILLISECONDS);
        }
    };

    Flux<Heartbeat> eventFlux = Flux.create(sink -> eventProcessor.register(
            new EventListener<>() {
                public void onDataChunk(Heartbeat chunk) {
                    sink.next(chunk);
                }

                public void processComplete() {
                    sink.complete();
                }
            }));

    public void push(Heartbeat heartbeat) {
        eventProcessor.dataChunk(heartbeat);
    }

    public Flux<Heartbeat> asFlux() {
        return eventFlux;
    }

}
