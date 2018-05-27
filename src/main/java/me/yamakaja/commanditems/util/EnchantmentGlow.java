package me.yamakaja.commanditems.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class EnchantmentGlow extends EnchantmentWrapper {

    private static Enchantment glow;

    public EnchantmentGlow(int id) {
        super(id);
    }

    public static Enchantment getGlow() {
        if (glow != null)
            return glow;
        else if ((glow = Enchantment.getByName("Glow")) != null)
            return glow;

        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        glow = new EnchantmentGlow(187);
        Enchantment.registerEnchantment(glow);
        return glow;
    }

    public static void addGlow(ItemStack item) {
        item.addEnchantment(getGlow(), 1);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public String getName() {
        return "Glow";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }
}