package me.yamakaja.commanditems.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Created by Yamakaja on 23.06.17.
 */
public class NMSUtil {

    private static String nmsVersion;
    private static Class<?> craftMetaItemClass;
    private static Class<?> nbtTagCompound;

    private static Field internalDataField;

    private static Method setString;
    private static Method getString;

    static {
        nmsVersion = getNMSVersion();

        craftMetaItemClass = getOBCClass("inventory.CraftMetaItem");
        nbtTagCompound = getNMSClass("NBTTagCompound");

        try {
            if (nbtTagCompound != null) {
                setString = nbtTagCompound.getMethod("setString", String.class, String.class);
                getString = nbtTagCompound.getMethod("getString", String.class);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            if (craftMetaItemClass != null) {
                internalDataField = craftMetaItemClass.getDeclaredField("internalTag");
                internalDataField.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    private NMSUtil() {
    }

    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + nmsVersion + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getOBCClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + nmsVersion + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split(Pattern.quote("."))[3];
    }

    public static void setNBTString(ItemMeta meta, String key, String value) {
        try {
            Object tag = internalDataField.get(meta);
            if (tag == null) {
                tag = nbtTagCompound.newInstance();
                internalDataField.set(meta, tag);
            }

            setString.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static String getNBTString(ItemMeta meta, String key) {
        try {
            Object tag = internalDataField.get(meta);
            if (tag == null)
                return null;

            return (String) getString.invoke(tag, key);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
