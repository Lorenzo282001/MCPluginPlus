package org.xlorenzo_x.BotMinecraft.PersonalBot.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.BotMinecraft.PersonalBot.GUI_PersonalBot.GuiBot;
import org.xlorenzo_x.BotMinecraft.PersonalBot.GUI_PersonalBot.SaveInventory.InventoryGui_YAMLConf;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class TakeInventoryOfPlayer implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("pbgui")) {
            if (sender instanceof Player p) {
                if (p.hasPermission("botPersonal.openInventoryPlayer")) {

                    if (args.length > 0)
                    {
                        // Questo restituisce tutti i player
                        OfflinePlayer[] offlinePlayers = Bukkit.getServer().getOfflinePlayers();
                        boolean findPlayer = false;
                        boolean playerHaveInv = true;
                        for (OfflinePlayer checkPlayer: offlinePlayers)
                        {
                            if (args[0].equalsIgnoreCase(checkPlayer.getName()))
                            {
                                findPlayer = true;
                                // Verifico se il checkPlayer's Name è nel yml Inventory
                                FileInputStream inputStream;
                                try {
                                    inputStream = new FileInputStream(InventoryGui_YAMLConf.file.getPath());
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }

                                Map<String, Object> data = new Yaml().load(inputStream);

                                // Estrai i dati relativi agli inventari
                                LinkedHashMap<String, Object> inventories = (LinkedHashMap<String, Object>) data.get("inventories");
                                LinkedHashMap<String, Object> players = (LinkedHashMap<String, Object>) inventories.get("players");

                                // Itera sui giocatori
                                if (players != null) {
                                    for (Map.Entry<String, Object> playerEntry : players.entrySet()) {

                                        //String playerId = playerEntry.getKey();
                                        LinkedHashMap<String, Object> playerData = (LinkedHashMap<String, Object>) playerEntry.getValue();

                                        // Estrai i dati specifici del giocatore
                                        String playerName = (String) playerData.get("playerName");

                                        if (playerName.equalsIgnoreCase(checkPlayer.getName())) { // Controllo a chi devo caricare l' inv!
                                            playerHaveInv = true; // Se entra qui, il player ha un inv
                                            GuiBot g = new GuiBot(ChatColor.DARK_RED + "Inventory of " + playerName);
                                            LinkedHashMap<String, Object> inventory = (LinkedHashMap<String, Object>) playerData.get("inventory");

                                            // Ora puoi fare qualcosa con i dati del giocatore

                                            // Puoi anche iterare sull'inventario del giocatore se necessario
                                            for (Map.Entry<String, Object> inventoryEntry : inventory.entrySet()) {
                                                String slotInventario = inventoryEntry.getKey();
                                                Object itemStack = inventoryEntry.getValue();

                                                // Ignoriamo il warning di unchecked cast
                                                Map<String, Object> itemData = (Map<String, Object>) itemStack;

                                                Material material = Material.valueOf((String) itemData.get("material")); // Tipo dell'oggetto
                                                int amount = (int) itemData.get("quantity"); // Quantità

                                                ItemStack nuovoItemStack = new ItemStack(material, amount);

                                                // Verificare se l'oggetto ha un customName
                                                if (itemData.containsKey("customName") && itemData.get("customName") instanceof  String customName) {
                                                    ItemMeta itemMeta = nuovoItemStack.getItemMeta();
                                                    itemMeta.setDisplayName(customName);
                                                    nuovoItemStack.setItemMeta(itemMeta);
                                                }

                                                // Verifico la durabilità dell'item
                                                try {
                                                    if (itemData.get("durability") instanceof Integer d) {

                                                        int durability = d;
                                                        ItemMeta itemMeta = nuovoItemStack.getItemMeta();

                                                        if (itemMeta instanceof Damageable damageableMeta) { // Verifica se l'ItemMeta supporta la durabilità
                                                            // Effettua il cast dell' ItemMeta a Damageable
                                                            damageableMeta.setDamage(durability); // Imposta la durabilità

                                                            nuovoItemStack.setItemMeta(itemMeta); // Applica l'ItemMeta modificata all'ItemStack
                                                        }
                                                    }

                                                } catch (Exception e) {
                                                    Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + " [ERROR - Durability Personal Gui] " + Arrays.toString(e.getStackTrace()));
                                                }

                                                // Verifica se l'oggetto ha degli incantesimi

                                                // Verifica se l'oggetto ha degli incantesimi
                                                if (itemData.containsKey("enchants") && itemData.get("enchants") instanceof Map) {
                                                    Map<String, Object> enchantMap = (Map<String, Object>) itemData.get("enchants");

                                                    for (Map.Entry<String, Object> enchantEntry : enchantMap.entrySet()) {
                                                        if (enchantEntry.getValue() instanceof Map<?, ?>) {
                                                            Map<String, String> theEnchant = (Map<String, String>) enchantEntry.getValue();

                                                            // Accedi al primo incantesimo nella mappa
                                                            if (!theEnchant.isEmpty()) {

                                                                String nomeIncantesimo = null;
                                                                int livello = -1;

                                                                int a = 1;
                                                                int cicli_Ench_level = 2;
                                                                for (Map.Entry<String, String> f: theEnchant.entrySet())
                                                                {
                                                                    String tempE = f.getValue();

                                                                    if (a == 1) // Nome incantesimo
                                                                    {
                                                                        nomeIncantesimo = tempE;
                                                                    }

                                                                    if (a == 2) // livello in formato Intero (giù)
                                                                    {
                                                                        livello = Integer.parseInt(tempE);
                                                                    }

                                                                    if (a <= cicli_Ench_level) // Faccio 2 giri, il primo conterrà in value nome enchant e il secondo conterrà il level
                                                                    {
                                                                        a++;
                                                                    }
                                                                    else {
                                                                        break;
                                                                    }
                                                                }

                                                                // Carico l'incantesimo
                                                                if (nomeIncantesimo != null){
                                                                    if (livello > -1)
                                                                    {
                                                                        nuovoItemStack.addEnchantment(Enchantment.getByName(nomeIncantesimo), livello);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                g.getInventory().setItem(Integer.parseInt(slotInventario), nuovoItemStack);
                                            }

                                            g.openInventory(p);
                                            break;
                                        }
                                        else {
                                            playerHaveInv = false;
                                        }

                                    }
                                }

                                break;
                            }
                        }

                        if (!findPlayer)
                        {
                            p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Nessun Player con nome: <" + args[0] + ">");
                        } else if (!playerHaveInv) {
                            p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Il player <" + args[0] + "> non ha mai usato il bot personale!");
                        }
                    }
                    else {
                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Comando: /pbgui <player>");
                    }

                }
                else {
                    p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_RED + " Non hai il permesso per eseguire questo comando!");
                }
            }
            else {
                sender.sendMessage(MCPlugin_Util.namePlugin + " Non sei un player!");
            }
        }

                    return false;
    }
}
