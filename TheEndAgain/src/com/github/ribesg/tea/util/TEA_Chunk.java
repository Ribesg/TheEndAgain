package com.github.ribesg.tea.util;

import org.bukkit.Chunk;


public class TEA_Chunk {

    private final int    x, z;
    private final String world;
    private boolean      hasToBeRegen;

    public TEA_Chunk(final int x, final int z, final String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public TEA_Chunk(final Chunk bukkitChunk) {
        this.x = bukkitChunk.getX();
        this.z = bukkitChunk.getZ();
        this.world = bukkitChunk.getWorld().getName();
    }

    public boolean hasToBeRegen() {
        return this.hasToBeRegen;
    }

    public void setToBeRegen(final boolean value) {
        this.hasToBeRegen = value;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public String getWorld() {
        return this.world;
    }
}
