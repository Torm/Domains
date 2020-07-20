package no.hyp.domains;

import java.util.Optional;
import java.util.UUID;

/**
 * A chunk partitioned into columns, where each column is separately claimed by a domain.
 */
public final class PartitionedChunk {

    private final UUID worldUuid;

    private final int chunkX;

    private final int chunkZ;

    /**
     * An array representing the 16 * 16 grid of columns in a chunk.
     * Each column can belong to a different domain.
     */
    private final Key[] claims;

    public PartitionedChunk(UUID worldUuid, int chunkX, int chunkZ, Key[] claims) throws IllegalArgumentException {
        this.worldUuid = worldUuid;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        if (claims.length != 16 * 16) {
            throw new IllegalArgumentException("claims array must have a length of 16 * 16.");
        }
        this.claims = claims;
    }

    public PartitionedChunk(UUID worldUuid, int chunkX, int chunkZ) {
        this(worldUuid, chunkX, chunkZ, new Key[16 * 16]);
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

    public Key[] getClaims() {
        return this.claims;
    }

    public Optional<Key> getDomainKey(int i, int k) throws IllegalArgumentException {
        if (i < 0 || i >= 16 || k < 0 || k >= 16) {
            throw new IllegalArgumentException("i and k must have values between 0 and 15.");
        }
        return Optional.ofNullable(this.claims[PartitionedChunk.columnNumber(i, k)]);
    }

    public void setDomainKey(int i, int k, Key domainKey) {
        this.claims[PartitionedChunk.columnNumber(i, k)] = domainKey;
    }

    public static int columnNumber(int i, int k) {
        return i * 16 + k;
    }

    public static int iFromColumnNumber(int columnNumber) {
        return columnNumber / 16;
    }

    public static int kFromColumnNumber(int columnNumber) {
        return columnNumber % 16;
    }

}
