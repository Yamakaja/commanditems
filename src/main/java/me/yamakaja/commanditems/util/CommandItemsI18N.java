package me.yamakaja.commanditems.util;

import me.yamakaja.commanditems.CommandItems;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public final class CommandItemsI18N {

    public enum MsgKey {
        // Keys with default message
        ITEM_DISABLED("&cThis item has been disabled!"),
        ITEM_NOPERMISSION("&cYou don't have permission to use this item!"),
        ITEM_COOLDOWN("&cYou can only use this item once every $TIME_PERIOD!")
        ;

        private final String defaultMessage;

        MsgKey(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        private String getDefaultMessage() {
            return this.defaultMessage;
        }

        private String getKey() {
            return this.name().toLowerCase().replace('_', '.');
        }

        public String get() {
            return CommandItemsI18N.get(this);
        }

        public String get(Map<String, String> params) {
            return CommandItemsI18N.get(this, params);
        }
    }

    private YamlConfiguration messagesConfig;
    private final CommandItems plugin;

    private CommandItemsI18N(CommandItems plugin) {
        this.plugin = plugin;

        this.reload();
    }

    public void reload() {
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    private static CommandItemsI18N instance;

    public static void initialize(CommandItems plugin) {
        instance = new CommandItemsI18N(plugin);
    }

    public static CommandItemsI18N getInstance() {
        return instance;
    }

    public static String get(MsgKey key) {
        return get(key, Collections.emptyMap());
    }

    public static String get(MsgKey key, Map<String, String> params) {
        return instance.getMessage(key, params);
    }

    public String getMessage(MsgKey key, Map<String, String> params) {
        String msg = this.messagesConfig.getString(key.getKey());

        if (msg == null)
            msg = key.getDefaultMessage();

        for (Map.Entry<String, String> entry : params.entrySet())
            msg = msg.replace("$" + entry.getKey(), entry.getValue());

        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
