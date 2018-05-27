package me.yamakaja.commanditems.data.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.yamakaja.commanditems.interpreter.InterpretationContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class ActionMessage extends Action {

    private MessageTarget target;
    private String message;
    private String permission;

    public ActionMessage(@JsonProperty("action") ActionType type, @JsonProperty("to") MessageTarget target, @JsonProperty(value = "message", required = true) String message, @JsonProperty("perm") String permission) {
        super(type);
        this.target = target;

        if (target == null)
            this.target = MessageTarget.PLAYER;

        this.message = ChatColor.translateAlternateColorCodes('&', message);
        this.permission = permission;
    }

    @Override
    public void process(InterpretationContext context) {
        String message = context.resolveLocalsInString(this.message);
        switch (this.target) {
            case PLAYER:
                context.getPlayer().sendMessage(message);
                break;

            case CONSOLE:
                context.getPlugin().getLogger().info("[MSG] " + message);
                break;

            case EVERYBODY:
                Bukkit.getServer().broadcastMessage(message);
                break;

            case PERMISSION:
                if (permission == null)
                    throw new RuntimeException("[CMDI] Permission is null in permission mode!");

                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.hasPermission(this.permission))
                        .forEach(player -> player.sendMessage(message));
                break;
        }
    }

    /**
     * Created by Yamakaja on 26.05.18.
     */
    public enum MessageTarget {

        PLAYER,
        CONSOLE,
        EVERYBODY,
        PERMISSION

    }
}
