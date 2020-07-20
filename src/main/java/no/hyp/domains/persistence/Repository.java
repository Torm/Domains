package no.hyp.domains.persistence;

import no.hyp.domains.*;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Repository extends AutoCloseable {

    Optional<Domain> loadDomain(Key domainKey) throws RepositoryException;

    void saveDomain(Domain domain) throws RepositoryException;

    void deleteDomain(Key domainKey) throws RepositoryException;

    ChunkType loadChunkType(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException;

    Optional<ExclusiveChunk> loadExclusiveChunk(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException;

    void saveExclusiveChunk(ExclusiveChunk chunk) throws RepositoryException;

    Optional<PartitionedChunk> loadPartitionedChunk(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException;

    void savePartitionedChunk(PartitionedChunk chunk) throws RepositoryException;

    void deleteChunk(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException;

    Collection<Title> loadPlayerTitles(UUID playerUuid) throws RepositoryException;

    Collection<Title> loadDomainTitles(Key domainKey) throws RepositoryException;

    default Domain getDomain(UUID worldUuid, int x, int z) throws RepositoryException {
        int chunkX = x / 16;
        int chunkZ = z / 16;
        ChunkType type = this.loadChunkType(worldUuid, chunkX, chunkZ);
        if (type == ChunkType.EXCLUSIVE) {
            ExclusiveChunk chunk = this.loadExclusiveChunk(worldUuid, chunkX, chunkZ).;
        } else if (type == ChunkType.PARTITIONED) {
            PartitionedChunk chunk = this.loadPartitionedChunk(worldUuid, chunkX, chunkZ);
        }
    }

}
