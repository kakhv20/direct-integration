package ge.xcoder.playcore.direct_integration.api.port.impl;

import ge.xcoder.playcore.direct_integration.api.port.NonceStore;
import org.springframework.stereotype.Component;

@Component
public class RedisNonceStore implements NonceStore {
    @Override
    public boolean storeIfAbsent(String nonce) {
        return false;
    }
}
