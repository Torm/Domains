package no.hyp.domains.persistence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Collection;

public interface SqlDatabase extends AutoCloseable {

    /**
     * Get the database schema version.
     */
    int version() throws SQLException;

    void transaction() throws SQLException;

    /**
     * Commit transaction.
     */
    void commit() throws SQLException;

    /**
     * Rollback transaction.
     */
    void rollback() throws SQLException;

    /**
     * Create or upgrade the database schema.
     */
    void upgradeDatabase() throws SQLException;

    @Nullable DomainData selectDomain(String domainKey) throws SQLException;

    void upsertDomain(String domainKey, String defaultRole, @Nullable String displayName) throws SQLException;

    void deleteDomain(String domainKey) throws SQLException;

    @Nullable TitleData selectTitle(String domainKey, String playerUuid) throws SQLException;

    void upsertTitle(String domainKey,  String playerUuid, String role, String title) throws SQLException;

    void deleteTitle(String domainKey, String playerUuid) throws SQLException;

    Collection<TitleData> selectDomainTitles(String domainKey) throws SQLException;

    Collection<TitleData> selectPlayerTitles(String playerUuid) throws SQLException;

    @Nullable String selectChunkType(String worldUuid, int chunkX, int chunkZ) throws SQLException;

    void upsertChunkType(String worldUuid, int chunkX, int chunkZ, String type) throws SQLException;

    void deleteChunkType(String worldUuid, int chunkX, int chunkZ) throws SQLException;

    @Nullable String selectExclusiveChunk(String worldUuid, int chunkX, int chunkZ) throws SQLException;

    void upsertExclusiveChunk(String worldUuid, int chunkX, int chunkZ, String domainKey) throws SQLException;

    void deleteExclusiveChunk(String worldUuid, int chunkX, int chunkZ) throws SQLException;

    String[] selectPartitionedChunk(String worldUuid, int chunkX, int chunkZ) throws SQLException;

    @Nullable String selectPartitionedChunkColumn(String worldUuid, int chunkX, int chunkZ, int columnNumber) throws SQLException;

    void upsertPartitionedChunkColumn(String worldUuid, int chunkX, int chunkZ, int columnNumber, String domainKey) throws SQLException;

    void deletePartitionedChunkColumn(String worldUuid, int chunkX, int chunkZ, int columnNumber) throws SQLException;

    void deletePartitionedChunk(String worldUuid, int chunkX, int chunkZ) throws SQLException;

    default void deleteChunk(String worldUuid, int chunkX, int chunkZ) throws SQLException {
        this.deleteChunkType(worldUuid, chunkX, chunkZ);
        this.deleteExclusiveChunk(worldUuid, chunkX, chunkZ);
        this.deletePartitionedChunk(worldUuid, chunkX, chunkZ);
    }

}
