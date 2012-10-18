package com.github.ribesg.tea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
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
import com.github.ribesg.tea.util.EndWorldConfig;
import com.github.ribesg.tea.util.ExtendedChunk;


public class TheEndAgain extends JavaPlugin {

    // Headers for plugin messages
    public String                                 header      = ChatColor.BLACK + "[" + ChatColor.GREEN + "End" + ChatColor.BLACK + "] " + ChatColor.WHITE;

    // Config file
    private final String                          directory   = "plugins" + File.separator + "TheEndAgain";
    File                                          f_config    = new File(this.directory + File.separator + "config.yml");
    File                                          f_endChunks = new File(this.directory + File.separator + "endChunks.yml");
    EndWorldConfig                                mainEndConfig;
    public World                                  mainEndWorld;
    public EndChunks                              endChunks;

    // To store who hit which ED
    public HashMap<UUID, HashMap<String, Double>> data;                                                                                                    // DragonId, <PlayerName, TotalDamage>

    // To store custom ED health
    public HashMap<UUID, Integer>                 edHealth;

    private final TEA_Listener                    listener    = new TEA_Listener(this);
    private final TEA_CommandExecutor             myExecutor  = new TEA_CommandExecutor(this);

    @Override
    public void onDisable() {
        // Method called when the server stop

        if (this.mainEndWorld == null) {
            this.getLogger().warning("No End world found ! Nothing will be down.");
        } else {
            if (this.mainEndConfig.regenOnStop()) {
                this.hardRegen(this.mainEndWorld);
            } else {
                this.getLogger().info("Regen on Stop disabled, nothing to do.");
            }
            this.endChunks.save(this.f_endChunks);
        }

        Bukkit.getScheduler().cancelTasks(this);
        this.getLogger().info("TheEndAgain successfully disabled.");
    }

    @Override
    public void onEnable() {
        // Method called when the server starts
        this.data = new HashMap<UUID, HashMap<String, Double>>();
        this.edHealth = new HashMap<UUID, Integer>();
        this.mainEndConfig = new EndWorldConfig(this);

        // Loading config
        this.checkConfig();
        this.loadConfig(this.mainEndWorld);

        this.mainEndConfig.setRespawnTimerTask(-42);

        this.getCommand("end").setExecutor(this.myExecutor);

        // Registering events, if needed
        this.getServer().getPluginManager().registerEvents(this.listener, this);

        // Do what we have to do
        for (final World w : this.getServer().getWorlds()) {
            if (w.getEnvironment().equals(Environment.THE_END)) {
                this.mainEndWorld = w;
                break;
            }
        }
        if (this.mainEndWorld == null) {
            this.getLogger().warning("No End world found ! Nothing will be done.");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            this.endChunks = new EndChunks(this, this.mainEndWorld);

            // Creating all thoses objects is a bit long, dn't block the main thread for that.
            Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {

                @Override
                public void run() {
                    synchronized (TheEndAgain.this.endChunks) {
                        TheEndAgain.this.endChunks.load(TheEndAgain.this.f_endChunks);
                    }
                }
            });
            this.mainEndWorld.setKeepSpawnInMemory(false);
            if (this.mainEndConfig.getRespawnTimer() == 0) {
                this.spawnEnderDragonsToActualNumber(this.mainEndWorld);
            } else {
                this.launchRespawnTask(this.mainEndWorld);
            }

            // Task which check if there is not too much Dragons
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

                @Override
                public void run() {
                    TheEndAgain.this.updateNbAliveED(TheEndAgain.this.mainEndWorld);
                    if (TheEndAgain.this.mainEndConfig.getNbEd() > TheEndAgain.this.mainEndConfig.getActualNbMaxEnderDragon()) {
                        TheEndAgain.this.removeEnderDragons(TheEndAgain.this.mainEndWorld, TheEndAgain.this.mainEndConfig.getNbEd() - TheEndAgain.this.mainEndConfig.getNbMaxEnderDragon());
                    }
                }
            }, 20 * 5, 20 * 5);

            for (final Entity e : this.mainEndWorld.getEntities()) {
                if (e instanceof EnderDragon) {
                    final EnderDragon ed = (EnderDragon) e;
                    ed.setHealth(200);
                    this.edHealth.put(ed.getUniqueId(), this.mainEndConfig.getEnderDragonHealth());
                }
            }

        }
        this.getLogger().info("TheEndAgain successfully enabled.");
    }

    public void hardRegen(final World w) {
        final EndWorldConfig config = this.mainEndConfig; // Later we will choose the correct config here according to w
        this.getLogger().info("Hard Regen - Taking out the players...");
        for (final Player p : this.mainEndWorld.getPlayers()) {
            if (config.getActionOnRegen() == 0) {
                p.kickPlayer(this.header + ChatColor.GREEN + this.toColor(config.getRegenMessage()));
            } else {
                p.sendMessage(this.header + ChatColor.GREEN + this.toColor(config.getRegenMessage()));
                p.teleport(this.getServer().getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
            }
        }
        this.getLogger().info("Hard Regen - Removing entities...");
        for (final Entity e : this.mainEndWorld.getEntities()) {
            e.remove();
        }
        this.getLogger().info("Hard Regen - Regenerating...");
        for (final ExtendedChunk chunk : this.endChunks.getIterableChunks()) {
            final Chunk c = this.mainEndWorld.getChunkAt(chunk.getX(), chunk.getZ());
            this.mainEndWorld.regenerateChunk(c.getX(), c.getZ());
        }
        this.getLogger().info("Hard Regen - Saving...");
        this.mainEndWorld.save();
        this.getLogger().info("Hard Regen - Done !");
    }

    public void softRegen(final World w) {
        final EndWorldConfig config = this.mainEndConfig; // Later we will choose the correct config here according to w
        config.setNbEd(0);
        this.getLogger().info("Soft Regen - Taking out the players...");
        for (final Player p : this.mainEndWorld.getPlayers()) {
            if (config.getActionOnRegen() == 0) {
                p.kickPlayer(this.header + ChatColor.GREEN + this.toColor(config.getRegenMessage()));
            } else {
                p.teleport(this.getServer().getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
                p.sendMessage(this.header + ChatColor.GREEN + this.toColor(config.getRegenMessage()));
            }
        }
        this.getLogger().info("Soft Regen - Removing entities...");
        for (final Entity e : this.mainEndWorld.getEntities()) {
            e.remove();
        }
        this.getLogger().info("Soft Regen - Unloading chunks...");
        for (final Chunk c : this.mainEndWorld.getLoadedChunks()) {
            c.unload();
        }
        this.getLogger().info("Soft Regen - Flag chunks as to-be-regen-on-reload...");
        this.endChunks.regen(this.mainEndWorld.getName());
        this.getLogger().info("Soft Regen - Chunks flagged. Waiting...");
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

            @Override
            public void run() {
                TheEndAgain.this.getLogger().info("Soft Regen - Respawning EDs...");
                TheEndAgain.this.spawnEnderDragonsToActualNumber(TheEndAgain.this.mainEndWorld);
                TheEndAgain.this.getLogger().info("Soft Regen - EDs respawned !");
            }
        }, 20 * 3);
    }

    public int spawnEnderDragonsToActualNumber(final World w) {
        final EndWorldConfig config = this.mainEndConfig; // Later we will choose the correct config here according to w
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                this.mainEndWorld.loadChunk(x, z);
            }
        }
        int dragonNumber = 0;
        int spawned = 0;
        if (this.mainEndWorld != null) {
            this.updateNbAliveED(w);
            dragonNumber = config.getNbEd();
            final Random rand = new Random();
            while (dragonNumber < config.getActualNbMaxEnderDragon()) {
                final Location loc = new Location(this.mainEndWorld, rand.nextInt(20) - 10, rand.nextInt(20) + 70, rand.nextInt(20) - 10);
                loc.getChunk().load();
                if (this.mainEndWorld.spawnEntity(loc, EntityType.ENDER_DRAGON) == null) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                        @Override
                        public void run() {
                            TheEndAgain.this.mainEndWorld.spawnEntity(loc, EntityType.ENDER_DRAGON);
                        }
                    });
                }
                dragonNumber++;
                spawned++;
            }
        }
        if (spawned > 0) {
            this.broadcastSpawned(this.mainEndWorld);
        }
        return spawned;
    }

    protected void removeEnderDragons(final World w, final int quantity) {
        final World endWorld = this.mainEndWorld; // Later we will choose the correct world (w)
        // Delete the farthest first
        final SortedMap<Double, EnderDragon> dragons = new TreeMap<Double, EnderDragon>();
        for (final Entity e : endWorld.getEntities()) {
            if (e.getType() == EntityType.ENDER_DRAGON && ((EnderDragon) e).getHealth() > 0) {
                dragons.put(e.getLocation().lengthSquared(), (EnderDragon) e);
            }
        }
        int deletedEDs = 0;
        while (deletedEDs < quantity && dragons.size() > 0) {
            final EnderDragon e = dragons.get(dragons.lastKey());
            this.data.remove(e.getUniqueId());
            e.remove();
            dragons.remove(dragons.lastKey());
            deletedEDs++;
        }

    }

    public void launchRespawnTask(final World w) {
        final EndWorldConfig config = this.mainEndConfig; // Later we will choose the correct config here according to w
        if (config.getRespawnTimer() != 0) {
            if (config.getRespawnTimerTask() == -42) {
                config.setRespawnTimerTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

                    @Override
                    public void run() {
                        config.newActualNumber();
                        if (config.regenOnRespawn()) {
                            TheEndAgain.this.softRegen(w);
                        } else { // The softRegen() method also call spawnEnderDragonsToActualNumber() so we don't have to call it if regenOnRespawn
                            TheEndAgain.this.spawnEnderDragonsToActualNumber(w);
                        }
                    }
                }, 10 * 20, config.getRespawnTimer() * 60 * 20));
            } else {
                this.getServer().getScheduler().cancelTask(config.getRespawnTimerTask());
                config.setRespawnTimerTask(-42);
                this.launchRespawnTask(w);
            }
        }
    }

    public void checkConfig() {
        new File(this.directory).mkdir();
        final YamlConfiguration yamlConfig = new YamlConfiguration();
        if (!this.f_config.exists()) {
            this.newConfig(this.mainEndWorld);
        } else {
            try {
                yamlConfig.load(this.f_config);
            } catch (final FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (final IOException e1) {
                e1.printStackTrace();
            } catch (final InvalidConfigurationException e1) {
                e1.printStackTrace();
            }
            if (!yamlConfig.isSet("pluginVersion") || !yamlConfig.getString("pluginVersion").equals(this.getDescription().getVersion())) {
                // Old config
                this.newConfig(this.mainEndWorld);
            }
        }
    }

    public void loadConfig(final World w) {
        final EndWorldConfig config = this.mainEndConfig; // Later we will choose the correct config here according to w
        final YamlConfiguration yamlConfig = new YamlConfiguration();
        try {
            yamlConfig.load(this.f_config); // We will have to have one file per world to choose from, here
        } catch (final FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (final IOException e1) {
            e1.printStackTrace();
        } catch (final InvalidConfigurationException e1) {
            e1.printStackTrace();
        }
        if (!yamlConfig.getBoolean("useTEAPrefix", true)) {
            this.header = "";
        }
        config.setRegenOnStop(yamlConfig.getBoolean("regenOnStop", true));
        config.setRegenOnRespawn(yamlConfig.getBoolean("regenOnRespawn", false));
        config.setActionOnRegen(yamlConfig.getInt("actionOnRegen", 0));
        if (config.getActionOnRegen() != 0 && config.getActionOnRegen() != 1) {
            this.getLogger().severe("actionOnRegen should be 0 or 1. Check config. Value set to 0 !");
            config.setActionOnRegen(0);
        }
        config.setRespawnTimer(yamlConfig.getInt("respawnTimer", 180));
        config.setNbMaxEnderDragon(yamlConfig.getInt("nbMaxEnderDragon", 1));
        if (config.getNbMaxEnderDragon() < 1) {
            this.getLogger().severe("nbMaxEnderDragon is lesser than 1 ! Value reset to 1 !");
        } else if (config.getNbMaxEnderDragon() > 10) {
            this.getLogger().warning("nbMaxEnderDragon is greater than 10 ! This could be dangerous !");
        }
        config.setNbMinEnderDragon(yamlConfig.getInt("nbMinEnderDragon", 1));
        if (config.getNbMinEnderDragon() < 1) {
            this.getLogger().severe("nbMinEnderDragon is lesser than 1 ! Value reset to 1 !");
        } else if (config.getNbMinEnderDragon() > 10) {
            this.getLogger().warning("nbMinEnderDragon is greater than 10 ! This could be dangerous !");
        }
        if (!config.newActualNumber()) {
            this.getLogger().severe("nbMinEnderDragon and nbMaxEnderDragon have bad values. They have been setted to 1 for now.");
            this.getLogger().severe("Please check config");
        }
        config.setXpRewardingType(yamlConfig.getInt("xpRewardingType", 0));
        if (config.getXpRewardingType() != 0 && config.getXpRewardingType() != 1) {
            config.setXpRewardingType(0);
            this.getLogger().severe("xpRewardingType should be 0 or 1. Check config. Value set to 0 !");
        }
        config.setXpReward(yamlConfig.getInt("xpReward", 20000));
        if (config.getXpReward() < 0) {
            config.setXpReward(0);
            this.getLogger().severe("xpReward should greater than 0. Check config. Value set to 0 !");
        }

        config.setRegenMessage(yamlConfig.getString("regenMessage"));

        final String respawnMessagesTmp = yamlConfig.getString("respawnMessages", "");
        config.setRespawnMessages(respawnMessagesTmp.length() > 0 ? respawnMessagesTmp.split(";") : new String[0]);
        config.setExpMessage1(yamlConfig.getString("expMessage1", "The EnderDragon died ! You won ").split(";"));
        config.setExpMessage2(yamlConfig.getString("expMessage2", " exp !").split(";"));

        config.setPreventPortals(yamlConfig.getInt("preventPortals", 0));
        config.setEnderDragonHealth(yamlConfig.getInt("enderDragonHealth", 200));
        if (config.getEnderDragonHealth() < 1) {
            config.setEnderDragonHealth(200);
            this.getLogger().warning("enderDragonHealth should greater than 1. Check config. Value set to 200 !");
        }
        config.setEnderDragonDamageMultiplier(yamlConfig.getDouble("enderDragonDamageMultiplier", 1.0));
        if (config.getEnderDragonDamageMultiplier() < 0.0) {
            config.setEnderDragonDamageMultiplier(1.0);
            this.getLogger().warning("enderDragonDamageMultiplier should greater than 0.0. Check config. Value set to 1.0 !");
        }
        config.setCustomEggHandling(yamlConfig.getInt("customEggHandling", 0));
        if (config.getCustomEggHandling() != 1 && config.getCustomEggHandling() != 0) {
            this.getLogger().severe("customEggHandling should be 0 or 1. Check config. Value set to 0 !");
            config.setCustomEggHandling(0);
        }
        config.setEggMessage(yamlConfig.getString("eggMessage", "You earn the &cDragon Egg &a!"));
    }

    public void newConfig(final World w) {
        try {
            boolean update = false;
            YamlConfiguration yamlConfig;
            // Try to load old config to prevent erasing everything
            try {
                if (this.f_config.exists()) {
                    yamlConfig = new YamlConfiguration();
                    yamlConfig.load(this.f_config);
                    update = true;
                } else {
                    yamlConfig = new YamlConfiguration();
                    update = false;
                }
            } catch (final Exception e) {
                yamlConfig = new YamlConfiguration();
                update = false;
            }
            this.f_config.createNewFile();
            final FileWriter fstream = new FileWriter(this.f_config);
            final BufferedWriter out = new BufferedWriter(fstream);

            out.write("#Version of the plugin, DO NOT CHANGE THIS VALUE !\n");
            out.write("pluginVersion: " + this.getDescription().getVersion() + "\n\n");

            out.write("#Should we use the [End] prefix in messages ? Yes=true, No=false\n");
            out.write("useTEAPrefix: " + yamlConfig.getBoolean("useTEAPrefix", true) + "\n\n");

            out.write("#Should we regen the End world at server stop ? Yes=true, No=false\n");
            out.write("regenOnStop: " + yamlConfig.getBoolean("regenOnStop", false) + "\n\n");

            out.write("#Should we regen the End world when respawning EnderDragons ? Yes=true, No=false\n");
            out.write("regenOnRespawn: " + yamlConfig.getBoolean("regenOnRespawn", false) + "\n\n");

            out.write("#What should we do if there are players in the End world on regen ?\n");
            out.write("#	* 0 = All players in the End world get kicked, so they can rejoin directly in the End after restart <= Default Value\n");
            out.write("#	* 1 = All players in the End world get teleported to first world's spawn\n");
            out.write("actionOnRegen: " + yamlConfig.getInt("actionOnRegen", 0) + "\n\n");

            out.write("#Messages to send when the End regen. Used for broadcast and kick message (actionOnRegen value above)\n");
            out.write("regenMessage: '" + yamlConfig.getString("regenMessage", "The &cEnd &ais regenerating !") + "'\n\n");

            out.write("#The end will be regenerated and ED will respawn every X minutes. Here are some examples :\n");
            out.write("#	* 0    = Disabled\n");
            out.write("#	* 10   = 10 minutes\n");
            out.write("#	* 60   = 1 hour\n");
            out.write("#	* 240  = 4 hours (6 times per day)\n");
            out.write("#	* 360  = 6 hours (4 times per day)\n");
            out.write("#	* 480  = 8 hours (3 times per day)\n");
            out.write("#	* 720  = 12 hours (2 times per day)\n");
            out.write("#	* 1440 = 24 hours (1 time per day) <= Default Value\n");
            out.write("respawnTimer: " + yamlConfig.getInt("respawnTimer", 0) + "\n\n");

            out.write("#Maximum number of EnderDragon to be respawned in the End world ? Should not be greater than 3 or 4\n");
            out.write("nbMaxEnderDragon: " + yamlConfig.getInt("nbMaxEnderDragon", 1) + "\n\n");

            out.write("#Minimum number of EnderDragon to be respawned in the End world ? Should not be greater than 3 or 4\n");
            out.write("nbMinEnderDragon: " + yamlConfig.getInt("nbMinEnderDragon", 1) + "\n\n");

            out.write("#Use custom XP rewarding system ? Yes=1, No=0\n");
            out.write("xpRewardingType: " + yamlConfig.getInt("xpRewardingType", 0) + "\n\n");

            out.write("#How many XP points does the ED drop/give ?\n");
            out.write("xpReward: " + yamlConfig.getInt("xpReward", 20000) + "\n\n");

            out.write("#Messages to send when the ED respawn. Set to '' for no messages. Seperate different lines with ;\n");
            out.write("respawnMessages: '" + yamlConfig.getString("respawnMessages", "The &cEnderDragon &arespawned !;Will you try to &ckill him &a?") + "'\n\n");

            out.write("#Messages to send when the ED die and players receive exp with custom system.\n");
            out.write("#	Message format : expMessage1 <expQuantity> expMessage2\n");
            out.write("#	Example with 100 as quantity : The EnderDragon died ! You won 100 exp !\n");
            out.write("expMessage1: '" + yamlConfig.getString("expMessage1", "The &cED &adied !;You won &c") + "'\n");
            out.write("expMessage2: '" + yamlConfig.getString("expMessage2", " &aexp !") + "'\n\n");

            out.write("#Change the health value of the EnderDragon. Default = 200\n");
            out.write("enderDragonHealth: " + yamlConfig.getInt("enderDragonHealth", 200) + "\n\n");

            out.write("#Change the damage done by EnderDragons. Default absolute value depends on which difficulty you play (3 to 7.5 hearts), so it's a multiplier\n");
            out.write("enderDragonDamageMultiplier: " + yamlConfig.getDouble("enderDragonDamageMultiplier", 1.0) + "\n\n");

            out.write("#Prevent EnderDragon from creating portals on Death ?\n");
            out.write("#    * 0    = Disabled - portal will spawn normally. Removes any obsidian tower who could block it.\n");
            out.write("#    * 1    = Egg      - portal will be removed but DragonEgg still spawn. Also removes obsi tower.\n");
            out.write("#    * 2    = Enabled  - portal will not spawn. No more cuted obsidian towers. No Egg.\n");
            out.write("preventPortals: " + yamlConfig.getInt("preventPortals", 0) + "\n\n");

            out.write("#Directly give the egg to one of the principal killer of the EnderDragon ?\n");
            out.write("# WARNING - If preventPortals has a value of 2, this does nothing !\n");
            out.write("#    * 0    = Disabled - The egg will spawn normally\n");
            out.write("#    * 1    = Enabled  - The egg will be semi-randomly given to one of the best fighter against this EnderDragon\n");
            out.write("customEggHandling: " + yamlConfig.getInt("customEggHandling", 0) + "\n\n");

            out.write("#Messages to send when the the player obtains an Egg\n");
            out.write("eggMessage: '" + yamlConfig.getString("eggMessage", "You earn the &cDragon Egg &a!") + "'\n\n");
            out.close();
            this.getLogger().info("config.yml " + (update ? "updated" : "generated") + ", please see plugins/TheEndAgain/config.yml !");
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateNbAliveED(final World w) {
        final EndWorldConfig config = this.mainEndConfig; // Later we will choose the correct config here according to w
        int nbED = 0;
        if (this.mainEndWorld != null) {
            for (final Entity e : this.mainEndWorld.getEntities()) {
                if (e.getType() == EntityType.ENDER_DRAGON && ((EnderDragon) e).getHealth() > 0) {
                    nbED++;
                }
            }
        }
        config.setNbEd(nbED);
    }

    public void broadcastSpawned(final World w) {
        final EndWorldConfig config = this.mainEndConfig; // Later we will choose the correct config here according to w
        for (final String s : config.getRespawnMessages()) {
            this.getServer().broadcastMessage(this.header + ChatColor.GREEN + this.toColor(s));
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
