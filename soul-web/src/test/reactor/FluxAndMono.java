package reactor;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class FluxAndMono {

    public static void main(String[] args) throws Exception {
        FluxAndMono fluxAndMono = new FluxAndMono();
        fluxAndMono.test01();
    }

    public void test01() throws Exception {
        Flux.create(sink -> {
            sink.next("sink：" + Thread.currentThread().getName());
            sink.complete();
        })
                .publishOn(Schedulers.single())
                .map(x -> x + "   map：" + Thread.currentThread().getName())
                .subscribeOn(Schedulers.parallel())
                .toStream()
                .forEach(System.out::println);


    }
}
