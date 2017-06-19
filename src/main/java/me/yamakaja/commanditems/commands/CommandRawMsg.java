package me.yamakaja.commanditems.commands;

import me.yamakaja.commanditems.CommandItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Yamakaja on 07.06.17.
 */
public class CommandRawMsg implements CommandExecutor {

    private CommandItems plugin;

    public CommandRawMsg(CommandItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.hasPermission("commanditems.rawmsg")) {
            commandSender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            commandSender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Not enough arguments! Syntax: /rawmsg [target|@a] [message]");
            return true;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]);

            if (i != args.length - 1)
                builder.append(' ');
        }

        String message = builder.toString();

        if (args[0].equals("@a")) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);

        if (target == null) {
            commandSender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Player \"" + args[0] + "\" not found!");
            return true;
        }

        target.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return true;
    }

}
