package com.github.ribesg.tea.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;


public class EndChunks {
    private final HashMap<String, ExtendedChunk> chunks;
    private final World                          endWorld;

    public EndChunks(final World endWorld) {
        this.chunks = new HashMap<String, ExtendedChunk>();
        this.endWorld = endWorld;
    }

    public void addChunk(final ExtendedChunk chunk) {
        final String coords = new StringBuffer().append(chunk.getX()).append(';').append(chunk.getZ()).append(';').append(chunk.isProtected()).toString();
        this.chunks.put(coords, chunk);
    }

    public ExtendedChunk addChunk(final Chunk c) {
        final String coords = new StringBuffer().append(c.getX()).append(';').append(c.getZ()).append(';').append("false").toString();
        final ExtendedChunk chunk = new ExtendedChunk(c);
        this.chunks.put(coords, chunk);
        return chunk;
    }

    public ExtendedChunk getChunk(final int x, final int z) {
        final String coords = new StringBuffer().append(x).append(';').append(z).toString();
        final ExtendedChunk c = this.chunks.get(coords + ";false");
        if (c == null) {
            return this.chunks.get(coords + ";true");
        } else {
            return c;
        }
    }

    public ExtendedChunk getChunk(final Chunk c) {
        return this.getChunk(c.getX(), c.getZ());
    }

    public void regen() {
        for (final ExtendedChunk c : this.chunks.values()) {
            c.setToBeRegen(true);
        }
    }

    public Collection<ExtendedChunk> getIterableChunks() {
        return this.chunks.values();
    }

    public void save(final File f_endChunks) {
        final List<String> coords = new ArrayList<String>(this.chunks.keySet());
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
                            final int x = Integer.parseInt(split[0]);
                            final int z = Integer.parseInt(split[1]);
                            final ExtendedChunk c = new ExtendedChunk(x, z, this.endWorld.getName());
                            if (split.length == 3) {
                                final boolean isProtected = Boolean.parseBoolean(split[2]);
                                c.setProtected(isProtected);
                                this.chunks.put(coord, c);
                            } else {
                                this.chunks.put(coord + ";false", c);
                            }
                        } catch (final Exception e) {
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
