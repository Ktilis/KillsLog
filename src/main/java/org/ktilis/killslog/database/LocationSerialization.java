package org.ktilis.killslog.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerialization {
    public static String LocationToString(Location loc) {
        JsonObject obj = new JsonObject();
        obj.addProperty("worldName", loc.getWorld().getName());
        obj.addProperty("x", String.valueOf(loc.getX()));
        obj.addProperty("y", String.valueOf(loc.getY()));
        obj.addProperty("z", String.valueOf(loc.getZ()));
        return obj.toString();
    }

    public static Location StringToLocation(String data) {
        JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
        Location loc = new Location(
                Bukkit.getWorld(obj.get("worldName").getAsString()),
                Double.parseDouble(obj.get("x").getAsString()),
                Double.parseDouble(obj.get("y").getAsString()),
                Double.parseDouble(obj.get("z").getAsString())
        );

        return loc;
    }
}
