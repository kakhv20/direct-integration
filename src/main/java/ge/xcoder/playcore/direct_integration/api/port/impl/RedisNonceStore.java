package ge.xcoder.playcore.direct_integration.api.port.impl;

import ge.xcoder.playcore.direct_integration.api.port.NonceStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisNonceStore implements NonceStore {
    private static final String KEY_PREFIX = "nonce:";
    private final StringRedisTemplate redis;

    public RedisNonceStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean storeIfAbsent(String nonce, long ttlSeconds) {
        // SET nonce:<value> <value> NX EX <ttlSeconds> — atomic; returns true only if the key was absent.
        // TTL tracks the timestamp drift window: any replay older than that is already rejected by
        // the timestamp check, so nonces need not be retained longer.
        Boolean result = redis.opsForValue().setIfAbsent(KEY_PREFIX + nonce, nonce, Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(result);
    }
}
