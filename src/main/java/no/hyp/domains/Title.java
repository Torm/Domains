package no.hyp.domains;

import java.util.Arrays;
import java.util.UUID;

public final class Title {

    private final Key domainKey;

    private final UUID playerUuid;

    /**
     * The name of the title.
     * - The title with an administrator role in a city might be "Mayor".
     * - The title with an administrator role of a house might be "Owner".
     *
     */
    private final String name;

    private final Role role;

    public Title(Key domainKey, UUID playerUuid, String name, Role role) {
        this.domainKey = domainKey;
        this.playerUuid = playerUuid;
        this.name = name;
        this.role = role;
    }

    public Key getDomainKey() {
        return this.domainKey;
    }

    public UUID getPlayerUuid() {
        return this.playerUuid;
    }

    public String getName() {
        return this.name;
    }

    public Role getRole() {
        return this.role;
    }

    public Privilegium[] getPrivileges() {
        return this.role.getPrivileges();
    }

    public boolean hasPrivilege(Privilegium privilege) {
        return Arrays.stream(this.getPrivileges()).anyMatch(x -> x == privilege);
    }

}
