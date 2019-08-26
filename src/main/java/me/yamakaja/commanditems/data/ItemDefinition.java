package me.yamakaja.commanditems.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.yamakaja.commanditems.data.action.Action;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ItemDefinition {

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

    @JsonProperty
private String alreadyUsed;

public String getAlreadyUsed() {
return alreadyUsed;
}
    
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

}
