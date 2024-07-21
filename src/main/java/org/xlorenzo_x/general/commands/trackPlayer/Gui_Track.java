package org.xlorenzo_x.general.commands.trackPlayer;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.general.commands.trackPlayer.InventoryHoldersTrack.HolderConfirmGui;
import org.xlorenzo_x.general.commands.trackPlayer.InventoryHoldersTrack.HolderTrackGui;
import org.xlorenzo_x.start.Main;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Gui_Track implements Listener {

    public static Player p_sender;
    public static Player p_toTrack;
    private static OfflinePlayer p_toTrackOffline;

    private static Location location; // Where is the player to track?

    private static int kills;
    private static int deaths;

    private static float kd;

    private static int block_broken;
    private static int block_placed;
    private static int num_entityKilled;

    protected static Inventory invGui;

    private final static int INVENTORY_GUI_CELLS = 36;

    private static boolean isOnline;

    private static BukkitTask task_update;

    public Gui_Track () {

    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent e_closeInv) {
        InventoryHolder invHold = e_closeInv.getInventory().getHolder();

        if (invHold instanceof HolderTrackGui || invHold instanceof HolderConfirmGui)
        {
            if (task_update != null)
            {
                task_update.cancel();
            }
        }
    }


    public Gui_Track (Player p_sender, Player p_toTrack){ // Nel caso in cui il Player sia Online!

        Gui_Track.p_sender = p_sender;
        Gui_Track.p_toTrack = p_toTrack;

        invGui = Bukkit.createInventory(new HolderTrackGui(), INVENTORY_GUI_CELLS, ChatColor.RED + "TrackPlayer: " + ChatColor.GOLD + Gui_Track.p_toTrack.getDisplayName());
        isOnline = true;

        kills = getKills();
        deaths = getDeaths();
        kd = (float) (Math.round(((double) kills /deaths) * 100.0) / 100.0);
        location = getPlayerLocation();
        block_broken = getBlock_broken();
        block_placed = getBlock_placed();
        num_entityKilled = getEntityKilled();
    }

    public Gui_Track (Player p_sender, OfflinePlayer p_toTrackOffline) // Nel caso il Player sia offline
    {
        Gui_Track.p_sender = p_sender;
        Gui_Track.p_toTrackOffline = p_toTrackOffline;

        invGui = Bukkit.createInventory(new HolderTrackGui(), INVENTORY_GUI_CELLS, ChatColor.RED + "TrackPlayer: " + ChatColor.GOLD + Gui_Track.p_toTrackOffline.getName() + ChatColor.DARK_RED + " - OFF ");
        isOnline = false;

        kills = getKills();
        deaths = getDeaths();
        kd = (float) (Math.round(((double) kills /deaths) * 100.0) / 100.0);
        location = getPlayerLocation();
        block_broken = getBlock_broken();
        block_placed = getBlock_placed();
        num_entityKilled = getEntityKilled();
    }

    public static @NotNull ItemStack createTrackButton(Material nameMaterial, int amount, List<String> lore, String display, Enchantment enchant, int lvl_enchant) {
        ItemStack item = new ItemStack(nameMaterial, amount);

        if (enchant != null) {
            if (enchant.canEnchantItem(item) && lvl_enchant > 0) {
                try {
                    item.addEnchantment(enchant, lvl_enchant);
                } catch (Exception e) {
                    System.out.println(MCPlugin_Util.namePlugin + " Errore createTrackButton: " + e.getMessage());
                }
            }
        }

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(display);
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static void updateGUI() { // Update il last_time join del player

        task_update = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {

            if (!isOnline) // Se il player è unicamente offline... allora....
            {
                // Aggiungere il LastJoin
                List<String> lastJoin = new ArrayList<>();

                // Ottengo il tempo attuale
                long currentTime = System.currentTimeMillis();

                // Calcolo la differenza tra il tempo attuale e l'ultimo accesso
                long difference = currentTime - p_toTrackOffline.getLastPlayed(); // in long

                Duration duration = Duration.ofMillis(difference); // <--

                long hours = duration.toHours();
                int days = (int) Math.floor((float) hours/24); // Approssimo per difetto
                long remainingHours = Math.abs(hours - (days * 24L));
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;
                lastJoin.add(ChatColor.BLUE + String.valueOf(days) + "d " + remainingHours + "h " + minutes + "m " + seconds + "s");

                invGui.setItem(27, createTrackButton(Material.ARROW, 1 , lastJoin, ChatColor.GOLD + "Last Join Server", null, 1));
            }

        }, 0, 20);

    }

    public static void openGuiTrack() {

        // PlayerLocation
        List<String> locationInfo = new ArrayList<>();
        locationInfo.add(ChatColor.BLUE + "x: " + location.getX());
        locationInfo.add(ChatColor.BLUE + "y: " + location.getY());
        locationInfo.add(ChatColor.BLUE + "z: " + location.getZ());
        locationInfo.add("");
        locationInfo.add(ChatColor.DARK_PURPLE + " -- Press to Teleport --");
        invGui.setItem(0, createTrackButton(Material.DARK_OAK_SIGN, 1, locationInfo, ChatColor.GREEN + "    Player Location", null, 1));

        // Mob killed
        invGui.setItem(8, createTrackButton(Material.ZOMBIE_HEAD, 1, new ArrayList<>(), ChatColor.GREEN + "Mob Kills: " + ChatColor.GOLD + num_entityKilled , null, 1));

        //Kills
        invGui.setItem(11, createTrackButton(Material.DIAMOND_SWORD, 1, new ArrayList<>(), ChatColor.DARK_RED + "PvP Kills: " + ChatColor.GOLD + kills , Enchantment.KNOCKBACK, 2));

        // K/D
        invGui.setItem(13, createTrackButton(Material.BOW, 1, new ArrayList<>(), ChatColor.DARK_PURPLE + "K/D: " + ChatColor.GOLD + kd , Enchantment.DURABILITY, 2));

        // Deaths
        invGui.setItem(15, createTrackButton(Material.BONE, 1, new ArrayList<>(), ChatColor.GRAY + "PvP Deaths: " + ChatColor.GOLD + deaths, null, 1));

        // Block-Broken
        invGui.setItem(30, createTrackButton(Material.IRON_PICKAXE, 1, new ArrayList<>(), ChatColor.BLUE + "Block Broken: " + ChatColor.GOLD + block_broken, Enchantment.MENDING, 1));

        // Block-placed
        invGui.setItem(32, createTrackButton(Material.GRASS_BLOCK, 1, new ArrayList<>(), ChatColor.BLUE + "Block Placed: " + ChatColor.GOLD + block_placed, null, 1));

        // Exit
        invGui.setItem(35, createTrackButton(MCPlugin_Util.exitButton, 1, new ArrayList<>(), ChatColor.GOLD + "EXIT", null, 1));

        if (!isOnline) // Se il player è unicamente offline... allora....
        {
            // Aggiungere il LastJoin
            List<String> lastJoin = new ArrayList<>();

            // Ottengo il tempo attuale
            long currentTime = System.currentTimeMillis();

            // Calcolo la differenza tra il tempo attuale e l'ultimo accesso
            long difference = currentTime - p_toTrackOffline.getLastPlayed(); // in long

            Duration duration = Duration.ofMillis(difference); // <--

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            lastJoin.add(ChatColor.BLUE + String.valueOf(hours) + "h " + minutes + "m " + seconds + "s");

            invGui.setItem(27, createTrackButton(Material.ARROW, 1 , lastJoin, ChatColor.GOLD + "Last Join Server", null, 1));
        }

        // RIEMPIO IL RESTO DELL'INVENTARIO CON PANNELLI DI VETRO!
        fillEmptySpace(invGui, MCPlugin_Util.empty_cell_invGui);

        p_sender.openInventory(invGui);
        updateGUI();
    }

    public static void openGuiForConfirm (Player player, String message)
    {
        Inventory invConfirm = Bukkit.createInventory(new HolderConfirmGui(), 27, ChatColor.DARK_PURPLE + "Choice: " + message);
        // CREARE 3 BOTTONI, UNO PER ACCETTARE, UNO PER RIFIUTARE E UNO PER TORNARE INDIETRO!
        //Confirm
        ItemStack confirm = new ItemStack(MCPlugin_Util.confirmButton, 1);

        ItemMeta meta_confirm = confirm.getItemMeta();
        assert meta_confirm != null;
        meta_confirm.setDisplayName(ChatColor.DARK_GREEN + "Confirm!");
        confirm.setItemMeta(meta_confirm);

        //Back
        ItemStack back = new ItemStack(MCPlugin_Util.backTo, 1);

        ItemMeta meta_back = confirm.getItemMeta();
        assert meta_back != null;
        meta_back.setDisplayName(ChatColor.GOLD + "Go Back");
        back.setItemMeta(meta_back);

        //Reject
        ItemStack reject = new ItemStack(MCPlugin_Util.rejectButton,1);

        ItemMeta meta_reject = reject.getItemMeta();
        assert meta_reject != null;
        meta_reject.setDisplayName(ChatColor.DARK_RED + "Reject!");
        reject.setItemMeta(meta_reject);

        invConfirm.setItem(11, confirm);

        // Add back
        invConfirm.setItem(13, back);

        invConfirm.setItem(15, reject);

        // RIEMPIO IL RESTO DELL'INVENTARIO CON PANNELLI DI VETRO!
        fillEmptySpace(invConfirm, MCPlugin_Util.empty_cell_invGui);

        // player sender
        player.openInventory(invConfirm);
    }

    private int getDeaths() {

        if (p_toTrack != null) // Player Online
        {

            return p_toTrack.getStatistic(Statistic.DEATHS);
        }
        else { // Offline Player

            return p_toTrackOffline.getStatistic(Statistic.DEATHS);
        }

    }

    private int getKills() {

        if (p_toTrack != null) // Player Online
        {

            return p_toTrack.getStatistic(Statistic.PLAYER_KILLS);
        }
        else { // Offline Player

            return p_toTrackOffline.getStatistic(Statistic.PLAYER_KILLS);
        }

    }

    private int getBlock_broken () {

        int i = 0;
        if (p_toTrack != null) // Player Online
        {
            // Itera su tutti i tipi di materiali
            for (Material material : Material.values()) {
                // Ottieni le statistiche per il tipo di blocco attuale
                int blocksBroken = p_toTrack.getStatistic(Statistic.MINE_BLOCK, material);
                // Stampa le statistiche solo se il giocatore ha rotto almeno un blocco di questo tipo
                if (blocksBroken > 0) {
                    i += blocksBroken;
                }
            }

        }
        else { // Offline Player

            // Itera su tutti i tipi di materiali
            for (Material material : Material.values()) {
                // Ottieni le statistiche per il tipo di blocco attuale
                int blocksBroken = p_toTrackOffline.getStatistic(Statistic.MINE_BLOCK, material);

                // Stampa le statistiche solo se il giocatore ha rotto almeno un blocco di questo tipo
                if (blocksBroken > 0) {
                    i += blocksBroken;
                }
            }

        }
        return i;
    }

    private int getBlock_placed () {
        int i = 0;
        if (p_toTrack != null) // Player Online
        {
            // Itera su tutti i tipi di materiali
            for (Material material : Material.values()) {
                // Ottieni le statistiche per il tipo di blocco attuale
                int blocksPlaced = p_toTrack.getStatistic(Statistic.USE_ITEM, material);

                // Stampa le statistiche solo se il giocatore ha rotto almeno un blocco di questo tipo
                if (blocksPlaced > 0) {
                    i += blocksPlaced;
                }
            }

        }
        else { // Offline Player

            // Itera su tutti i tipi di materiali
            for (Material material : Material.values()) {
                // Ottieni le statistiche per il tipo di blocco attuale
                int blocksPlaced = p_toTrackOffline.getStatistic(Statistic.USE_ITEM, material);

                // Stampa le statistiche solo se il giocatore ha rotto almeno un blocco di questo tipo
                if (blocksPlaced > 0) {
                    i += blocksPlaced;
                }
            }

        }
        return i;
    }

    // Get Number of Monster killed
    private int getEntityKilled () {
        if (p_toTrack != null) // Player Online
        {
            return p_toTrack.getStatistic(Statistic.MOB_KILLS);
        }
        else { // Offline Player

            return p_toTrackOffline.getStatistic(Statistic.MOB_KILLS);

        }
    }

    // Get Player Location!
    private Location getPlayerLocation () {

        if (p_toTrack != null) // Player Online
        {
            return p_toTrack.getLocation();
        }
        else { // Offline Player
            return p_toTrackOffline.getLocation();
        }
    }

    // RIEMPIO IL RESTO DELL'INVENTARIO CON PANNELLI DI VETRO!
    public static void fillEmptySpace (@NotNull Inventory inv , Material m) {
        for (int x = 0; x < inv.getContents().length; x++)
        {
            ItemStack item = inv.getContents()[x];

            if (item == null || item.getType() == Material.AIR)
            {
                ItemStack newItem = createTrackButton(m, 1, new ArrayList<>(), "", null, 1);

                ItemMeta meta = newItem.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "SLOT EMPTY");

                LinkedList<String> lore = new LinkedList<>();
                lore.add(ChatColor.DARK_RED + "" + ChatColor.MAGIC + "####" + ChatColor.DARK_BLUE + ChatColor.MAGIC + "####" + ChatColor.YELLOW + ChatColor.MAGIC + "####");

                meta.setLore(lore);

                newItem.setItemMeta(meta);

                inv.setItem(x, newItem);
            }
        }
    }

}
