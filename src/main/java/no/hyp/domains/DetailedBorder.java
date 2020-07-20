package no.hyp.domains;

import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

public class DetailedBorder {

    private final List<Vector> vertices;

    private final List<Particle.DustOptions> colours;

    private int colourPosition;

    public DetailedBorder(List<Vector> vertices, List<Particle.DustOptions> colours) {
        this.vertices = vertices;
        this.colours = colours;
        this.colourPosition = 0;
    }

    public List<Vector> getVertices() {
        return vertices;
    }

    public List<Particle.DustOptions> getColours() {
        return colours;
    }

}
