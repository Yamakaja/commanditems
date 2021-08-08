package me.yamakaja.commanditems.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import me.yamakaja.commanditems.data.action.Action;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ItemDefinition {

    public static class ExecutionTrace {
        public final int depth;
        public final String label;

        public ExecutionTrace(int depth, String label) {
            this.depth = depth;
            this.label = label;
        }
    }

    @JsonProperty
    private boolean consumed;

    @JsonProperty
    private long cooldown;

    @JsonProperty
    private ItemStack item;

    @JsonProperty
    private Action[] actions;

    @JsonProperty
    private boolean sneaking;

    public boolean isConsumed() {
        return this.consumed;
    }

    public long getCooldown() {
        return this.cooldown;
    }

    public ItemStack getItem() {
        return this.item.clone();
    }

    public Action[] getActions() {
        return this.actions;
    }

    public boolean isSneaking() {
        return this.sneaking;
    }

    public List<ExecutionTrace> getExecutionTrace() {
        List<ExecutionTrace> trace = new ArrayList<>();

        for (Action action : this.actions) action.trace(trace, 0);

        return trace;
    }

}
