package no.hyp.domains;

import org.bukkit.World;
import org.bukkit.util.Vector;

public class InstancedBorder {

    private final World world;

    private final Vector origin;

    private final Direction orientation;

    private final float length;

    public InstancedBorder(World world, Vector origin, Direction orientation, float length) {
        this.world = world;
        this.origin = origin;
        this.orientation = orientation;
        this.length = length;
    }

    public World getWorld() {
        return this.world;
    }

    public Vector getOrigin() {
        return origin;
    }

    public Direction getOrientation() {
        return orientation;
    }

    public float getLength() {
        return length;
    }

}
