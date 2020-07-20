package no.hyp.domains;

import java.util.Optional;
import java.util.UUID;

/**
 * A chunk that is exclusively claimed as a whole for a domain.
 */
public final class ExclusiveChunk {

    private final UUID worldUuid;

    private final int chunkX;

    private final int chunkZ;

    /**
     * The domain this chunk belongs to.
     */
    private final Key domainKey;

    public ExclusiveChunk(UUID worldUuid, int chunkX, int chunkZ, Key domainKey) {
        this.worldUuid = worldUuid;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.domainKey = domainKey;
    }

    public UUID getWorldUuid() {
        return this.worldUuid;
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public Optional<Key> getDomainKey(int i, int k) {
        return Optional.of(this.domainKey);
    }

    public Key getDomainKey() {
        return this.domainKey;
    }

}
