package me.yamakaja.commanditems.commands;

import me.yamakaja.commanditems.CommandItems;
import me.yamakaja.commanditems.util.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yamakaja on 23.06.17.
 */
public class CommandCMDI implements CommandExecutor, TabCompleter {

    private static String NO_PERM_MESSAGE = ChatColor.RED + "You don't have permission to use this command!";

    private CommandItems plugin;

    public CommandCMDI(CommandItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 0 || args[0].toLowerCase().equalsIgnoreCase("help")) {
            this.sendHelp(commandSender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                processAdd(commandSender, args);
                break;
            case "reload":
                processReload(commandSender);
                break;
            case "msg":
                processRawMsg(commandSender, args);
                break;
            default:
                commandSender.sendMessage(ChatColor.RED + "Unknown subcommand!");
                break;
        }
        return true;
    }

    private void processRawMsg(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commanditems.msg")) {
            commandSender.sendMessage(CommandItems.PREFIX + NO_PERM_MESSAGE);
            return;
        }

        if (args.length < 3) {
            commandSender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Not enough arguments! Syntax: /cmdi msg [target|@a] [message]");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            builder.append(args[i]);

            if (i != args.length - 1)
                builder.append(' ');
        }

        String message = builder.toString();

        if (args[1].equals("@a")) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
            return;
        }

        Player target = plugin.getServer().getPlayerExact(args[1]);

        if (target == null) {
            commandSender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Player \"" + args[1] + "\" not found!");
            return;
        }

        target.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private void processReload(CommandSender commandSender) {
        if (!commandSender.hasPermission("commanditems.reload")) {
            commandSender.sendMessage(CommandItems.PREFIX + NO_PERM_MESSAGE);
            return;
        }

        plugin.getCommandManager().reloadCommands();
        commandSender.sendMessage(CommandItems.PREFIX + ChatColor.GREEN + "Successfully reloaded commands!");
    }

    private void processAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("commanditems.admin")) {
            sender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "You don't have permission to use this command!\n" +
                    CommandItems.PREFIX + ChatColor.RED + "This incident will be reported!"); // ;)

            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Only players may use this command!");
            return;
        }

        if (args.length == 1) {
            sender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "No command-set provided!");
            return;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Please take an item into your main hand!");
            return;
        }

        if (!this.plugin.getCommandManager().getCommands().containsKey(args[1])) {
            player.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Unknown command-set!");
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();

        if (meta == null)
            meta = plugin.getServer().getItemFactory().getItemMeta(itemInHand.getType());

        NMSUtil.setNBTString(meta, "commands", args[1]);

        itemInHand.setItemMeta(meta);

        player.getInventory().setItemInMainHand(itemInHand);
        player.updateInventory();
        player.sendMessage(CommandItems.PREFIX + ChatColor.GOLD + "Successfully applied command-set to item!");
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "List of subcommands:");
        s.sendMessage(" - " + ChatColor.YELLOW + "help     " + ChatColor.AQUA + "Shows this help");
        s.sendMessage(" - " + ChatColor.YELLOW + "add      " + ChatColor.AQUA + "Adds a command-set to an item");
        s.sendMessage(" - " + ChatColor.YELLOW + "reload  " + ChatColor.AQUA + "Reloads configs");
        s.sendMessage(" - " + ChatColor.YELLOW + "msg      " + ChatColor.AQUA + "Sends a raw message to players");
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1)
            return Stream.of("help", "add", "reload", "msg").filter(cmd -> cmd.startsWith(args[0])).collect(Collectors.toList());

        if (args.length == 2 && args[0].equalsIgnoreCase("add"))
            return this.plugin.getCommandManager().getCommands().keySet().stream().filter(key -> key.startsWith(args[1])).collect(Collectors.toList());

        return null;
    }

}
