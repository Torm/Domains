package no.hyp.domains;

import org.bukkit.Location;
import org.bukkit.block.Lectern;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class InventoryListener implements Listener {

    private final DomainsPlugin plugin;

    public InventoryListener(DomainsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Control removal of a book from a lectern.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onTakeBook(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        Lectern block = event.getLectern();
        Domain domain = plugin.getDomain(block.getX(), block.getZ());
        if (!domain.playerHasPrivilege(player, Privilegium.INVENTORY)) {
            event.setCancelled(true);
        }
    }

    /**
     * Control the modification of inventories.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryInteract(InventoryInteractEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) {
            return;
        }
        Player player = (Player) human;
        Location location = event.getInventory().getLocation();
        if (location == null) {
            return;
        }
        Domain domain = plugin.getDomain(location.getBlockX(), location.getBlockZ());
        if (!domain.playerHasPrivilege(player, Privilegium.INVENTORY)) {
            event.setCancelled(true);
        }
    }

    /**
     *
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        Location location = event.getRightClicked().getLocation();
        Domain domain = plugin.getDomain(location.getBlockX(), location.getBlockZ());
        if (!domain.playerHasPrivilege(player, Privilegium.INVENTORY)) {
            event.setCancelled(true);
        }
    }

}
