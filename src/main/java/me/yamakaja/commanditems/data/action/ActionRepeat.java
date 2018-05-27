package me.yamakaja.commanditems.data.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.yamakaja.commanditems.interpreter.InterpretationContext;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ActionRepeat extends Action {

    protected ActionRepeat() {
        super(ActionType.REPEAT);
    }

    @JsonProperty
    private int period = 20;

    @JsonProperty
    private int delay = 20;

    @JsonProperty
    private int from = 0;

    @JsonProperty
    private int to = 9;

    @JsonProperty
    private int increment = 1;

    @JsonProperty
    private String counterVar = "i";

    @JsonProperty(required = true)
    private Action[] actions;

    @Override
    public void process(InterpretationContext context) {
        InterpretationContext clone = context.copy();
        new BukkitRunnable() {

            private int i = from;

            @Override
            public void run() {
                if (increment > 0 && i > to || increment < 0 && i < to) {
                    this.cancel();
                    return;
                }

                clone.pushFrame();
                clone.pushLocal(counterVar, String.valueOf(i));

                for (Action action : actions)
                    action.process(clone);

                clone.popFrame();

                i += increment;
            }

        }.runTaskTimer(context.getPlugin(), this.delay, this.period);
    }

}
