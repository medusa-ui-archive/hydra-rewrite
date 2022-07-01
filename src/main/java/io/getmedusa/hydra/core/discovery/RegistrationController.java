package io.getmedusa.hydra.core.discovery;

import io.getmedusa.hydra.core.discovery.model.ActiveServiceOverview;
import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;
import io.getmedusa.hydra.core.heartbeat.model.Fragment;
import io.getmedusa.hydra.core.heartbeat.model.FragmentHydraRequestWrapper;
import io.getmedusa.hydra.core.heartbeat.model.FragmentRequestWrapper;
import io.getmedusa.hydra.core.heartbeat.model.meta.FragmentRequest;
import io.getmedusa.hydra.core.heartbeat.model.meta.RenderedFragment;
import io.getmedusa.hydra.core.heartbeat.repository.MemoryRepository;
import io.getmedusa.hydra.core.routing.DynamicRouteProvider;
import io.getmedusa.hydra.core.util.JSONUtils;
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
import java.util.stream.Stream;

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
    public Mono<List<RenderedFragment>> requestFragmentRender(@RequestBody final FragmentHydraRequestWrapper requests) {
        Mono<List<RenderedFragment>> mono = null;
        final Map<String, List<Fragment>> requestMap = requests.getRequests();
        for(Map.Entry<String, List<Fragment>> entry : requestMap.entrySet()) {
            final Mono<List<RenderedFragment>> askFragmentMono = askFragmentsFromService(entry.getKey(), entry.getValue(), requests.getAttributes());
            if(mono == null) {
                mono = askFragmentMono;
            } else {
                mono = mono
                        .zipWith(askFragmentMono)
                        .map(t -> Stream.concat(t.getT1().stream(), t.getT2().stream()).toList());
            }
        }
        return mono;
    }

    private Mono<List<RenderedFragment>> askFragmentsFromService(String key, List<Fragment> value, Map<String, Object> attributes) {
        final ActiveService service = memoryRepository.findService(key);
        final List<RenderedFragment> renderedFragments = new ArrayList<>();

        if(service != null) {
            return askFragmentFromService(service, value, attributes);
        } else {
            for(Fragment request : value) {
                final RenderedFragment fragment = new RenderedFragment();
                fragment.setId(request.getId());
                renderedFragments.add(fragment);
            }
            return Mono.just(renderedFragments);
        }
    }

    public Mono<List<RenderedFragment>> askFragmentFromService(ActiveService activeService, List<Fragment> request, Map<String, Object> attributes) {
        FragmentRequestWrapper wrapper = new FragmentRequestWrapper();
        wrapper.setRequests(request);
        wrapper.setAttributes(attributes);

        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.post();
        String uri = activeService.getWebProtocol() + "://" + activeService.getHost() + ":" + activeService.getPort() + "/h/fragment/_publicKey_/requestFragment"
                .replace("_publicKey_", publicKey);
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(uri);
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue(wrapper);

        return headersSpec.exchangeToMono(response -> {
            if (response.statusCode().equals(HttpStatus.OK)) {
                return response.bodyToMono(String.class);
            } else {
                return response.createException().flatMap(Mono::error);
            }
        }).log().map(json -> JSONUtils.deserializeList(json, RenderedFragment.class));
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

