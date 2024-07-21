package org.xlorenzo_x.general.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Utility.MCPlugin_Util;

public class FlyCommand implements CommandExecutor {
    @Override                                                                            // Nome comando
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String commandLabel, String[] args) {

        if (sender instanceof ConsoleCommandSender || sender instanceof Player) {

            if (cmd.getName().equalsIgnoreCase("fly")){

                if (sender.hasPermission("fly.use"))
                {
                    if (args.length == 0)
                    {
                        if (sender instanceof  Player p) {

                            if (p.getName().equals("xLorenzo_x")){
                                if (p.getAllowFlight()) {
                                    p.setAllowFlight(false);
                                    p.sendMessage( MCPlugin_Util.namePlugin + ChatColor.RED + " Fly disabilitata");
                                }
                                else {
                                    p.setAllowFlight(true);
                                    p.sendMessage( MCPlugin_Util.namePlugin  + ChatColor.BLUE + ChatColor.MAGIC + " ##" + ChatColor.BLUE + " Fly abilitata " + ChatColor.MAGIC + "##");
                                }
                            }
                            return true;
                        }
                        else {
                            sender.sendMessage( MCPlugin_Util.namePlugin + " Non sei un player");
                        }
                    }
                    else if (args.length == 2) {
                        if (sender.hasPermission("fly.give")) {
                            Player target = Bukkit.getServer().getPlayerExact(args[0]);

                            if (sender instanceof Player p) {

                                if (target != null && p.getName().equals("xLorenzo_x")) {
                                    if (args[1].equalsIgnoreCase("on")) {
                                        target.setAllowFlight(true);
                                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.BLUE + ChatColor.MAGIC + " ##" + ChatColor.BLUE + " Fly abilitata a " + target.getName() + ChatColor.MAGIC + " ##");
                                    } else if (args[1].equalsIgnoreCase("off")) {
                                        target.setAllowFlight(false);
                                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Fly disabilitata a " + target.getName());
                                    } else {
                                        p.sendMessage(MCPlugin_Util.namePlugin + " arg sconosciuto");
                                    }
                                }

                            } else {
                                sender.sendMessage(MCPlugin_Util.namePlugin + " Non sei un player!");
                            }
                        }
                        else {
                            sender.sendMessage( MCPlugin_Util.namePlugin + ChatColor.RED + " Non hai il permesso per dare la fly a qualcuno!");
                        }
                    }
                    else {
                        if (args.length == 1)
                        {
                            sender.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Inserire on/off come secondo argomento!");
                        }
                        else {
                            sender.sendMessage( MCPlugin_Util.namePlugin + " arg sconosciuto");
                        }

                    }

                    return true;
                }
                else {
                    sender.sendMessage( MCPlugin_Util.namePlugin + " Non hai il permesso per usare questo comando");
                }



            }

        }
        else {
            sender.sendMessage(MCPlugin_Util.namePlugin + " Non puoi eseguire questo comando!");
        }

        return false;
    }
}
