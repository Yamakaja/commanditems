package me.yamakaja.commanditems.parser;

import me.yamakaja.commanditems.CommandItems;
import me.yamakaja.commanditems.data.action.Action;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ActionConstructor extends Constructor {

    private CommandItems plugin;

    public ActionConstructor(CommandItems plugin) {
        super(Action.class);
        this.plugin = plugin;
    }



}
