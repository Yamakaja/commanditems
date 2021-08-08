package me.yamakaja.commanditems.data.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.yamakaja.commanditems.data.ItemDefinition;
import me.yamakaja.commanditems.interpreter.InterpretationContext;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Created by Yamakaja on 27.05.18.
 */
public class ActionIterate extends Action {

    @JsonProperty
    private String perm;

    @JsonProperty(required = true)
    private Action[] actions;

    @Override
    public void init() {
        for (Action action : this.actions) action.init();
    }

    @JsonProperty(value = "what")
    private IterationTarget target;

    protected ActionIterate() {
        super(ActionType.ITER);
    }

    @Override
    public void trace(List<ItemDefinition.ExecutionTrace> trace, int depth) {
        String line;
        if (perm == null)
            line = "for (all online players)";
        else
            line = String.format("for (all online players with permission %s)", perm);

        trace.add(new ItemDefinition.ExecutionTrace(depth, line));

        for (Action action : this.actions) action.trace(trace, depth + 1);
    }

    @Override
    public void process(InterpretationContext context) {
        context.pushFrame();
        target.process(this, context);
        context.popFrame();
    }

    public enum IterationTarget {
        ONLINE_PLAYERS() {
            @Override
            public void process(ActionIterate action, InterpretationContext context) {
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> action.perm == null || player.hasPermission(action.perm))
                        .forEach(player -> {
                            context.pushLocal("iter_locX", String.valueOf(player.getLocation().getBlockX()));
                            context.pushLocal("iter_locY", String.valueOf(player.getLocation().getBlockY()));
                            context.pushLocal("iter_locZ", String.valueOf(player.getLocation().getBlockZ()));

                            context.pushLocal("iter_name", player.getName());
                            context.pushLocal("iter_displayname", player.getDisplayName());
                            context.pushLocal("iter_uuid", player.getUniqueId().toString());

                            context.pushLocal("iter_health", String.valueOf((int) player.getHealth()));
                            context.pushLocal("iter_level", String.valueOf(player.getLevel()));
                            context.pushLocal("iter_food", String.valueOf(player.getFoodLevel()));

                            for (Action subAction : action.actions)
                                subAction.process(context);
                        });
            }

        };

        public abstract void process(ActionIterate action, InterpretationContext context);
    }

}
