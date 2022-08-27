package io.getmedusa.hydra.core.repository;

import io.getmedusa.hydra.core.repository.meta.InMemoryStorage;
import io.getmedusa.hydra.core.repository.meta.RedisRepository;
import io.getmedusa.hydra.core.repository.MemoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

class MemoryRepositoryTest {

    @Mock
    private RedisRepository redis;

    MemoryRepository memoryRepositoryNoRedis;
    MemoryRepository memoryRepositoryWRedis;

    final InMemoryStorage inMemoryStorageWRedis = new InMemoryStorage();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        memoryRepositoryNoRedis = new MemoryRepository(new InMemoryStorage(), redis, false);
        memoryRepositoryWRedis = new MemoryRepository(inMemoryStorageWRedis, redis, true);
    }

    @Test
    void hasRouteHashChanged() {
        final String hash1 = UUID.randomUUID().toString();
        final String hash2 = hash1;
        final String hash3 = UUID.randomUUID().toString();

        Assertions.assertFalse(memoryRepositoryNoRedis.hasRouteHashChanged()); //this would be immediately applied, so no change pinging required

        Mockito.when(redis.retrieveOverallRouteHashKey())
                .thenReturn(null)
                .thenReturn(hash1)
                .thenReturn(hash2)
                .thenReturn(hash3);

        Assertions.assertFalse(memoryRepositoryWRedis.hasRouteHashChanged()); //first time, in memory storage is null but redis is also null
        Assertions.assertTrue(memoryRepositoryWRedis.hasRouteHashChanged()); //in memory storage is null but redis has value

        inMemoryStorageWRedis.setOverallRouteHashKey(hash1); //we remember value

        Assertions.assertFalse(memoryRepositoryWRedis.hasRouteHashChanged()); //value is the same, so no change
        Assertions.assertTrue(memoryRepositoryWRedis.hasRouteHashChanged()); //a new value is retrieved and mismatches with the previous value
    }

}
