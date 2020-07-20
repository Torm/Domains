package no.hyp.domains;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public class Domain {

    /**
     * Unique id of this domain.
     */
    private final Key key;

    /**
     * Default privileges given to anyone in this domain.
     */
    private final Role defaultRole;

    @Nullable
    private final String displayName;

    /**
     * The players who have a title in this domain.
     */
    private final Map<UUID, Title> titles;

    public Domain(Key key, Role defaultTitleLevel, @Nullable String displayName, Map<UUID, Title> titles) {
        this.key = key;
        this.displayName = displayName;
        this.defaultRole = defaultTitleLevel;
        this.titles = titles;
    }

    public Key getKey() {
        return this.key;
    }

    public Role getDefaultRole() {
        return this.defaultRole;
    }

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(this.displayName);
    }

    public Map<UUID, Title> getTitles() {
        return this.titles;
    }

    public Optional<Title> getTitle(Player player) {
        return getTitle(player.getUniqueId());
    }

    public Optional<Title> getTitle(UUID player) {
        return Optional.ofNullable(this.titles.get(player));
    }

    public boolean playerHasPrivilege(Player player, Privilegium privilege) {
        return this.playerHasPrivilege(player.getUniqueId(), privilege);
    }

    /**
     * Check if a player has a privilege in this domain. A player has
     * the privilege if they have a title granting the privilege in this
     * domain or in any superdomain.
     *
     * @param player
     * @param privilege
     * @return
     */
    public boolean playerHasPrivilege(UUID player, Privilegium privilege) {
        // Anyone has the privilege if it is default in the domain or a superdomain.
        if (Arrays.stream(getDefaultRole().getPrivileges()).anyMatch(x -> x == privilege)) {
            return true;
        }
        // Check if the player's title has this privilege.
        Optional<Title> title = this.getTitle(player);
        return title.map(value -> value.hasPrivilege(privilege)).orElse(false);
    }

}
