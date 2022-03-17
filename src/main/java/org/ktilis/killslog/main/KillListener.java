package org.ktilis.killslog.main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.ktilis.killslog.database.SQLiteDatabase;

public class KillListener implements Listener {
    @EventHandler
    public void kill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            Player vistim = event.getEntity().getPlayer();

            SQLiteDatabase.newKill(killer.getName(),vistim.getName());
            killer.sendTitle(Main.getInstance().getConfig().getString("messages.youKilledPlayer"), null, 0, 30, 0);
        }
    }
}
