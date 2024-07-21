package org.xlorenzo_x.general.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.Utility.TextOnScreen;

public class sendAllToBarCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("sendToAll")) {

            if (sender instanceof Player p)
            {
                // Bisogna che il Player abbia il permesso -- utility.sendall.use
                if (p.hasPermission("utility.sendall.use")) {

                    if (args.length > 0) // Quindi ha almeno un parametro!
                    {
                        StringBuilder message = new StringBuilder();

                        for (String arg : args) {
                            message.append(arg).append(" ");
                        }

                        new TextOnScreen().showTextOnScreenAll(ChatColor.RED + p.getDisplayName()  + ChatColor.WHITE + ChatColor.BOLD + ": " + message);
                    }
                    else {
                        // Nessun Parametro inserito
                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.GREEN + " Devi inserire un messaggio!");
                    }

                }
                else {
                    p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Non hai il permesso per utilizzare questo comando!");
                }
            }
            else {
                sender.sendMessage( MCPlugin_Util.namePlugin +  " Non sei un player!");
            }

        }

        return false;
    }
}
