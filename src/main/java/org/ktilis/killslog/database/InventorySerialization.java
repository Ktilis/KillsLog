package org.ktilis.killslog.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class InventorySerialization {
    public static String InventoryToString(Inventory inventory) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", inventory.getType().name());
        obj.addProperty("name", "Player's inventory");
        if (inventory.getType().name().equalsIgnoreCase("player")) {
            obj.addProperty("size", 41);
        }
        JsonArray items = new JsonArray();
        JsonArray itemsArmor = new JsonArray();
        for (int i = 0; i <= inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (i > 35) {
                if (item != null) {
                    JsonObject jitem = new JsonObject();
                    jitem.addProperty("slot", i - 36);
                    String itemData = ItemStackToString(item);
                    jitem.addProperty("data", itemData);
                    itemsArmor.add(jitem);
                    continue;
                }
            }
            if (item != null) {
                JsonObject jitem = new JsonObject();
                jitem.addProperty("slot", i);
                String itemData = ItemStackToString(item);
                jitem.addProperty("data", itemData);
                items.add(jitem);
            }
        }
        obj.add("items", items);
        obj.add("itemsArmor", itemsArmor);
        return obj.toString();
    }

    public static String ItemStackToString(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    public static Inventory StringToInventory(String s) {
        JsonObject obj = JsonParser.parseString(s).getAsJsonObject();

        Inventory inv = Bukkit.createInventory(null, 54, obj.get("name").getAsString());

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerItemMeta = filler.getItemMeta();
        fillerItemMeta.setDisplayName(ChatColor.GRAY+""+ChatColor.ITALIC+"...");
        filler.setItemMeta(fillerItemMeta);
        for(int i = 36; i <= 44; i++) {
            inv.setItem(i, filler);
        }
        for(int i = 50; i <= 53; i++) {
            inv.setItem(i, filler);
        }

        JsonArray items = obj.get("items").getAsJsonArray();
        for (JsonElement itemele: items) {
            JsonObject jitem = itemele.getAsJsonObject();
            ItemStack item = ItemStackFromBase64(jitem.get("data").getAsString());
            inv.setItem(jitem.get("slot").getAsInt(), item);
        }

        JsonArray itemsArmor = obj.get("itemsArmor").getAsJsonArray();
        for (JsonElement itemele: itemsArmor) {
            JsonObject jitem = itemele.getAsJsonObject();
            ItemStack item = ItemStackFromBase64(jitem.get("data").getAsString());
            inv.setItem(jitem.get("slot").getAsInt()+45, item);
        }

        return inv;
    }

    public static ItemStack ItemStackFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to decode class type.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
