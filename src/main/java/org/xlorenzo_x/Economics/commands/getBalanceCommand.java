package org.xlorenzo_x.Economics.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Economics.events.onJoin;
import org.xlorenzo_x.Utility.MCPlugin_Util;

import java.util.HashMap;
import java.util.Map;

public class getBalanceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("balance")) {

            if (sender instanceof Player player)
            {
                HashMap<Object, Object> playerEconomy = onJoin.loadPlayersFromYAML();

                if (!playerEconomy.isEmpty()) {
                    for (Map.Entry<Object, Object> data : playerEconomy.entrySet()) {
                        // data keys conterrà il nome del player -- data values conterrà una mappa con balance = 0.0
                        String playerName = (String) data.getKey();

                        if (playerName.equals(player.getName()))
                        {
                            Map<Object, Object> balancePlayer = (Map<Object, Object>) data.getValue();

                            for (Map.Entry<Object, Object> balance : balancePlayer.entrySet())
                            {
                                Double bal = (Double) balance.getValue();

                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.GOLD +" Money: " + ChatColor.DARK_GREEN + bal);
                                Bukkit.getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + ChatColor.WHITE + "Il giocatore " + ChatColor.GOLD + player.getName() + ChatColor.WHITE + " ha " + bal + " money");
                                break;
                            }

                            break;
                        }
                    }
                }
                else {
                    player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_RED + " Problemi con il caricamento del [playerOnServer.yml]");
                }

            }
            else {
                if (sender instanceof ConsoleCommandSender)
                {
                    if (args.length == 1)
                    {
                        OfflinePlayer[] ofs = Bukkit.getOfflinePlayers();

                        for (OfflinePlayer of : ofs)
                        {
                            if (args[0].equals(of.getName()))
                            {

                                HashMap<Object, Object> playerEconomy = onJoin.loadPlayersFromYAML();

                                if (!playerEconomy.isEmpty()) {
                                    for (Map.Entry<Object, Object> data : playerEconomy.entrySet()) {
                                        // data keys conterrà il nome del player -- data values conterrà una mappa con balance = 0.0
                                        String playerName = (String) data.getKey();

                                        if (playerName.equals(of.getName()))
                                        {
                                            Map<Object, Object> balancePlayer = (Map<Object, Object>) data.getValue();

                                            for (Map.Entry<Object, Object> balance : balancePlayer.entrySet())
                                            {
                                                Double bal = (Double) balance.getValue();

                                                Bukkit.getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + ChatColor.WHITE + "Il giocatore " + ChatColor.GOLD + of.getName() + ChatColor.WHITE + " ha " + bal + " money");
                                                break;
                                            }

                                            break;
                                        }
                                    }
                                }

                                break;
                            }
                        }

                    }
                    else {
                        Bukkit.getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + " Inserire un solo parametro <NomeGiocatore>!");
                    }
                }
            }

        }

        return false;
    }
}
