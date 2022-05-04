package org.ktilis.killslog.database;

import org.bukkit.Bukkit;
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
                    + " inventory  TEXT NOT NULL"
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

    public static boolean newKill(String killers, String victim, Inventory inv) {
        String currentTime = new Date().toString();

        try {
            String invStr =  InventorySerialization.InventoryToString(inv);

            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO kills(killer, victim, time, inventory) VALUES(?, ?, ?, ?)"
            );

            insertStmt.setString(1, killers);
            insertStmt.setString(2, victim);
            insertStmt.setString(3, currentTime);
            insertStmt.setString(4, invStr);

            insertStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String> getLastFiveKills() {
        try {
            return killsList("SELECT * FROM kills ORDER BY ID DESC LIMIT 5;");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> search(int mode, String nick) {
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

            DatabaseTransferByID data = new DatabaseTransferByID(killer, victim, time, inv);

            statement.close();
            if(killer == null) return new DatabaseTransferByID("null", "null", "null", Bukkit.createInventory(null, 54, "null"));
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<String> killsList(String sql) throws SQLException {
        ResultSet rs = stat.executeQuery(sql);

        ArrayList<String> result = new ArrayList<String>();

        while(rs.next()) {
            int id = rs.getInt("id");
            String killer = rs.getString("killer");
            String victim = rs.getString("victim");
            String time = rs.getString("time");

            result.add(String.format("ID - %s: %s killed %s at %s", id, killer, victim, time));
        }

        return result;
    }

    public static class DatabaseTransferByID {
        public String killer;
        public String victim;
        public String time;
        public Inventory inventory;

        public DatabaseTransferByID(String killer, String victim, String time, Inventory inv) {
            this.killer = killer;
            this.victim = victim;
            this.time = time;
            this.inventory = inv;
        }

        @Override
        public String toString() {
            return "DatabaseTransferByID{" +
                    "killer='" + killer + '\'' +
                    ",victim='" + victim + '\'' +
                    ",time='" + time + '\'' +
                    ",inventory=" + inventory +
                    '}';
        }
    }

}
