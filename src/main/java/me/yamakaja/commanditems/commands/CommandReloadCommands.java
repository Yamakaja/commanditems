package me.yamakaja.commanditems.commands;

import me.yamakaja.commanditems.CommandItems;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Yamakaja on 19.06.17.
 */
public class CommandReloadCommands implements CommandExecutor {

    private CommandItems plugin;

    public CommandReloadCommands(CommandItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("commanditems.admin")) {
            commandSender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        plugin.getCommandManager().reloadCommands();

        commandSender.sendMessage(CommandItems.PREFIX + ChatColor.GREEN + "Successfully reloaded commands!");
        return true;
    }

}
