package org.ktilis.killslog.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ktilis.killslog.database.SQLiteDatabase;

import java.util.Objects;

public class KillListener implements Listener {
    @EventHandler
    public void kill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {

            Player killer = event.getEntity().getKiller();
            Player victim = event.getEntity().getPlayer();

            assert victim != null;
            SQLiteDatabase.newKill(killer.getName(), victim.getName(), victim.getInventory(), event.getEntity().getLocation());
        }
    }

    @EventHandler
    public void dontTouchInventory(InventoryClickEvent e) {
        if (!e.getInventory().equals(CMDMenu.lastInv)) return;
        e.setCancelled(true);
    }
}
