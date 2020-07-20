package no.hyp.domains;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * The BorderDrawer uses chunk and claim data to calculate, cache and draw borders.
 */
public class DetailedBorderDrawer implements BorderDrawer {

    private final DomainsPlugin plugin;

    /**
     * The number of vertices on the border per block.
     */
    private final int vertices;

    /**
     * The calculated distance between each vertex.
     */
    private final float stride;

    /**
     * The calculated vertex offset from the edge of a block.
     */
    private final float offset;

    /**
     * The number of vertices the colours on the border move per draw.
     */
    private final int speed;

    /**
     * Only one class is drawn per drawing.
     */
    private final int classes;

    private int classCurrent;

    /**
     * The colours used to draw the border.
     */
    private final List<Particle.DustOptions> colours;

    /**
     * vertices is the number of possible positions on the border. Given a position,
     * divide it by the colourClassDivisor to get the index of the colour to use.
     * The colourClassDivisor is calculated as vertices divided by the number of colours.
     */
    private final int colourClassDivisor;

    /**
     * A cache of calculated border vertices per chunk.
     */
    private final Map<ExclusiveChunk, List<Vector>> cache;

    /**
     * The radius around a chunk to cache.
     */
    private final int radius;

    /**
     * The current position.
     */
    private int colourPosition;

    private final int colourStride;

    public DetailedBorderDrawer(DomainsPlugin plugin, int vertices) {
        this.plugin = plugin;
        this.vertices = vertices;
        this.stride = 1.0f / vertices;
        this.offset = stride / 2.0f;
        colours = new ArrayList<>();
        colours.add(new Particle.DustOptions(Color.RED, 0.75f));
        colours.add(new Particle.DustOptions(Color.YELLOW, 0.75f));
        this.colourClassDivisor = vertices;
        this.colourPosition = 0;
        this.colourStride = vertices * colours.size();
        this.cache = new HashMap<>();
        this.radius = 1;
        this.speed = 1;
        this.classes = colourClassDivisor;
        this.classCurrent = 0;
    }

    @Override
    public void drawBorder() {
        for (Map.Entry<ExclusiveChunk, List<Vector>> cachedBorder : cache.entrySet()) {
            World world = cachedBorder.getKey().getWorld();
            List<Vector> vertices = cachedBorder.getValue();
            // Iterate over all vertices in a class.
            int maxN = vertices.size();
            int n = this.classCurrent;
            while (n < maxN) {
                Vector vertex = vertices.get(n);
                int colourOffset = Math.floorMod(n - colourPosition, vertices.size());
                int colourClass = (colourOffset / colourClassDivisor) % colours.size();
                Particle.DustOptions colour = this.colours.get(colourClass);
                world.spawnParticle(Particle.REDSTONE, vertex.getX(), vertex.getY(), vertex.getZ(), 1, 0, 0, 0, 0, colour);
                n += classes;
            }
        }
        // Update position.
        this.colourPosition = (this.colourPosition + this.speed) % colourStride;
        this.classCurrent = (this.classCurrent + this.speed) % classes;
    }

    @Override
    public void cacheChunks(Collection<Chunk> origins) {

    }

    @Override
    public void cacheNearby(Collection<Player> onlinePlayers) {

    }

    public void cacheChunks(Player[] players) {
        List<ExclusiveChunk> chunks = new ArrayList<>();
        for (Player player : players) {
            chunks.add(player.getLocation().getChunk());
        }
        cacheChunks(chunks);
    }

    /**
     *
     *
     * @param origins
     */
    @Override
    public void cacheChunks(Collection<Chunk> origins) {
        // Find all the chunks that are within the given radius of the origins.
        Set<ExclusiveChunk> nearbyChunks = new HashSet<ExclusiveChunk>();
        for (Chunk chunk : origins) {
            int i = -radius;
            while (i <= radius) {
                int k = -radius;
                while (k <= radius) {
                    int x = chunk.getX() + i;
                    int z = chunk.getZ() + k;
                    Chunk c = chunk.getWorld().getChunkAt(x, z);
                    nearbyChunks.add(c);
                    k++;
                }
                i++;
            }
        }
        // For the chunks that are not cached, calculate their borders and add them
        // to the cache.
        for (ExclusiveChunk nearbyChunk : nearbyChunks) {
            if (!this.cache.containsKey(nearbyChunk)) {
                this.cache.put(nearbyChunk, calculateChunkBorder(nearbyChunk.getWorld(), nearbyChunk.getX(), nearbyChunk.getZ()));
                this.plugin.getServer().broadcastMessage(String.format("Caching; Count: %d", this.cache.size()));
            }
        }
        // Remove the cached chunks that are not in the updated cache list.
        Set<ExclusiveChunk> farChunks = new HashSet<>();
        for (ExclusiveChunk cachedChunk : this.cache.keySet()) {
            if (!nearbyChunks.contains(cachedChunk)) {

                farChunks.add(cachedChunk);
            }
        }
        for (ExclusiveChunk chunk : farChunks) {
            this.cache.remove(chunk);
            this.plugin.getServer().broadcastMessage(String.format("Uncaching; Count: %d", this.cache.size()));
        }
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
                    int y = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE) + 1;
                    Domain holding = plugin.getDomain(x, y);
                    boolean north = !holding.equals(plugin.getDomain(x, z - 1));
                    if (north) {
                        border.addAll(calculateColumnBorder(x, z, y, Cardinality.EAST, world.getHighestBlockYAt(x - 1, z, HeightMap.WORLD_SURFACE) + 1, world.getHighestBlockYAt(x + 1, z, HeightMap.WORLD_SURFACE) + 1, false));
                    }
                    boolean northEast = !holding.equals(plugin.getDomain(x + 1, z - 1));
                    boolean east = !holding.equals(plugin.getDomain(x + 1, z));
                    if (east) {
                        border.addAll(calculateColumnBorder(x, z, y, Cardinality.SOUTH, world.getHighestBlockYAt(x, z - 1, HeightMap.WORLD_SURFACE) + 1, world.getHighestBlockYAt(x, z + 1, HeightMap.WORLD_SURFACE) + 1, false));
                    }
                    boolean southEast = !holding.equals(plugin.getDomain(x + 1, z + 1));
                    boolean south = !holding.equals(plugin.getDomain(x, z + 1));
                    if (south) {
                        border.addAll(calculateColumnBorder(x, z, y, Cardinality.WEST, world.getHighestBlockYAt(x + 1, z, HeightMap.WORLD_SURFACE) + 1, world.getHighestBlockYAt(x - 1, z, HeightMap.WORLD_SURFACE) + 1, false));
                    }
                    boolean southWest = !holding.equals(plugin.getDomain(x - 1, z + 1));
                    boolean west = !holding.equals(plugin.getDomain(x - 1, z));
                    if (west) {
                        border.addAll(calculateColumnBorder(x, z, y, Cardinality.NORTH, world.getHighestBlockYAt(x, z + 1, HeightMap.WORLD_SURFACE) + 1, world.getHighestBlockYAt(x, z - 1, HeightMap.WORLD_SURFACE) + 1, false));
                    }
                    boolean northWest = !holding.equals(plugin.getDomain(x - 1, z - 1));
                    k++;
                }
                i++;
            }
        }
        return border;
    }

    /**
     * Calculate the border within a column.
     *
     * @param x
     * @param z
     * @param height
     * @param direction
     * @param previousHeight
     * @param nextHeight
     * @return
     */
    public List<Vector> calculateColumnBorder(int x, int z, int height, Cardinality direction, int previousHeight, int nextHeight, boolean corner) {
        List<Vector> border = new ArrayList<>();
        float into = offset;
        if (direction == Cardinality.NORTH) {
            float i = into;
            float k = 1 - into;
            border.addAll(calculateDescendingBorder(x + i, z + k, height, previousHeight));
            int kIndex = this.vertices;
            while (kIndex >= 0) {
                k = offset + stride * kIndex;
                border.add(new Vector(x + i, height, z + k));
                kIndex--;
            }
            border.addAll(calculateAscendingBorder(x + i, z + k, height, nextHeight));
        } else if (direction == Cardinality.SOUTH) {
            float i = 1 - into;
            float k = into;
            border.addAll(calculateDescendingBorder(x + i, z + k, height, previousHeight));
            int kIndex = 0;
            while (kIndex < this.vertices) {
                k = offset + stride * kIndex;
                border.add(new Vector(x + i, height, z + k));
                kIndex++;
            }
            border.addAll(calculateAscendingBorder(x + i, z + k, height, nextHeight));
        } else if (direction == Cardinality.EAST) {
            float i = into;
            float k = into;
            border.addAll(calculateDescendingBorder(x + i, z + k, height, previousHeight));
            int iIndex = 0;
            while (iIndex < this.vertices) {
                k = offset + stride * iIndex;
                border.add(new Vector(x + i, height, z + k));
                iIndex++;
            }
            border.addAll(calculateAscendingBorder(x + i, z + k, height, nextHeight));
        } else if (direction == Cardinality.WEST) {
            float i = 1 - into;
            float k = 1 - into;
            border.addAll(calculateDescendingBorder(x + i, z + k, height, previousHeight));
            int iIndex = this.vertices;
            while (iIndex >= 0) {
                k = offset + stride * iIndex;
                border.add(new Vector(x + i, height, z + k));
                iIndex--;
            }
            border.addAll(calculateAscendingBorder(x + i, z + k, height, nextHeight));
        }
        return border;
    }

    public List<Vector> calculateAscendingBorder(float x, float z, int y, int higherY) {
        List<Vector> verticalBorder = new ArrayList<>();
        int jMaxIndex = (higherY - y) * this.vertices;
        int jIndex = 0;
        while (jIndex < jMaxIndex) {
            float j = y + offset + jIndex * stride;
            verticalBorder.add(new Vector(x, j, z));
            jIndex++;
        }
        return verticalBorder;
    }

    public List<Vector> calculateDescendingBorder(float x, float z, int y, int lowerY) {
        List<Vector> verticalBorder = new ArrayList<>();
        int jIndex = (lowerY - y) * this.vertices;
        while (jIndex > 0) {
            float j = y + offset + jIndex * stride;
            verticalBorder.add(new Vector(x, j, z));
            jIndex--;
        }
        return verticalBorder;
    }

    public List<BlockVector> calculateBlockSequence(World world, int x, int z, Cardinality direction, int length) {
        List<BlockVector> columns = new ArrayList<>();
        if (direction == Cardinality.NORTH) {
            int zTarget = z - length - 1;
            int k = z + 1;
            while (k >= zTarget) {
                columns.add(new BlockVector(x, world.getHighestBlockYAt(x, k, HeightMap.WORLD_SURFACE), k));
                k--;
            }
        } else if (direction == Cardinality.SOUTH) {
            int zTarget = z + length + 1;
            int k = z - 1;
            while (k <= zTarget) {
                columns.add(new BlockVector(x, world.getHighestBlockYAt(x, k, HeightMap.WORLD_SURFACE), k));
                k++;
            }
        } else if (direction == Cardinality.EAST) {
            int xTarget = x + length + 1;
            int i = x - 1;
            while (i <= xTarget) {
                columns.add(new BlockVector(i, world.getHighestBlockYAt(i, z, HeightMap.WORLD_SURFACE), z));
                i++;
            }
        } else if (direction == Cardinality.WEST) {
            int xTarget = x - length - 1;
            int i = x + 1;
            while (i >= xTarget) {
                columns.add(new BlockVector(i, world.getHighestBlockYAt(i, z, HeightMap.WORLD_SURFACE), z));
                i--;
            }
        }
        return columns;
    }

}
