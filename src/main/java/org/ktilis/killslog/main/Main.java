package org.ktilis.killslog.main;

import org.bukkit.plugin.java.JavaPlugin;
import org.ktilis.killslog.database.SQLiteDatabase;

public final class Main extends JavaPlugin {

    public static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        SQLiteDatabase.createConnection();

        getCommand("kills").setExecutor(new CMDMenu());
        this.getServer().getPluginManager().registerEvents(new KillListener(), this);

    }

    @Override
    public void onDisable() {
        SQLiteDatabase.closeConnection();
    }

    public static Main getInstance() {
        return instance;
    }
}
