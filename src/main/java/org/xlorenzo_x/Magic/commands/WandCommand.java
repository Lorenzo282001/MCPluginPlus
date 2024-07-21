package org.xlorenzo_x.Magic.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Magic.MagicWand;
import org.xlorenzo_x.Utility.MCPlugin_Util;

import java.util.List;

public class WandCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] strings) {

        if (cmd.getName().equalsIgnoreCase("wand")) {
            if (sender instanceof Player p)
            {

                // Verificare se il player ha già la wand
                boolean c = checkWandOnInv(p);

                if (!c) {
                    MagicWand magicWand = new MagicWand();

                    p.getInventory().addItem(magicWand.giveWand());

                }
                else {
                    p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_PURPLE  + " Hai già una wand in mano!");
                }
            }
        }


        return false;
    }

    public static boolean checkWandOnInv(@NotNull Player p) {

        Inventory playerInv = p.getInventory();
        MagicWand w = new MagicWand();

        for (ItemStack i: playerInv)
        {
            if (i != null) {
                ItemMeta meta = i.getItemMeta();

                if (meta.getDisplayName().equals(String.valueOf(MCPlugin_Util.nameWand))) {
                    if (meta.getLore().equals(List.of(ChatColor.DARK_PURPLE + "MCPlugin"))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


}
