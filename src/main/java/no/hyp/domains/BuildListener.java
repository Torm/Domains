package no.hyp.domains;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * Listens to events that require the Build privilege.
 */
public class BuildListener implements Listener {

    private final DomainsPlugin plugin;

    public BuildListener(DomainsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.BUILD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFertilise(BlockFertilizeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.BUILD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.BUILD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.BUILD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.BUILD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.BUILD)) {
            event.setCancelled(true);
        }
    }

}
