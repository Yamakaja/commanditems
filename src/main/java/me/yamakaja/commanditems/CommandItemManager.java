package me.yamakaja.commanditems;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.yamakaja.commanditems.data.ItemDefinition;
import me.yamakaja.commanditems.util.NMSUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class CommandItemManager implements Listener {

    private CommandItems plugin;

    private Table<UUID, String, Long> cooldowns = HashBasedTable.create();

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
        long cooldownEnd = 0;
        if (this.cooldowns.contains(player.getUniqueId(), command))
            cooldownEnd = this.cooldowns.get(player.getUniqueId(), command);

        if (System.currentTimeMillis() < cooldownEnd)
            return false;

        this.cooldowns.put(player.getUniqueId(), command, System.currentTimeMillis() + duration * 1000L);
        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getItem() == null)
            return;

        String command = NMSUtil.getNBTString(event.getItem().getItemMeta(), "command");
        if (command == null)
            return;

        ItemDefinition itemDefinition = this.plugin.getConfigManager().getConfig().getItems().get(command);
        if (itemDefinition == null) {
            event.getPlayer().sendMessage(ChatColor.RED + "This command item has been disabled!");
            return;
        }

        event.setCancelled(true);
        if (itemDefinition.isSneaking() && !event.getPlayer().isSneaking())
            return;

        if (!event.getPlayer().hasPermission("cmdi.item." + command)) {
            event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to use this item!");
            return;
        }

        if (!checkCooldown(event.getPlayer(), command, itemDefinition.getCooldown())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can only use this item every " + getTimeString(itemDefinition.getCooldown()) + "!");
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

        this.plugin.getExecutor().processInteraction(event.getPlayer(), itemDefinition);
    }

}
