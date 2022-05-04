package org.ktilis.killslog.main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.ktilis.killslog.database.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;

public class CMDMenu implements CommandExecutor, Listener {
    public static Inventory lastInv;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        Player p = (Player) sender;

        if(args.length == 0) {
            sender.sendMessage(
                    ChatColor.GRAY + "-------------" + ChatColor.RED + "Kills" + ChatColor.WHITE + "Log" + ChatColor.GRAY + "-------------",
                    ChatColor.RED + "" + ChatColor.ITALIC + "/" + lbl + " last " + ChatColor.RESET + ChatColor.WHITE + "- Views last 5 kills.",
                    ChatColor.BLUE + "" + ChatColor.ITALIC + "/" + lbl + " search [killer/vistim] [nick] " + ChatColor.RESET + ChatColor.WHITE + "- Searches killer/vistim kills/dies.",
                    ChatColor.RED + "" + ChatColor.ITALIC + "/" + lbl + " id [id]" + ChatColor.RESET + ChatColor.WHITE + "- Searches kill by id.",
                    ChatColor.GRAY + "----------------------------------"
            );
            return true;
        } else if(args[0].equalsIgnoreCase("openInv") && args[1] != null) {
            p.openInventory(lastInv);
            return true;
        } else if(args[0].equalsIgnoreCase("last")) {
            ArrayList<String> list = SQLiteDatabase.getLastFiveKills();
            if(list == null) {
                sender.sendMessage(ChatColor.DARK_RED +"Error: return null");
                return true;
            }

            sender.sendMessage(ChatColor.AQUA +"Last 5 kills:");
            for (String s : list) {
                sender.sendMessage(s);
            }
            return true;
        } else if(args[0].equalsIgnoreCase("search") && args[1] != null && args[2] != null) {

            if(args[1].equalsIgnoreCase("killer")) {
                String nick = args[2];
                ArrayList<String> list = SQLiteDatabase.search(1,nick);
                if(list == null) {
                    sender.sendMessage(ChatColor.DARK_RED +"Error: return null");
                    return true;
                }

                sender.sendMessage(ChatColor.BLUE+"Results \"killer\" for "+nick+":");
                for (String s : list) {
                    sender.sendMessage(s);
                }
            } else if(args[1].equalsIgnoreCase("vistim")) {
                String nick = args[2];
                ArrayList<String> list = SQLiteDatabase.search(2, nick);
                if (list == null) {
                    sender.sendMessage(ChatColor.DARK_RED + "Error: return null");
                    return true;
                }

                sender.sendMessage(ChatColor.BLUE + "Results \"victim\" for " + nick + ":");
                for (String s : list) {
                    sender.sendMessage(s);
                }
            } else {
                sender.sendMessage(ChatColor.RED+"Uncorrected command");
            }
        } else if(args[0].equalsIgnoreCase("id") && args[1] != null) {
            int id = Integer.parseInt(args[1]);
            SQLiteDatabase.DatabaseTransferByID array = null;
            try {
                array = SQLiteDatabase.getKillByID(id);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String killer = array.killer;
            String victim = array.victim;
            String time = array.time;
            lastInv = array.inventory;

            TextComponent visibleINV = new TextComponent("[inventory]");
            visibleINV.setBold(true);
            visibleINV.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            visibleINV.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills openInv "+ p.getUniqueId()));

            sender.sendMessage(
                    ChatColor.GRAY + "----------------------------------",
                    ChatColor.GREEN+String.format("ID %s",id),
                    String.format("%s was killed by %s at %s", victim, killer, time)
            );
            sender.spigot().sendMessage(visibleINV);
            sender.sendMessage(ChatColor.GRAY + "----------------------------------");

            return true;
        } else {
            sender.sendMessage(ChatColor.RED+"Uncorrected command.");
            return true;
        }
        return true;
    }
}
