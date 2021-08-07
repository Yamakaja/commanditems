package me.yamakaja.commanditems;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.yamakaja.commanditems.data.ItemDefinition;
import me.yamakaja.commanditems.util.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

import me.yamakaja.commanditems.util.CommandItemsI18N.MsgKey;

public class CommandItemManager implements Listener {

    private CommandItems plugin;

    private Table<UUID, String, Long> lastUse = HashBasedTable.create();

    public CommandItemManager(CommandItems plugin) {
        this.plugin = plugin;

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    private static String getTimeString(long duration) {
        int seconds = (int) (duration % 60);
        duration /= 60;
        int minutes = (int) (duration % 60);
        duration /= 60;
        int hours = (int) (duration % 60);
        duration /= 24;
        int days = (int) duration;

        StringBuilder builder = new StringBuilder();

        if (days != 0) {
            builder.append(days);
            builder.append('d');
        }

        if (hours != 0) {
            if (builder.length() > 0)
                builder.append(' ');

            builder.append(hours);
            builder.append('h');
        }

        if (minutes != 0) {
            if (builder.length() > 0)
                builder.append(' ');

            builder.append(minutes);
            builder.append('m');
        }

        if (seconds != 0) {
            if (builder.length() > 0)
                builder.append(' ');

            builder.append(seconds);
            builder.append('s');
        }

        return builder.toString();
    }

    private boolean checkCooldown(Player player, String command, long duration) {
        long lastUse = 0;
        if (this.lastUse.contains(player.getUniqueId(), command))
            lastUse = this.lastUse.get(player.getUniqueId(), command);

        if (System.currentTimeMillis() < lastUse + duration * 1000)
            return false;

        this.lastUse.put(player.getUniqueId(), command, System.currentTimeMillis());
        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getItem() == null)
            return;

        ItemMeta itemMeta = event.getItem().getItemMeta();
        if (itemMeta == null)
            return;

        String command = NMSUtil.getNBTString(itemMeta, "command");
        if (command == null)
            return;

        ItemDefinition itemDefinition = this.plugin.getConfigManager().getConfig().getItems().get(command);
        if (itemDefinition == null) {
            event.getPlayer().sendMessage(MsgKey.ITEM_DISABLED.get());
            return;
        }

        event.setCancelled(true);
        if (itemDefinition.isSneaking() && !event.getPlayer().isSneaking())
            return;

        if (!event.getPlayer().hasPermission("cmdi.item." + command)) {
            event.getPlayer().sendMessage(MsgKey.ITEM_NOPERMISSION.get());
            return;
        }

        if (!checkCooldown(event.getPlayer(), command, itemDefinition.getCooldown())) {
            event.getPlayer().sendMessage(MsgKey.ITEM_COOLDOWN.get(Collections.singletonMap("TIME_PERIOD",
                    getTimeString(itemDefinition.getCooldown()))));
            return;
        }

        if (itemDefinition.isConsumed()) {
            ItemStack[] contents = event.getPlayer().getInventory().getContents();
            for (int i = 0; i < contents.length; i++)
                if (contents[i] != null && contents[i].isSimilar(event.getItem())) {
                    int amount = contents[i].getAmount();
                    if (amount == 1)
                        contents[i] = null;
                    else
                        contents[i].setAmount(amount - 1);

                    event.getPlayer().getInventory().setItem(i, contents[i]);
                    break;
                }
        }

        try {
            this.plugin.getExecutor().processInteraction(event.getPlayer(), itemDefinition);
        } catch (RuntimeException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to process command item: " + command);
            event.getPlayer().sendMessage(MsgKey.ITEM_ERROR.get());
            e.printStackTrace();
        }
    }

}
