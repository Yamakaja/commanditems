package me.yamakaja.commanditems.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import me.yamakaja.commanditems.CommandItems;
import me.yamakaja.commanditems.data.action.Action;
import me.yamakaja.commanditems.data.action.ActionType;

import java.io.IOException;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ActionDeserializer extends StdDeserializer<Action> {

    private CommandItems plugin;

    public ActionDeserializer(CommandItems plugin) {
        super(Action.class);
        this.plugin = plugin;
    }

    @Override
    public Action deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JsonProcessingException {
        ActionType type;

        if (!p.nextFieldName().equals("action"))
            throw new RuntimeException("Parsing config: The first field in an action needs to be the type field!");

        try {
            type = ActionType.valueOf(p.nextTextValue());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Parsing config: Unknown action '" + p.getValueAsString() + "'!");
        }

        return ctx.readValue(p, type.getActionClass());
    }

}
