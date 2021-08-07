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
    public void init() {
        if (counterVar.isEmpty())
            throw new RuntimeException("Empty counter variable name in REPEAT!");

        if (period < 0)
            throw new RuntimeException("Negative period in REPEAT!");

        if (delay < 0)
            throw new RuntimeException("Negative delay in REPEAT!");

        if (increment == 0)
            throw new RuntimeException("Increment is 0, infinite loops are not supported by REPEAT!");

        if (Math.signum((double) to - from) * increment < 0)
            throw new RuntimeException("Increment is of the wrong sign in REPEAT!");

        for (Action action : this.actions) action.init();
    }

    @Override
    public void process(InterpretationContext context) {
        context.pushFrame();
        if (delay == 0 && period == 0) {
            for (int i = from; increment > 0 && i > to || increment < 0 && i < to; i+= increment) {
                context.pushLocal(this.counterVar, String.valueOf(i));
                for (Action action : this.actions) action.process(context);
            }

            context.popFrame();
            return;
        }

        InterpretationContext clone = context.copy();
        new BukkitRunnable() {

            private int i = from;

            @Override
            public void run() {
                if (increment > 0 && i > to || increment < 0 && i < to) {
                    this.cancel();
                    clone.popFrame();
                    clone.release();
                    return;
                }


                clone.pushLocal(counterVar, String.valueOf(i));

                for (Action action : actions) action.process(clone);

                i += increment;
            }

        }.runTaskTimer(context.getPlugin(), this.delay, this.period);
    }

}
