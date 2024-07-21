package org.xlorenzo_x.Economics.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Economics.savePlayers.playerServerYML;
import org.xlorenzo_x.Utility.MCPlugin_Util;

import java.io.IOException;
import java.util.LinkedList;

public class MoneyToHandCommand implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("takemoney"))
        {
            if (sender instanceof Player player)
            {
                if (args.length > 0)
                {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(playerServerYML.file);

                    LinkedList<String> loreMoney = new LinkedList<>();

                    loreMoney.add(MCPlugin_Util.loreMoney);

                    try {
                        int soldi;
                        soldi = Integer.parseInt(args[0]);

                        if (config.getDouble("server_players." + player.getName() + ".balance") >= soldi){
                            double newBalance = config.getDouble("server_players." + player.getName() + ".balance") - soldi;

                            newBalance = Math.round(newBalance * 100.0) / 100.0;

                            if (config.contains("server_players."+ player.getName()))
                            {
                                config.set("server_players." + player.getName() + ".balance", newBalance);

                                ItemStack money = new ItemStack(MCPlugin_Util.money, 1);
                                ItemMeta meta = money.getItemMeta();
                                meta.setDisplayName(MCPlugin_Util.displayNameMoney + soldi);
                                meta.setLore(loreMoney);
                                money.setItemMeta(meta);

                                player.getInventory().addItem(money);
                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_GREEN + " Ritirati dal bilancio " + soldi + " money");
                            }
                        }
                        else {
                            player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Non puoi andare al di sotto del tuo bilancio!");
                        }

                        config.save(playerServerYML.file);
                        YamlConfiguration.loadConfiguration(playerServerYML.file);

                    } catch (NumberFormatException e1) {
                        try {
                            double soldi;
                            soldi = Double.parseDouble(args[0]);
                            soldi = Math.round(soldi * 100.0) / 100.0;

                            if (config.getDouble("server_players." + player.getName() + ".balance") >= soldi){
                                double newBalance = config.getDouble("server_players." + player.getName() + ".balance") - soldi;

                                newBalance = Math.round(newBalance * 100.0) / 100.0;

                                if (config.contains("server_players."+ player.getName()))
                                {
                                    config.set("server_players." + player.getName() + ".balance", newBalance);

                                    ItemStack money = new ItemStack(MCPlugin_Util.money, 1);
                                    ItemMeta meta = money.getItemMeta();
                                    meta.setDisplayName(ChatColor.GOLD + "Money " + ChatColor.DARK_GREEN + soldi);
                                    meta.setLore(loreMoney);
                                    money.setItemMeta(meta);

                                    player.getInventory().addItem(money);
                                    player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_GREEN + " Ritirati dal bilancio " + soldi + " money");
                                }
                            }
                            else {
                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Non puoi andare al di sotto del tuo bilancio!");
                            }

                            config.save(playerServerYML.file);
                            YamlConfiguration.loadConfiguration(playerServerYML.file);

                        } catch (NumberFormatException | IOException e2) {
                            player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Formato numero non riconosciuto!");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Inserire la quantità di money da prelevare!");
                }
            }
            else {
                Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + " Non puoi eseguire questo comando!");
            }




        }

        return false;
    }

    // Verificare se un itemstack di lore MCEconomy vada sopra un altro itemstack dello stesso tipo (che deve essere money).
    // Successivamente impostare un unico itemstack money al player, che sarà un moneyObj dato dalla somma dei soldi dei primi 2 itemstack!
    @EventHandler
    public void onPlayerMoveMoneyToMoney(@NotNull InventoryClickEvent event)
    {
        if (event == null)
            return;

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack itemPicked = event.getCursor();

        // Verifica se il giocatore ha un oggetto nel cursore
        if (itemPicked != null && itemPicked.getType() != Material.AIR) {

            // Verificare se l'oggetto tenuto nel cursore sia l'itemstack che cerco!
            ItemStack try_money = new ItemStack(MCPlugin_Util.money, 1);
            ItemMeta meta = try_money.getItemMeta();

            LinkedList<String> loreMoney = new LinkedList<>();

            loreMoney.add(MCPlugin_Util.loreMoney);

            meta.setLore(loreMoney);
            try_money.setItemMeta(meta);

            if (itemPicked.getItemMeta() != null) {
                if (!itemPicked.getType().equals(try_money.getType())) {
                    if (itemPicked.getItemMeta().getLore() != null) {
                        if (!itemPicked.getItemMeta().getLore().equals(try_money.getItemMeta().getLore())) {
                            return;
                        }
                    } else {
                        return;
                    }
                    return;
                }
            } else {
                return;
            }

            // Ottieni l'ItemStack che il giocatore ha cliccato nell'inventario
            if (event.getCurrentItem() != null) {
                ItemStack clickedItem = event.getCurrentItem();

                if (clickedItem.getItemMeta() != null) {
                    if (!clickedItem.getType().equals(try_money.getType())) {
                        if (clickedItem.getItemMeta().getLore() != null) {
                            if (!clickedItem.getItemMeta().getLore().equals(try_money.getItemMeta().getLore())) {
                                return;
                            }
                        } else {
                            return;
                        }
                        return;
                    }
                } else {
                    return;
                }

                // Qui sia clickedItem che itemPicked (nel cursore) sono entrambi di tipo money.
                double moneyItemCursor = Double.parseDouble(ChatColor.stripColor(itemPicked.getItemMeta().getDisplayName().split(" ")[1]));
                double moneyItemClicked = Double.parseDouble(ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName().split(" ")[1]));

                double sumMoney = moneyItemCursor + moneyItemClicked; // Somma dei due money

                // Verifica se l'ItemStack cliccato non è nullo e non è aria
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    player.sendMessage(String.valueOf(sumMoney));
                    // Il giocatore ha cliccato su un altro ItemStack mentre tiene un oggetto nel cursore

                    // Rimuovo gli oggetti dall'inventario!

                    player.getInventory().remove(itemPicked);
                    player.getInventory().remove(clickedItem);

                    try {
                        // Aggiungo un nuovo obj money, che sarà dato dalla somma dei due precendenti
                        // Verificare se l'oggetto tenuto nel cursore sia l'itemstack che cerco!
                        ItemStack money = new ItemStack(MCPlugin_Util.money, 1);
                        ItemMeta new_meta = try_money.getItemMeta();

                        new_meta.setDisplayName(MCPlugin_Util.displayNameMoney + sumMoney);

                        LinkedList<String> new_loreMoney = new LinkedList<>();

                        new_loreMoney.add(MCPlugin_Util.loreMoney);

                        new_meta.setLore(new_loreMoney);
                        money.setItemMeta(new_meta);

                        player.getInventory().addItem(money);
                    } catch (Exception e) {
                        Bukkit.getConsoleSender().sendMessage(e.getMessage());
                    }


                }
            }
        }
    }




}
