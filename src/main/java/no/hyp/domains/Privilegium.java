package no.hyp.domains;

public enum Privilegium {

    /**
     * Can place and destroy blocks.
     */
    BUILD,

    /**
     * Can use doors and buttons.
     */
    INTERACT,

    /**
     * Can add and remove items from inventories.
     * Can take and place books on lecterns.
     */
    INVENTORY,

    /**
     * Can damage neutral entities.
     */
    HERD,

    /**
     * Can damage aggressive entities.
     */
    GUARD,

    /**
     * Can grant and revoke titles in a domain.
     */
    ADMINISTRATE,

    /**
     * Can grant and revoke titles in a domain, except for the administrator title.
     */
    WEAK_ADMINISTRATE,

}
