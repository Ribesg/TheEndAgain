package com.github.ribesg.tea.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.ribesg.tea.TheEndAgain;


public class EndChunks {
    private final TheEndAgain                plugin;
    private final Map<String, ExtendedChunk> chunks;
    private final Set<World>                 endWorlds;

    public EndChunks(final TheEndAgain plugin, final World endWorld) {
        this.plugin = plugin;
        this.chunks = new HashMap<String, ExtendedChunk>();
        this.endWorlds = new HashSet<World>();
        this.endWorlds.add(endWorld);
    }

    public void addChunk(final ExtendedChunk chunk) {
        final String coords = new StringBuffer().append(chunk.getWorld()).append(';').append(chunk.getX()).append(';').append(chunk.getZ()).toString();
        this.chunks.put(coords, chunk);
    }

    public ExtendedChunk addChunk(final Chunk c) {
        final String coords = new StringBuffer().append(c.getWorld().getName()).append(';').append(c.getX()).append(';').append(c.getZ()).toString();
        final ExtendedChunk chunk = new ExtendedChunk(c);
        this.chunks.put(coords, chunk);
        return chunk;
    }

    public ExtendedChunk getChunk(final String worldName, final int x, final int z) {
        final String coords = new StringBuffer().append(worldName).append(';').append(x).append(';').append(z).toString();
        return this.chunks.get(coords);
    }

    public ExtendedChunk getChunk(final Chunk c) {
        return this.getChunk(c.getWorld().getName(), c.getX(), c.getZ());
    }

    public void regen(final String worldName) {
        for (final ExtendedChunk c : this.chunks.values()) {
            c.setToBeRegen(c.getWorld().equals(worldName));
        }
    }

    public Collection<ExtendedChunk> getIterableChunks() {
        return this.chunks.values();
    }

    public void save(final File f_endChunks) {
        final List<String> coords = new ArrayList<String>();
        for (final String coord : this.chunks.keySet()) {
            coords.add(coord + ';' + this.chunks.get(coord).isProtected());
        }
        Collections.sort(coords);
        final YamlConfiguration endChunks = new YamlConfiguration();
        endChunks.set("chunks", coords);
        try {
            if (!f_endChunks.exists()) {
                f_endChunks.createNewFile();
            }
            endChunks.save(f_endChunks);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void load(final File f_endChunks) {
        if (f_endChunks.exists()) {
            final YamlConfiguration endChunks = new YamlConfiguration();
            try {
                endChunks.load(f_endChunks);
                if (endChunks.isList("chunks")) {
                    for (final String coord : endChunks.getStringList("chunks")) {
                        try {
                            final String[] split = coord.split(";");
                            final String worldName = split[0];
                            final int x = Integer.parseInt(split[1]);
                            final int z = Integer.parseInt(split[2]);
                            final ExtendedChunk c = new ExtendedChunk(x, z, worldName);
                            final boolean isProtected = Boolean.parseBoolean(split[3]);
                            c.setProtected(isProtected);
                            this.chunks.put(worldName + ';' + x + ';' + z, c);
                        } catch (final Exception e) {
                            this.plugin.getLogger().severe("ERROR loading endChunks.yml, invalid chunk description found : " + coord);
                            e.printStackTrace();
                        }
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
