package com.github.ribesg.tea.util;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Chunk;


public class TEA_EndChunks {
    private final HashMap<String, TEA_Chunk> chunks;

    public TEA_EndChunks() {
        this.chunks = new HashMap<String, TEA_Chunk>();
    }

    public void addChunk(final TEA_Chunk chunk) {
        final String coords = new StringBuffer().append(chunk.getX()).append(';').append(chunk.getZ()).toString();
        this.chunks.put(coords, chunk);
    }

    public void addChunk(final Chunk c) {
        final String coords = new StringBuffer().append(c.getX()).append(';').append(c.getZ()).toString();
        this.chunks.put(coords, new TEA_Chunk(c));
    }

    public TEA_Chunk getChunk(final int x, final int z) {
        final String coords = new StringBuffer().append(x).append(';').append(z).toString();
        return this.chunks.get(coords);
    }

    public TEA_Chunk getChunk(final Chunk c) {
        return this.getChunk(c.getX(), c.getZ());
    }

    public void regen() {
        for (final TEA_Chunk c : this.chunks.values()) {
            c.setToBeRegen(true);
        }
    }

    public Collection<TEA_Chunk> getIterableChunks() {
        return this.chunks.values();
    }
}
