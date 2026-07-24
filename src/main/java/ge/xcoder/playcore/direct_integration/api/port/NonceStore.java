package ge.xcoder.playcore.direct_integration.api.port;

public interface NonceStore {
    /**
     * @return true if the nonce was NOT already present (and is now stored); false if it existed.
     */
    boolean storeIfAbsent(String nonce, long ttlSeconds);
}
