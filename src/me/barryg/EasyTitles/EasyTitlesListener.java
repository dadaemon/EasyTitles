package me.barryg.EasyTitles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class EasyTitlesListener implements Listener {

    private EasyTitles plugin;

    public EasyTitlesListener(EasyTitles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        String newFormat = plugin.formatMessage(player);

        String.format(newFormat, event.getPlayer(), event.getMessage());
        event.setFormat(newFormat);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.c.checkPlayer(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        checkCommand(event.getMessage());
    }

    @EventHandler
    public void onServerCommandEvent(ServerCommandEvent event) {
        checkCommand(event.getCommand());
    }

    private void checkCommand(String message) {
        String msg = message.toLowerCase();
        if (msg.charAt(0) == '/') {
            msg = msg.substring(1);
        }
        //plugin.writeDebug(msg);
        if (msg.startsWith("perm player addgroup") || msg.startsWith("perm player removegroup")) {
            String[] msgSplit = msg.split(" ");
            Player player = Bukkit.getPlayer(msgSplit[3]);
            if(player != null) {
                //plugin.writeDebug("CheckPlayer: " + player.getName());
                plugin.c.clearFormat(player.getName());
            }
        }
    }
}
