package me.yamakaja.commanditems;

import me.yamakaja.commanditems.commands.CommandAddCommand;
import me.yamakaja.commanditems.commands.CommandRawMsg;
import me.yamakaja.commanditems.commands.CommandReloadCommands;
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
        CommandAddCommand addCommand = new CommandAddCommand(this);

        PluginCommand bukkitAddCommand = this.getCommand("addcommand");
        bukkitAddCommand.setExecutor(addCommand);
        bukkitAddCommand.setTabCompleter(addCommand);

        this.getCommand("rawmsg").setExecutor(new CommandRawMsg(this));
        this.getCommand("cmireload").setExecutor(new CommandReloadCommands(this));

        this.saveDefaultConfig();


        this.commandManager = new CommandManager(this);
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

}
