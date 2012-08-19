package com.github.ribesg.tea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.ribesg.tea.util.EndChunks;
import com.github.ribesg.tea.util.ExtendedChunk;


public class TheEndAgain extends JavaPlugin {

    // Headers for plugin messages
    public String                                 header      = ChatColor.BLACK + "[" + ChatColor.GREEN + "End" + ChatColor.BLACK + "] " + ChatColor.WHITE;

    // Config file
    private final String                          directory   = "plugins" + File.separator + "TheEndAgain";
    File                                          f_config    = new File(this.directory + File.separator + "config.yml");
    File                                          f_endChunks = new File(this.directory + File.separator + "endChunks.yml");
    YamlConfiguration                             config;
    public boolean                                regenOnStop, preventPortals, regenOnRespawn;
    public int                                    actionOnRegen, respawnTimer, nbMinEnderDragon, nbMaxEnderDragon, TASK_respawnTimerTask, xpRewardingType, xpReward, actualNbEnderDragon, actualNbPlayerInEndWorld,
    enderDragonHealth;
    public String                                 regenMessage;
    public String[]                               respawnMessages;
    public String[]                               expMessage1, expMessage2;

    public World                                  endWorld;
    public EndChunks                              endChunks;

    // To store who hit which ED
    public HashMap<UUID, HashMap<String, Double>> data;

    // To store custom ED health
    public HashMap<UUID, Integer>                 edHealth;

    // Actual number of ED in End
    public int                                    nbED        = 0;

    private final TEA_Listener                    listener    = new TEA_Listener(this);
    private final TEA_CommandExecutor             myExecutor  = new TEA_CommandExecutor(this);

    @Override
    public void onDisable() {
        // Method called when the server stop

        if (this.endWorld == null) {
            this.getLogger().warning("No End world found ! Nothing will be down.");
        } else {
            if (this.regenOnStop) {
                this.getLogger().info("Regenerating the End world...");
                this.hardRegen();
            }
            this.endChunks.save(this.f_endChunks);
        }

        this.getLogger().info("TheEndAgain successfully disabled.");
    }

    @Override
    public void onEnable() {
        // Method called when the server starts
        this.data = new HashMap<UUID, HashMap<String, Double>>();
        this.edHealth = new HashMap<UUID, Integer>();
        this.actualNbPlayerInEndWorld = 0;

        // Loading config
        this.checkConfig();
        try {
            this.config.load(this.f_config);
        } catch (final FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (final IOException e1) {
            e1.printStackTrace();
        } catch (final InvalidConfigurationException e1) {
            e1.printStackTrace();
        }
        if (!this.config.getBoolean("useTEAPrefix", true)) {
            this.header = "";
        }
        this.regenOnStop = this.config.getBoolean("regenOnStop", true);
        this.regenOnStop = this.config.getBoolean("regenOnRespawn", false);
        this.actionOnRegen = this.config.getInt("actionOnRegen", 0);
        if (this.actionOnRegen != 0 && this.actionOnRegen != 1) {
            this.getLogger().severe("actionOnRegen should be 0 or 1. Check config. Value set to 0 !");
            this.actionOnRegen = 0;
        }
        this.respawnTimer = this.config.getInt("respawnTimer", 180);
        this.nbMaxEnderDragon = this.config.getInt("nbMaxEnderDragon", 1);
        if (this.nbMaxEnderDragon < 1) {
            this.getLogger().severe("nbMaxEnderDragon is lesser than 1 ! Value reset to 1 !");
        } else if (this.nbMaxEnderDragon > 10) {
            this.getLogger().warning("nbMaxEnderDragon is greater than 10 ! This could be dangerous !");
        }
        this.nbMinEnderDragon = this.config.getInt("nbMinEnderDragon", 1);
        if (this.nbMinEnderDragon < 1) {
            this.getLogger().severe("nbMinEnderDragon is lesser than 1 ! Value reset to 1 !");
        } else if (this.nbMinEnderDragon > 10) {
            this.getLogger().warning("nbMinEnderDragon is greater than 10 ! This could be dangerous !");
        }
        if (!this.newActualNumber()) {
            this.getLogger().severe("nbMinEnderDragon and nbMaxEnderDragon have bad values. They have been setted to 1 for now.");
            this.getLogger().severe("Please check config");
        }
        this.xpRewardingType = this.config.getInt("xpRewardingType", 0);
        if (this.xpRewardingType != 0 && this.xpRewardingType != 1) {
            this.xpRewardingType = 0;
            this.getLogger().severe("xpRewardingType should be 0 or 1. Check config. Value set to 0 !");
        }
        this.xpReward = this.config.getInt("xpReward", 20000);
        if (this.xpReward < 0) {
            this.xpReward = 0;
            this.getLogger().severe("xpReward should greater than 0. Check config. Value set to 0 !");
        }

        this.regenMessage = this.config.getString("regenMessage");

        final String respawnMessagesTmp = this.config.getString("respawnMessages", "");
        this.respawnMessages = respawnMessagesTmp.length() > 0 ? respawnMessagesTmp.split(";") : new String[0];
        this.expMessage1 = this.config.getString("expMessage1", "The EnderDragon died ! You won ").split(";");
        this.expMessage2 = this.config.getString("expMessage2", " exp !").split(";");

        this.preventPortals = this.config.getBoolean("preventPortals", false);
        this.enderDragonHealth = this.config.getInt("enderDragonHealth", 200);
        if (this.enderDragonHealth < 1) {
            this.enderDragonHealth = 200;
            this.getLogger().warning("enderDragonHealth should greater than 1. Check config. Value set to 200 !");
        }

        this.TASK_respawnTimerTask = -42;

        this.getCommand("respawnenderdragon").setExecutor(this.myExecutor);
        this.getCommand("nbenderdragon").setExecutor(this.myExecutor);
        this.getCommand("regenend").setExecutor(this.myExecutor);

        // Registering events, if needed
        this.getServer().getPluginManager().registerEvents(this.listener, this);

        // Do what we have to do
        for (final World w : this.getServer().getWorlds()) {
            if (w.getEnvironment().equals(Environment.THE_END)) {
                this.endWorld = w;
                break;
            }
        }
        if (this.endWorld == null) {
            this.getLogger().warning("No End world found ! Nothing will be done.");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            this.endChunks = new EndChunks(this.endWorld);
            this.endChunks.load(this.f_endChunks);
            this.endWorld.setKeepSpawnInMemory(false);
            this.spawnEnderDragonsToActualNumber();
            this.launchRespawnTask();

            for (final Entity e : this.endWorld.getEntities()) {
                if (e instanceof EnderDragon) {
                    final EnderDragon ed = (EnderDragon) e;
                    ed.setHealth(200);
                    this.edHealth.put(ed.getUniqueId(), this.enderDragonHealth);
                }
            }
        }
        this.getLogger().info("TheEndAgain successfully enabled.");
    }

    public void hardRegen() {
        for (final Player p : this.endWorld.getPlayers()) {
            if (this.actionOnRegen == 0) {
                p.kickPlayer(this.header + ChatColor.GREEN + this.toColor(this.regenMessage));
            } else {
                p.sendMessage(this.header + ChatColor.GREEN + this.toColor(this.regenMessage));
                p.teleport(this.getServer().getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
            }
        }
        for (final Entity e : this.endWorld.getEntities()) {
            e.remove();
        }
        for (final ExtendedChunk chunk : this.endChunks.getIterableChunks()) {
            final Chunk c = this.endWorld.getChunkAt(chunk.getX(), chunk.getZ());
            this.endWorld.regenerateChunk(c.getX(), c.getZ());
        }
        this.endWorld.save();
    }

    public void softRegen() {
        for (final Player p : this.endWorld.getPlayers()) {
            if (this.actionOnRegen == 0) {
                p.kickPlayer(this.header + ChatColor.GREEN + this.toColor(this.regenMessage));
            } else {
                p.teleport(this.getServer().getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
                p.sendMessage(this.header + ChatColor.GREEN + this.toColor(this.regenMessage));
            }
        }
        for (final Entity e : this.endWorld.getEntities()) {
            e.remove();
        }
        for (final Chunk c : this.endWorld.getLoadedChunks()) {
            c.unload();
        }
        this.endChunks.regen();
        this.spawnEnderDragonsToActualNumber();
    }

    public int spawnEnderDragonsToActualNumber() {
        int dragonNumber = 0;
        int spawned = 0;
        if (this.endWorld != null) {
            this.updateNbAliveED();
            dragonNumber = this.nbED;
            final Random rand = new Random();
            while (dragonNumber < this.actualNbEnderDragon) {
                final Location loc = new Location(this.endWorld, rand.nextInt(20) - 10, rand.nextInt(20) + 90, rand.nextInt(20) - 10);
                loc.getChunk().load();
                this.endWorld.spawnEntity(loc, EntityType.ENDER_DRAGON);
                dragonNumber++;
                spawned++;
            }
        }
        if (spawned > 0) {
            this.broadcastSpawned();
        }
        return spawned;
    }

    public void launchRespawnTask() {
        if (this.TASK_respawnTimerTask == -42) {
            this.TASK_respawnTimerTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

                @Override
                public void run() {
                    TheEndAgain.this.newActualNumber();
                    if (TheEndAgain.this.regenOnRespawn) {
                        TheEndAgain.this.softRegen();
                    } else { // The softRegen() method also call spawnEnderDragonsToActualNumber() so we don't have to call it if regenOnRespawn
                        TheEndAgain.this.spawnEnderDragonsToActualNumber();
                    }
                }
            }, 2 * 20, this.respawnTimer * 60 * 20);
        } else {
            this.getServer().getScheduler().cancelTask(this.TASK_respawnTimerTask);
            this.TASK_respawnTimerTask = -42;
            this.launchRespawnTask();
        }
    }

    public void checkConfig() {
        new File(this.directory).mkdir();
        this.config = new YamlConfiguration();
        if (!this.f_config.exists()) {
            this.newConfig();
        } else {
            try {
                this.config.load(this.f_config);
            } catch (final FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (final IOException e1) {
                e1.printStackTrace();
            } catch (final InvalidConfigurationException e1) {
                e1.printStackTrace();
            }
            if (this.config.isSet("respawnType")) {
                // Old config
                this.newConfig();
            } else if (!this.config.isSet("useTEAPrefix")) {
                // Old config
                this.newConfig();
            } else if (!this.config.isSet("regenOnRespawn")) {
                // Old config
                this.newConfig();
            }
        }
    }

    public void newConfig() {
        try {
            this.f_config.createNewFile();
            final FileWriter fstream = new FileWriter(this.f_config);
            final BufferedWriter out = new BufferedWriter(fstream);

            out.write("#Should we use the [TheEndAgain] prefix in messages ? Yes=true, No=false\n");
            out.write("useTEAPrefix: true\n\n");

            out.write("#Should we regen the End world at server stop ? Yes=true, No=false\n");
            out.write("regenOnStop: true\n\n");

            out.write("#Should we regen the End world when respawning EnderDragons ? Yes=true, No=false\n");
            out.write("regenOnRespawn: false\n\n");

            out.write("#What should we do if there are players in the End world on regen ?\n");
            out.write("#	* 0 = All players in the End world get kicked, so they can rejoin directly in the End after restart <= Default Value\n");
            out.write("#	* 1 = All players in the End world get teleported to first world's spawn\n");
            out.write("actionOnRegen: 0\n\n");

            out.write("#Messages to send when the End regen. Used for broadcast and kick message (actionOnRegen value above)\n");
            out.write("regenMessage: 'The &cEnd &ais regenerating !'\n\n");

            out.write("#The end will be regenerated and ED will respawn every X minutes. Here are some examples :\n");
            out.write("#	* 10   = 10 minutes\n");
            out.write("#	* 60   = 1 hour\n");
            out.write("#	* 240  = 4 hours (6 times per day)\n");
            out.write("#	* 360  = 6 hours (4 times per day)\n");
            out.write("#	* 480  = 8 hours (3 times per day)\n");
            out.write("#	* 720  = 12 hours (2 times per day)\n");
            out.write("#	* 1440 = 24 hours (1 time per day) <= Default Value\n");
            out.write("respawnTimer: 1440\n\n");

            out.write("#Maximum number of EnderDragon to be respawned in the End world ?\n");
            out.write("nbMaxEnderDragon: 1\n\n");

            out.write("#Minimum number of EnderDragon to be respawned in the End world ?\n");
            out.write("nbMinEnderDragon: 1\n\n");

            out.write("#Use custom XP rewarding system ? Yes=1, No=0\n");
            out.write("xpRewardingType: 1\n\n");

            out.write("#How many XP points does the ED drop/give ?\n");
            out.write("xpReward: 20000\n\n");

            out.write("#Messages to send when the ED respawn. Set to '' for no messages. Seperate different lines with ;\n");
            out.write("respawnMessages: 'The &cEnderDragon &arespawned !;Will you try to &ckill him &a?'\n\n");

            out.write("#Messages to send when the ED die and players receive exp with custom system.\n");
            out.write("#	Message format : expMessage1 <expQuantity> expMessage2\n");
            out.write("#	Example with 100 as quantity : The EnderDragon died ! You won 100 exp !\n");
            out.write("expMessage1: 'The &cED &adied !;You won &c'\n");
            out.write("expMessage2: ' &aexp !'\n\n");

            out.write("#Change the health value of the EnderDragon. Default = 200\n");
            out.write("enderDragonHealth: 200\n\n");

            out.write("#Prevent EnderDragon from creating portals on Death ? Yes=true, No=false\n");
            out.write("preventPortals: false\n\n");
            out.close();
            this.getLogger().info("config.yml generated, please see plugins/TheEndAgain/config.yml !");
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateNbAliveED() {
        this.nbED = 0;
        if (this.endWorld != null) {
            for (final ExtendedChunk chunk : this.endChunks.getIterableChunks()) {
                this.endWorld.loadChunk(chunk.getX(), chunk.getZ());
            }
            for (final Entity e : this.endWorld.getEntities()) {
                if (e.getType() == EntityType.ENDER_DRAGON && ((EnderDragon) e).getHealth() > 0) {
                    this.nbED++;
                }
            }
        }
    }

    public void broadcastSpawned() {
        for (final String s : this.respawnMessages) {
            this.getServer().broadcastMessage(this.header + ChatColor.GREEN + this.toColor(s));
        }
    }

    public boolean newActualNumber() {
        // RETURN : Are nbMin and nbMax valids ?
        if (this.nbMaxEnderDragon - this.nbMinEnderDragon < 0) {
            this.nbMinEnderDragon = 1;
            this.nbMaxEnderDragon = 1;
            this.actualNbEnderDragon = 1;
            return false;
        } else {
            this.actualNbEnderDragon = this.nbMinEnderDragon + new Random().nextInt(this.nbMaxEnderDragon - this.nbMinEnderDragon + 1);
            return true;
        }
    }

    // Utils
    public String toColor(final String input) {
        // This strange thing replace all '&1' and all of these with the
        // associated ChatColor element
        final String output = input.replaceAll("&([0-9a-fA-F])", "\u00A7$1");
        return output;
    }
}
