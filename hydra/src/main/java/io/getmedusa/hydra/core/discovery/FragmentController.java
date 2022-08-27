package io.getmedusa.hydra.core.discovery;

import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;
import io.getmedusa.hydra.core.discovery.model.Fragment;
import io.getmedusa.hydra.core.discovery.model.FragmentHydraRequestWrapper;
import io.getmedusa.hydra.core.discovery.model.FragmentRequestWrapper;
import io.getmedusa.hydra.core.discovery.model.RenderedFragment;
import io.getmedusa.hydra.core.repository.MemoryRepository;
import io.getmedusa.hydra.core.util.JSONUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
public class FragmentController {

    private final String publicKey;
    private final String privateKey;

    private final MemoryRepository memoryRepository;
    private final WebClient client;

    public FragmentController(@Value("${medusa.hydra.secret.public}") String publicKey,
                              @Value("${medusa.hydra.secret.private}") String privateKey,
                              MemoryRepository memoryRepository,
                              WebClient client) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.memoryRepository = memoryRepository;
        this.client = client;
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
        }).map(json -> JSONUtils.deserializeList(json, RenderedFragment.class));
    }

}
