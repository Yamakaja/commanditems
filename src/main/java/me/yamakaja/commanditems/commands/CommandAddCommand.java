package me.yamakaja.commanditems.commands;

import me.yamakaja.commanditems.CommandItems;
import me.yamakaja.commanditems.util.NMSUtil;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Yamakaja on 07.06.17.
 */
public class CommandAddCommand implements CommandExecutor, TabCompleter {

    private CommandItems plugin;

    public CommandAddCommand(CommandItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("commanditems.admin")) {
            sender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "You don't have permission to use this command!\n" +
                    CommandItems.PREFIX + ChatColor.RED + "This incident will be reported!"); // ;)

            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Only players may use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(CommandItems.PREFIX + ChatColor.RED + "No command provided!");
            return true;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Please take an item into your main hand!");
            return true;
        }

        if (!this.plugin.getCommandManager().getCommands().containsKey(args[0])) {
            player.sendMessage(CommandItems.PREFIX + ChatColor.RED + "Unknown command set!");
            return true;
        }

        ItemMeta meta = itemInHand.getItemMeta();

        if (meta == null)
            meta = plugin.getServer().getItemFactory().getItemMeta(itemInHand.getType());

        NMSUtil.setNBTString(meta, "commands", args[0]);

        itemInHand.setItemMeta(meta);

        player.getInventory().setItemInMainHand(itemInHand);
        player.updateInventory();
        player.sendMessage(CommandItems.PREFIX + ChatColor.GOLD + "Successfully applied command set to item!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0)
            return null;

        return plugin.getCommandManager().getCommands().keySet().stream().filter(key -> key.startsWith(args[0]))
                .collect(Collectors.toList());
    }

}
