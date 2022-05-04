package org.ktilis.killslog.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InventorySerialization {
    public static String InventoryToString(Inventory inventory) {

        JsonObject obj = new JsonObject();

        obj.addProperty("type", inventory.getType().name());
        obj.addProperty("name", "Player's inventory");
        if (inventory.getType().name().equalsIgnoreCase("player")) {
            obj.addProperty("size", 41);
        } else {
            obj.addProperty("size", inventory.getSize());
        }


        JsonArray items = new JsonArray();

        for (int i = 0; i <= inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                ItemMeta itemMeta = item.getItemMeta();
                Damageable dmg = (Damageable) itemMeta;
                JsonObject jitem = new JsonObject();
                jitem.addProperty("type", item.getType().toString());
                assert itemMeta != null;
                jitem.addProperty("name", itemMeta.getDisplayName());
                jitem.addProperty("damage", dmg.getDamage());
                jitem.addProperty("amount", item.getAmount());
                jitem.addProperty("slot", i);

                if (itemMeta.hasLore()) {
                    JsonArray lore = new JsonArray();
                    for (String s: itemMeta.getLore()) {
                        lore.add(s);
                    }
                    jitem.add("lore", lore);
                }

                if (itemMeta.hasEnchants()) {
                    JsonArray enchants = new JsonArray();
                    Iterator< Map.Entry < Enchantment, Integer >> irt = itemMeta.getEnchants().entrySet().iterator();
                    while (irt.hasNext()) {
                        Map.Entry < Enchantment, Integer > entry = irt.next();
                        JsonObject enc = new JsonObject();
                        Enchantment ee = (Enchantment) entry.getKey();
                        enc.addProperty("type", ee.getKey().getKey());
                        enc.addProperty("level", entry.getValue());
                        enchants.add(enc);
                    }


                    jitem.add("enchantments", enchants);
                }

                items.add(jitem);
            }
        }

        obj.add("items", items);

        return obj.toString();
    }

    public static Inventory StringToInventory(String s) {
        JsonObject obj = JsonParser.parseString(s).getAsJsonObject();

        Inventory inv = Bukkit.createInventory(null, 54, obj.get("name").getAsString());

        JsonArray items = obj.get("items").getAsJsonArray();

        for (JsonElement itemele: items) {
            JsonObject jitem = itemele.getAsJsonObject();

            ItemStack item = new ItemStack(Material.valueOf(jitem.get("type").getAsString()), jitem.get("amount").getAsInt());
            ItemMeta itemMeta = item.getItemMeta();

            Damageable dmg = (Damageable) itemMeta;
            assert dmg != null;
            dmg.setDamage(jitem.get("damage").getAsInt());


            if (jitem.has("lore")) {
                JsonArray jlore = jitem.get("lore").getAsJsonArray();
                List<String> lore = new ArrayList < String > ();
                for (JsonElement loreE: jlore) {
                    lore.add(loreE.getAsString());
                }
                itemMeta.setLore(lore);
            }

            if (jitem.has("enchantments")) {
                JsonArray jenchants = jitem.get("enchantments").getAsJsonArray();
                for (JsonElement je: jenchants) {
                    JsonObject encObj = je.getAsJsonObject();
                    System.out.println(encObj.toString());
                    itemMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(encObj.get("type").getAsString()))), encObj.get("level").getAsInt(), true);
                }
            }

            item.setItemMeta(itemMeta);
            inv.setItem(jitem.get("slot").getAsInt(), item);
        }

        return inv;
    }
}
