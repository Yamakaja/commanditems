package me.yamakaja.commanditems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yamakaja on 19.06.17.
 */
public class CommandManager implements Listener {

    private CommandItems plugin;
    private Map<String, List<String>> commands = new HashMap<>();
    private boolean shiftRequired;

    public CommandManager(CommandItems plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        reloadCommands();
    }

    public void reloadCommands() {
        this.plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        commands.clear();
        config.getKeys(false).stream().filter(key -> !key.equals("shift")).forEach(key -> commands.put(key, config.getStringList(key)));
        this.shiftRequired = config.getBoolean("shift");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)
                || event.getItem() == null || event.getItem().getType() == Material.AIR)
            return;

        if (shiftRequired && !event.getPlayer().isSneaking())
            return;

        ItemStack item = event.getItem();

        if (!item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();

        if (!meta.hasLore())
            return;

        List<String> lore = meta.getLore();

        if (lore.size() == 0)
            return;

        String commandSelectorLine = lore.get(lore.size() - 1);

        if (!commandSelectorLine.startsWith(ChatColor.BLACK.toString() + ChatColor.MAGIC.toString() + "\u00bb"))
            return;

        List<String> commandList = this.commands.get(commandSelectorLine.substring(5));
        commandList.stream().map(s -> s.replace("{player}", event.getPlayer().getName())).forEach(
                command -> {
                    if (command.startsWith("C~")) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                                command.substring(2));
                        return;
                    }

                    if (command.startsWith("O~")) {
                        Player player = event.getPlayer();
                        boolean wasOp = player.isOp();

                        try {
                            Bukkit.dispatchCommand(player,
                                    command.substring(2));
                        } catch (Throwable throwable) {
                            player.setOp(wasOp);
                            throw throwable;
                        }

                        player.setOp(wasOp);

                        return;
                    }

                    Bukkit.dispatchCommand(event.getPlayer(),
                            command);
                }
        );

        if (item.getAmount() == 1)
            item = new ItemStack(Material.AIR);
        else
            item.setAmount(item.getAmount() - 1);

        if (event.getHand() == EquipmentSlot.HAND)
            event.getPlayer().getInventory().setItemInMainHand(item);
        else
            event.getPlayer().getInventory().setItemInOffHand(item);
    }

    public Map<String, List<String>> getCommands() {
        return commands;
    }

}
