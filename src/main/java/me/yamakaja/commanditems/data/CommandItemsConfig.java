package me.yamakaja.commanditems.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class CommandItemsConfig {

    @JsonProperty("items")
    private Map<String, ItemDefinition> items;

    public Map<String, ItemDefinition> getItems() {
        return this.items;
    }

}
