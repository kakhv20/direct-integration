package ge.xcoder.playcore.direct_integration;

import ge.xcoder.playcore.direct_integration.api.port.NonceStore;

import java.util.HashSet;
import java.util.Set;

public class InMemoryNonceStore implements NonceStore {
    private final Set<String> seen = new HashSet<>();

    @Override
    public boolean storeIfAbsent(String nonce) {
        return seen.add(nonce);   // Set.add returns true if it was newly added
    }
}
