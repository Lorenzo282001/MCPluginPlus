package org.xlorenzo_x.Economics.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Economics.savePlayers.playerServerYML;
import org.xlorenzo_x.Utility.MCPlugin_Util;

import java.io.IOException;
import java.util.LinkedList;

public class OnClickMoneySymbol implements Listener {

    @EventHandler
    public void onClickRightMoney (@NotNull PlayerInteractEvent event){
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerServerYML.file);

            if (config.contains("server_players."+ player.getName()) && event.getItem() != null) {
                ItemStack moneyMaterial = event.getItem();
                int numOfMoneySet = moneyMaterial.getAmount();

                if (numOfMoneySet > 0) {
                    if (moneyMaterial != null) {
                        if (moneyMaterial.hasItemMeta()) {
                            ItemMeta meta = moneyMaterial.getItemMeta();

                            LinkedList<String> loreMoney = new LinkedList<>();

                            loreMoney.add(ChatColor.DARK_PURPLE + "MCEconomy");

                            if (meta.hasDisplayName() && meta.hasLore() && meta.getLore().equals(loreMoney)) {

                                // Distruggo l'oggetto dall'inventario del player
                                player.getInventory().remove(moneyMaterial);

                                // Recupero il bilancio attuale del player
                                double bilancioAttuale = config.getDouble("server_players." + player.getName() + ".balance");

                                // Salvo quanti soldi aggiungere al balance
                                double money = 0;
                                for (int s = 1; s <= numOfMoneySet; s++)
                                    money += Double.parseDouble(ChatColor.stripColor(meta.getDisplayName().split(" ")[1]));

                                // Sommo i money nel bilancio
                                config.set("server_players." + player.getName() + ".balance", bilancioAttuale + money);

                                try {
                                    config.save(playerServerYML.file);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                YamlConfiguration.loadConfiguration(playerServerYML.file);

                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_GREEN + " Aggiunti " + money + " money nel tuo bilancio personale!");
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler // Se piazzo il money cancello l'evento!
    public void onPlaceTheSymbol (@NotNull PlayerInteractEvent event) {

        if (event.getAction().name().contains("RIGHT_CLICK_BLOCK")) {
            // Verifica se l'azione è un clic destro su un blocco
            if (event.hasBlock() && event.getClickedBlock() != null) {
                // Verifica se il giocatore sta posizionando un blocco
                if (event.getItem() != null && event.getItem().getType() != Material.AIR) {

                    ItemStack m = event.getItem();
                    LinkedList<String> loreMoney = new LinkedList<>();

                    loreMoney.add(ChatColor.DARK_PURPLE + "MCEconomy");

                    if (m.hasItemMeta()) {
                        if (m.getItemMeta().getLore() != null) {
                            if (m.getItemMeta().getLore().equals(loreMoney)) {

                                event.setCancelled(true);

                            }
                        }
                    }
                }
            }
        }
    }
}
