package org.ktilis.killslog.main;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.ktilis.killslog.database.InventorySerialization;
import org.ktilis.killslog.database.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    public static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        SQLiteDatabase.createConnection();

        getCommand("kills").setExecutor(new CMDMenu());
        getServer().getPluginManager().registerEvents(new KillListener(), this);

    }

    @Override
    public void onDisable() {
        SQLiteDatabase.closeConnection();
    }

    public static Main getInstance() {
        return instance;
    }
}
