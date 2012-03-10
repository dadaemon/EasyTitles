/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barryg.EasyTitles;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author BarryG
 */
public class EasyTitlesCommandExecutor implements CommandExecutor {

    private EasyTitles plugin;
    // Initialise executable commands for players
    // aBCDefgHijklMnopQrsTuVwxyz
    private List<String> commandList = Arrays.asList("help", "h", "t", "r", "reload", "l", "list", "u", "use", "c", "clear", "f", "find");
    // Initialise executable commands for console
    // abcdefgHijklmnopqrStuVwxyz
    private List<String> consoleCommandList = Arrays.asList("help", "h", "r", "reload");

    public EasyTitlesCommandExecutor(EasyTitles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player != null) {
            // Player commands
            if (cmd.getName().equalsIgnoreCase("et") || cmd.getName().equalsIgnoreCase("title")) {
                if (args.length > 0) {
                    if (isCommand(args[0])) {
                        //sm("Doing command " + args[0], sender);
                        if (doCommand(sender, cmd, label, args)) {
                        } else {
                            plugin.writeLog("Something went wrong executing command '" + cmd + "' (or command isn't implemented yet)");
                        }
                    } else {
                        sm("This is not a recognised command.", sender);
                        sm("For help on EasyTitles use /epm help.", sender);
                    }
                    return true;
                }
                return false;
            }
        } else {
            // Console commands
            if (cmd.getName().equalsIgnoreCase("et") || cmd.getName().equalsIgnoreCase("title")) {
                if (args.length > 0) {
                    if (isConsoleCommand(args[0])) {
                        if (!doCommand(sender, cmd, label, args)) {
                            sm("Something went wrong executing command '" + cmd + "' (or command isn't implemented yet)", sender);
                        }
                    } else {
                        sm("This is not a recognised command.", sender);
                        sm("For help on EasyPM use epm help.", sender);
                    }
                } else {
                    sm("For help on EasyPM use epm help.", sender);
                }
                return true;
            }
            return true;
        }
        return false;
    }

    private boolean isCommand(String string) {
        return this.commandList.contains(string);
    }

    private void sm(String string, CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "[" + plugin.name + "] " + ChatColor.WHITE + string);
    }

    private boolean doCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (cmd.getName().equalsIgnoreCase("et") || cmd.getName().equalsIgnoreCase("title")) {
            if (args[0].equals("h") || args[0].equals("help")) {
                sm(ChatColor.GOLD + "  --== " + ChatColor.DARK_RED + plugin.name + ChatColor.WHITE + " version " + plugin.pdfFile.getVersion() + ChatColor.GOLD + " ==--", player);
                if (player != null) {
                    // Player help
                    sm("List titles: " + ChatColor.GRAY + "/title list " + ChatColor.BLUE + "[pagenumber]", sender);
                    sm("Find title: " + ChatColor.GRAY + "/title find " + ChatColor.BLUE + "[title]", sender);
                    sm("Use title: " + ChatColor.GRAY + "/title use " + ChatColor.BLUE + "[titlenumber]", sender);
                    sm("Clear title: " + ChatColor.GRAY + "/title clear", sender);
                } else {
                    // Console help
                    sm("No console commands available.", sender);
                }

                return true;
            } else if (args[0].equals("t")) {
                sm("Your title: " + plugin.c.getTitle(player), player);
                return true;
            } else if (args[0].equals("c") || args[0].equals("clear")) {
                plugin.c.clearTitle(player.getName());
                sm("Your title has been cleared.", player);
                return true;
            } else if (args[0].equals("r") || args[0].equals("reload")) {
                if (player != null) {
                    if (!player.hasPermission("easytitles.admin.reload")) {
                        sm(ChatColor.RED + "You don't have permission for that command!", sender);
                        return true;
                    }
                }
                plugin.c.reload();
                sm("Reloaded configs...", sender);
                return true;

            } else if (args[0].equals("f") || args[0].equals("find")) {
                if (player == null) {
                    return false;
                }

                List<String[]> titleList = plugin.c.getTitles(player.getName());
                if (titleList.isEmpty()) {
                    sm("You have no titles.", player);
                    return true;
                }

                if (args.length != 2) {
                    sm("Please enter a string.", player);
                    sm("To find a title type /title find TITLE", player);
                    return true;
                }

                String find = args[1].toLowerCase();

                sm(ChatColor.GOLD + "  --== " + ChatColor.DARK_RED + plugin.name + ChatColor.WHITE + " Find '" + find + "'" + ChatColor.GOLD + " ==--", player);
                int count = 0;
                String text = "";
                int width = plugin.c.getListWidth();
                int height = plugin.c.getListHeight();

                int maxSize = width * height;
                int i;
                for (i = 0; i < titleList.size(); i++) {
                    if (titleList.get(i)[1].toLowerCase().contains(find)) {
                        text += "" + ChatColor.WHITE + i + ": " + ChatColor.GREEN + titleList.get(i)[1] + " ";
                        
                        count++;                        
                        if (count == width) {
                            if (!"".equals(text)) {
                                sm(text, player);
                            }
                            text = "";
                            count = 0;
                        }
                        
                        maxSize--;
                        if(maxSize == 0) {
                            text = "";
                            sm(ChatColor.RED + "Too many found!" + ChatColor.WHITE + " Please be more specific.", player);
                            break;
                        }                        
                    }

                }
                
                if(maxSize == width * height                ) {
                    text = "No titles found with the text '" + ChatColor.AQUA + find + ChatColor.WHITE + "'";
                }
                
                if (!text.equals("")) {
                    sm(text, player);
                }
                return true;
            } else if (args[0].equals("l") || args[0].equals("list")) {
                if (player == null) {
                    return false;
                }

                List<String[]> titleList = plugin.c.getTitles(player.getName());
                if (titleList.isEmpty()) {
                    sm("You have no titles.", player);
                    return true;
                }

                int page = 0;
                if (args.length == 2) {
                    try {
                        page = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException nfe) {
                    }
                }

                if (page < 0) {
                    page = 0;
                }
                int width = plugin.c.getListWidth();
                int height = plugin.c.getListHeight();

                int maxPages = titleList.size() / (width * height);
                if (page > maxPages) {
                    page = maxPages;
                }
                sm(ChatColor.GOLD + "  --== " + ChatColor.DARK_RED + plugin.name + ChatColor.WHITE + " Page " + (page + 1) + " / " + (maxPages + 1) + ChatColor.GOLD + " ==--", player);
                page = page * (width * height); // The number is width * height
                int count = 1;
                String text = "";


                for (int i = page; i < page + (width * height); i++) {
                    if (i < titleList.size()) {
                        text += "" + ChatColor.WHITE + i + ": " + ChatColor.GREEN + titleList.get(i)[1] + " ";
                    }

                    if (count == width) {
                        if (!"".equals(text)) {
                            sm(text, player);
                        }
                        if (i >= titleList.size()) {
                            break;
                        }
                        text = "";
                        count = 1;
                    } else {
                        count++;
                    }
                }

                return true;
            } else if (args[0].equals("u") || args[0].equals("use")) {
                if (player == null || args.length != 2) {
                    return false;
                }
                int nr = -1;
                try {
                    nr = Integer.parseInt(args[1]);
                } catch (NumberFormatException nfe) {
                }

                if (nr == -1) {
                    sm("Please enter a valid number.", player);
                    sm("To use a title type /title use TITLENUMBER", player);
                    return true;
                }

                List<String[]> titleList = plugin.c.getTitles(player.getName());
                if (nr >= titleList.size()) {
                    sm("Please enter a valid number.", player);
                    sm("To list your titles type /title list [PAGENUMBER]", player);
                    return true;
                }

                String format = plugin.c.getFormat(player.getName());

                plugin.c.setTitle(player, titleList.get(nr), format);
                sm("Your title has been changed to '" + titleList.get(nr)[1] + "'", player);
                return true;
            }

        }
        return false;
    }

    private String getCommandList() {
        String itemList = "";
        for (String item : commandList) {
            if (item.length() > 1) {
                itemList = itemList + item + ", ";
            }
        }
        itemList = itemList.substring(0, itemList.length() - 2);
        return itemList;
    }

    private String getConsoleCommandList() {
        String itemList = "";
        for (String item : consoleCommandList) {
            if (item.length() > 1) {
                itemList = itemList + item + ", ";
            }
        }
        itemList = itemList.substring(0, itemList.length() - 2);
        return itemList;
    }

    private boolean isConsoleCommand(String string) {
        return this.consoleCommandList.contains(string);
    }
}
