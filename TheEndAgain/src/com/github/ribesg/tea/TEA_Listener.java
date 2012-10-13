package com.github.ribesg.tea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.util.Vector;

import com.github.ribesg.tea.util.EndWorldConfig;
import com.github.ribesg.tea.util.ExtendedChunk;


public class TEA_Listener implements Listener {

    private final TheEndAgain       plugin;
    private final ArrayList<Vector> portalBlocks;

    public TEA_Listener(final TheEndAgain instance) {
        this.plugin = instance;
        this.portalBlocks = new ArrayList<Vector>();
        this.portalBlocks.add(new Vector(0, -1, 0));
        this.portalBlocks.add(new Vector(0, -2, 0));
        this.portalBlocks.add(new Vector(+1, -2, 0));
        this.portalBlocks.add(new Vector(-1, -2, 0));
        this.portalBlocks.add(new Vector(0, -2, +1));
        this.portalBlocks.add(new Vector(0, -2, -1));
        this.portalBlocks.add(new Vector(0, -3, 0));

        this.portalBlocks.add(new Vector(+3, -4, -1));
        this.portalBlocks.add(new Vector(+3, -4, 0));
        this.portalBlocks.add(new Vector(+3, -4, +1));

        this.portalBlocks.add(new Vector(+2, -4, -2));
        this.portalBlocks.add(new Vector(+2, -4, -1));
        this.portalBlocks.add(new Vector(+2, -4, 0));
        this.portalBlocks.add(new Vector(+2, -4, +1));
        this.portalBlocks.add(new Vector(+2, -4, +2));

        this.portalBlocks.add(new Vector(+1, -4, -3));
        this.portalBlocks.add(new Vector(+1, -4, -2));
        this.portalBlocks.add(new Vector(+1, -4, -1));
        this.portalBlocks.add(new Vector(+1, -4, 0));
        this.portalBlocks.add(new Vector(+1, -4, +1));
        this.portalBlocks.add(new Vector(+1, -4, +2));
        this.portalBlocks.add(new Vector(+1, -4, +3));

        this.portalBlocks.add(new Vector(0, -4, -3));
        this.portalBlocks.add(new Vector(0, -4, -2));
        this.portalBlocks.add(new Vector(0, -4, -1));
        this.portalBlocks.add(new Vector(0, -4, 0));
        this.portalBlocks.add(new Vector(0, -4, +1));
        this.portalBlocks.add(new Vector(0, -4, +2));
        this.portalBlocks.add(new Vector(0, -4, +3));

        this.portalBlocks.add(new Vector(-1, -4, -3));
        this.portalBlocks.add(new Vector(-1, -4, -2));
        this.portalBlocks.add(new Vector(-1, -4, -1));
        this.portalBlocks.add(new Vector(-1, -4, 0));
        this.portalBlocks.add(new Vector(-1, -4, +1));
        this.portalBlocks.add(new Vector(-1, -4, +2));
        this.portalBlocks.add(new Vector(-1, -4, +3));

        this.portalBlocks.add(new Vector(-2, -4, -2));
        this.portalBlocks.add(new Vector(-2, -4, -1));
        this.portalBlocks.add(new Vector(-2, -4, 0));
        this.portalBlocks.add(new Vector(-2, -4, +1));
        this.portalBlocks.add(new Vector(-2, -4, +2));

        this.portalBlocks.add(new Vector(-3, -4, -1));
        this.portalBlocks.add(new Vector(-3, -4, 0));
        this.portalBlocks.add(new Vector(-3, -4, +1));

        this.portalBlocks.add(new Vector(+2, -5, -1));
        this.portalBlocks.add(new Vector(+2, -5, 0));
        this.portalBlocks.add(new Vector(+2, -5, +1));

        this.portalBlocks.add(new Vector(+1, -5, -2));
        this.portalBlocks.add(new Vector(+1, -5, -1));
        this.portalBlocks.add(new Vector(+1, -5, 0));
        this.portalBlocks.add(new Vector(+1, -5, +1));
        this.portalBlocks.add(new Vector(+1, -5, +2));

        this.portalBlocks.add(new Vector(0, -5, -2));
        this.portalBlocks.add(new Vector(0, -5, -1));
        this.portalBlocks.add(new Vector(0, -5, 0));
        this.portalBlocks.add(new Vector(0, -5, +1));
        this.portalBlocks.add(new Vector(0, -5, +2));

        this.portalBlocks.add(new Vector(-1, -5, -2));
        this.portalBlocks.add(new Vector(-1, -5, -1));
        this.portalBlocks.add(new Vector(-1, -5, 0));
        this.portalBlocks.add(new Vector(-1, -5, +1));
        this.portalBlocks.add(new Vector(-1, -5, +2));

        this.portalBlocks.add(new Vector(-2, -5, -1));
        this.portalBlocks.add(new Vector(-2, -5, 0));
        this.portalBlocks.add(new Vector(-2, -5, +1));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void rewardOnEDDeath(final EntityDeathEvent event) {
        final EndWorldConfig config = this.plugin.mainEndConfig;
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }
        final EnderDragon ed = (EnderDragon) event.getEntity();
        if (config.getXpRewardingType() == 0) {
            event.setDroppedExp(config.getXpReward());
        } else if (config.getXpRewardingType() == 1) {
            event.setDroppedExp(0);
            final HashMap<String, Double> edData = this.plugin.data.get(ed.getUniqueId());
            if (edData != null) {
                // Compute total damages done to ED
                double totalDmg = 0;
                for (final String s : edData.keySet()) {
                    totalDmg += edData.get(s);
                }

                // Substracte damages from offline players
                for (final String s : edData.keySet()) {
                    final Player p = this.plugin.getServer().getPlayerExact(s);
                    if (p == null) {
                        totalDmg -= edData.get(s);
                    }
                }

                // Now we can compute our percentages and give the exp to players
                for (final String s : edData.keySet()) {
                    final Player p = this.plugin.getServer().getPlayerExact(s);
                    if (p != null) {
                        final double expToGive = config.getXpReward() * (edData.get(s) / totalDmg);
                        p.giveExp((int) expToGive);
                        for (int i = 0; i < config.getExpMessage1().length - 1; i++) {
                            p.sendMessage(this.plugin.header + ChatColor.GREEN + this.plugin.toColor(config.getExpMessage1()[i]));
                        }
                        p.sendMessage(this.plugin.header + ChatColor.GREEN + this.plugin.toColor(config.getExpMessage1()[config.getExpMessage1().length - 1]) + (int) expToGive
                                + this.plugin.toColor(config.getExpMessage2()[0]));
                        for (int i = 1; i < config.getExpMessage2().length; i++) {
                            p.sendMessage(this.plugin.header + ChatColor.GREEN + this.plugin.toColor(config.getExpMessage2()[i]));
                        }
                    }
                }
            }
        }
        config.setNbEd(config.getNbEd() - 1);
        this.plugin.data.remove(ed.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerHitED(final EntityDamageEvent event) {
        final EndWorldConfig config = this.plugin.mainEndConfig;
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }
        final EnderDragon ed = (EnderDragon) event.getEntity();
        final UUID edId = ed.getUniqueId();
        HashMap<String, Double> edData;

        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }
        final EntityDamageByEntityEvent eventByEntity = (EntityDamageByEntityEvent) event;

        // Handle custom ED health
        if (!this.plugin.edHealth.containsKey(edId)) {
            this.plugin.edHealth.put(edId, ed.getHealth() >= 200 ? config.getEnderDragonHealth() : ed.getHealth());
        }
        final int oldHealth = this.plugin.edHealth.get(edId);
        final int newHealth = oldHealth - eventByEntity.getDamage();
        this.plugin.edHealth.put(edId, newHealth);
        if (oldHealth > 200 + eventByEntity.getDamage()) {
            event.setDamage(0);
        } else if (oldHealth > 200) {
            event.setDamage(200 - newHealth);
        } else {
            event.setDamage(eventByEntity.getDamage());
        }

        Player p;
        if (eventByEntity.getDamager() instanceof Player) {
            p = (Player) eventByEntity.getDamager();
        } else if (eventByEntity.getDamager() instanceof Projectile) {
            final LivingEntity e = ((Projectile) eventByEntity.getDamager()).getShooter();
            if (e instanceof Player) {
                p = (Player) e;
            } else {
                return;
            }
        } else {
            return;
        }
        final String playerName = p.getName().toLowerCase();

        if (this.plugin.data.containsKey(edId)) {
            edData = this.plugin.data.get(edId);
        } else {
            edData = new HashMap<String, Double>();
        }
        double totalDmg = eventByEntity.getDamage();
        if (edData.containsKey(playerName)) {
            totalDmg += edData.get(playerName);
        }

        edData.put(playerName, totalDmg);
        this.plugin.data.put(edId, edData);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEDCreatePortal(final EntityCreatePortalEvent event) {
        final EndWorldConfig config = this.plugin.mainEndConfig;
        if (event.getEntityType().equals(EntityType.ENDER_DRAGON)) {
            if (config.getPreventPortals() == 1) {
                Block egg = null;
                for (final BlockState b : event.getBlocks()) {
                    if (!b.getType().equals(Material.DRAGON_EGG)) {
                        b.setType(Material.AIR);
                    } else {
                        egg = b.getBlock();
                    }
                }
                if (egg != null) {
                    final int chunkX = egg.getChunk().getX();
                    final int chunkZ = egg.getChunk().getZ();
                    for (int x = chunkX - 2; x <= chunkX + 2; x++) {
                        for (int z = chunkZ - 2; z <= chunkZ + 2; z++) {
                            this.plugin.mainEndWorld.refreshChunk(x, z);
                        }
                    }
                }
            } else if (config.getPreventPortals() == 2) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEndChunkLoad(final ChunkLoadEvent event) {
        final Chunk c = event.getChunk();
        if (c.getWorld().equals(this.plugin.mainEndWorld)) {
            ExtendedChunk chunk = this.plugin.endChunks.getChunk(c);
            if (chunk == null) {
                chunk = this.plugin.endChunks.addChunk(c);
            } else if (chunk.hasToBeRegen()) {
                // Prevent existing EnderDragons to be deleted by regen
                int teleportDestX = 0;
                int teleportDestZ = 0;
                if (chunk.getX() == 0 && chunk.getZ() == 0) {
                    teleportDestX = 20;
                    teleportDestZ = 20;
                }
                final Location teleportDest = new Location(this.plugin.mainEndWorld, teleportDestX, 90, teleportDestZ);
                for (final Entity e : c.getEntities()) {
                    if (e.getType() == EntityType.ENDER_DRAGON && ((EnderDragon) e).getHealth() > 0) {
                        e.teleport(teleportDest);
                    } else if (e.getType() != EntityType.ENDER_DRAGON) {
                        e.remove();
                    }
                }
                // Now regen
                this.plugin.mainEndWorld.regenerateChunk(c.getX(), c.getZ());
                this.plugin.mainEndWorld.refreshChunk(c.getX(), c.getZ());
                chunk.setToBeRegen(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnderDragonSpawn(final CreatureSpawnEvent event) {
        final EndWorldConfig config = this.plugin.mainEndConfig;
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            if (config.getNbEd() >= config.getActualNbMaxEnderDragon()) {
                event.setCancelled(true);
            } else {
                final EnderDragon ed = (EnderDragon) event.getEntity();
                this.plugin.edHealth.put(ed.getUniqueId(), config.getEnderDragonHealth());
                config.setNbEd(config.getNbEd() + 1);
            }
        }
    }
}
