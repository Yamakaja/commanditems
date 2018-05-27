package me.yamakaja.commanditems.interpreter;

import me.yamakaja.commanditems.CommandItems;
import me.yamakaja.commanditems.data.action.Action;
import me.yamakaja.commanditems.data.ItemDefinition;
import org.bukkit.entity.Player;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ItemExecutor {

    private CommandItems plugin;

    public ItemExecutor(CommandItems plugin) {
        this.plugin = plugin;
    }

    public void processInteraction(Player player, ItemDefinition definition) {
        InterpretationContext context = new InterpretationContext(this.plugin, player);

        context.pushFrame();
        context.pushLocal("player", player.getName());
        context.pushLocal("uuid", player.getUniqueId().toString());

        for (Action action : definition.getActions())
            action.process(context);

        context.popFrame();
    }

}
