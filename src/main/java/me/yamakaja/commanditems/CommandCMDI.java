package me.yamakaja.commanditems;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import me.yamakaja.commanditems.data.ItemDefinition;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class CommandCMDI extends BaseCommand {

    private CommandItems plugin;

    public CommandCMDI(CommandItems plugin) {
        super("cmdi");
        this.plugin = plugin;
    }

    @Default
    public void onDefault(CommandSender issuer) {
        issuer.sendMessage(ChatColor.AQUA + "Running " + ChatColor.GOLD + "CommandItems v" + this.plugin.getDescription().getVersion()
                + ChatColor.AQUA + " by " + ChatColor.GOLD + "Yamakaja" + ChatColor.AQUA + "!");
        issuer.sendMessage(ChatColor.AQUA + "See " + ChatColor.GOLD + "/cmdi help" + ChatColor.AQUA + " for more information!");
    }

    @CommandPermission("cmdi.help")
    @Syntax("[page]")
    @HelpCommand
    public void onHelp(CommandSender issuer, @Default("1") Integer page) {
        CommandHelp commandHelp = this.getCommandHelp();
        commandHelp.setPage(page);
        commandHelp.showHelp();
    }

    @Subcommand("give")
    @CommandPermission("cmdi.give")
    @Syntax("<player> <item> [amount]")
    @CommandCompletion("@players @itemdefs")
    public void onGive(CommandSender issuer, OnlinePlayer player, ItemDefinition definition, @Default("1") Integer amount) {
        ItemStack item = definition.getItem();
        item.setAmount(amount);
        Map<Integer, ItemStack> leftovers = player.player.getInventory().addItem(item);

        for (ItemStack itemStack : leftovers.values())
            player.getPlayer().getWorld().dropItem(player.getPlayer().getLocation(), itemStack);

        issuer.sendMessage(ChatColor.GREEN + "Successfully gave " + player.player.getName() + " " + amount + " " + "command items!");
    }

    @Subcommand("reload")
    @CommandPermission("cmdi.reload")
    public void onReload(CommandSender sender) {
        try {
            this.plugin.getConfigManager().parse();
            sender.sendMessage(ChatColor.GREEN + "Successfully reloaded config!");
        } catch (RuntimeException e) {
            sender.sendMessage(ChatColor.RED + "Failed to read the configuration:");
            sender.sendMessage(ChatColor.RED + e.getCause().getMessage());
        }
    }

}
