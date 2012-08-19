package com.github.ribesg.tea;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TEA_CommandExecutor implements CommandExecutor {

    private final TheEndAgain plugin;

    public TEA_CommandExecutor(final TheEndAgain instance) {
        // Link the main plugin and his CommandExecutor
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        // This method handle commands
        if (commandLabel.equals("respawnenderdragon") || commandLabel.equalsIgnoreCase("respawnED")) {
            if (sender.hasPermission("tea.respawn") || sender.isOp()) {
                return this.cmdRespawn(sender, args);
            } else {
                this.denied(sender);
                return false;
            }
        } else if (commandLabel.equals("nbenderdragon") || commandLabel.equalsIgnoreCase("nbED")) {
            if (sender.hasPermission("tea.nb") || sender.isOp()) {
                return this.cmdNb(sender, args);
            } else {
                this.denied(sender);
                return false;
            }
        } else if (commandLabel.equals("regenend") || commandLabel.equalsIgnoreCase("regend")) {
            if (sender.hasPermission("tea.regen") || sender.isOp()) {
                return this.cmdRegen(sender, args);
            } else {
                this.denied(sender);
                return false;
            }
        } else {
            return false;
        }
    }

    private void denied(final CommandSender sender) {
        // Unfortunately, the sender does not have the permission to use that
        // command...
        sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.RED + "Sorry but you do not have the permission to use that command.");
    }

    private boolean cmdRespawn(final CommandSender sender, final String[] args) {
        // The command is like /respawnenderdragon or /respawnED
        if (args.length != 0) {
            return false; // This output the command 'usage' to the sender
        } else {
            if (this.plugin.endWorld != null) {
                final int nb = this.plugin.spawnEnderDragonsToActualNumber();
                if (nb == 0) {
                    sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.RED + "Reached the actual maximum number of EnderDragons !");
                    return true;
                } else if (nb == 1) {
                    sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.GREEN + "Spawned 1 EnderDragon !");
                    return true;
                } else {
                    sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.GREEN + "Spawned " + nb + " EnderDragons !");
                    return true;
                }
            } else {
                sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.RED + "There is no End world !");
                return true;
            }
        }
    }

    private boolean cmdNb(final CommandSender sender, final String[] args) {
        if (this.plugin.endWorld != null) {
            final int nb = this.plugin.nbED;
            if (nb == 0) {
                sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.GREEN + "There is no ED alive. Actual max number is " + this.plugin.actualNbEnderDragon);
                return true;
            } else if (nb == 1) {
                sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.GREEN + "There is 1 ED alive. Actual max number is " + this.plugin.actualNbEnderDragon);
                return true;
            } else {
                sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.GREEN + "There are " + nb + " EDs alive. Actual max number is " + this.plugin.actualNbEnderDragon);
                return true;
            }
        } else {
            sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.RED + "There is no End world !");
            return true;
        }
    }

    private boolean cmdRegen(final CommandSender sender, final String[] args) {
        if (this.plugin.endWorld != null) {
            this.plugin.softRegen();
            return true;
        } else {
            sender.sendMessage(this.plugin.messagesPrefix ? this.plugin.header : "" + ChatColor.RED + "There is no End world !");
            return true;
        }
    }
}
