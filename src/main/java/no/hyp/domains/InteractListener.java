package no.hyp.domains;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    private final DomainsPlugin plugin;

    public InteractListener(DomainsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.INTERACT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Location location = event.getRightClicked().getLocation();
        Domain domain = plugin.getDomain(location.getBlockX(), location.getBlockZ());
        if (!domain.playerHasPrivilege(player, Privilegium.INTERACT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBed();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.INTERACT)) {
            event.setCancelled(true);
        }
    }

    /**
     * Control damage to entities.
     *
     * @param event
     */
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        if (event.getEntityType() == EntityType.) {

        }
        if (damaged instanceof ) {

        }
        Entity damager = event.getDamager();
        if (damager instanceof Player) {

        }
    }

}
