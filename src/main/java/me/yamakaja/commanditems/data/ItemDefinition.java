package me.yamakaja.commanditems.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import me.yamakaja.commanditems.data.action.Action;
import me.yamakaja.commanditems.util.EnchantmentGlow;
import me.yamakaja.commanditems.util.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemDefinition {

    public static class ItemStackBuilder {
        @JsonProperty(required = true)
        private Material type;

        @JsonProperty(required = true)
        private String name;

        @JsonProperty
        private List<String> lore;

        @JsonProperty(defaultValue = "false")
        private boolean glow;

        @JsonProperty(defaultValue = "0")
        private int damage;

        @JsonProperty(defaultValue = "false")
        private boolean unbreakable;

        @JsonProperty(required = false)
        private Integer customModelData;

        @JsonProperty(defaultValue = "")
        private String skullUser;

        public ItemStack build(String key, Map<String, String> params) {
            Preconditions.checkNotNull(this.type, "No material specified!");

            ItemStack stack = new ItemStack(this.type, 1, (short) this.damage);
            ItemMeta meta = stack.getItemMeta();

            Preconditions.checkNotNull(meta, "ItemMeta is null! (Material: " + type + ")");

            if (name != null)
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (lore != null && !this.lore.isEmpty())
                meta.setLore(lore.stream()
                        .map(x -> ChatColor.translateAlternateColorCodes('&', x))
                        .collect(Collectors.toList()));

            meta.setUnbreakable(unbreakable);
            if (customModelData != null)
                meta.setCustomModelData(customModelData);

            if (this.type == Material.PLAYER_HEAD && skullUser != null && !skullUser.isEmpty()) {
                SkullMeta skullMeta = (SkullMeta) meta;
                OfflinePlayer player;
                try {
                    UUID uuid = UUID.fromString(this.skullUser);
                    player = Bukkit.getOfflinePlayer(uuid);
                } catch (IllegalArgumentException e) {
                    player = Bukkit.getOfflinePlayer(this.skullUser);
                }

                skullMeta.setOwningPlayer(player);
            }

            NMSUtil.setNBTString(meta, "command", key);
            NMSUtil.setNBTStringMap(meta, "params", params);

            stack.setItemMeta(meta);

            if (glow)
                stack.addEnchantment(EnchantmentGlow.getGlow(), 1);

            return stack;
        }

    }

    public static class ExecutionTrace {
        public final int depth;
        public final String label;

        public ExecutionTrace(int depth, String label) {
            this.depth = depth;
            this.label = label;
        }
    }

    private transient String key;

    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty
    private boolean consumed;

    @JsonProperty
    private long cooldown;

    @JsonProperty
    private ItemStackBuilder item;

    @JsonProperty
    private Action[] actions;

    @JsonProperty
    private boolean sneaking;

    @JsonProperty
    private Map<String, String> parameters;

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public boolean isConsumed() {
        return this.consumed;
    }

    public long getCooldown() {
        return this.cooldown;
    }

    public ItemStack getItem(Map<String, String> params) {
        return this.item.build(this.key, params);
    }

    public Action[] getActions() {
        return this.actions;
    }

    public boolean isSneaking() {
        return this.sneaking;
    }

    public List<ExecutionTrace> getExecutionTrace() {
        List<ExecutionTrace> trace = new ArrayList<>();

        for (Action action : this.actions) action.trace(trace, 0);

        return trace;
    }

}
