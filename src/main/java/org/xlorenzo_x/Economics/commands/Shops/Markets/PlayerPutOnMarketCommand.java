package org.xlorenzo_x.Economics.commands.Shops.Markets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xlorenzo_x.Economics.commands.Shops.Markets.YAML_Market.Market_GuiYaml;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.start.Main;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class PlayerPutOnMarketCommand implements CommandExecutor {

    private static int defaultTimer = 30; // 30 Minuti tempo limite in market di default!
    private static final int MAX_TIMER = 1440; // 24 hour in minutes

    public static int current_item;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("putmarket"))
        {
            if (sender instanceof Player player)
            {
                if (args.length > 0)
                {
                    if (args.length > 2)
                    {
                        player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Comando Errato: /putmarket <Price> -tempo");
                        return false;
                    }

                    if (args.length == 2)
                    {
                        try{
                            defaultTimer = Integer.parseInt(args[1]);

                            if (defaultTimer > MAX_TIMER)
                            {
                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Max Timer: " + MAX_TIMER + " minuti");
                                return false;
                            }

                        }catch (NumberFormatException e)
                        {
                            player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Formato time non corretto!");
                            return false;
                        }
                    }

                    // Ci deve essere solo un argomento!
                    double price;

                    try {
                         price = Double.parseDouble(args[0]);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Prezzo non corretto!");
                        return false;
                    }

                    if (price > 0) {
                        ItemStack item = takeItemStackFromPlayerInv(player);

                        if (item != null) {
                            if (item.getType() == Material.AIR) {
                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Slot in mano vuoto!");
                                return false;
                            }

                            if (itemNotAllowed(item.getType()))
                            {
                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Item non vendibile!");
                                return false;
                            }

                            FileConfiguration conf = Market_GuiYaml.conf;

                            boolean nick = hasDisplayName(item);

                            // Verifico se market sia pieno o vuoto
                            checkMarketEmpty();

                            // Aggiungo 1 al currentItem perchè sto inizializzando un nuovo oggetto nel mercato
                            current_item++;

                            conf.createSection("market." + current_item + "." + item.getType().name());
                            conf.set("market." + current_item + "." + item.getType().name() + ".size", item.getAmount());

                            conf.set("market." + current_item + "." + item.getType().name() + ".price", price);


                            conf.set("market." + current_item + "." + item.getType().name() + ".owner", player.getName());


                            if (nick) {
                                conf.set("market." + current_item + "." + item.getType().name() + ".displayName", item.getItemMeta().getDisplayName());
                            }

                            if (!item.getEnchantments().isEmpty()) { // Se ha degli enchants!

                                conf.createSection("market." + current_item + "." + item.getType().name() + ".enchants");

                                // Ottieni la mappa degli incantesimi e itera su di essa

                                Map<Enchantment, Integer> enchantments = item.getEnchantments();
                                for (Map.Entry<Enchantment, Integer> enchantsEntry : enchantments.entrySet()) {
                                    Enchantment enchantment = enchantsEntry.getKey();
                                    String level = String.valueOf(enchantsEntry.getValue());

                                    conf.createSection("market." + current_item + "." + item.getType().name() + ".enchants." + enchantment.getName());

                                    conf.set("market." + current_item + "." + item.getType().name() + ".enchants." + enchantment.getName(), level);

                                }
                            }

                            // Add time
                            if (defaultTimer > 0) {
                                conf.createSection("market." + current_item + "." + item.getType().name() + ".time");
                                conf.set("market." + current_item + "." + item.getType().name() + ".time.minutes", defaultTimer);
                                conf.set("market." + current_item + "." + item.getType().name() + ".time.seconds", 0);
                            }
                            else {
                                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Tempo errato!");
                                return false;
                            }

                            try {
                                conf.save(Market_GuiYaml.file);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            player.getInventory().remove(item);
                            YamlConfiguration.loadConfiguration(Market_GuiYaml.file);

                            // Azionare il timer pari DefaultTimer -- da controllare nel market
                            startTimer(current_item,item.getType().name(), (defaultTimer * 60) * 20);

                            return true;
                        } else {
                            player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " ItemStack non riconosciuto!");
                            return false;
                        }
                    }
                    else {
                        player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Il Prezzo deve essere maggiore di 0");
                        return false;
                    }
                }
                else {
                    player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Comando Errato: /putmarket <Price> -tempo");
                    return false;
                }
            }
            else {
                sender.sendMessage(MCPlugin_Util.namePlugin + " Devi essere un player per inserire oggetti nell'inventario!");
                return false;
            }
        }


        return false;
    }

    public static void startTimer(int ct ,String nomeOggetto, int minutesInTicks){ // time è un numero che rappresenta i minuti!

        AtomicInteger countdownDuration = new AtomicInteger(minutesInTicks); // Supponiamo che minutesInTicks rappresenti il numero di minuti

        // Start timer
        final BukkitTask[] task = new BukkitTask[1]; // Dichiarazione di un array di lunghezza 1 per contenere l'oggetto BukkitTask

        task[0] = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {

            // Riduci il countdown di 20 ticks ad ogni esecuzione
            countdownDuration.addAndGet(-20);

            // Ottieni il valore corrente di countdownDuration
            int valoreCountdown = countdownDuration.get();

            // Verifica se il countdown è arrivato a zero o meno
            if (valoreCountdown <= 0) {
                // Se il countdown è terminato, interrompi il timer
                task[0].cancel();
            }

            // Converti i ticks in secondi
            int seconds = valoreCountdown / 20;

            // Calcola i minuti rimanenti
            int minutes = seconds / 60;

            // Calcola i secondi rimanenti dopo aver sottratto i minuti
            seconds %= 60;

            // Inizio a modificare il file!
            FileConfiguration conf = Market_GuiYaml.conf;

            conf.set("market." + ct + "." + nomeOggetto + ".time.minutes", minutes);
            conf.set("market." + ct + "." + nomeOggetto + ".time.seconds", seconds);

            try {
                conf.save(Market_GuiYaml.file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            YamlConfiguration.loadConfiguration(Market_GuiYaml.file);

        }, 0, 20);


    }

    private boolean hasDisplayName(@NotNull ItemStack item) {

        if (item.hasItemMeta())
        {
            return !item.getItemMeta().getDisplayName().equals(item.getType().name());
        }

        return false;
    }

    private @Nullable ItemStack takeItemStackFromPlayerInv (@NotNull Player player)
    {
        ItemStack obj = player.getItemInHand();

        if (obj != null || obj.getType() != Material.AIR)
        {
            return obj;
        }

        return null;
    }

    private static void checkMarketEmpty () // Verifico se il market sia pieno o vuoto, nel caso sia vuoto imposto a 0 il contatore!
    {
        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(Market_GuiYaml.file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> data = new Yaml().load(inputStream);

        LinkedHashMap<String, Object> market = (LinkedHashMap<String, Object>) data.get("market");

        if (market.isEmpty()) {
            current_item = 0;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean itemNotAllowed (Material m) {

        Material[] notAllowed = {
                MCPlugin_Util.empty_cell_invGui,
                MCPlugin_Util.exitButton
        };

        for (Material nA: notAllowed)
        {
            if (m.equals(nA))
                return true;
        }

        return false;

    }


}
