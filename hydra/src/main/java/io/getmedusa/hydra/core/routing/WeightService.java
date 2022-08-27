package io.getmedusa.hydra.core.routing;

import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;
import io.getmedusa.hydra.core.discovery.model.meta.AwakeningType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class WeightService {

    private Map<String, Integer> amounts = new HashMap<>();
    private Map<String, Set<Long>> uriVersusVersions = new HashMap<>();

    //% based but as %10, so 10 = 100%, 5 = 50%, etc
    public int generateWeight(String uri, ActiveService activeService) {
        /*if(AwakeningType.NEWEST_VERSION_WINS.equals(activeService.getAwakening())) {
            Long latestVersion = getLatestVersion(uri);
            if(activeService.getVersion() != latestVersion){
                return 0;
            }
        }

        final Integer amount = amounts.getOrDefault(getKey(uri, activeService), 0);
        if(amount == 0) return 0;
        return 10 / amount;*/
        return 10;
    }

    private void calculateWeight(String uri, ActiveService activeService) {
        if(AwakeningType.NEWEST_VERSION_WINS.equals(activeService.getAwakening())) {
            Long latestVersion = getLatestVersion(uri);
            if(activeService.getVersion() != latestVersion){
                amounts.put(getKey(uri, activeService), 0);
                return;
            }
        }

        Integer amount = amounts.getOrDefault(getKey(uri, activeService), 0);
        amounts.put(getKey(uri, activeService), ++amount);
    }

    private String getKey(String uri, ActiveService activeService) {
        return uri + "#" + activeService.getVersion();
    }

    public Map<String, Integer> getAmounts() {
        return amounts;
    }

    public void load(Set<ActiveService> activeServices) {
        amounts.clear();
        uriVersusVersions.clear();

        for(ActiveService activeService : activeServices) {
            for(String uri : activeService.getEndpoints()) {
                Set<Long> set = this.uriVersusVersions.getOrDefault(uri, new HashSet<>());
                set.add(activeService.getVersion());
                this.uriVersusVersions.put(uri, set);
                calculateWeight(uri, activeService);
            }
        }
    }

    private Long getLatestVersion(String uri) {
        Set<Long> versionsActive = this.uriVersusVersions.getOrDefault(uri, new HashSet<>());
        Long longest = null;
        for(Long version : versionsActive){
            if(longest == null || longest < version) {
                longest = version;
            }
        }
        return longest;
    }
}