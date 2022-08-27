package io.getmedusa.hydra.core.discovery;

import io.getmedusa.hydra.core.discovery.model.ActiveServiceOverview;
import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;
import io.getmedusa.hydra.core.repository.MemoryRepository;
import io.getmedusa.hydra.core.routing.DynamicRouteProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class RegistrationController {

    //TODO basic auth w/ priv key

    private final String publicKey;
    private final String privateKey;

    private final DynamicRouteProvider dynamicRouteProvider;
    private final MemoryRepository memoryRepository;
    private final WebClient client;

    public RegistrationController(@Value("${medusa.hydra.secret.public}") String publicKey,
                                  @Value("${medusa.hydra.secret.private}") String privateKey,
                                  DynamicRouteProvider dynamicRouteProvider,
                                  MemoryRepository memoryRepository,
                                  WebClient client) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.dynamicRouteProvider = dynamicRouteProvider;
        this.memoryRepository = memoryRepository;
        this.client = client;
    }

    @PostMapping("/h/discovery/{publicKey}/registration")
    public Mono<List<ActiveServiceOverview>> incomingRegistration(@RequestBody ActiveService activeService,
                                                                  @PathVariable String publicKey,
                                                                  ServerHttpRequest request,
                                                                  ServerHttpResponse response) {
        if(this.publicKey.equals(publicKey)) {
            memoryRepository.storeActiveServices(activeService.getName(), activeService.updateFromRequest(request));
            dynamicRouteProvider.add(activeService);
            return Mono.just(ActiveServiceOverview.of(memoryRepository.retrieveActiveService())).doFinally(x -> dynamicRouteProvider.reload());
        } else {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return Mono.empty();
        }
    }

    @PostMapping("/h/discovery/{publicKey}/alive")
    public Mono<Boolean> incomingAlive(@RequestBody String name, @PathVariable String publicKey, ServerHttpResponse response) {
        if(this.publicKey.equals(publicKey)) {
            memoryRepository.updateAlive(name);
            return Mono.just(true);
        } else {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return Mono.empty();
        }
    }

}

