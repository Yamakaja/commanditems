package me.yamakaja.commanditems.interpreter;

import me.yamakaja.commanditems.CommandItems;
import me.yamakaja.commanditems.data.action.Action;
import me.yamakaja.commanditems.data.ItemDefinition;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ItemExecutor {

    private CommandItems plugin;

    public ItemExecutor(CommandItems plugin) {
        this.plugin = plugin;
    }

    public void processInteraction(Player player, ItemDefinition definition, Map<String, String> params) {
        InterpretationContext context = new InterpretationContext(this.plugin, player);

        context.pushFrame();
        context.pushLocal("player", player.getName());
        context.pushLocal("uuid", player.getUniqueId().toString());
        context.pushLocal("x", String.valueOf(player.getLocation().getX()));
        context.pushLocal("y", String.valueOf(player.getLocation().getY()));
        context.pushLocal("z", String.valueOf(player.getLocation().getZ()));
        context.pushLocal("yaw", String.valueOf(player.getLocation().getYaw()));
        context.pushLocal("pitch", String.valueOf(player.getLocation().getPitch()));
        context.pushLocal("food", String.valueOf(player.getFoodLevel()));
        context.pushLocal("health", String.valueOf(player.getHealth()));

        for (Map.Entry<String, String> entry : definition.getParameters().entrySet())
            context.pushLocal(entry.getKey(), params.getOrDefault(entry.getKey(), entry.getValue()));

        for (Action action : definition.getActions())
            action.process(context);

        context.popFrame();
    }

}
