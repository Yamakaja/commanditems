package me.yamakaja.commanditems.data.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import me.yamakaja.commanditems.interpreter.InterpretationContext;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "action",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionCommand.class, name = "COMMAND"),
        @JsonSubTypes.Type(value = ActionMessage.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = ActionRepeat.class, name = "REPEAT"),
        @JsonSubTypes.Type(value = ActionWait.class, name = "WAIT"),
        @JsonSubTypes.Type(value = ActionIterate.class, name = "ITER"),
        @JsonSubTypes.Type(value = ActionCalc.class, name = "CALC"),
        @JsonSubTypes.Type(value = ActionMathExpr.class, name = "MATH_EXPR")
})
public abstract class Action {

    @JsonProperty("action")
    private ActionType type;

    protected Action(ActionType type) {
        this.type = type;
    }

    public ActionType getType() {
        return type;
    }

    public abstract void process(InterpretationContext context);

    public void init() {
    }

}
