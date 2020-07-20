package no.hyp.domains;

import no.hyp.domains.persistence.Repository;
import no.hyp.domains.persistence.RepositoryException;
import no.hyp.domains.persistence.SqlRepository;
import no.hyp.domains.persistence.SqliteDatabase;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public final class DomainsPlugin extends JavaPlugin implements Listener {

    private Repository database;

    private BorderDrawer borderDrawer;

    @Override
    public void onEnable() {
        // Set up database.
        try {
            String databasePath = this.getDataFolder().toPath().resolve("domains.db").toString();
            this.database = new SqlRepository(new SqliteDatabase(databasePath));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Register commands.
        this.getCommand("holding").setExecutor(this);
        this.getCommand("holding").setTabCompleter(this);
        // Register listeners.
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new BuildListener(this), this);
        pluginManager.registerEvents(new InventoryListener(this), this);
        pluginManager.registerEvents(new InteractListener(this), this);
        // Set up a border drawer and a repeating task drawing domain borders.
        this.borderDrawer = new DetailedBorderDrawer(this, 24);
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            this.borderDrawer.cacheChunks(this.getServer().getOnlinePlayers());
            this.borderDrawer.drawBorder();
        }, 2, 2);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public Repository getDatabase() {
        return this.database;
    }

    public BorderDrawer getBorderDrawer() {
        return this.borderDrawer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!command.getName().equalsIgnoreCase("domain")) {
            return false;
        }
        if (arguments.length < 1) {
            return true;
        }
        String subCommand = arguments[0];
        // Create a new domain and become an administrator.
        if (subCommand.equalsIgnoreCase("create")) {

        }
        // Delete a domain.
        else if (subCommand.equalsIgnoreCase("delete")) {

        } else if (subCommand.equalsIgnoreCase("view")) {

        } else if (subCommand.equalsIgnoreCase("list")) {


        }
        // d rename <domain> to <target_domain>
        // Rename a domain.
        // Requires administration privilege in their common domain.
        else if (subCommand.equalsIgnoreCase("rename")) {


        }
        // d integrate <domain> into <target_domain>
        // Delete a domain and integrate its territory into another domain.
        else if (subCommand.equalsIgnoreCase("integrate")) {

        }
        // d annex chunk|column into <domain>
        // Annex unclaimed territory.
        else if (subCommand.equalsIgnoreCase("annex") || subCommand.equalsIgnoreCase("claim")) {
            this.onCommandAnnex(sender, command, label, arguments);
            return true;
        }
        // Abandon territory.
        else if (subCommand.equalsIgnoreCase("disclaim" )) {

        }
        // Transfer territory from a domain to another domain.
        // Requires administration privileges in their common domain.
        else if (subCommand.equalsIgnoreCase("transfer")) {

        } else if (subCommand.equalsIgnoreCase("title")) {
            if (arguments.length < 2) {
                return true;
            }
            String titleCommand = arguments[1];
            // Grant a player a domain title.
            if (titleCommand.equalsIgnoreCase("grant")) {

            }
            // Revoke the domain title from a player.
            else if (titleCommand.equalsIgnoreCase("revoke")) {

            }
            // List all titles granted to players in a domain.
            else if (titleCommand.equalsIgnoreCase("list")) {

            }
            // Renounce your domain title.
            else if (titleCommand.equalsIgnoreCase("renounce")) {

            }
        } else if (subCommand.equalsIgnoreCase("transfer")) {

        }
        // Send help if no subcommand match.
        else {

        }
    }

    public void onCommandAnnex(CommandSender sender, Command command, String label, String[] arguments) {
        try {
            if (arguments.length != 4) {
                sender.sendMessage(String.format("/d annex chunk|column to <domain>"));
                return;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(String.format("You must be a player to execute this command."));
                return;
            }
            Player player = (Player) sender;
            // The third argument must be "to" for readability.
            String into = arguments[2];
            if (!into.equalsIgnoreCase("into")) {
                sender.sendMessage(String.format("Third argument must be \"into\": %s.", into));
                return;
            }
            // Attempt to load the domain specified in the fourth argument.
            String sDomainKey = arguments[3];
            Domain domain = this.parseDomain(player, sDomainKey).orElse(null);
            if (domain == null) return;
            // The player must be an administrator in this domain to annex to it.
            if (!domain.playerHasPrivilege(player, Privilegium.ADMINISTRATE)) {
                sender.sendMessage(String.format("You must have administrative privileges to annex land to a domain."));
                return;
            }

            // The second argument is either "chunk" or "column".
            String annexType = arguments[1];
            UUID worldUuid = player.getWorld().getUID();
            Location location = player.getLocation();
            int chunkX = location.getChunk().getX();
            int chunkZ = location.getChunk().getZ();
            int i = Math.floorMod(location.getBlockX(), 16);
            int k = Math.floorMod(location.getBlockZ(), 16);
            ChunkType chunkType;
            try {
                chunkType = this.database.loadChunkType(worldUuid, chunkX, chunkZ);
            } catch (RepositoryException e) {
                e.printStackTrace();
                return;
            }
            // Annex a chunk. The chunk must be unclaimed.
            if (annexType.equalsIgnoreCase("chunk")) {

                if (chunkType == ChunkType.UNCLAIMED) {
                    ExclusiveChunk chunk = new ExclusiveChunk(worldUuid, chunkX, chunkZ, domain.getKey());
                    this.database.saveExclusiveChunk(chunk);
                    sender.sendMessage(String.format("Chunk was annexed."));
                } else if (chunkType == ChunkType.EXCLUSIVE) {
                    ExclusiveChunk chunk = this.database.loadExclusiveChunk(worldUuid, chunkX, chunkZ).get();
                    if (chunk.getDomainKey().equals(domain.getKey())) {
                        sender.sendMessage(String.format("This chunk is already incorporated."));
                    } else {
                        sender.sendMessage(String.format("This chunk is already incorporated into another domain."));
                    }
                } else if (chunkType == ChunkType.PARTITIONED) {
                    sender.sendMessage(String.format("This chunk cannot be claimed because there are column claims in it."));
                }
            }
            // Annex a column. The column must be unclaimed.
            else if (annexType.equalsIgnoreCase("column")) {
                // If there are no claims in this chunk, create a new PartitionedChunk and claim the column.
                if (chunkType == ChunkType.UNCLAIMED) {
                    PartitionedChunk chunk = new PartitionedChunk(worldUuid, chunkX, chunkZ);
                    chunk.setDomainKey(i, k, domain.getKey());
                    this.database.savePartitionedChunk(chunk);
                    sender.sendMessage(String.format("Column was annexed."));
                }
                // The column cannot be annexed if the whole chunk is already claimed.
                else if (chunkType == ChunkType.EXCLUSIVE) {
                    sender.sendMessage(String.format("This chunk is already claimed."));
                }
                // If the chunk is partitioned, check if the column is unclaimed. If it is,
                // annex it, otherwise, it cannot be annexed.
                else if (chunkType == ChunkType.PARTITIONED) {
                    PartitionedChunk chunk = this.database.loadPartitionedChunk(worldUuid, chunkX, chunkZ).get();
                    Key existingDomainKey = chunk.getDomainKey(i, k).orElse(null);
                    if (existingDomainKey == null) {
                        chunk.setDomainKey(i, k, domain.getKey());
                        this.database.savePartitionedChunk(chunk);
                        sender.sendMessage(String.format("Column was annexed."));
                    } else {
                        sender.sendMessage(String.format("That column is already occupied."));
                    }
                }
            }
            // Send an error if invalid annexation type.
            else {
                sender.sendMessage(String.format("You must annex either a chunk or a column."));
            }
        } catch (RepositoryException e) {

        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] arguments) {
        return null;
    }

    public Optional<Domain> parseDomain(CommandSender sender, String sDomainKey) {
        Key domainKey;
        try {
            domainKey = new Key(sDomainKey);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(String.format("Illegal domain key: %s.", sDomainKey));
            return Optional.empty();
        }
        Optional<Domain> mDomain;
        try {
            mDomain = this.database.loadDomain(domainKey);
        } catch (RepositoryException e) {
            this.getLogger().severe(e.getMessage());
            e.printStackTrace();
            sender.sendMessage(String.format("Database error occurred."));
            return Optional.empty();
        }
        if (!mDomain.isPresent()) {
            sender.sendMessage(String.format("No such domain."));
            return Optional.empty();
        }
        return mDomain;
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage("Insufficient permissions.");
    }

    private void sendInvalidArguments(CommandSender sender, String label) {
        sender.sendMessage(String.format("Invalid arguments. See \"%s ?\" for help.", label));
    }

    private void sendHelp(CommandSender sender) {

    }

}
