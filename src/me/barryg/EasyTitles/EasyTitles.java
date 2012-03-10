/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barryg.EasyTitles;

import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author BarryG
 */
public class EasyTitles extends JavaPlugin {

    private static Logger logger;
    public PluginDescriptionFile pdfFile;
    public String name;
    public String version;
    public EasyTitlesConfig c;
    //private EasyTitlesPlayerListener easyTitlesPlayerListener;
    private EasyTitlesListener easyTitlesPlayerListener;

    @Override
    public void onDisable() {
        System.out.println(this + " is now disabled!");
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();
        // Register plugin description file
        pdfFile = this.getDescription();
        name = pdfFile.getName();
        version = pdfFile.getVersion();

        // Read config
        c = new EasyTitlesConfig(this);

        // Register PluginManager
        PluginManager pm = getServer().getPluginManager();

        // Register events
        easyTitlesPlayerListener = new EasyTitlesListener(this);
        pm.registerEvents(easyTitlesPlayerListener, this);

        EasyTitlesCommandExecutor ce = new EasyTitlesCommandExecutor(this);
        getCommand("et").setExecutor(ce);
        getCommand("title").setExecutor(ce);

        try {
            Metrics metrics = new Metrics();

            // Plot the total amount of protections
            metrics.addCustomData(this, new Metrics.Plotter("Total Titles") {

                @Override
                public int getValue() {
                    return c.getTitlesCount();
                }
            });

            metrics.beginMeasuringPlugin(this);
        } catch (IOException e) {
            writeLog(e.getMessage());
        }

        writeLog("== ENABLED ==");
    }

    public void writeLog(String text) {
        this.logger.info(text);
    }

    public void writeDebug(String text) {
        if (c.getConfigDebug()) {
            this.logger.info("[DEBUG] " + text);
        }
    }

    public String formatMessage(Player player) {
        String title = c.getTitle(player);
        String format = c.getFormat(player);

        if (format.equals("")) {
            c.checkPlayer(player.getName());
            format = c.getFormat(player);
            if (format.equals("")) {
                format = c.getDefaultFormat();
            }
        }
        if (title.equals("")) {
            title = c.getDefaultTitle();
        }

        format = format.replace("@", title);
        format = format.replace("&", "\u00A7");

        return format;
    }
}
