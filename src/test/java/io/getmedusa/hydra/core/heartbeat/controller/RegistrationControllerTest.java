package io.getmedusa.hydra.core.heartbeat.controller;

import io.getmedusa.hydra.core.discovery.RegistrationController;
import io.getmedusa.hydra.core.heartbeat.model.Fragment;
import io.getmedusa.hydra.core.heartbeat.model.FragmentHydraRequestWrapper;
import io.getmedusa.hydra.core.heartbeat.model.meta.RenderedFragment;
import io.getmedusa.hydra.core.heartbeat.repository.MemoryRepository;
import io.getmedusa.hydra.core.routing.DynamicRouteProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class RegistrationControllerTest {

    @Mock
    DynamicRouteProvider dynamicRouteProvider;

    @Mock
    MemoryRepository memoryRepository;

    @Mock
    WebClient client;

    private RegistrationController controller;



    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        controller = new RegistrationController("public", "private", dynamicRouteProvider, memoryRepository, client);
    }

    @Test
    void testRequestFragment() {
        FragmentHydraRequestWrapper wrapper = new FragmentHydraRequestWrapper();
        final Fragment fragment = new Fragment();
        fragment.setId("123");
        fragment.setRef("ref");
        fragment.setService("sample-service");
        fragment.setFallback("<p>hello world</p>");
        wrapper.setRequests(Map.of("sample-service", List.of(fragment)));

        final Mono<List<RenderedFragment>> mono = controller.requestFragmentRender(wrapper);

        final List<RenderedFragment> renderedFragments = mono.block();
        System.out.println(renderedFragments);

        Assertions.assertNotNull(renderedFragments);
        Assertions.assertEquals(1, renderedFragments.size());
        Assertions.assertEquals("123", renderedFragments.get(0).getId());
    }

}
