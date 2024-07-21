package org.xlorenzo_x.BotMinecraft.PersonalBot;

// UNICODE CHARACTER HEARTH: \u2764

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.BotMinecraft.PersonalBot.GUI_PersonalBot.GuiBot;
import org.xlorenzo_x.BotMinecraft.PersonalBot.GUI_PersonalBot.SaveInventory.InventoryGui_YAMLConf;
import org.xlorenzo_x.BotMinecraft.PersonalBot.holderGui.HolderPersonalBotGui;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.start.Main;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class PersonalBot implements Listener { // Tutti gli eventi del bot

    public static Map<Player, PersonalBot> listaPersonalBotServer = new HashMap<>();
    public static Map<Player, GuiBot> listaInventoryPlayerBot = new HashMap<>();  // Creo una guiBot solo se non si trova qui dentro

    public boolean alreadySpawned = false;

    public Player ownerBot;

    public BukkitTask autoChunk; // AutoChunk + Raggio movimento!
    private static final int chunkRadius = 3; // Imposta il raggio dei chunk che vuoi caricare attorno al bot
    private static final int raggio_playerEntity = 6; // Distanza massima tra player e la entità

    public LivingEntity personalBotFollow = null; // PERSONAL BOT

    private boolean personalBot_onStop = false; // Se è falso vuol dire che il bot è in movimento!
    private final static int timeStop = 10; // seconds

    public PersonalBot() {}

    public PersonalBot (Player p) {
        listaPersonalBotServer.put(p, this);
    }

    public void spawnPersonalBot(Player p, @NotNull Location location) {
        ownerBot = p;
        LivingEntity p_bot = (LivingEntity) Objects.requireNonNull(location.getWorld()).spawnEntity(location, MCPlugin_Util.personalBotSkin);
        p_bot.setMetadata("p_bot_entity", new FixedMetadataValue(Main.getPlugin(Main.class), true));
        p_bot.setAI(true); // Disattivo l'intelligenza artificiale
        p_bot.setInvulnerable(true);
        p_bot.resetMaxHealth();
        p_bot.setHealth(20);

        p_bot.setSilent(true);
        p_bot.setCustomName(ChatColor.GOLD + ownerBot.getDisplayName() + ChatColor.DARK_PURPLE + "'s Bot" +  ChatColor.DARK_RED + " \u2764 " + ChatColor.GOLD + p_bot.getHealth());
        personalBotFollow = p_bot;


        autoChunk = Bukkit.getServer().getScheduler().runTaskTimer(Main.getInstance(), () -> {
            if (personalBotFollow != null) {

                //Auto Chunk
                // Ottieni la posizione attuale del bot
                Location botLocation = personalBotFollow.getLocation(); // Metodo per ottenere la posizione attuale del bot

                // Carica i chunk circostanti
                loadChunksAroundLocation(botLocation); // Metodo per caricare i chunk circostanti

                // ------------
                // Raggio Movimento
                double distanceSquared = p.getLocation().distanceSquared(botLocation);

                if (distanceSquared > (raggio_playerEntity * raggio_playerEntity)) {

                    // L'entità è al di fuori del raggio, ricalcola la posizione
                    double ratio = raggio_playerEntity / Math.sqrt(distanceSquared);
                    double newX = p.getLocation().getX() + (botLocation.getX() - p.getLocation().getX()) * ratio;
                    double newY = p.getLocation().getY() + (botLocation.getY() - p.getLocation().getY()) * ratio;
                    double newZ = p.getLocation().getZ() + (botLocation.getZ() - p.getLocation().getZ()) * ratio;

                    // Imposta la nuova posizione per l'entità
                    personalBotFollow.teleport(new Location(p.getWorld(), newX, newY, newZ, botLocation.getYaw(), botLocation.getPitch()));
                }
            }
        }, 0, 20); // Esegui il controllo ogni secondo (20 ticks)

        alreadySpawned = true;
    }

    public boolean isPersonalBot(@NotNull LivingEntity entity) {
        List<MetadataValue> metadata = entity.getMetadata("p_bot_entity");
        for (MetadataValue value : metadata) {
            if (value != null && value.value() instanceof Boolean && (Boolean) value.value()) {
                return true;
            }
        }
        return false;
    }

    // Metodo per far attaccare un'entità ad un'altra
    public void attackEntity(@NotNull LivingEntity attacker, LivingEntity target) {
        attacker.attack(target); // L'entità "attacker" attacca l'entità "target"
    }

    private void stopMovementBot (Player playerEvent){ // Mantengo immobile il bot per tot TIME

        for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet()) {

            Player player = entry.getKey();
            PersonalBot personalBot = entry.getValue();

            if (player.equals(playerEvent.getPlayer())) {

                if (personalBot.personalBotFollow != null && isPersonalBot(personalBot.personalBotFollow)) {
                    personalBot.personalBotFollow.setAI(false);
                    personalBot.personalBot_onStop = true;
                }


                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {

                    if (personalBot.personalBotFollow != null) {
                        personalBot.personalBotFollow.setAI(true);
                        personalBot.personalBot_onStop = false;

                            InventoryView invView = player.getOpenInventory();

                            if (invView.getTitle().contains(MCPlugin_Util.invPersonalBot_Title)) {
                                invView.close();
                            }

                    }

                }, 20L * timeStop);

                break;

            }
        }
    }

    // Metodo per caricare i chunk
    private void loadChunksAroundLocation(@NotNull Location location) {
        // Carica i chunk attorno alla posizione specificata con un determinato raggio
        int chunkX = location.getBlockX() >> 4; // Calcola la coordinata X del chunk
        int chunkZ = location.getBlockZ() >> 4; // Calcola la coordinata Z del chunk

        // Carica i chunk nell'area definita dal raggio
        for (int x = chunkX - PersonalBot.chunkRadius; x <= chunkX + PersonalBot.chunkRadius; x++) {
            for (int z = chunkZ - PersonalBot.chunkRadius; z <= chunkZ + PersonalBot.chunkRadius; z++) {
                Objects.requireNonNull(location.getWorld()).loadChunk(x, z); // Carica il chunk
            }
        }
    }

    private @NotNull Location getBlockAheadLocation(@NotNull Player player) {
        // Ottieni la direzione in cui il giocatore sta guardando
        Vector direction = player.getEyeLocation().getDirection();

        // Aggiungi un offset alla posizione attuale del giocatore per ottenere la posizione del blocco davanti
        // Ritorna la posizione del blocco davanti
        return player.getLocation().add(direction);
    }

    // Evento per verificare se il bot è morto!
    @EventHandler
    public void onPersonalBotDeath(@NotNull EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();

        if (isPersonalBot(entity)){
            alreadySpawned = false;
            personalBotFollow = null;
        }

        try {
            saveMapInventoryPersonalGui();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPlayerClickGround(PlayerInteractEvent event) {

        for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet()) {

            Player player = entry.getKey();
            PersonalBot personalBot = entry.getValue();

            if (player.equals(event.getPlayer())) {

                if (personalBot.personalBotFollow != null && personalBot.ownerBot.getName().equals(event.getPlayer().getName()) && !personalBot.personalBot_onStop) {
                    // Player
                    Player p = event.getPlayer();
                    Block blockClicked = event.getClickedBlock();

                    if (blockClicked != null && !MCPlugin_Util.isInteractable(blockClicked.getType())){ // Definisci i tipi di blocchi che sono considerati interagibili e non verranno considerati!

                        Location blockAheadLocation = getBlockAheadLocation(p).add(0, 1.5, 0);

                        if (event.getAction().name().equalsIgnoreCase("RIGHT_CLICK_AIR") || event.getAction().name().equalsIgnoreCase("RIGHT_CLICK_BLOCK")) {

                            if ((p.getInventory().getItemInMainHand().getType() == MCPlugin_Util.personalBot_richiamo.getType()) || (p.getInventory().getItemInOffHand().getType() == MCPlugin_Util.personalBot_richiamo.getType()))
                            {
                                if ((p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(MCPlugin_Util.richiamoNameOnItemStack)) || (p.getInventory().getItemInOffHand().getItemMeta().getDisplayName().equals(MCPlugin_Util.richiamoNameOnItemStack)))
                                {
                                    personalBot.personalBotFollow.teleport(blockAheadLocation);
                                    stopMovementBot(player);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMoveEvent (PlayerMoveEvent event) {

        for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet()) {

            Player player = entry.getKey();
            PersonalBot personalBot = entry.getValue();

            if (player.equals(event.getPlayer())) {

                if (personalBot.personalBotFollow != null && personalBot.ownerBot.getName().equals(event.getPlayer().getName())) {

                    // Player
                    Player p = event.getPlayer();
                    Location playerLocation = p.getLocation();

                    // Distance player from bot
                    double distance = playerLocation.distance(personalBot.personalBotFollow.getLocation());

                    if (distance >= 25) {
                        personalBot.personalBotFollow.teleport(new Location(p.getWorld(), playerLocation.getX() + 1, +playerLocation.getY(), playerLocation.getZ() + 1));
                    }

                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEntityTakeDamage(@NotNull EntityDamageEvent event) { // Resetto la vita al massimo!

        if (event.getEntity() instanceof LivingEntity) {
            if (isPersonalBot((LivingEntity) event.getEntity())) {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {

                    event.getEntity().setInvulnerable(true);
                    ((LivingEntity) event.getEntity()).setHealth(20);

                }, 600);
            }
        }
    }

    // Se clicco sull'entità ---- Carico l'inventario!
    @EventHandler
    public void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) throws FileNotFoundException {
        Player player_event = event.getPlayer();
        Entity clickedEntity = event.getRightClicked(); // Tasto destro!

        for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet()) {

            Player player = entry.getKey();
            PersonalBot personalBot = entry.getValue();

            if (player.equals(player_event)) {
                // Verifica se il giocatore ha fatto clic su un'entità
                if (clickedEntity instanceof LivingEntity) {
                    if (personalBot.personalBotFollow != null && isPersonalBot((LivingEntity) clickedEntity) && personalBot.personalBot_onStop) { // Da sistemare!

                        // Creo una new guiBot solo se non si trova nella lista statica dell'inventario
                        // listaInventoryPlayerBot MAPPA

                        boolean playerFound = false;
                        for (Map.Entry<Player, GuiBot> gui : PersonalBot.listaInventoryPlayerBot.entrySet()) {
                            Player p = gui.getKey();
                            GuiBot g = gui.getValue();

                            if (player.equals(p)) { // Vuol dire che già è stato trovato un inventory Bot associato a quel giocatore
                                playerFound = true;

                                // CARICO L'INVENTARIO
                                loadInventoryToPlayer(g, player);

                                break;
                            }

                        }

                        if (!playerFound) { // Prima volta!

                            GuiBot g = new GuiBot();

                            // Visto che non ha trovato nessun Player con un inventario Gui del bot,
                            // lo aggiungo.

                            listaInventoryPlayerBot.put(player, g); // Salvo la nuova gui!

                            // CARICO L'INVENTARIO
                            loadInventoryToPlayer(g, player);
                        }
                    }
                }
            }
        }
    }

    public static void deleteAllBotsOnReload () {
        for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet()) {

            Player player = entry.getKey();
            PersonalBot personalBot = entry.getValue();

            personalBot.ownerBot = null;
            personalBot.personalBotFollow.remove();
            personalBot.personalBotFollow = null;
            personalBot.autoChunk = null;

            PersonalBot.listaPersonalBotServer.remove(player);
            player.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Il tuo bot personale è andato via!");
        }
    }

    @EventHandler
    public void onPlayerQuitRemoveBot (PlayerQuitEvent event) throws IOException { // Quando un player esce dal server, in automatico il bot scompare!


        for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet())
        {
            Player p = entry.getKey();
            PersonalBot personalBot = entry.getValue();

            if (event.getPlayer().equals(p)) {

                saveMapInventoryPersonalGui();
                personalBot.ownerBot = null;
                personalBot.personalBotFollow.remove();
                personalBot.personalBotFollow = null;
                personalBot.autoChunk = null;

                PersonalBot.listaPersonalBotServer.remove(p);
                PersonalBot.listaInventoryPlayerBot.remove(p);
                break;
            }
        }

        Main.onReload();
    }

    @EventHandler // Quando chiudo l'inventario salvo i dati
    public void onPlayerQuitInventory (@NotNull InventoryCloseEvent event) throws IOException {
        if (event.getInventory().getHolder() instanceof HolderPersonalBotGui)
        {
            saveMapInventoryPersonalGui();
        }
    }

    // Funzione per salvare la mappa player gui degli inventari
    public static void saveMapInventoryPersonalGui () throws IOException {
        int numero_inventario = 0; // Numero totale di persone
        for (Map.Entry<Player, GuiBot> entry : PersonalBot.listaInventoryPlayerBot.entrySet()) {

            if (entry != null) {
                Player player = entry.getKey();
                GuiBot g = entry.getValue();

                if (player != null && g != null) {
                    // Salvare la map nel file YAML // inventories.players
                    String nuovaSezione = "inventories.players." + numero_inventario;
                    InventoryGui_YAMLConf.conf.createSection(nuovaSezione);

                    // Configuro il player
                    InventoryGui_YAMLConf.conf.set(nuovaSezione + ".playerName", player.getName());

                    // Configuro l'inventario!
                    InventoryGui_YAMLConf.conf.createSection(nuovaSezione + ".inventory");

                    ItemStack[] contents = g.getInventory().getContents();
                    for (int slot = 0; slot < contents.length; slot++) {
                        ItemStack item = contents[slot];
                        if (item != null) {

                            // Section invetory
                            InventoryGui_YAMLConf.conf.createSection(nuovaSezione + ".inventory." + slot);

                            // Verifico se ha un customName
                            if (item.hasItemMeta()) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta.hasDisplayName()) {
                                    if (!meta.getDisplayName().equals(item.getType().name())){
                                        InventoryGui_YAMLConf.conf.set(nuovaSezione + ".inventory." + slot + ".customName", meta.getDisplayName());
                                    }
                                }
                            }

                            InventoryGui_YAMLConf.conf.set(nuovaSezione + ".inventory." + slot + ".material", item.getType().toString());
                            InventoryGui_YAMLConf.conf.set(nuovaSezione + ".inventory." + slot + ".quantity", item.getAmount());

                            // Verifico durability
                            int durability = item.getDurability();
                            if (durability > 0) { // Se è maggiore di 0, vuol dire che l'item ha subito dei danni pari a durability
                                InventoryGui_YAMLConf.conf.set(nuovaSezione + ".inventory." + slot + ".durability", durability);
                            }
                            // Se è pari a 0, o è un tools/armor che non ha subito danni oppure è un item che non subisce danni (es. CraftingTable)

                            // Verifico enchants
                            if (!item.getEnchantments().isEmpty()) { // Se ha degli enchants!

                                InventoryGui_YAMLConf.conf.createSection(nuovaSezione + ".inventory." + slot + ".enchants");

                                // Ottieni la mappa degli incantesimi e itera su di essa
                                int e = 1;
                                Map<Enchantment, Integer> enchantments = item.getEnchantments();
                                for (Map.Entry<Enchantment, Integer> enchantsEntry : enchantments.entrySet()) {
                                    Enchantment enchantment = enchantsEntry.getKey();
                                    String level = String.valueOf(enchantsEntry.getValue());

                                    InventoryGui_YAMLConf.conf.createSection(nuovaSezione + ".inventory." + slot + ".enchants." + e);
                                    InventoryGui_YAMLConf.conf.set(nuovaSezione + ".inventory." + slot + ".enchants." + e + ".name", enchantment.getName());
                                    InventoryGui_YAMLConf.conf.set(nuovaSezione + ".inventory." + slot + ".enchants." + e + ".lvl", level);
                                    e++;
                                }
                            }
                        }
                    }

                    // Aumento il conteggio degli inventari!
                    numero_inventario += 1;
                }
            }
        }


        InventoryGui_YAMLConf.conf.set("inventories.size", numero_inventario);

        InventoryGui_YAMLConf.conf.save(InventoryGui_YAMLConf.file);
        YamlConfiguration.loadConfiguration(InventoryGui_YAMLConf.file);
    }

    // Carico gli inventari ogni qual volta vengano aperti
    public static void loadInventoryToPlayer(GuiBot guiBot, Player player) throws FileNotFoundException {

        // Carico inventario da file YAML
        // Ricerca del player nello YAML
        FileInputStream inputStream = new FileInputStream(InventoryGui_YAMLConf.file.getPath());

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

                if (playerName.equals(player.getName())) { // Controllo a chi devo caricare l' inv!
                    LinkedHashMap<String, Object> inventory = (LinkedHashMap<String, Object>) playerData.get("inventory");

                    // Ora puoi fare qualcosa con i dati del giocatore

                    // Puoi anche iterare sull'inventario del giocatore se necessario
                    if (inventory != null) {
                        for (Map.Entry<String, Object> inventoryEntry : inventory.entrySet()) {
                            String slotInventario = inventoryEntry.getKey();
                            Object itemStack = inventoryEntry.getValue();

                            // Ignoriamo il warning di unchecked cast
                            Map<String, Object> itemData = (Map<String, Object>) itemStack;

                            Material material = Material.valueOf((String) itemData.get("material")); // Tipo dell'oggetto
                            int amount = (int) itemData.get("quantity"); // Quantità

                            ItemStack nuovoItemStack = new ItemStack(material, amount);

                            // Verificare se l'oggetto ha un customName
                            if (itemData.containsKey("customName") && itemData.get("customName") instanceof String customName) {
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
                                            for (Map.Entry<String, String> f : theEnchant.entrySet()) {
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
                                                } else {
                                                    break;
                                                }
                                            }

                                            // Carico l'incantesimo
                                            if (nomeIncantesimo != null) {
                                                if (livello > -1) {
                                                    nuovoItemStack.addEnchantment(Enchantment.getByName(nomeIncantesimo), livello);
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            guiBot.getInventory().setItem(Integer.parseInt(slotInventario), nuovoItemStack);
                        }
                    }
                }
            }
        }

        squitSound(player);

        // Dopo aver caricato, se c'è, apro l'inventario!
        guiBot.openInventory(player);

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void squitSound (Player player) {

        // Riproduco audio dell'entità
        for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet())
        {
            Player p = entry.getKey();
            PersonalBot pb = entry.getValue();

            if (p.equals(player)) {
                // Audio entita`
                Sound sound = Sound.ENTITY_BAT_AMBIENT;

                // Supponiamo che "volume" e "pitch" siano il volume e il pitch del suono, rispettivamente
                float volume = 0.2f;
                float pitch = 1.0f;

                pb.personalBotFollow.getWorld().playSound(pb.personalBotFollow.getLocation(), sound, volume, pitch);
                break;
            }
        }

    }


}
