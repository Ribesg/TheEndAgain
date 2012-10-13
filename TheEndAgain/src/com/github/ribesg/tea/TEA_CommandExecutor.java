package com.github.ribesg.tea;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.ribesg.tea.util.ExtendedChunk;


public class TEA_CommandExecutor implements CommandExecutor {

    private final TheEndAgain plugin;

    public TEA_CommandExecutor(final TheEndAgain instance) {
        // Link the main plugin and his CommandExecutor
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        // This method handle commands
        if (commandLabel.equalsIgnoreCase("end")) {
            if (args.length == 0 || args.length == 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))) {
                if (sender.hasPermission("tea.end")) {
                    return this.cmdHelp(sender);
                } else {
                    this.denied(sender);
                    return true;
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("regen")) {
                    if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.regen")) {
                        return this.cmdRegen(sender);
                    } else {
                        this.denied(sender);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("respawnEnderDragon") || args[0].equalsIgnoreCase("respawnED") || args[0].equalsIgnoreCase("respawn")) {
                    if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.respawn")) {
                        return this.cmdRespawn(sender);
                    } else {
                        this.denied(sender);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("nbEnderDragon") || args[0].equalsIgnoreCase("nbED") || args[0].equalsIgnoreCase("nb")) {
                    if (sender.hasPermission("tea.user") || sender.hasPermission("tea.user.nb")) {
                        return this.cmdNb(sender);
                    } else {
                        this.denied(sender);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("chunk")) {
                    if (sender.hasPermission("tea.user") || sender.hasPermission("tea.user.chunkinfo")) {
                        return this.cmdChunkInfo(sender);
                    } else {
                        this.denied(sender);
                        return true;
                    }
                } else {
                    return false;
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("chunk")) {
                    if (args[1].equalsIgnoreCase("protect")) {
                        if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.chunkprotect")) {
                            return this.cmdChunkProtect(sender);
                        } else {
                            this.denied(sender);
                            return true;
                        }
                    } else if (args[1].equalsIgnoreCase("unprotect")) {
                        if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.chunkunprotect")) {
                            return this.cmdChunkUnProtect(sender);
                        } else {
                            this.denied(sender);
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void denied(final CommandSender sender) {
        // Unfortunately, the sender does not have the permission to use that
        // command...
        sender.sendMessage(this.plugin.header + ChatColor.RED + "Sorry but you do not have the permission to use that command.");
    }

    private boolean cmdHelp(final CommandSender sender) {
        final String helpHelp = "/end help - Access to this informations";
        final String regenHelp = "/end regen - Regen the End world";
        final String respawnHelp = "/end respawn - Respawn EDs";
        final String nbHelp = "/end nb - Returns actual EDs alive number";
        final String chunkHelp = "/end chunk - Returns info about the chunk you are in";
        final String chunkProtectHelp = "/end chunk protect - Protect the current chunk";
        final String chunkUnProtectHelp = "/end chunk unprotect - Unprotect the current chunk";
        sender.sendMessage(this.plugin.header + ChatColor.GREEN + "TheEndAgain plugin help : ");
        sender.sendMessage(this.plugin.header + helpHelp);
        if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.regen") || sender.isOp()) {
            sender.sendMessage(this.plugin.header + regenHelp);
        }
        if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.respawn") || sender.isOp()) {
            sender.sendMessage(this.plugin.header + respawnHelp);
        }
        if (sender.hasPermission("tea.user") || sender.hasPermission("tea.user.nb") || sender.isOp()) {
            sender.sendMessage(this.plugin.header + nbHelp);
        }
        if (sender.hasPermission("tea.user") || sender.hasPermission("tea.user.chunk") || sender.isOp()) {
            sender.sendMessage(this.plugin.header + chunkHelp);
        }
        if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.chunk.protect") || sender.isOp()) {
            sender.sendMessage(this.plugin.header + chunkProtectHelp);
        }
        if (sender.hasPermission("tea.admin") || sender.hasPermission("tea.admin.chunk.unprotect") || sender.isOp()) {
            sender.sendMessage(this.plugin.header + chunkUnProtectHelp);
        }
        return true;
    }

    private boolean cmdRegen(final CommandSender sender) {
        // TODO Ability to choose which endWorld
        if (this.plugin.mainEndWorld != null) {
            this.plugin.softRegen(this.plugin.mainEndWorld);
            return true;
        } else {
            sender.sendMessage(this.plugin.header + ChatColor.RED + "There is no End world !");
            return true;
        }
    }

    private boolean cmdRespawn(final CommandSender sender) {
        // TODO Ability to choose which endWorld
        // The command is like /respawnenderdragon or /respawnED
        if (this.plugin.mainEndWorld != null) {
            final int nb = this.plugin.spawnEnderDragonsToActualNumber(this.plugin.mainEndWorld);
            if (nb == 0) {
                sender.sendMessage(this.plugin.header + ChatColor.RED + "Reached the actual maximum number of EnderDragons !");
                return true;
            } else if (nb == 1) {
                sender.sendMessage(this.plugin.header + ChatColor.GREEN + "Spawned 1 EnderDragon !");
                return true;
            } else {
                sender.sendMessage(this.plugin.header + ChatColor.GREEN + "Spawned " + nb + " EnderDragons !");
                return true;
            }
        } else {
            sender.sendMessage(this.plugin.header + ChatColor.RED + "There is no End world !");
            return true;
        }
    }

    private boolean cmdNb(final CommandSender sender) {
        // TODO Ability to choose which endWorld
        if (this.plugin.mainEndWorld != null) {
            final int nb = this.plugin.mainEndConfig.getNbEd();
            if (nb == 0) {
                sender.sendMessage(this.plugin.header + ChatColor.GREEN + "There is no ED alive. Actual max number is " + this.plugin.mainEndConfig.getActualNbMaxEnderDragon());
                return true;
            } else if (nb == 1) {
                sender.sendMessage(this.plugin.header + ChatColor.GREEN + "There is 1 ED alive. Actual max number is " + this.plugin.mainEndConfig.getActualNbMaxEnderDragon());
                return true;
            } else {
                sender.sendMessage(this.plugin.header + ChatColor.GREEN + "There are " + nb + " EDs alive. Actual max number is " + this.plugin.mainEndConfig.getActualNbMaxEnderDragon());
                return true;
            }
        } else {
            sender.sendMessage(this.plugin.header + ChatColor.RED + "There is no End world !");
            return true;
        }
    }

    private boolean cmdChunkInfo(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.header + ChatColor.RED + "Command only available to In Game Players !");
            return true;
        } else {
            final Location loc = ((Player) sender).getLocation();
            final World world = loc.getWorld();
            final Chunk chunk = loc.getChunk();
            sender.sendMessage(this.plugin.header + ChatColor.GREEN + "Informations about the chunk you are in : ");
            sender.sendMessage(this.plugin.header + "Chunk coords : " + chunk.getX() + "," + chunk.getZ());
            sender.sendMessage(this.plugin.header + "Min/max locations : " + 16 * chunk.getX() + "," + 16 * chunk.getZ() + " / " + (16 * chunk.getX() + (chunk.getX() >= 0 ? 15 : -15)) + ","
                    + (16 * chunk.getZ() + (chunk.getZ() >= 0 ? 15 : -15)));
            if (this.plugin.mainEndWorld == world) {
                final ExtendedChunk c = this.plugin.endChunks.getChunk(chunk);
                if (c == null) {
                    sender.sendMessage(this.plugin.header + ChatColor.GOLD + "Unknown state End World chunk");
                } else if (c.isProtected()) {
                    sender.sendMessage(this.plugin.header + ChatColor.GREEN + "Protected End World chunk");
                } else {
                    sender.sendMessage(this.plugin.header + ChatColor.RED + "Unprotected state End World chunk");
                }
            } else {
                sender.sendMessage(this.plugin.header + ChatColor.RED + "Not an End World chunk !");
            }
            return true;
        }
    }

    private boolean cmdChunkProtect(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.header + ChatColor.RED + "Command only available to In Game Players !");
            return true;
        } else {
            final Location loc = ((Player) sender).getLocation();
            final World world = loc.getWorld();
            final Chunk chunk = loc.getChunk();
            if (this.plugin.mainEndWorld == world) {
                final ExtendedChunk c = this.plugin.endChunks.getChunk(chunk);
                if (c == null) {
                    sender.sendMessage(this.plugin.header + ChatColor.GOLD + "Unknown state End World chunk, please try again after server reboot");
                } else if (c.isProtected()) {
                    sender.sendMessage(this.plugin.header + ChatColor.RED + "This chunk is already protected !");
                } else {
                    sender.sendMessage(this.plugin.header + ChatColor.GREEN + "This chunk is now protected !");
                    c.setProtected(true);
                }
            } else {
                sender.sendMessage(this.plugin.header + ChatColor.RED + "Not an End World chunk !");
            }
            return true;
        }
    }

    private boolean cmdChunkUnProtect(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.header + ChatColor.RED + "Command only available to In Game Players !");
            return true;
        } else {
            final Location loc = ((Player) sender).getLocation();
            final World world = loc.getWorld();
            final Chunk chunk = loc.getChunk();
            if (this.plugin.mainEndWorld == world) {
                final ExtendedChunk c = this.plugin.endChunks.getChunk(chunk);
                if (c == null) {
                    sender.sendMessage(this.plugin.header + ChatColor.GOLD + "Unknown state End World chunk, please try again after server reboot");
                } else if (c.isProtected()) {
                    sender.sendMessage(this.plugin.header + ChatColor.GREEN + "This chunk is no longer protected !");
                    c.setProtected(false);
                } else {
                    sender.sendMessage(this.plugin.header + ChatColor.RED + "This chunk is not protected !");
                }
            } else {
                sender.sendMessage(this.plugin.header + ChatColor.RED + "Not an End World chunk !");
            }
            return true;
        }
    }
}
