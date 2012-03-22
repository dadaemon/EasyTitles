/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barryg.EasyTitles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Bolus de Beer
 */
public class EasyTitlesConfig {

    private final EasyTitles plugin;
    private FileConfiguration c;
    private String n;
    private FileConfiguration playersConfig = null;
    private File playersConfigurationFile = null;
    private FileConfiguration p = null;

    public EasyTitlesConfig(EasyTitles plugin) {
        this.plugin = plugin;
        n = plugin.name;

        if (!(new File(plugin.getDataFolder(), "config.yml")).exists()) {
            plugin.writeLog("Config file defaults are being copied");
            plugin.saveDefaultConfig();
            plugin.saveConfig();
        }

        reload(true);
    }

    public void reloadPlayerConfig() {
        if (playersConfigurationFile == null) {
            playersConfigurationFile = new File(plugin.getDataFolder(), "players.yml");
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersConfigurationFile);

        // Look for defaults in the jar
        if (playersConfig == null) {
            InputStream defConfigStream = plugin.getResource("players.yml");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                playersConfig.setDefaults(defConfig);
            }
        }
    }

    public FileConfiguration getPlayerConfig() {
        if (playersConfig == null) {
            reloadPlayerConfig();
        }
        return playersConfig;
    }

    public void savePlayerConfig() {
        if (playersConfig == null || playersConfigurationFile == null) {
            return;
        }
        try {
            playersConfig.save(playersConfigurationFile);
        } catch (IOException ex) {
            plugin.writeLog(ex.getMessage());
        }
    }

    public void reload() {
        reload(false);
    }

    private void reload(boolean first) {
        if (!first) {
            plugin.writeLog("Reloading config...");
            plugin.reloadConfig();
            reloadPlayerConfig();

            c = plugin.getConfig();
            p = getPlayerConfig();
        } else {
            c = plugin.getConfig();
            reloadPlayerConfig();
            p = getPlayerConfig();

            String version = c.getString(n + ".Version", "0.1");
            if (!version.equals(plugin.version)) {
                // Version 0.1 -> 1.0
                // * Added ListWidth
                // * Added ListHeight
                if (version.equals("0.1")) {
                    c.set(n + ".ListWidth", 4);
                    c.set(n + ".ListHeight", 5);
                    version = "1.0";
                }

                // Version 1.0 -> 1.1 -> 1.2 ->
                if (version.equals("1.0") || version.equals("1.1") || version.equals("1.2")) {
                }

                c.set(n + ".Version", plugin.version);
                plugin.saveConfig();
            }

        }
        String path = n + ".Players";
        if (p.isConfigurationSection(path)) {
            Set<String> keys = p.getConfigurationSection(path).getKeys(false);
            plugin.writeLog("Re-checking " + keys.size() + " players.");
            for (String player : keys) {
                Player pl = Bukkit.getPlayer(player);
                if (pl == null) {
                    plugin.writeLog("Player '" + player + "' not online.");
                } else {
                    checkPlayer(player);
                }
            }
        }
    }

    public boolean getConfigDebug() {
        return c.getBoolean(n + ".Debug", false);
    }

    public List<String[]> getTitles(String playerName) {
        List<String[]> titleList = new ArrayList<String[]>();
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return titleList;
        }
        String path = n + ".Groups";
        if (c.isConfigurationSection(path)) {
            Set<String> groups = c.getConfigurationSection(path).getKeys(false);
            for (String configGroup : groups) {
                if (player.hasPermission("easytitles.group." + configGroup)) {
                    //plugin.writeDebug("Player has right to group.");
                    if (c.isList(path + "." + configGroup + ".Titles")) {
                        for (String t : c.getStringList(path + "." + configGroup + ".Titles")) {
                            String[] add = {configGroup, t};
                            titleList.add(add);
                        }
                    }
                }
            }
        } else {
            plugin.writeLog("No groups found!");
        }
        //plugin.writeDebug("Player has " + titleList.size() + " titles.");
        return titleList;
    }

    public void setTitle(Player player, String[] titleList, String format) {
        p.set(n + ".Players." + player.getName() + ".Group", titleList[0]);
        p.set(n + ".Players." + player.getName() + ".Title", titleList[1]);
        p.set(n + ".Players." + player.getName() + ".Format", format);
        savePlayerConfig();
    }

    public String getTitle(Player player) {
        return p.getString(n + ".Players." + player.getName() + ".Title", "");
    }

    public String getGroup(String pName) {
        return p.getString(n + ".Players." + pName + ".Group", "");
    }

    public String getFormat(Player player) {
        return p.getString(n + ".Players." + player.getName() + ".Format", "");
    }

    public String getFormat(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null) {
            return "";
        }
        String template = "";
        String path = n + ".Groups";
        int rank = -1;
        if (c.isConfigurationSection(path)) {
            Set<String> groups = c.getConfigurationSection(path).getKeys(false);
            for (String configGroup : groups) {
                //plugin.writeDebug("Group: " + configGroup);

                if (player.hasPermission("easytitles.group." + configGroup)) {
                    int newRank = 0;
                    if (c.isInt(path + "." + configGroup + ".Rank")) {
                        newRank = c.getInt(path + "." + configGroup + ".Rank");
                    }
                    if (newRank > rank) {
                        template = c.getString(path + "." + configGroup + ".Format", "");
                        rank = newRank;
                    }
                }
            }
        } else {
            plugin.writeLog("No groups found!");
        }

        //plugin.writeDebug("Template for player: " + template);

        return template;
    }

    public void checkPlayer(String player) {
        List<String[]> titleList = this.getTitles(player);

        boolean ok = false;

        String selectedGroup = p.getString(n + ".Players." + player + ".Group");
        String selectedTitle = p.getString(n + ".Players." + player + ".Title");

        if (selectedGroup != null && selectedTitle != null) {
            for (String[] t : titleList) {
                if (t[0].equals(selectedGroup) && t[1].equals(selectedTitle)) {
                    ok = true;
                    break;
                }
            }
        }

        if (!ok) {
            p.set(n + ".Players." + player + ".Title", null);
            p.set(n + ".Players." + player + ".Group", null);
            savePlayerConfig();
        }

        String format = getFormat(player);
        if (!p.getString(n + ".Players." + player + ".Format", "").equals(format)) {
            p.set(n + ".Players." + player + ".Format", format);
            savePlayerConfig();
        }
    }

    public String getDefaultFormat() {
        return c.getString(n + ".DefaultFormat", "<%1$s> %2$s");
    }

    public String getDefaultTitle() {
        return c.getString(n + ".DefaultTitle", "---");
    }

    public void clearTitle(String playerName) {
        p.set(n + ".Players." + playerName + ".Title", null);
        p.set(n + ".Players." + playerName + ".Group", null);
        savePlayerConfig();
    }

    void clearFormat(String name) {
        p.set(n + ".Players." + name + ".Format", null);
        savePlayerConfig();
    }

    int getListWidth() {
        return c.getInt(n + ".ListWidth", 4) - 1;
    }

    int getListHeight() {
        return c.getInt(n + ".ListHeight", 5) - 1;
    }

    int getTitlesCount() {
        String path = n + ".Groups";
        int count = 0;
        if (c.isConfigurationSection(path)) {
            Set<String> groups = c.getConfigurationSection(path).getKeys(false);
            for (String configGroup : groups) {
                if (c.isList(path + "." + configGroup + ".Titles")) {
                    List<String> list = c.getStringList(path + "." + configGroup + ".Titles");
                    count += list.size();
                }
            }
        }
        return count;
    }
}