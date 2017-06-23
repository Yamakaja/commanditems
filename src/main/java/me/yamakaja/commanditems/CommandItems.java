package me.yamakaja.commanditems;

import me.yamakaja.commanditems.commands.CommandCMDI;
import org.bstats.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Yamakaja on 07.06.17.
 */
public class CommandItems extends JavaPlugin {

    public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "CommandItems" + ChatColor.DARK_GRAY + "] ";
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this);

        CommandCMDI addCommand = new CommandCMDI(this);
        PluginCommand bukkitAddCommand = this.getCommand("cmdi");

        bukkitAddCommand.setExecutor(addCommand);
        bukkitAddCommand.setTabCompleter(addCommand);

        this.saveDefaultConfig();
        this.commandManager = new CommandManager(this);
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

}
