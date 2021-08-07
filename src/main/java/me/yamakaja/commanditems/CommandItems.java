package me.yamakaja.commanditems;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import me.yamakaja.commanditems.data.ItemDefinition;
import me.yamakaja.commanditems.interpreter.ItemExecutor;
import me.yamakaja.commanditems.parser.ConfigManager;
import me.yamakaja.commanditems.util.CommandItemsI18N;
import me.yamakaja.commanditems.util.EnchantmentGlow;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

/**
 * Created by Yamakaja on 07.06.17.
 */
public class CommandItems extends JavaPlugin {

    private ConfigManager configManager;
    private BukkitCommandManager commandManager;
    private ItemExecutor executor;
    private CommandItemManager commandItemManager;

    @Override
    public void onEnable() {
        new Metrics(this);

        boolean debug = System.getProperty("me.yamakaja.debug") != null;
        this.saveResource("config.yml", debug);
        this.saveResource("messages.yml", debug);

        CommandItemsI18N.initialize(this);

        this.configManager = new ConfigManager(this);
        configManager.parse();

        this.commandManager = new PaperCommandManager(this);

        this.commandManager.getCommandContexts().registerContext(ItemDefinition.class, context -> {
            ItemDefinition itemDef = this.configManager.getConfig().getItems().get(context.popFirstArg());

            if (itemDef == null)
                throw new InvalidCommandArgument("Unknown item definition!");

            return itemDef;
        });

        this.commandManager.getCommandCompletions().registerCompletion("itemdefs", context -> this.configManager.getConfig().getItems().keySet().stream()
                .filter(key -> key.toLowerCase().startsWith(context.getInput().toLowerCase()))
                .collect(Collectors.toList()));

        this.commandManager.registerCommand(new CommandCMDI(this));
        this.commandManager.enableUnstableAPI("help");

        this.executor = new ItemExecutor(this);
        this.commandItemManager = new CommandItemManager(this);

        EnchantmentGlow.getGlow();
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public ItemExecutor getExecutor() {
        return executor;
    }

    public CommandItemManager getCommandItemManager() {
        return commandItemManager;
    }

}
