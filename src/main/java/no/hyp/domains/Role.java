package no.hyp.domains;

public enum Role {

    /**
     *
     * An administrator can create and delete subdomains.
     *
     * If the domain is a root domain, an administrator can delete it.
     */
    ADMINISTRATOR(Privilegium.ADMINISTRATE, Privilegium.BUILD, Privilegium.INVENTORY,
            Privilegium.INTERACT, Privilegium.HERD, Privilegium.GUARD),

    /**
     * A vice administrator can grant and revoke lesser titles, but can not
     * modify administrators and vice administrators.
     */
    VICE_ADMINISTRATOR(Privilegium.WEAK_ADMINISTRATE, Privilegium.BUILD, Privilegium.INVENTORY,
            Privilegium.INTERACT, Privilegium.HERD, Privilegium.GUARD),

    /**
     *
     */
    RESIDENT(Privilegium.BUILD, Privilegium.INVENTORY, Privilegium.INTERACT,
            Privilegium.HERD, Privilegium.GUARD),

    /**
     * A builder can place and destroy blocks
     * Privileges: Build, Inventory, Interact, Herd, Guard
     */
    BUILDER(Privilegium.BUILD, Privilegium.INVENTORY, Privilegium.INTERACT,
            Privilegium.HERD, Privilegium.GUARD),



    /**
     * A clerk can modify the inventories in a domain.
     * Privileges: Inventory, Interact, Guard
     *
     */
    CLERK(Privilegium.INVENTORY, Privilegium.INTERACT),

    /**
     * A herder can kill farm animals.
     * Privileges: Interact, Herd, Guard
     */
    HERDER(Privilegium.INTERACT, Privilegium.HERD),

    /**
     * A guard can kill aggressive mobs.
     * Privileges: Interact, Guard
     */
    GUARD(Privilegium.INTERACT, Privilegium.GUARD),

    /**
     * A visitor can interact with doors, buttons and beds etc.
     * Privileges: Interact
     */
    VISITOR(Privilegium.INTERACT);

    private final Privilegium[] privileges;

    Role(Privilegium... privileges) {
        this.privileges = privileges;
    }

    public Privilegium[] getPrivileges() {
        return privileges;
    }



}
