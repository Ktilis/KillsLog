package org.ktilis.killslog.database;

import org.ktilis.killslog.main.Main;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class SQLiteDatabase {
    public static Connection conn = null;
    public static Statement stat = null;

    //Создает подключение к базе данных
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
                    + " killers    TEXT NOT NULL,"
                    + " vistim     TEXT NOT NULL,"
                    + " time       TEXT NOT NULL"
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

    //Добавляет в базу данных новый килл
    public static boolean newKill(String killers, String vistim) {
        String currentTime = new Date().toString();

        try {
            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO kills(killers, vistim, time) VALUES(?, ?, ?)");

            insertStmt.setString(1, killers);
            insertStmt.setString(2, vistim);
            insertStmt.setString(3, currentTime);
            insertStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String> getLastTenKills() {
        try {
            return killsList("SELECT * FROM kills ORDER BY ID DESC LIMIT 5;");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> search(int mode, String nick) {
        try {
            if (mode == 1) return killsList("SELECT * FROM kills WHERE killers=\""+nick+"\" ORDER BY ID DESC LIMIT 5;"); //Search killer
            if (mode == 2) return killsList("SELECT * FROM kills WHERE vistim=\""+nick+"\" ORDER BY ID DESC LIMIT 5;"); //Search vistim

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
            String killers = rs.getString("killers");
            String vistim = rs.getString("vistim");
            String time = rs.getString("time");
            result.add(String.format("ID - %s: %s killed %s at %s", id, killers, vistim, time));
        }

        return result;
    }

}
