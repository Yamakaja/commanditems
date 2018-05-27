package me.yamakaja.commanditems.data.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.yamakaja.commanditems.interpreter.InterpretationContext;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ActionWait extends Action {

    @JsonProperty
    private int duration = 20;

    @JsonProperty(required = true)
    private Action[] actions;

    protected ActionWait() {
        super(ActionType.WAIT);
    }

    @Override
    public void process(InterpretationContext context) {
        InterpretationContext contextClone = context.copy();
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Action action : actions)
                    action.process(contextClone);
            }

        }.runTaskLater(context.getPlugin(), duration);
    }

}
