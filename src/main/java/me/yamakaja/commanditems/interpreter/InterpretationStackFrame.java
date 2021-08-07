package me.yamakaja.commanditems.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class InterpretationStackFrame {

    private final Map<String, String> locals = new HashMap<>();

    public String getLocal(String key) {
        return this.locals.get(key);
    }

    public void pushLocal(String key, String value) {
        this.locals.put(key, value);
    }

    public void reset() {
        this.locals.clear();
    }

    public Map<String, String> getLocals() {
        return locals;
    }

    public InterpretationStackFrame copy(InterpretationStackFrame into) {
        into.locals.putAll(locals);
        return into;
    }
}
