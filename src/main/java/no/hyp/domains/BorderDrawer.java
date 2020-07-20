package no.hyp.domains;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface BorderDrawer {

    public void drawBorder();

    public void cacheChunks(Collection<Chunk> origins);

    public void cacheNearby(Collection<Player> onlinePlayers);

}
