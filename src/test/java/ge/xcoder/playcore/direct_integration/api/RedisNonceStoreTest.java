package ge.xcoder.playcore.direct_integration.api;

import ge.xcoder.playcore.direct_integration.api.port.impl.RedisNonceStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration test for the real Redis adapter. It talks to a Redis at REDIS_HOST:REDIS_PORT
 * (default localhost:6379) — locally you run one via `docker run -d -p 6379:6379 redis`, in CI
 * a GitLab `services: redis` provides it. If no Redis is reachable, the test SKIPS (not fails),
 * so `./gradlew test` stays green everywhere. No Testcontainers / docker-java involved.
 */
class RedisNonceStoreTest {
    private static final String HOST = System.getenv().getOrDefault("REDIS_HOST", "localhost");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

    private RedisNonceStore store;

    private static boolean redisReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 300);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        assumeTrue(redisReachable(), () -> "Redis not reachable at " + HOST + ":" + PORT + " — skipping");

        var factory = new LettuceConnectionFactory(HOST, PORT);
        factory.afterPropertiesSet();
        var template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        store = new RedisNonceStore(template);
    }

    @Test
    void firstUseAccepted_replayRejected() {
        String nonce = "nonce-" + UUID.randomUUID();     // unique per run — no cross-run pollution
        int ttlSeconds = 30;

        Assertions.assertTrue(store.storeIfAbsent(nonce, ttlSeconds), "first use should be accepted");
        Assertions.assertFalse(store.storeIfAbsent(nonce, ttlSeconds), "replay of the same nonce should be rejected");
    }
}
