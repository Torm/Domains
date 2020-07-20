package no.hyp.domains.persistence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class SqliteDatabase implements SqlDatabase {

    private final Connection connection;

    public SqliteDatabase(String databasePath) throws SQLException {
        String connectionString = "jdbc:sqlite:" + databasePath;
        this.connection = DriverManager.getConnection(connectionString);
    }

    @Override
    public int version() throws SQLException {
        String sql = "PRAGMA user_version; ";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    @Override
    public void transaction() throws SQLException {
        this.connection.setAutoCommit(false);
    }

    @Override
    public void commit() throws SQLException {
        this.connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        this.connection.rollback();
    }

    @Override
    public void upgradeDatabase() throws SQLException {
        // If version is 0, the database was just created.
        if (this.version() == 0) {
            this.setupDatabase();
        }
        // Database version should now be 1, otherwise something is wrong.
        this.version();
    }

    private void setupDatabase() throws SQLException {
        String sql = "CREATE TABLE Domain ( "
                   + "domain_key TEXT NOT NULL, "
                   + "display_name TEXT, "
                   + "default_role TEXT NOT NULL, "
                   + "PRIMARY KEY ( domain_key ), "
                   + "); "
                   + " "
                   + "CREATE TABLE Title ( "
                   + "domain_key TEXT NOT NULL, "
                   + "player_uuid TEXT NOT NULL, "
                   + "role TEXT NOT NULL, "
                   + "title TEXT NOT NULL, "
                   + "PRIMARY KEY ( domain_key, player_uuid ), "
                   + "FOREIGN KEY ( domain_key ) REFERENCES Domain ( domain_key ) ON DELETE CASCADE ON UPDATE CASCADE "
                   + "); "
                   + " "
                   + "CREATE TABLE ClaimableType ( "
                   + "world_uuid TEXT NOT NULL, "
                   + "chunk_x INTEGER NOT NULL, "
                   + "chunk_z INTEGER NOT NULL, "
                   + "type TEXT CHECK(type IN ( \"COLUMNS\", \"CHUNK\" )) NOT NULL, "
                   + "PRIMARY KEY ( world_uuid, chunk_x, chunk_z ), "
                   + "FOREIGN KEY ( domain_key ) REFERENCES Domain ( domain_key ) ON DELETE CASCADE ON UPDATE CASCADE "
                   + "); "
                   + " "
                   + "CREATE TABLE ClaimableChunk ( "
                   + "world_uuid TEXT NOT NULL, "
                   + "chunk_x INTEGER NOT NULL, "
                   + "chunk_z INTEGER NOT NULL, "
                   + "domain_key TEXT NOT NULL, "
                   + "PRIMARY KEY ( world_uuid, chunk_x, chunk_z ), "
                   + "FOREIGN KEY ( domain_key ) REFERENCES Domain ( domain_key ) ON DELETE CASCADE ON UPDATE CASCADE "
                   + "); "
                   + " "
                   + "CREATE TABLE ClaimableColumn ( "
                   + "world_uuid TEXT NOT NULL, "
                   + "chunk_x INTEGER NOT NULL, "
                   + "chunk_z INTEGER NOT NULL, "
                   + "column_number INTEGER CHECK(column_number BETWEEN 0 AND 255) NOT NULL, "
                   + " domain_key TEXT NOT NULL, "
                   + "PRIMARY KEY ( world_uuid, chunk_x, chunk_z, column_number ), "
                   + "FOREIGN KEY ( domain_key ) REFERENCES Domain ( domain_key ) ON DELETE CASCADE ON UPDATE CASCADE "
                   + "); "
                   + " "
                   + "PRAGMA user_version = 1; "
        ;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    @Override
    public @Nullable DomainData selectDomain(@Nonnull String domainKey) throws SQLException {
        String sql = "SELECT ( default_role, display_name ) "
                   + "FROM Domain "
                   + "WHERE domain_key = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, domainKey);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String defaultRole = result.getString("default_role");
                    String displayName = result.getString("display_name");
                    return new DomainData(domainKey, defaultRole, displayName);
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public void upsertDomain(@Nonnull String domainKey, @Nonnull String defaultRole, @Nullable String displayName) throws SQLException {
        String sql = "INSERT INTO Domain ( domain_key, default_role, display_name ) "
                   + "VALUES ( ?, ?, ? ) "
                   + "ON CONFLICT ( domain_key ) "
                   + "DO UPDATE SET "
                   + "default_role = excluded.default_role, "
                   + "display_name = excluded.display_name; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, domainKey);
            statement.setString(2, defaultRole);
            statement.setString(3, displayName);
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteDomain(@Nonnull String domainKey) throws SQLException {
        String sql = "DELETE FROM Domain "
                   + "WHERE domain_key = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, domainKey);
            statement.executeUpdate();
        }
    }

    public @Nullable TitleData selectTitle(@Nonnull String domainKey, @Nonnull String playerUuid) throws SQLException {
        String sql = "SELECT role, title "
                   + "FROM Title "
                   + "WHERE domain_key = ? AND player_uuid = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, domainKey);
            statement.setString(2, playerUuid);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String role = result.getString("role");
                    String title = result.getString("title");
                    return new TitleData(domainKey, playerUuid, role, title);
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public void upsertTitle(@Nonnull String domainKey, @Nonnull String playerUuid, @Nonnull String role, @Nonnull String title) throws SQLException {
        String sql = "INSERT INTO Title ( domain_key, player_uuid, role, title ) "
                   + "VALUES ( ?, ?, ?, ? ) "
                   + "ON CONFLICT ( domain_key, player_uuid ) "
                   + "DO UPDATE SET "
                   + "role = excluded.role, "
                   + "title = excluded.title; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, domainKey);
            statement.setString(2, playerUuid);
            statement.setString(3, role);
            statement.setString(4, title);
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteTitle(@Nonnull String domainKey, @Nonnull String playerUuid) throws SQLException {
        String sql = "DELETE FROM Title "
                   + "WHERE domain_key = ? AND player_uuid = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, domainKey);
            statement.setString(2, playerUuid);
            statement.executeUpdate();
        }
    }

    /**
     * Load all the titles granted in a domain.
     */
    @Override
    public @Nonnull Collection<TitleData> selectDomainTitles(@Nonnull String domainKey) throws SQLException {
        Collection<TitleData> titles = new ArrayList<>();
        {
            String sql = "SELECT player_uuid, role, title "
                       + "FROM Title "
                       + "WHERE domain_key = ?; "
            ;
            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
                statement.setString(1, domainKey);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        String playerUuid = result.getString("player_uuid");
                        String role = result.getString("role");
                        String title = result.getString("title");
                        titles.add(new TitleData(domainKey, playerUuid, role, title));
                    }
                }
            }
        }
        return titles;
    }

    @Override
    public @Nonnull Collection<TitleData> selectPlayerTitles(@Nonnull String playerUuid) throws SQLException {
        Collection<TitleData> titles = new ArrayList<>();
        String sql = "SELECT domain_key, role, title "
                   + "FROM Title "
                   + "WHERE player_uuid = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String sDomainKey = result.getString("domain_key");
                    String sRole = result.getString("role");
                    String title = result.getString("title");
                    titles.add(new TitleData(sDomainKey, playerUuid, sRole, title));
                }
            }
        }
        return titles;
    }

    @Override
    public @Nullable String selectChunkType(@Nonnull String worldUuid, int chunkX, int chunkZ) throws SQLException {
        String sql = "SELECT domain_key "
                   + "FROM ClaimableType "
                   + "WHERE world_uuid = ? AND chunk_x = ? AND chunk_z = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("domain_key");
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public void upsertChunkType(@Nonnull String worldUuid, int chunkX, int chunkZ, @Nonnull String type) throws SQLException {
        String sql = "INSERT INTO ClaimableType ( world_uuid, chunk_x, chunk_z, type ) "
                   + "VALUES ( ?, ?, ?, ? ) "
                   + "ON CONFLICT ( world_uuid, chunk_x, chunk_z ) "
                   + "DO UPDATE SET "
                   + "type = excluded.type; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.setString(4, type);
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteChunkType(@Nonnull String worldUuid, int chunkX, int chunkZ) throws SQLException {
        String sql = "DELETE FROM ClaimableType "
                   + "WHERE world_uuid = ? AND chunk_x = ? AND chunk_z = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.executeUpdate();
        }
    }

    @Override
    public @Nullable String selectExclusiveChunk(@Nonnull String worldUuid, int chunkX, int chunkZ) throws SQLException {
        String sql = "SELECT domain_key "
                   + "FROM ExclusiveChunk "
                   + "WHERE world_uuid = ? AND chunk_x = ? AND chunk_z = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("domain_key");
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public void upsertExclusiveChunk(@Nonnull String worldUuid, int chunkX, int chunkZ, @Nonnull String domainKey) throws SQLException {
        String sql = "INSERT INTO ExclusiveChunk ( world_uuid, chunk_x, chunk_z, domain_key ) "
                   + "VALUES ( ?, ?, ?, ? ) "
                   + "ON CONFLICT ( world_uuid, chunk_x, chunk_z ) "
                   + "DO UPDATE SET "
                   + "domain_key = excluded.domain_key; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.setString(4, domainKey);
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteExclusiveChunk(@Nonnull String worldUuid, int chunkX, int chunkZ) throws SQLException {
        String sql = "DELETE FROM ExclusiveChunk "
                   + "WHERE world_uuid = ? AND chunk_x = ? AND chunk_z = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.executeUpdate();
        }
    }

    @Override
    public @Nonnull String[] selectPartitionedChunk(String worldUuid, int chunkX, int chunkZ) throws SQLException {
        String[] columns = new String[16 * 16];
        String sql = "SELECT column_number, domain_key "
                   + "FROM PartitionedChunkColumn "
                   + "WHERE world_uuid = ? AND chunk_x = ? AND chunk_z = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int columnNumber = resultSet.getInt("column_number");
                    String sDomainKey = resultSet.getString("domain_key");
                    columns[columnNumber] = sDomainKey;
                }
            }
        }
        return columns;
    }

    @Override
    public @Nullable String selectPartitionedChunkColumn(@Nonnull String worldUuid, int chunkX, int chunkZ, int columnNumber) throws SQLException {
        String sql = "SELECT domain_key "
                   + "FROM PartitionedChunkColumn "
                   + "WHERE world_uuid = ? AND chunk_x = ? AND chunk_z = ? AND column_number = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.setInt(4, columnNumber);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("domain_key");
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public void upsertPartitionedChunkColumn(@Nonnull String worldUuid, int chunkX, int chunkZ, int columnNumber, @Nonnull String domainKey) throws SQLException {
        String sql = "INSERT INTO PartitionedChunkColumn ( world_uuid, chunk_x, chunk_z, column_number, domain_key ) "
                   + "VALUES ( ?, ?, ?, ?, ? ) "
                   + "ON CONFLICT ( world_uuid, chunk_x, chunk_z ) "
                   + "DO UPDATE SET "
                   + "column_number = excluded.column_number, "
                   + "domain_key = excluded.domain_key; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.setInt(4, columnNumber);
            statement.setString(5, domainKey);
            statement.executeUpdate();
        }
    }

    @Override
    public void deletePartitionedChunkColumn(@Nonnull String worldUuid, int chunkX, int chunkZ, int columnNumber) throws SQLException {
        String sql = "DELETE FROM PartitionedChunkColumn "
                   + "WHERE world_uuid = ? AND chunk_x = ? AND chunk_z = ? AND column_number = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.setInt(4, columnNumber);
            statement.executeUpdate();
        }
    }

    @Override
    public void deletePartitionedChunk(@Nonnull String worldUuid, int chunkX, int chunkZ) throws SQLException {
        String sql = "DELETE FROM PartitionedChunkColumn "
                   + "WHERE worldUuid = ? AND chunk_x = ? AND chunk_z = ?; "
        ;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, worldUuid);
            statement.setInt(2, chunkX);
            statement.setInt(3, chunkZ);
            statement.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

}
