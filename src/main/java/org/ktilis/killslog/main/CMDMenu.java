package org.ktilis.killslog.main;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.ktilis.killslog.database.InventorySerialization;
import org.ktilis.killslog.database.SQLiteDatabase;
import org.sqlite.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

public class CMDMenu implements CommandExecutor, Listener {
    public static Inventory lastInv;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        Player p = (Player) sender;

        if(args.length == 0) {
            sender.sendMessage(
                    ChatColor.GRAY + "-------------" + ChatColor.RED + "Kills" + ChatColor.WHITE + "Log" + ChatColor.GRAY + "-------------",
                    ChatColor.RED + "" + ChatColor.ITALIC + "/" + lbl + " last " + ChatColor.RESET + ChatColor.WHITE + "- Views last 5 kills.",
                    ChatColor.BLUE + "" + ChatColor.ITALIC + "/" + lbl + " search [killer/victim] [nick] " + ChatColor.RESET + ChatColor.WHITE + "- Searches killer/victim kills/dies.",
                    ChatColor.RED + "" + ChatColor.ITALIC + "/" + lbl + " id [id]" + ChatColor.RESET + ChatColor.WHITE + "- Searches kill by id.",
                    ChatColor.GRAY + "----------------------------------"
            );
            return true;
        } else if(args[0].equalsIgnoreCase("openInv") && args[1] != null) {
            p.openInventory(lastInv);
            return true;
        } else if(args[0].equalsIgnoreCase("last")) {
            ArrayList<BaseComponent[]> list = SQLiteDatabase.getLastFiveKills();
            if(list == null) {
                sender.sendMessage(ChatColor.DARK_RED +"Error: return null");
                return true;
            }

            sender.sendMessage(ChatColor.GRAY + "----------------------------------");
            sender.sendMessage(ChatColor.AQUA +"Last 5 kills:");
            for (BaseComponent[] s : list) {
                sender.spigot().sendMessage(s);
            }
            sender.sendMessage(ChatColor.GRAY + "----------------------------------");

            return true;
        } else if(args[0].equalsIgnoreCase("search") && args[1] != null && args[2] != null) {

            if(args[1].equalsIgnoreCase("killer")) {
                String nick = args[2];
                ArrayList<BaseComponent[]> list = SQLiteDatabase.search(1,nick);
                if(list == null) {
                    sender.sendMessage(ChatColor.DARK_RED +"Error: return null");
                    return true;
                }

                sender.sendMessage(ChatColor.GRAY + "----------------------------------");
                sender.sendMessage(ChatColor.BLUE+"Results \"killer\" for "+nick+":");
                for (BaseComponent[] s : list) {
                    sender.spigot().sendMessage(s);
                }
                sender.sendMessage(ChatColor.GRAY + "----------------------------------");
            } else if(args[1].equalsIgnoreCase("victim")) {
                String nick = args[2];
                ArrayList<BaseComponent[]> list = SQLiteDatabase.search(2, nick);
                if (list == null) {
                    sender.sendMessage(ChatColor.DARK_RED + "Error: return null");
                    return true;
                }

                sender.sendMessage(ChatColor.BLUE + "Results \"victim\" for " + nick + ":");
                for (BaseComponent[] s : list) {
                    sender.spigot().sendMessage(s);
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
            visibleINV.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills service openInventory " + id));

            TextComponent visiblePOS = new TextComponent("[position]");
            visiblePOS.setBold(true);
            visiblePOS.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
            visiblePOS.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills service openPos showMenu " + id));

            TextComponent deleteKill = new TextComponent("[DELETE]");
            deleteKill.setBold(true);
            deleteKill.setColor(net.md_5.bungee.api.ChatColor.RED);
            deleteKill.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills service deleteKill " + id));

            BaseComponent[] text = new BaseComponent[5];
            text[0] = visibleINV;
            text[1] = new TextComponent(" ");
            text[2] = visiblePOS;
            text[3] = new TextComponent(" ");
            text[4] = deleteKill;

            sender.sendMessage(
                    ChatColor.GRAY + "----------------------------------",
                    ChatColor.GREEN+String.format("ID %s",id),
                    String.format("%s was killed by %s at %s", victim, killer, time)
            );
            sender.spigot().sendMessage(text);
            sender.sendMessage(ChatColor.GRAY + "----------------------------------");

            return true;
        } else if(args[0].equalsIgnoreCase("service")) {
            return serviceSubCMD(sender, cmd, lbl, args, p);
        } else {
            sender.sendMessage(ChatColor.RED+"Uncorrected command.");
            return true;
        }
        return true;
    }

    private boolean serviceSubCMD(CommandSender sender, Command cmd, String lbl, String[] args, Player p) {
        if(args[1].equalsIgnoreCase("openInventory")) {
            int id = Integer.parseInt(args[2]);
            SQLiteDatabase.DatabaseTransferByID array = null;
            try {
                array = SQLiteDatabase.getKillByID(id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            assert array != null;
            p.openInventory(array.inventory);
        } else if(args[1].equalsIgnoreCase("deleteKill")) {
            if(args.length == 3) {
                int id = Integer.parseInt(args[2]);

                BaseComponent[] text = new BaseComponent[2];
                text[0] = new TextComponent("Are you sure you want to delete this entry?\n");
                text[0].setBold(true);
                text[0].setColor(net.md_5.bungee.api.ChatColor.RED);
                text[1] = new TextComponent("[YES]");
                text[1].setBold(true);
                text[1].setColor(net.md_5.bungee.api.ChatColor.GREEN);
                text[1].setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills service deleteKill "+id+" confirm"));

                sender.sendMessage(ChatColor.GRAY + "----------------------------------");
                sender.spigot().sendMessage(text);
                sender.sendMessage(ChatColor.GRAY + "----------------------------------");
                return true;
            } else if(args[3].equalsIgnoreCase("confirm")) {
                try {
                    int id = Integer.parseInt(args[2]);
                    sender.sendMessage(ChatColor.RED + "Deleting...");

                    SQLiteDatabase.DatabaseTransferByID data = SQLiteDatabase.getKillByID(id);
                    boolean complete = SQLiteDatabase.deleteKillById(id);

                    if (complete) {
                        Main.getInstance().getLogger().log(Level.WARNING, String.format("%s was deleted kill with id %s", sender.getName(), id));
                        Main.getInstance().getLogger().log(Level.WARNING, String.format("%s was killed by %s at %s", data.victim, data.killer, data.time));
                        sender.sendMessage(ChatColor.GREEN + "Successful!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "An error has occurred");
                    }
                } catch (SQLException ignored) {
                }
            }
        } else if (args[1].equalsIgnoreCase("openPos")) {
            if(args[2].equalsIgnoreCase("showMenu")) {
                try {
                    int id = Integer.parseInt(args[3]);

                    SQLiteDatabase.DatabaseTransferByID data = SQLiteDatabase.getKillByID(id);

                    BaseComponent[] text = new BaseComponent[3];
                    text[0] = new TextComponent("[COPY]");
                    text[0].setBold(true);
                    text[0].setColor(net.md_5.bungee.api.ChatColor.AQUA);
                    text[0].setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, data.location.getBlockX()+" "+data.location.getBlockY()+" "+data.location.getBlockZ()));
                    text[1] = new TextComponent(" ");
                    text[2] = new TextComponent("[TP]");
                    text[2].setBold(true);
                    text[2].setColor(net.md_5.bungee.api.ChatColor.BLUE);
                    text[2].setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kills service openPos teleport " + id));


                    sender.sendMessage(ChatColor.GRAY + "----------------------------------");
                    sender.sendMessage(
                            "ID: " + id,
                            "World: "+data.location.getWorld().getName(),
                            "X: "  +  data.location.getBlockX(),
                            "Y: "  +  data.location.getBlockY(),
                            "Z: "  +  data.location.getBlockZ()
                    );
                    sender.spigot().sendMessage(text);
                    sender.sendMessage(ChatColor.GRAY + "----------------------------------");
                } catch (Exception e) {

                }
            } else if (args[2].equalsIgnoreCase("teleport")) {
                try {
                    int id = Integer.parseInt(args[3]);

                    SQLiteDatabase.DatabaseTransferByID data = SQLiteDatabase.getKillByID(id);
                    sender.sendMessage("Teleporting...");
                    p.teleport(data.location);

                } catch (Exception e) {

                }
            }
        }
        return true;
    }
}
