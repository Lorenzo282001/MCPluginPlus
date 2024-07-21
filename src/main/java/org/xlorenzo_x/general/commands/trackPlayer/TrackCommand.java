package org.xlorenzo_x.general.commands.trackPlayer;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.general.commands.trackPlayer.InventoryHoldersTrack.HolderConfirmGui;
import org.xlorenzo_x.general.commands.trackPlayer.InventoryHoldersTrack.HolderTrackGui;

import java.util.Objects;

public class TrackCommand implements CommandExecutor, Listener {

    private static TrackCommand instance;
    private  Gui_Track guiTrackOffline = null;
    private  Gui_Track guiTrackOnline = null;

    private String [] argomenti = null;

    private TrackCommand() {
        // Costruttore privato per impedire la creazione di istanze esterne
    }

    // Metodo per ottenere l'istanza singleton di TrackCommand
    public static TrackCommand getInstance() {
        if (instance == null) {
            instance = new TrackCommand();
        }
        return instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("trackP")) {
            if (sender instanceof Player p){
                if (p.getName().equalsIgnoreCase("xLorenzo_x")) {
                    if (p.hasPermission("utility.trackP.use")) {
                        if (args.length != 0) {
                            argomenti = args;
                            Player p_sender = (Player) sender; // Il mio player , per modifiche future!

                            Player p_toTrack = findTrackPlayer(args[0]); // Online

                            if (p_toTrack == null) // Se è null vuol dire che non esiste il giocatore o che è offline!
                            {
                                // Provo a verificare invece se il player è offline!
                                OfflinePlayer offp_toTrack = findTrackPlayerOffline(args[0]);

                                if (offp_toTrack != null) {
                                    // Da qui possiamo controllare il player -- OFFLINE!
                                    guiTrackOffline = new Gui_Track(p_sender, offp_toTrack);
                                    Gui_Track.openGuiTrack();
                                } else {
                                    p_sender.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Player [" + args[0] + "] non trovato!");
                                    return false;
                                }
                            } else {
                                // Da qui possiamo controllare il player -- ONLINE!
                                guiTrackOnline = new Gui_Track(p_sender, p_toTrack);
                                Gui_Track.openGuiTrack();
                            }

                        } else {
                            sender.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Inserire un player: /trackP <Player>");
                            return false;
                        }
                    }
                    else {
                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Non hai il permesso per usare questo comando");
                    }
                }
            }
            else {
                sender.sendMessage(MCPlugin_Util.namePlugin + " Non sei un player!");
                return false;
            }

        }

        return false;
    }

    private Player findTrackPlayer(String nameOfPlayer){
        // Trovare il player da tracciare!
        Player p_toTrack;
        Player[] OnlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        for (Player p: OnlinePlayers)
        {
            if (p.getName().equals(nameOfPlayer))
            {
                p_toTrack = p;
                return p_toTrack; 
            }
        }
        //////////////////////////////

        // Se va qui non ha trovato nessun player!
        return null;
    }

    private OfflinePlayer findTrackPlayerOffline(String nameOfPlayer){
        // Trovare il player da tracciare!
        OfflinePlayer p_toTrack;
        OfflinePlayer[] playerOff = Bukkit.getServer().getOfflinePlayers();

        for (OfflinePlayer p: playerOff)
        {
            if (Objects.equals(p.getName(), nameOfPlayer))
            {
                p_toTrack = p;
                return p_toTrack;
            }
        }
        //////////////////////////////

        // Se va qui non ha trovato nessun player!
        return null;
    }

    // Controllo cosa sto cliccando nel mio Holder del track!
    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent e) { // Click On MyHolder
        if (e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player))
            return; // skip if click outside of inventory
        InventoryHolder holder = e.getClickedInventory().getHolder();

        // GUI TRACK
        if(holder instanceof HolderTrackGui) { // our holder!

            ItemStack obj = e.getCurrentItem();
            Player player = (Player) e.getWhoClicked();

            // Tutti gli eventi nell'inventario della guiTrack!
            if (e.getClick().isLeftClick() && obj != null)
            {
                // Prima azione se clicco nel tasto di uscita!
                if (obj.getType().equals(MCPlugin_Util.exitButton))
                {
                    MCPlugin_Util.playSoundOrbThrow(player);
                    player.closeInventory();
                } else if (obj.getType().equals(Material.DARK_OAK_SIGN)) { // TP Verso il giocatore
                    // Aprire la GUI CONFIRM

                    Player p_toTrack = findTrackPlayer(argomenti[0]); // Online

                    if (p_toTrack == null) // Se è null vuol dire che non esiste il giocatore o che è offline!
                    {
                        // Provo a verificare invece se il player è offline!
                        OfflinePlayer offp_toTrack = findTrackPlayerOffline(argomenti[0]);

                        if (offp_toTrack != null)
                        {
                            guiTrackOffline.openGuiForConfirm(Gui_Track.p_sender,"Tp to " +  ChatColor.GOLD + offp_toTrack.getName());
                        }
                    }
                    else {
                        guiTrackOnline.openGuiForConfirm(Gui_Track.p_sender, "Tp to " +  ChatColor.GOLD + p_toTrack.getDisplayName());
                    }

                }


            }


            e.setCancelled(true);
        }

        if (holder instanceof HolderConfirmGui)
        {
            ItemStack obj = e.getCurrentItem();
            Player player = (Player) e.getWhoClicked();

            // Tutti gli eventi nell'inventario della guiTrack!
            if (e.getClick().isLeftClick() && obj != null)
            {
                if (obj.getType().equals(MCPlugin_Util.confirmButton))
                {
                    player.closeInventory();

                    // Mando il giocatore al punto del player selezionato!
                    Player p_toTrack = findTrackPlayer(argomenti[0]); // Online
                    if (p_toTrack != null)
                    {
                        player.teleport(new Location(p_toTrack.getWorld(), p_toTrack.getLocation().getX(), p_toTrack.getLocation().getY(), p_toTrack.getLocation().getZ()));
                    }
                    else
                    {
                        OfflinePlayer offp_toTrack = findTrackPlayerOffline(argomenti[0]);
                        assert offp_toTrack != null;
                        player.teleport(Objects.requireNonNull(offp_toTrack.getLocation()));
                    }
                }
                else if (obj.getType().equals(MCPlugin_Util.rejectButton)) {
                    player.closeInventory();
                }
                else if (obj.getType().equals(MCPlugin_Util.backTo)) {
                    player.closeInventory();
                    Gui_Track.openGuiTrack();
                }
            }

            e.setCancelled(true);
        }

    }


}
