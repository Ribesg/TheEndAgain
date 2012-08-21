package com.github.ribesg.tea.util;

import org.bukkit.Chunk;


public class ExtendedChunk {

    private final int    x, z;
    private final String world;
    private boolean      hasToBeRegen;
    private boolean      isProtected;

    public ExtendedChunk(final int x, final int z, final String world) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.hasToBeRegen = false;
        this.isProtected = false;
    }

    public ExtendedChunk(final Chunk bukkitChunk) {
        this.x = bukkitChunk.getX();
        this.z = bukkitChunk.getZ();
        this.world = bukkitChunk.getWorld().getName();
        this.hasToBeRegen = false;
        this.isProtected = false;
    }

    public boolean hasToBeRegen() {
        return this.isProtected ? false : this.hasToBeRegen;
    }

    public void setToBeRegen(final boolean value) {
        this.hasToBeRegen = this.isProtected ? false : value;
    }

    public boolean isProtected() {
        return this.isProtected;
    }

    public void setProtected(final boolean value) {
        this.isProtected = value;
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
