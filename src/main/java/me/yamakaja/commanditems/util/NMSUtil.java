package me.yamakaja.commanditems.util;

import me.yamakaja.commanditems.CommandItems;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Yamakaja on 23.06.17.
 */
public class NMSUtil {

    private static final String NBT_KEY = "cmdi";
    private static String nmsVersion;
    private static Class<?> craftMetaItemClass;
    private static Class<?> nbtTagCompound;
    private static Class<?> nbtBase;

    private static Field unhandledTagsField;

    private static Method setString;
    private static Method getString;
    private static Method setNBTBase;
    private static Method getNBTBase;
    private static Method getKeys;

    static {
        nmsVersion = getNMSVersion();

        craftMetaItemClass = getOBCClass("inventory.CraftMetaItem");
        nbtTagCompound = getNMSClass("NBTTagCompound", "nbt.NBTTagCompound");
        nbtBase = getNMSClass("NBTBase", "nbt.NBTBase");

        try {
            setString = nbtTagCompound.getMethod("a", String.class, String.class);
            getString = nbtTagCompound.getMethod("getString", String.class);
            setNBTBase = nbtTagCompound.getMethod("set", String.class, nbtBase);
            getNBTBase = nbtTagCompound.getMethod("get", String.class);
            getKeys = nbtTagCompound.getMethod("getKeys");
        } catch (NoSuchMethodException noSuchMethodException) {
            try {
                setString = nbtTagCompound.getMethod("a", String.class, String.class);
                getString = nbtTagCompound.getMethod("l", String.class);
                setNBTBase = nbtTagCompound.getMethod("a", String.class, nbtBase);
                getNBTBase = nbtTagCompound.getMethod("c", String.class);
                getKeys = nbtTagCompound.getMethod("d");
            }catch (NoSuchMethodException e){
                e.printStackTrace();
            }
        }

        try {
            if (craftMetaItemClass != null) {
                unhandledTagsField = craftMetaItemClass.getDeclaredField("unhandledTags");
                unhandledTagsField.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    private NMSUtil() {
    }

    public static Class<?> getNMSClass(String oldName, String newName) {
        // MC Version <= 1.16.5
        try {
            return Class.forName("net.minecraft.server." + nmsVersion + "." + oldName);
        } catch (ClassNotFoundException ignored) {
        }

        // MC Version >= 1.17
        try {
            return Class.forName("net.minecraft." + newName);
        } catch (ClassNotFoundException ignored) {
        }

        throw new RuntimeException("Couldn't find NMS class: " + oldName  + " / " + newName);
    }

    public static Class<?> getOBCClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + nmsVersion + "." + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Coudln't find OBC class: " + nmsVersion, e);
        }
    }

    public static String getNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split(Pattern.quote("."))[3];
    }

    private static Object getCMDITag(ItemMeta meta, boolean create) throws IllegalAccessException, InstantiationException {
        Map<String, Object> unhandledTags = (Map<String, Object>) unhandledTagsField.get(meta);
        Object cmdiTag = unhandledTags.get(NBT_KEY);

        if (cmdiTag == null && create) {
            cmdiTag = nbtTagCompound.newInstance();
            unhandledTags.put(NBT_KEY, cmdiTag);
        }

        return cmdiTag;
    }

    public static void setNBTString(ItemMeta meta, String key, String value) {
        try {
            Object tag = getCMDITag(meta, true);
            setString.invoke(tag, key, value);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            CommandItems.getPlugin(CommandItems.class).getLogger().severe("Failed to write NBT, please report to author!");
            e.printStackTrace();
        }
    }

    public static String getNBTString(ItemMeta meta, String key) {
        try {
            Object tag = getCMDITag(meta, false);
            if (tag == null)
                return null;

            return (String) getString.invoke(tag, key);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            CommandItems.getPlugin(CommandItems.class).getLogger().severe("Failed to read NBT, please report to author!");
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> getNBTStringMap(ItemMeta meta, String key) {
        try {
            Object cmdiTag = getCMDITag(meta, false);
            if (cmdiTag == null)
                return Collections.emptyMap();

            Object mapTag = getNBTBase.invoke(cmdiTag, key);
            if (mapTag == null)
                return Collections.emptyMap();

            Map<String, String> map = new HashMap<>();
            Set<String> keys = (Set<String>) getKeys.invoke(mapTag);

            for (String k : keys) map.put(k, (String) getString.invoke(mapTag, k));

            return map;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            CommandItems.getPlugin(CommandItems.class).getLogger().severe("Failed to read NBT, please report to author!");
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static void setNBTStringMap(ItemMeta meta, String key, Map<String, String> entries) {
        try {
            Object cmdiTag = getCMDITag(meta, true);

            if (cmdiTag == null)
                throw new RuntimeException("cmdi tag doesn't exist yet!");

            Object mapTag = nbtTagCompound.newInstance();
            for (Map.Entry<String, String> entry : entries.entrySet())
                setString.invoke(mapTag, entry.getKey(), entry.getValue());

            setNBTBase.invoke(cmdiTag, key, mapTag);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            CommandItems.getPlugin(CommandItems.class).getLogger().severe("Failed to write NBT, please report to author!");
            e.printStackTrace();
        }
    }

}
