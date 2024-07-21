package org.xlorenzo_x.BotMinecraft.PersonalBot.commands;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.BotMinecraft.PersonalBot.PersonalBot;
import org.xlorenzo_x.Utility.MCPlugin_Util;

import java.io.IOException;
import java.util.Map;

public class DestroyBotCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender , @NotNull Command cmd, @NotNull String s, @NotNull String[] strings) {

        if (cmd.getName().equalsIgnoreCase("despawnPB")) {
            if (sender instanceof Player p)
            {
                if (p.hasPermission("botPersonal.despawn")) {

                    boolean find = false;
                    for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet()) {
                        Player player = entry.getKey();
                        PersonalBot personalBot = entry.getValue();

                        if (player.equals(p)) {
                            if (personalBot.alreadySpawned) { // Se è spawnato, allora puoi distruggerlo!
                                personalBot.alreadySpawned = false;

                                LivingEntity entity = personalBot.personalBotFollow;

                                if (personalBot.isPersonalBot(entity) && personalBot.ownerBot.getName().equalsIgnoreCase(player.getName()))
                                {
                                    try {
                                        PersonalBot.saveMapInventoryPersonalGui();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    entity.remove();
                                    personalBot.ownerBot = null;
                                    personalBot.personalBotFollow = null;
                                    personalBot.autoChunk = null;
                                    p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED +" Il tuo bot personale è andato via!");
                                    PersonalBot.listaPersonalBotServer.remove(player);

                                    // Tolgo lo stick dall'inventario del player
                                    Inventory inv = p.getInventory();

                                    for (ItemStack i : inv.getContents())
                                    {
                                        if (i != null) {
                                            if (i.getType().equals(Material.STICK) && i.getItemMeta().getDisplayName().equals(MCPlugin_Util.richiamoNameOnItemStack))
                                            {
                                                inv.remove(i);
                                            }
                                        }
                                    }
                                }

                            }
                            find = true;
                            break;
                        }

                    }

                    if (!find) {
                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Non hai ancora spawnato nessun bot!");
                    }

                }
                else {
                    p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Non hai i permessi per eseguire questo comando!");
                }


            }
            else {
                sender.sendMessage(MCPlugin_Util.namePlugin + " Non puoi eseguire questo comando!");
            }
        }

        return false;
    }
}
