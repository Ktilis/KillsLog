package org.ktilis.killslog.database;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ktilis.killslog.main.Main;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SQLiteDatabase {
    public static Connection conn = null;
    public static Statement stat = null;

    public static boolean createConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + Main.getInstance().getDataFolder()+ File.separator + "plugin.db");
            stat = conn.createStatement();
            stat.setQueryTimeout(2);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            stat.executeUpdate("CREATE TABLE IF NOT EXISTS kills ("
                    + " id         INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " killer     TEXT NOT NULL,"
                    + " victim     TEXT NOT NULL,"
                    + " time       TEXT NOT NULL,"
                    + " inventory  TEXT NOT NULL,"
                    + " location   TEXT NOT NULL"
                    + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean closeConnection() {
        try {

            if(conn.isClosed() || stat.isClosed()) return false;

            stat.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean newKill(String killers, String victim, Inventory inv, Location loc) {
        String currentTime = new Date().toString();

        try {
            String invStr = InventorySerialization.InventoryToString(inv);
            String locStr = LocationSerialization.LocationToString(loc);

            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO kills(killer, victim, time, inventory, location) VALUES(?, ?, ?, ?, ?)"
            );

            insertStmt.setString(1, killers);
            insertStmt.setString(2, victim);
            insertStmt.setString(3, currentTime);
            insertStmt.setString(4, invStr);
            insertStmt.setString(5, locStr);

            insertStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<BaseComponent[]> getLastFiveKills() {
        try {
            return killsList("SELECT * FROM kills ORDER BY ID DESC LIMIT 5;");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<BaseComponent[]> search(int mode, String nick) {
        try {
            if (mode == 1) return killsList("SELECT * FROM kills WHERE killer=\""+nick+"\" ORDER BY ID DESC LIMIT 5;"); //Search killer
            if (mode == 2) return killsList("SELECT * FROM kills WHERE victim=\""+nick+"\" ORDER BY ID DESC LIMIT 5;"); //Search victim

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DatabaseTransferByID getKillByID(int id) throws SQLException {
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM kills WHERE id=\""+id+"\" ORDER BY ID DESC LIMIT 5;");

            String killer = (rs.getString("killer") != null) ? rs.getString("killer") : "null";
            String victim = (rs.getString("victim") != null) ? rs.getString("victim") : "null";
            String time = (rs.getString("time") != null) ? rs.getString("time") : "null";
            Inventory inv = InventorySerialization.StringToInventory((rs.getString("inventory") != null) ? rs.getString("inventory") : "{\"name\":\"PLAYER\",\"size\":41,\"items\":[]}");
            Location loc = LocationSerialization.StringToLocation((rs.getString("location") != null) ? rs.getString("location") : "{\"world\":\"null\",\"x\":0,\"y\":0,\"z\":0}");

            DatabaseTransferByID data = new DatabaseTransferByID(killer, victim, time, inv, loc);

            statement.close();
            if(killer == null) return new DatabaseTransferByID("null", "null", "null", Bukkit.createInventory(null, 54, "null"), new Location(Bukkit.getWorld("world"), 0, 0, 0));
            return data;
        } catch (SQLException ignored) {}
        return null;
    }

    private static ArrayList<BaseComponent[]> killsList(String sql) throws SQLException {
        try {
            ResultSet rs = stat.executeQuery(sql);

            ArrayList<BaseComponent[]> result = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String killer = rs.getString("killer");
                String victim = rs.getString("victim");
                String time = rs.getString("time");

                BaseComponent[] component = new BaseComponent[3];
                component[0] = new TextComponent(String.format("ID - %s: %s killed %s at %s", id, killer, victim, time));

                TextComponent visibleINV = new TextComponent(" [inv]");
                visibleINV.setBold(true);
                visibleINV.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                visibleINV.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills service openInventory " + id));
                component[1] = visibleINV;

                TextComponent visiblePOS = new TextComponent(" [pos]");
                visiblePOS.setBold(true);
                visiblePOS.setColor(ChatColor.BLUE);
                visiblePOS.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills service openPos showMenu " + id));
                component[2] = visiblePOS;

                result.add(component);
            }

            return result;
        } catch (Exception ignored) {}
        return null;
    }

    public static boolean deleteKillById(Integer id) {
        try {
            PreparedStatement insertStmt = conn.prepareStatement(
                    "DELETE FROM kills WHERE id=\""+id+"\";"
            );
            insertStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static class DatabaseTransferByID {
        public String killer;
        public String victim;
        public String time;
        public Inventory inventory;
        public Location location;

        public DatabaseTransferByID(String killer, String victim, String time, Inventory inv, Location loc) {
            this.killer = killer;
            this.victim = victim;
            this.time = time;
            this.inventory = inv;
            this.location = loc;
        }

        @Override
        public String toString() {
            return "DatabaseTransferByID{" +
                    "killer='" + killer + '\'' +
                    ",victim='" + victim + '\'' +
                    ",time='" + time + '\'' +
                    ",inventory=" + inventory +
                    ",location=" + location +
                    '}';
        }
    }

}
