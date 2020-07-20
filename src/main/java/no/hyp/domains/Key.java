package no.hyp.domains;

import java.util.Optional;

public class Key {

    private final String string;

    public Key(String string) throws IllegalArgumentException {
        this.string = string;
    }

    /**
     * Get the supernode of this node. If this is a root node, Empty is returned.
     *
     * @return
     */
    public Optional<Key> superKey() {
        return this.string.s
    }

    public Key subKey(String string) {
        return this.string + ":" + string;
    }

    /**
     * The common node of two nodes, often known as lowest common ancestor.
     *
     * If a and b are keys in different trees, Empty is returned.
     *
     * - The common node of "no:ak:os" and "no:ak:ls" is "no:ak".
     * - The common node of "no:ak" and "ch" is Empty, since "no" and "ch" are different root nodes.
     *
     * @return The common node of a and b, or nothing if a and b are in different trees.
     */
    public static Optional<Key> common(Key a, Key b) {

    }

    @Override
    public String toString() {
        return this.string;
    }

}
