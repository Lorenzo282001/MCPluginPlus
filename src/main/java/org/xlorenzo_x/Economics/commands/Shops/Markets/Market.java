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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Economics.commands.Shops.Markets.Holders.MarketHolder;
import org.xlorenzo_x.Economics.commands.Shops.Markets.YAML_Market.Market_GuiYaml;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.general.commands.trackPlayer.Gui_Track;
import org.xlorenzo_x.start.Main;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/*
* ASTA
* */

public class Market implements CommandExecutor, Listener {

    private static final int slotExit = 49;
    private static BukkitTask taskUpdate = null;

    public static HashMap<Integer, Material> findItemToCancel = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        // All' invio del comando market, aprire l'inventario del market!
        if (cmd.getName().equalsIgnoreCase("market"))
        {
            if (sender instanceof Player player)
            {
                try {
                    takeMarket(player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                sender.sendMessage(MCPlugin_Util.namePlugin + " Devi essere un player per aprire il market!");
            }
        }

        return false;
    }

    private void takeMarket (@NotNull Player player) throws IOException {

        Inventory marketInventory = Bukkit.createInventory(new MarketHolder(), MCPlugin_Util.marketInventoryCells, MCPlugin_Util.marketName);

        marketInventory.setItem(slotExit, Gui_Track.createTrackButton(MCPlugin_Util.exitButton, 1, new ArrayList<>(), ChatColor.GOLD + "EXIT", null, 1));

        // Caricare l'inventario
        boolean i = loadMarket(player, marketInventory);

        if (i) {

            Gui_Track.fillEmptySpace(marketInventory, MCPlugin_Util.empty_cell_invGui);

            // Verifica finale se mercato è riempito o meno, verificare se almeno uno slot del mercato è diverso dal EMPTY CELL MATERIAL
            boolean find = false;
            for (ItemStack item : marketInventory.getContents())
            {
                if (!item.getType().equals(MCPlugin_Util.empty_cell_invGui)) {
                    if (!item.getType().equals(MCPlugin_Util.exitButton)) {
                        find = true;
                        break;
                    }
                }
            }

            if (find) {
                player.openInventory(marketInventory);
                updateMarket(player, marketInventory);
            }
            else {
                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_PURPLE + " The market is empty!");
                PlayerPutOnMarketCommand.current_item = 0; // Reset se market vuoto!

                if (taskUpdate != null) {
                    taskUpdate.cancel();
                    taskUpdate = null;
                }
            }
        }
    }

    private boolean loadMarket(Player player, @NotNull Inventory inv) throws IOException {

        if (inv != null) {

            FileInputStream inputStream;

            try {
                inputStream = new FileInputStream(Market_GuiYaml.file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            Map<String, Object> data = new Yaml().load(inputStream);

            LinkedHashMap<String, Object> market = (LinkedHashMap<String, Object>) data.get("market");

            if (market != null && !market.isEmpty()) {

                for (Map.Entry<String, Object> counting: market.entrySet())
                {
                    int countItem = Integer.parseInt(counting.getKey()); // Il "0" dentro market!

                    LinkedHashMap<Object, Object> realMarket = (LinkedHashMap<Object, Object>) counting.getValue();

                    for (Map.Entry<Object, Object> i : realMarket.entrySet()) {
                        Material m = Material.getMaterial((String) i.getKey());

                        LinkedHashMap<Object, Object> MaterialProperty = (LinkedHashMap<Object, Object>) i.getValue();

                        String displayName = null;
                        int size = 0;
                        double price = 0.0;
                        String owner = null;
                        LinkedHashMap<Enchantment, Integer> enchantments = new LinkedHashMap<>();

                        int minutes = 0;
                        int seconds = 0;

                        for (Map.Entry<Object, Object> p : MaterialProperty.entrySet()) {
                            if (p.getKey().equals("size"))
                                size = (int) p.getValue();

                            if (p.getKey().equals("price"))
                                price = (double) p.getValue();

                            if (p.getKey().equals("owner"))
                                owner = (String) p.getValue();

                            if (p.getKey().equals("displayName"))
                                displayName = (String) p.getValue();

                            if (p.getKey().equals("enchants")) {
                                LinkedHashMap<Object, Object> enchants = (LinkedHashMap<Object, Object>) p.getValue();

                                for (Map.Entry<Object, Object> e : enchants.entrySet()) {
                                    if (e != null && e.getKey() != null && e.getValue() != null) {
                                        enchantments.put(Enchantment.getByName((String) e.getKey()), Integer.parseInt((String) e.getValue()));
                                    }
                                }
                            }

                            if (p.getKey().equals("time"))
                            {
                                LinkedHashMap<Object, Object> times = (LinkedHashMap<Object, Object>) p.getValue();

                                for (Map.Entry<Object, Object> t : times.entrySet())
                                {
                                    if (t.getKey().equals("minutes"))
                                        minutes = (int) t.getValue();

                                    if (t.getKey().equals("seconds"))
                                        seconds = (int) t.getValue();
                                }
                            }
                        }

                        if (owner == null || size == 0 || price == 0.0)
                        {
                            minutes = -1;
                            seconds = -1;
                        }

                        // Se minuti è 0, rimetto l'oggetto nell'inventario dell'owner, togliendolo dal market
                        if (minutes <= 0 && seconds <= 1)
                        {
                            ItemStack item = new ItemStack(m, size);

                            ItemMeta meta = item.getItemMeta();

                            if (displayName != null)
                            {
                                meta.setDisplayName(ChatColor.stripColor(displayName));
                            }

                            if (!enchantments.isEmpty()) {
                                for (Map.Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                                    meta.addEnchant(e.getKey(), e.getValue(), false);
                                }
                            }

                            item.setItemMeta(meta);

                            for (Map.Entry<Integer, Material> f : findItemToCancel.entrySet()) {
                                int marketID = f.getKey();
                                Material itemMarket = f.getValue();

                                if (item.getType().equals(itemMarket))
                                {
                                    if (owner != null)
                                        Bukkit.getPlayer(owner).getInventory().addItem(item); // se owner è offline credo lanci una exception da controllare in seguito

                                    // Tolgo l'item dal market
                                    market.remove(m); // Rimuovo dalla lista dinamica
                                    // Ora rimuovere dal file
                                    FileConfiguration conf = Market_GuiYaml.conf;
                                    conf.set("market." + marketID + "." + item.getType() + ".time.minutes", 0);
                                    conf.set("market." + marketID + "." + item.getType() + ".time.seconds", 0);
                                    conf.set("market." + marketID, null);
                                    conf.save(Market_GuiYaml.file);
                                    YamlConfiguration.loadConfiguration(Market_GuiYaml.file);
                                    findItemToCancel.remove(marketID);
                                    player.closeInventory();
                                    addOneMinuteOnMarket();
                                    break;
                                }
                            }
                            continue;
                        }

                        if (owner != null && size > 0 && price > 0.0) {
                            ItemStack item = new ItemStack(m, size);

                            ItemMeta meta = item.getItemMeta();

                            if (displayName != null)
                            {
                                meta.setDisplayName(displayName);
                            }

                            if (!enchantments.isEmpty()) {
                                for (Map.Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                                    meta.addEnchant(e.getKey(), e.getValue(), false);
                                }
                            }

                            meta.setDisplayName(ChatColor.DARK_RED + m.name());
                            List<String> lore = new ArrayList<>();

                            if (!enchantments.isEmpty())
                                lore.add("");
                            lore.add(ChatColor.GOLD + "Price: " + price);
                            lore.add(ChatColor.BLUE + "----------------------");
                            lore.add(ChatColor.GOLD + "Owner: " + owner);
                            lore.add("");
                            lore.add(ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "LEFT" + ChatColor.DARK_PURPLE + " --> BUY!");
                            if (player.getName().equals(owner))
                                lore.add(ChatColor.BOLD + "" + ChatColor.DARK_RED + "RIGHT" + ChatColor.DARK_PURPLE + " --> TAKE!");

                            if (minutes >= 6)
                                lore.add(ChatColor.DARK_AQUA + "Time Left -> " + ChatColor.GREEN + minutes + "m " + seconds + "s");
                            else{
                                if (minutes > 0)
                                    lore.add(ChatColor.DARK_AQUA + "Time Left -> " + ChatColor.DARK_RED + minutes + "m " + seconds + "s");
                                else
                                    lore.add(ChatColor.DARK_AQUA + "Time Left -> " + ChatColor.DARK_RED + seconds + "s");
                            }

                            meta.setLore(lore);

                            item.setItemMeta(meta);

                            // Set item in inventory

                            for (int z = 0; z < MCPlugin_Util.marketInventoryCells; z++)
                            {
                                if (inv.getItem(z) != null && inv.getItem(z).getType().equals(MCPlugin_Util.exitButton))
                                    continue;

                                if (inv.getItem(z) == null || inv.getItem(z).getType().equals(MCPlugin_Util.empty_cell_invGui))
                                {
                                    inv.setItem(z, item);

                                    findItemToCancel.put(countItem, item.getType()); // CountItem è il numero che corrisponde nel market.yml e dovrò eliminare proprio quel item

                                    break;
                                }
                            }
                           
                        }
                    }

                }

                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return true;
            }
            else {
                player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_PURPLE + " The market is empty!");
                PlayerPutOnMarketCommand.current_item = 0; // Reset se market vuoto!

                if (taskUpdate != null) {
                    taskUpdate.cancel();
                    taskUpdate = null;
                }

                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                player.closeInventory();
                return false;
            }
        }

        return false;
    }

    private void updateMarket (Player player, Inventory marketInv) {

        taskUpdate = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () ->{

            marketInv.clear();

            marketInv.setItem(slotExit, Gui_Track.createTrackButton(MCPlugin_Util.exitButton, 1, new ArrayList<>(), ChatColor.GOLD + "EXIT", null, 1));

            boolean i;
            try {
               i = loadMarket(player, marketInv);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Gui_Track.fillEmptySpace(marketInv, MCPlugin_Util.empty_cell_invGui);

            if (!i)
            {
                if (taskUpdate != null)
                    taskUpdate.cancel();
                player.closeInventory();
            }

        }, 0, 20);


    }

    public static void addOneMinuteOnMarket() {

        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(Market_GuiYaml.file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> data = new Yaml().load(inputStream);

        LinkedHashMap<String, Object> market = (LinkedHashMap<String, Object>) data.get("market");

        if (market != null && !market.isEmpty()) {

            for (Map.Entry<String, Object> counting : market.entrySet()) {
                int countItem = Integer.parseInt(counting.getKey()); // Il "0" dentro market!

                LinkedHashMap<Object, Object> realMarket = (LinkedHashMap<Object, Object>) counting.getValue();

                for (Map.Entry<Object, Object> i : realMarket.entrySet()) {
                    Material m = Material.getMaterial((String) i.getKey());

                    LinkedHashMap<Object, Object> MaterialProperty = (LinkedHashMap<Object, Object>) i.getValue();

                    int minutes = 0;

                    for (Map.Entry<Object, Object> p : MaterialProperty.entrySet()) {
                        if (p.getKey().equals("time")) {
                            LinkedHashMap<Object, Object> times = (LinkedHashMap<Object, Object>) p.getValue();

                            for (Map.Entry<Object, Object> t : times.entrySet()) {
                                if (t.getKey().equals("minutes")) {
                                    minutes = (int) t.getValue();

                                    Market_GuiYaml.conf.set("market." + countItem + "." + m.name() + ".time.minutes", minutes + 1);
                                    try {
                                        Market_GuiYaml.conf.save(Market_GuiYaml.file);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }

    }

    @EventHandler
    public void PlayerInteractWithMarket (@NotNull InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();

        if (inv != null) {
            if (inv.getHolder() instanceof MarketHolder && player != null) {
                if (event.getClickedInventory() == null || !(event.getWhoClicked() instanceof Player))
                    return; // skip if click outside of inventory

                if (event.getClick().isLeftClick()) {
                    // Check tasti

                    // Exit
                    if (event.getCurrentItem() != null) {
                        if (event.getCurrentItem().getType().equals(MCPlugin_Util.exitButton)) {
                            if (event.getCurrentItem().getItemMeta().getDisplayName().contains("EXIT")) {
                                if (event.getRawSlot() == slotExit) {
                                    MCPlugin_Util.playSoundOrbThrow(player);
                                    player.closeInventory();
                                }
                            }
                        }
                    }
                }

                // RIGHT CLICK TO REMOVE OBJECT FROM MARKET IF YOU ARE THE OBJECT'S OWNER
                if (event.getClick().isRightClick())
                {
                    ItemStack item = event.getCurrentItem();

                    ItemMeta metaItem = item.getItemMeta();

                    List<String> loreOwner = metaItem.getLore();

                    String displayName = null;
                    LinkedHashMap<Enchantment, Integer> enchantments = new LinkedHashMap<>();

                    if (loreOwner != null) {

                        for (String s : loreOwner) {
                            int countItem = -1;
                            if (s.contains(player.getName())) // Se si, l'oggetto è di proprietà del player!
                            {
                                // Andare a prendere l' item corretto!
                                FileInputStream inputStream;

                                try {
                                    inputStream = new FileInputStream(Market_GuiYaml.file);
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }

                                Map<String, Object> data = new Yaml().load(inputStream);

                                LinkedHashMap<String, Object> market = (LinkedHashMap<String, Object>) data.get("market");

                                if (market != null && !market.isEmpty()) {

                                    for (Map.Entry<String, Object> counting : market.entrySet()) {
                                        countItem = Integer.parseInt(counting.getKey()); // Il "0" dentro market!

                                        if ((event.getRawSlot()+1) == countItem) // Se l' item è il medesimo!
                                        {
                                            LinkedHashMap<Object, Object> realMarket = (LinkedHashMap<Object, Object>) counting.getValue();

                                            for (Map.Entry<Object, Object> i : realMarket.entrySet()) {
                                                Material m = Material.getMaterial((String) i.getKey());

                                                LinkedHashMap<Object, Object> MaterialProperty = (LinkedHashMap<Object, Object>) i.getValue();

                                                for (Map.Entry<Object, Object> p : MaterialProperty.entrySet()) {

                                                    if (p.getKey().equals("displayName"))
                                                        displayName = (String) p.getValue();

                                                    if (p.getKey().equals("enchants")) {
                                                        LinkedHashMap<Object, Object> enchants = (LinkedHashMap<Object, Object>) p.getValue();

                                                        for (Map.Entry<Object, Object> e : enchants.entrySet()) {
                                                            if (e != null && e.getKey() != null && e.getValue() != null) {
                                                                enchantments.put(Enchantment.getByName((String) e.getKey()), Integer.parseInt((String) e.getValue()));
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            break;
                                        }
                                    }

                                    // Impostare l' itemstack da dare al proprietario, aggiungendo i meta a quello cliccato!
                                    if (displayName != null)
                                        metaItem.setDisplayName(ChatColor.stripColor(displayName));
                                    else
                                        metaItem.setDisplayName(item.getType().name());

                                    if (!enchantments.isEmpty())
                                    {
                                        for (Map.Entry<Enchantment, Integer> e: enchantments.entrySet())
                                        {
                                            metaItem.addEnchant(e.getKey(), e.getValue(), false);
                                        }
                                    }

                                    metaItem.setLore(new ArrayList<>());

                                    item.setItemMeta(metaItem);

                                    for (Map.Entry<Integer, Material> f : findItemToCancel.entrySet()) {
                                        int marketID = f.getKey();
                                        Material itemMarket = f.getValue();

                                        if (item.getType().equals(itemMarket))
                                        {

                                            player.getInventory().addItem(item); // Dò l'oggetto al player

                                            // Ora rimuovere dal file
                                            FileConfiguration conf = Market_GuiYaml.conf;

                                            conf.set("market." + marketID + "." + item.getType() + ".time.minutes", 0);
                                            conf.set("market." + marketID + "." + item.getType() + ".time.seconds", 0);
                                            conf.set("market." + marketID, null);

                                            try {
                                                conf.save(Market_GuiYaml.file);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }

                                            YamlConfiguration.loadConfiguration(Market_GuiYaml.file);
                                            player.sendMessage(MCPlugin_Util.namePlugin + " " + item.getAmount() + " x " + item.getType().name() + " restituito con successo!");
                                            findItemToCancel.remove(marketID);
                                            player.closeInventory();
                                            addOneMinuteOnMarket();
                                            break;
                                        }
                                    }
                                }

                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                break;
                            }
                        }
                    }

                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCloseMarket (@NotNull InventoryCloseEvent event)
    {
        if (event.getInventory() != null) {
            if (event.getInventory().getHolder() instanceof MarketHolder) {
                if (taskUpdate != null)
                    taskUpdate.cancel();
            }
        }
    }


}
