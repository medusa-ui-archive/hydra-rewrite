package io.getmedusa.hydra.core.discovery;

import io.getmedusa.hydra.core.discovery.model.ActiveServiceOverview;
import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;
import io.getmedusa.hydra.core.heartbeat.model.meta.FragmentRequest;
import io.getmedusa.hydra.core.heartbeat.model.meta.RenderedFragment;
import io.getmedusa.hydra.core.heartbeat.repository.MemoryRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/h/discovery/{publicKey}/requestFragment")
    public Mono<List<RenderedFragment>> requestFragmentRender(List<FragmentRequest> requests) {
        Map<String, List<FragmentRequest>> requestsPerService = toMap(requests);
        List<RenderedFragment> resultList = new ArrayList<>();
        for(Map.Entry<String, List<FragmentRequest>> entry : requestsPerService.entrySet()) {
            List<RenderedFragment> renderedFragments = askFragmentsFromService(entry.getKey(), entry.getValue());
            resultList.addAll(renderedFragments);
        }
        return Mono.just(resultList);
    }

    private List<RenderedFragment> askFragmentsFromService(String key, List<FragmentRequest> value) {
        final ActiveService service = memoryRepository.findService(key);
        final ArrayList<RenderedFragment> renderedFragments = new ArrayList<>();
        for(FragmentRequest request : value) {
            if(service == null) {
                renderedFragments.add(null);
            } else {
                renderedFragments.add(askFragmentFromService(service, request));
            }
        }
        return renderedFragments;
    }

    public RenderedFragment askFragmentFromService(ActiveService activeService, FragmentRequest request) {
        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.post();
        String uri = activeService.getHost() + ":" + activeService.getPort() + "/h/fragment/_publicKey_/requestFragment"
                .replace("_publicKey_", publicKey);
        System.out.println(uri);
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(uri);
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue(activeService);

        headersSpec.exchangeToMono(response -> {
            if (response.statusCode().equals(HttpStatus.OK)) {
                return response.bodyToMono(String.class);
            } else {
                return response.createException()
                        .flatMap(Mono::error);
            }
        }).log().subscribe(System.out::println);
        return null;
    }

    private Map<String, List<FragmentRequest>> toMap(List<FragmentRequest> requests) {
        Map<String, List<FragmentRequest>> map = new HashMap<>();
        for(FragmentRequest request : requests) {
            List<FragmentRequest> r = map.get(request.getService());
            if(r == null) {
                r = new ArrayList<>();
            }
            r.add(request);
            map.put(request.getService(), r);
        }
        return map;
    }
}

