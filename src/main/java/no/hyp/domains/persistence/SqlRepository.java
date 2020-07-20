package no.hyp.domains.persistence;

import no.hyp.domains.*;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.*;

public class SqlRepository implements Repository {

    private final SqlDatabase database;

    public SqlRepository(SqlDatabase database) {
        this.database = database;
    }

    /**
     * Called when an SQLException occurs.
     * Attempt to roll back the database and return the exception wrapped in a RepositoryException.
     */
    private RepositoryException wrapSqlException(String message, Throwable cause) {
        RepositoryException rethrow = new RepositoryException(message, cause);
        try {
            this.database.rollback();
            return rethrow;
        } catch (SQLException rollbackException) {
            RuntimeException rollbackRethrow = new RuntimeException("Exception occurred while rolling back database. Very bad.", rollbackException);
            rollbackRethrow.addSuppressed(cause);
            throw rollbackRethrow;
        }
    }

    @Override
    public Optional<Domain> loadDomain(Key domainKey) throws RepositoryException {
        try {
            this.database.transaction();
            DomainData data = this.database.selectDomain(domainKey.toString());
            this.database.commit();
            if (data != null) {
                @Nullable String displayName = data.displayName;
                String roleString = data.defaultRole;
                Role defaultRole = Role.valueOf(roleString);
                Map<UUID, Title> titles = new HashMap<>();
                {
                    Collection<TitleData> titlesData = database.selectDomainTitles(domainKey.toString());
                    for (TitleData titleData : titlesData) {
                            UUID playerUuid = UUID.fromString(titleData.playerUuid);
                            Role role = Role.valueOf(titleData.role);
                            String name = titleData.title;
                            Title title = new Title(domainKey, playerUuid, name, role);
                            titles.put(playerUuid, title);

                    }
                }
                return Optional.of(new Domain(domainKey, defaultRole, Optional.ofNullable(displayName), titles));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while loading a Domain.", e);
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid value loaded from database.", e);
        }
    }

    @Override
    public void saveDomain(Domain domain) throws RepositoryException {
        try {
            String domainKey = domain.getKey().toString();
            String defaultRole = domain.getDefaultRole().name();
            String displayName = domain.getDisplayName().orElse(null);
            Map<UUID, Title> titles = domain.getTitles();
            this.database.transaction();
            // Retrieve the saved titles, compare them and delete the titles that have been removed.
            Collection<TitleData> storedTitles = this.database.selectDomainTitles(domainKey);
            for (TitleData titleData : storedTitles) {
                String playerUuid = titleData.playerUuid;
                if (titles.keySet().stream().noneMatch(x -> x.toString().equalsIgnoreCase(playerUuid))) {
                    this.database.deleteTitle(domainKey, playerUuid);
                }
            }
            // Insert the titles into the database.
            for (Map.Entry<UUID, Title> entry : titles.entrySet()) {
                String playerUuid = entry.getKey().toString();
                String roleString = entry.getValue().getRole().name();
                String title = entry.getValue().getName();
                this.database.upsertTitle(domainKey, playerUuid, roleString, title);
            }
            this.database.upsertDomain(domainKey, defaultRole, displayName);
            this.database.commit();
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while saving a Domain.", e);
        }
    }

    @Override
    public void deleteDomain(Key domainKey) throws RepositoryException {
        try {
            this.database.transaction();
            this.database.deleteDomain(domainKey.toString());
            this.database.commit();
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while deleting a Domain.", e);
        }
    }

    @Override
    public ChunkType loadChunkType(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException {
        try {
            this.database.transaction();
            String claimableType = this.database.selectChunkType(worldUuid.toString(), chunkX, chunkZ);
            this.database.commit();
            if (claimableType == null) {
                return ChunkType.UNCLAIMED;
            }
            try {
                return ChunkType.valueOf(claimableType);
            } catch ( IllegalArgumentException e ) {
                throw new RepositoryException("Invalid enum value loaded from database.", e);
            }
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while loading a ChunkType.", e);
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid value loaded from database.", e);
        }
    }

    @Override
    public Optional<ExclusiveChunk> loadExclusiveChunk(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException {
        try {
            this.database.transaction();
            String domainKey = this.database.selectExclusiveChunk(worldUuid.toString(), chunkX, chunkZ);
            this.database.commit();
            try {
                return Optional.of(new ExclusiveChunk(worldUuid, chunkX, chunkZ, new Key(domainKey)));
            } catch (IllegalArgumentException e) {
                throw new RepositoryException("Illegal domain key loaded from database.", e);
            }
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while loading an ExclusiveChunk.", e);
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid value loaded from database.", e);
        }
    }

    @Override
    public void saveExclusiveChunk(ExclusiveChunk chunk) throws RepositoryException {
        try {
            this.database.transaction();
            String worldUuidString = chunk.getWorldUuid().toString();
            int chunkX = chunk.getChunkX();
            int chunkZ = chunk.getChunkZ();
            String domainKey = chunk.getDomainKey().toString();
            this.database.deleteChunk(worldUuidString, chunkX, chunkZ);
            this.database.upsertChunkType(worldUuidString, chunkX, chunkZ, ChunkType.EXCLUSIVE.name());
            this.database.upsertExclusiveChunk(worldUuidString, chunkX, chunkZ, domainKey);
            this.database.commit();
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while saving an ExclusiveChunk.", e);
        }
    }

    @Override
    public Optional<PartitionedChunk> loadPartitionedChunk(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException {
        try {
            this.database.transaction();
            String sWorldUuid = worldUuid.toString();
            String sType = this.database.selectChunkType(sWorldUuid, chunkX, chunkZ);
            ChunkType type = ChunkType.valueOf(sType);
            if (type != ChunkType.PARTITIONED) {
                return Optional.empty();
            }
            String[] sColumns = this.database.selectPartitionedChunk(sWorldUuid, chunkX, chunkZ);
            Key[] columns = new Key[16 * 16];
            int m = 0;
            while (m < 16 * 16) {
                columns[m] = new Key(sColumns[m]);
                m++;
            }
            this.database.commit();
            return Optional.of(new PartitionedChunk(worldUuid, chunkX, chunkZ, columns));
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while loading a PartitionedChunk.", e);
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid value loaded from database.", e);
        }
    }

    @Override
    public void savePartitionedChunk(PartitionedChunk chunk) throws RepositoryException {
        try {
            this.database.transaction();
            String worldUuidString = chunk.getWorldUuid().toString();
            int chunkX = chunk.getChunkX();
            int chunkZ = chunk.getChunkX();
            Key[] claims = chunk.getClaims();
            this.database.deleteChunk(worldUuidString, chunkX, chunkZ);
            this.database.upsertChunkType(worldUuidString, chunkX, chunkZ, ChunkType.PARTITIONED.name());
            int m = 0;
            while (m < 16 * 16) {
                Key domainKey = claims[m];
                if (domainKey != null) {
                    String domainKeyString = domainKey.toString();
                    this.database.upsertPartitionedChunkColumn(worldUuidString, chunkX, chunkZ, m, domainKeyString);
                } else {
                    continue;
                }
                m++;
            }
            this.database.commit();
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while saving a PartitionedChunk.", e);
        }
    }

    @Override
    public void deleteChunk(UUID worldUuid, int chunkX, int chunkZ) throws RepositoryException {
        try {
            this.database.transaction();
            this.database.deleteChunk(worldUuid.toString(), chunkX, chunkZ);
            this.database.commit();
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while deleting a chunk.", e);
        }
    }

    @Override
    public Collection<Title> loadPlayerTitles(UUID playerUuid) throws RepositoryException {
        try {
            this.database.transaction();
            Collection<TitleData> titlesData = this.database.selectPlayerTitles(playerUuid.toString());
            this.database.commit();
            Collection<Title> titles = new ArrayList<>();
            for (TitleData titleData : titlesData) {
                Key domainKey = new Key(titleData.domainKey);
                String title = titleData.title;
                Role role = Role.valueOf(titleData.role);
                titles.add(new Title(domainKey, playerUuid, title, role));
            }
            return titles;
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while loading player titles.", e);
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid value loaded from database.", e);
        }
    }

    @Override
    public Collection<Title> loadDomainTitles(Key domainKey) throws RepositoryException {
        try {
            this.database.transaction();
            Collection<TitleData> titlesData = this.database.selectDomainTitles(domainKey.toString());
            this.database.commit();
            Collection<Title> titles = new ArrayList<>();
            for (TitleData titleData : titlesData) {
                UUID playerUuid = UUID.fromString(titleData.playerUuid);
                String title = titleData.title;
                Role role = Role.valueOf(titleData.role);
                titles.add(new Title(domainKey, playerUuid, title, role));
            }
            return titles;
        } catch (SQLException e) {
            throw this.wrapSqlException("Persistence error while loading domain titles.", e);
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid value loaded from database.", e);
        }
    }

    @Override
    public void close() throws RepositoryException {
        try {
            this.database.close();
        } catch (Exception e) {
            throw new RepositoryException("Error closing database connection.", e);
        }
    }

}
