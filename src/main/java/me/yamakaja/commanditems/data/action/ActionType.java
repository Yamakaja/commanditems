package me.yamakaja.commanditems.data.action;

/**
 * Created by Yamakaja on 26.05.18.
 */
public enum ActionType {

    COMMAND(ActionCommand.class),
    MESSAGE(ActionMessage.class),
    REPEAT(ActionRepeat.class),
    WAIT(ActionWait.class),
    ITER(ActionIterate.class),
    CALC(ActionCalc.class);

    private Class<? extends Action> actionClass;

    ActionType(Class<? extends Action> actionClass) {
        this.actionClass = actionClass;
    }

    public Class<? extends Action> getActionClass() {
        return this.actionClass;
    }
}
