package org.xlorenzo_x.Magic;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.start.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicWand implements Listener {

    private ItemStack magic_wand;
    private ItemMeta meta_magic_wand;

    public static String[] modes = {"strikeLightning", "Disappear", "Levitation"};
    private int currentModeIndex = 0;
    private Map<Player, Long> lastScrollTime = new HashMap<>(); // Mappa per tenere traccia del tempo dell'ultimo evento



    public MagicWand () {

        magic_wand = MCPlugin_Util.magicWand;
        meta_magic_wand = magic_wand.getItemMeta();

        // Disegno la mia wand
        if (meta_magic_wand != null) {

            meta_magic_wand.setDisplayName(MCPlugin_Util.nameWand);
            meta_magic_wand.setLore(List.of(
                    ChatColor.DARK_PURPLE + "           MCPlugin",
                    ChatColor.WHITE + " Mode:     " + ChatColor.GOLD + modes[0].toUpperCase(),
                    "",
                    ChatColor.GRAY + "SHIFT + WHEEL to select modes."
            ));

            magic_wand.setItemMeta(meta_magic_wand);
        }
    }

    // Magic wand place event
    @EventHandler
    public void magicWandPlaceEvent(@NotNull BlockPlaceEvent event) {
        ItemStack verifyBlockWand = event.getPlayer().getInventory().getItemInMainHand();

        if (isMagicWand(verifyBlockWand))
        {
            event.setCancelled(true);
        }
    }

    // Change Mode
    @EventHandler
    public void onPlayerShiftWand(@NotNull PlayerItemHeldEvent e) {

        Player p = e.getPlayer();

        if (p.isSneaking()) {

            ItemStack itemMainHand = p.getInventory().getItemInMainHand(); // Oggetto nella mano principale

            if (itemMainHand != null && !itemMainHand.getType().isAir()){

                if (isMagicWand(itemMainHand))
                {
                    long currentTime = System.currentTimeMillis();
                    long lastTime = lastScrollTime.getOrDefault(p, 0L);

                    if (currentTime - lastTime > 200) {
                        e.setCancelled(true);

                        changeModeMeta(p , itemMainHand);

                        lastScrollTime.put(p, currentTime); // Aggiorna l'ultimo tempo di scroll
                    }
                }

            }
        }
    }

    private void changeModeMeta(@NotNull Player player, @NotNull ItemStack i) {

        ItemMeta metaItemHand = i.getItemMeta();

        currentModeIndex = (currentModeIndex + 1) % modes.length;

        // Aggiorniamo la lore
        metaItemHand.setLore(List.of(
                ChatColor.DARK_PURPLE + "           MCPlugin",
                ChatColor.WHITE + " Mode:     " + ChatColor.GOLD + modes[currentModeIndex].toUpperCase(),
                "",
                ChatColor.GRAY + "SHIFT + WHEEL to select modes."
        ));

        i.setItemMeta(metaItemHand);

        player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_PURPLE + " Magic Wand mod set: " + ChatColor.GOLD + modes[currentModeIndex].toUpperCase());
    }

    // Azione
    @EventHandler
    public void onWandRightClick(@NotNull PlayerInteractEvent e){

        if (e.getAction().toString().contains("RIGHT")) {

            ItemStack itemInHand = e.getItem();

            // Verifico che l'item sia la wand
            if (isMagicWand(itemInHand))
            {
                // Lancio un fulmine nella direzione cliccata
                Player p = e.getPlayer();

                if (!p.isSneaking()) { // Se il player non ha lo shift

                    ItemMeta metaWand = itemInHand.getItemMeta();

                    List<String> lore = metaWand.getLore();

                    // Modalità in indice 1
                    String selected_mode = lore.get(1).strip().toLowerCase();

                    if (selected_mode.contains(modes[0].toLowerCase()))
                        modeLighting(p);
                    else if (selected_mode.contains(modes[1].toLowerCase()))
                        modeDisappear(p);
                }
            }
        }
    }

    private void modeLighting(@NotNull Player p) {

        // Ottieni la posizione degli occhi del giocatore
        Location eyeLocation = p.getEyeLocation();

        // Ottieni la direzione in cui il giocatore sta guardando
        Vector direction = eyeLocation.getDirection();

        // Calcola la posizione del blocco in cui il fulmine dovrebbe colpire
        Location targetBlock = getTargetBlockLocation(eyeLocation);

        // Lancia il fulmine nella posizione calcolata
        p.getWorld().strikeLightning(targetBlock);
        igniteSurroundingBlocks(targetBlock);

    }

    private void modeDisappear(@NotNull Player p) {

        LivingEntity target = getEntityInLineOfSight(p, 50);

        if (target != null)
        {
            target.remove();
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f,1.0f);
            target.getWorld().spawnParticle(Particle.SMOKE_LARGE, target.getLocation(), 20);
        }

    }

    // Capire quale blocco sto guardando tramite evento
    @EventHandler
    public void seeBlockOnLevitation (@NotNull PlayerInteractEvent event) {

        Player p = event.getPlayer();

        if (event.getAction().toString().contains("RIGHT")) { // Se sto cliccando col destro
            // Verificare se il player ha la wand in mano
            ItemStack item = event.getItem();

            if (isMagicWand(item))
            {
                // Verificare se la wand si trova in modalità Levitation
                ItemMeta metaWand = item.getItemMeta();

                List<String> lore = metaWand.getLore();

                // Modalità in indice 1
                String selected_mode = lore.get(1).strip().toLowerCase();

                if (selected_mode.contains(modes[2].toLowerCase())) // Se contiene Levitation
                {

                    // Ottieni la posizione del giocatore e la direzione in cui sta guardando
                    Vector direction = p.getLocation().getDirection();
                    org.bukkit.Location eyeLocation = p.getEyeLocation();

                    // Imposta la distanza massima per il raycasting
                    double maxDistance = 30.0;
                    double step = 0.1;

                    // Raycasting lungo la linea di visione
                    for (double distance = 0; distance < maxDistance; distance += step) {
                        // Calcola la posizione attuale lungo la direzione del giocatore
                        org.bukkit.Location location = eyeLocation.clone().add(direction.clone().multiply(distance));
                        Block block = location.getBlock();

                        // Controllo se il blocco è diverso da AIR
                        if (block.getType() != Material.AIR) {
                            startLevitation(p,block);
                            break;
                        }
                    }
                }
            }

        }
    }

    private void startLevitation (@NotNull Player player, @NotNull Block block) {

        new BukkitRunnable() {
            final FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(block.getLocation(), block.getType().createBlockData());

            @Override
            public void run () {

                if (fallingBlock.isValid() && player.isOnline()) {
                    // Aumenta l'altezza dell'oggetto per simulare la levitazione
                    fallingBlock.setVelocity(new Vector(0, 0.1, 0));

                    // Se il giocatore ha smesso di premere il tasto destro, ferma il task
                    if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                        fallingBlock.remove();
                        this.cancel();
                    }
                } else {
                    this.cancel();
                }

            }


        }.runTaskTimer(Main.plugin, 0L, 1L);


    }




    //////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS

    // Metodo per ottenere la posizione del blocco a terra nella direzione di guardata del giocatore
    private @NotNull Location getTargetBlockLocation(Location start) {
        // Massima distanza in cui può colpire il fulmine
        int maxDistance = 50;
        BlockIterator iterator = new BlockIterator(start, 0, maxDistance);

        // Itera fino al blocco solido più vicino
        while (iterator.hasNext()) {
            Location blockLocation = iterator.next().getLocation();
            if (blockLocation.getBlock().getType() != Material.AIR) {
                return blockLocation;
            }
        }

        // Se non trova blocchi solidi entro la distanza massima, ritorna la posizione massima
        return start.add(start.getDirection().multiply(maxDistance));
    }

    // Metodo per infiammare i blocchi circostanti al punto di impatto del fulmine
    private void igniteSurroundingBlocks(Location targetLocation) {
        int radius = 3; // Raggio di blocchi da infiammare
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLocation = targetLocation.clone().add(x, y, z);
                    if (blockLocation.getBlock().getType() == Material.AIR) {
                        blockLocation.getBlock().setType(Material.FIRE);
                    }
                }
            }
        }
    }

    private LivingEntity getEntityInLineOfSight(Player player, double range) {
        Vector direction = player.getEyeLocation().getDirection();
        double distance = range;
        Vector eyeLocation = player.getEyeLocation().toVector();

        for (double d = 0; d < distance; d += 0.5) {
            Vector currentLocation = eyeLocation.add(direction.clone().multiply(d));
            if (player.getWorld().getNearbyEntities(currentLocation.toLocation(player.getWorld()), 1, 1, 1).stream()
                    .filter(entity -> entity instanceof LivingEntity)
                    .anyMatch(entity -> !(entity instanceof Player))) {
                return (LivingEntity) player.getWorld().getNearbyEntities(currentLocation.toLocation(player.getWorld()), 1, 1, 1).stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .findFirst().orElse(null);
            }
        }
        return null;
    }

    private boolean isMagicWand(ItemStack item) {
        if (item == null) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        // Verifica il nome
        if (!MCPlugin_Util.nameWand.equals(meta.getDisplayName())) return false;

        // Verifica la lore
        List<String> lore = meta.getLore();
        if (lore == null) return false;

        return true;
    }

    public ItemStack giveWand () {
        return magic_wand;
    }

    public ItemMeta getMeta_magic_wand() {
        return meta_magic_wand;
    }

}
