package me.yamakaja.commanditems.data.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.yamakaja.commanditems.interpreter.InterpretationContext;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionAttachment;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ActionCommand extends Action {

    @JsonProperty(value = "by")
    private CommandMode commandMode = CommandMode.PLAYER;

    @JsonProperty(required = true)
    private String command;

    @JsonProperty(value = "perm")
    private String providedPermission = "*";

    public ActionCommand() {
        super(ActionType.COMMAND);
    }

    @Override
    public void process(InterpretationContext context) {
        String command = context.resolveLocalsInString(this.command);

        switch (this.commandMode) {
            case PLAYER:
                Bukkit.getServer().dispatchCommand(context.getPlayer(), command);
                break;

            case CONSOLE:
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
                break;

            case PLAYER_PRIVILEGED:
                PermissionAttachment attachment = null;

                try {
                    (attachment = context.getPlayer().addAttachment(context.getPlugin())).setPermission(providedPermission, true);
                    Bukkit.getServer().dispatchCommand(context.getPlayer(), command);
                } finally {
                    if (attachment != null)
                        context.getPlayer().removeAttachment(attachment);
                }

                break;
        }
    }


    public enum CommandMode {

        PLAYER,
        CONSOLE,
        PLAYER_PRIVILEGED

    }

}
