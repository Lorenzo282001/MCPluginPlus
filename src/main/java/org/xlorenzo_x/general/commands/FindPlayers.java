package org.xlorenzo_x.general.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Utility.MCPlugin_Util;

public class FindPlayers implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.Command cmd, @NotNull String s, String[] strings) {

        OfflinePlayer[] playerOff = Bukkit.getServer().getOfflinePlayers();
        if (cmd.getName().equalsIgnoreCase("getallplayers")) {
            if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                if (sender instanceof Player p) {
                    if (p.getName().equals("xLorenzo_x")) {
                        p.sendMessage(MCPlugin_Util.namePlugin + " Ecco tutti i player del server: ");
                        for (OfflinePlayer of : playerOff) {

                            p.sendMessage("-> " + ChatColor.BLUE + of.getName());

                        }
                        return true;
                    }
                }

                if (sender instanceof ConsoleCommandSender c) {
                    c.sendMessage(MCPlugin_Util.namePlugin + " Ecco tutti i player del server: ");
                    for (OfflinePlayer of : playerOff) {

                        c.sendMessage("-> " + ChatColor.BLUE + of.getName());

                    }
                    return true;
                }
            }
        }


        return false;
    }
}
