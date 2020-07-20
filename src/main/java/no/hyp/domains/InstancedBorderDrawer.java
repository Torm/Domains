package no.hyp.domains;

import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.*;

public class InstancedBorderDrawer implements BorderDrawer {

    private final DomainsPlugin plugin;

    private final Map<Chunk, List<InstancedBorder>> cache;

    public InstancedBorderDrawer(DomainsPlugin plugin) {
        this.plugin = plugin;
        this.cache = new HashMap<>();
    }

    @Override
    public void drawBorder() {
        for (List<InstancedBorder> borders : cache.values()) {
            for (InstancedBorder border : borders) {
                Vector origin = border.getOrigin();
                Direction orientation = border.getOrientation();
                float length = border.getLength();
                float xOffset = orientation == Direction.LONGITUDINAL ? length : 0.0f;
                float zOffset = orientation == Direction.LATITUDINAL ? length : 0.0f;
                float yOffset = orientation == Direction.ALTITUDINAL ? length : 0.0f;
                border.getWorld().spawnParticle(Particle.CLOUD, origin.getX(), origin.getY(), origin.getZ(), 100, xOffset, yOffset, zOffset, 0.0);
            }
        }
    }

    @Override
    public void cacheChunks(Collection<Chunk> origins) {

    }

    public List<Vector> calculateChunkBorder(World world, int chunkX, int chunkZ) {
        List<Vector> border = new ArrayList<>();
        {
            int i = 0;
            while (i < 16) {
                int k = 0;
                while (k < 16) {
                    int x = chunkX * 16 + i;
                    int z = chunkZ * 16 + k;
                    Domain holding = plugin.getDomain(x, z);
                    int y = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE) + 1;
                    int yNorth = world.getHighestBlockYAt(x - 1, z, HeightMap.WORLD_SURFACE) + 1;
                    int yNorthEast = world.getHighestBlockYAt(x - 1, z + 1, HeightMap.WORLD_SURFACE) + 1;
                    int yEast = world.getHighestBlockYAt(x, z + 1, HeightMap.WORLD_SURFACE) + 1;
                    int ySouthEast = world.getHighestBlockYAt(x + 1, z + 1, HeightMap.WORLD_SURFACE) + 1;
                    int ySouth = world.getHighestBlockYAt(x, z + 1, HeightMap.WORLD_SURFACE) + 1;
                    int ySouthWest = world.getHighestBlockYAt(x - 1, z + 1, HeightMap.WORLD_SURFACE) + 1;
                    int yWest = world.getHighestBlockYAt(x - 1, z, HeightMap.WORLD_SURFACE) + 1;
                    int yNorthWest = world.getHighestBlockYAt(x - 1, z - 1, HeightMap.WORLD_SURFACE) + 1;
                    boolean northBorder = !holding.equals(plugin.getDomain(x, z - 1));
                    if (northBorder) {
                        border.addAll(calculateColumnBorder(x, z, Math.max(y, yNorth), Cardinality.EAST, Math.max(yNorthWest, yWest), Math.max(yEast, yNorthEast)));
                    }
                    boolean eastBorder = !holding.equals(plugin.getDomain(x + 1, z));
                    if (eastBorder) {
                        border.addAll(calculateColumnBorder(x, z, Math.max(y, yEast), Cardinality.SOUTH, Math.max(yNorthEast, yNorth), Math.max(ySouth, ySouthEast)));
                    }
                    k++;
                }
                i++;
            }
        }
        return border;
    }

    private int max(int a, int b, int c) {
        return Math.max(a, Math.max(b, c));
    }

    public List<InstancedBorder> calculateColumnBorder(World world, int x, int z, int y, Cardinality direction, int yPrevious, int yNext) {
        List<InstancedBorder> border = new ArrayList<>();
        if (direction == Cardinality.NORTH) {

        } else if (direction == Cardinality.EAST) {
            border.add(new InstancedBorder(world, ))
        }
    }

}
