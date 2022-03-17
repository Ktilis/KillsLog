package org.ktilis.killslog.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.ktilis.killslog.database.SQLiteDatabase;

import java.util.ArrayList;

public class CMDMenu implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(
                    ChatColor.GRAY+"-------------"+ChatColor.RED+"Kills"+ChatColor.WHITE+"Log"+ChatColor.GRAY+"-------------",
                    ChatColor.RED+""+ChatColor.ITALIC+"/"+lbl+" last "+ChatColor.RESET+ChatColor.WHITE+"- Views last 5 kills.",
                    ChatColor.BLUE +""+ChatColor.ITALIC+"/"+lbl+" search [killer/vistim] [nick] "+ChatColor.RESET+ChatColor.WHITE+"- Searches killer/vistim kills/dies.",
                    ChatColor.GRAY+"----------------------------------"
            );
            return true;
        } else if(args[0].equalsIgnoreCase("last")) {
            ArrayList<String> list = SQLiteDatabase.getLastTenKills();
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
                ArrayList<String> list = SQLiteDatabase.search(2,nick);
                if(list == null) {
                    sender.sendMessage(ChatColor.DARK_RED +"Error: return null");
                    return true;
                }

                sender.sendMessage(ChatColor.BLUE+"Results \"vistim\" for "+nick+":");
                for (String s : list) {
                    sender.sendMessage(s);
                }
            } else {
                sender.sendMessage(ChatColor.RED+"Error");
            }
        }
        return true;
    }
}
