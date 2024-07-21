# Tracciamento degli utenti: [fatto]
    Un sistema per monitorare le azioni dei giocatori, come il numero di uccisioni, le morti, i blocchi piazzati
    e distrutti, per avere un maggiore controllo sull'attività nel server.
    
    -- Come potrei farlo?
        
        Potrei creare una gui che si apre quando faccio quel comando, con dei blocchi all'interno e 
        modificare titolo/lore di quel blocco per far vedere le statistiche medie di quel player!

        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = player.getStatistic(Statistic.DEATHS);
        int blocksBroken = player.getStatistic(Statistic.MINE_BLOCK, Material.AIR);
        int blocksPlaced = player.getStatistic(Statistic.USE_ITEM, Material.AIR);

# Aggiungere un bot a minecraft per effettuare diverse azioni [fatto]
        
    public static void spawnBot(Location location) {
        LivingEntity bot = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        bot.setMetadata("bot_entity", new FixedMetadataValue(Main.getPlugin(Main.class), true));
        bot.setAI(true);
    }

       public static void spawnBot(Location location) {
        LivingEntity bot = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        bot.setCustomName("Steve");
        bot.setCustomNameVisible(true);
        bot.setAI(true);
        bot.setRemoveWhenFarAway(false); // Impedisci alla creatura di despawnare quando il giocatore è lontano
        bot.getEquipment().setHelmet(/* Setta l'elmetto di Steve */);
        bot.getEquipment().setChestplate(/* Setta il pettorale di Steve */);
        bot.getEquipment().setLeggings(/* Setta i pantaloni di Steve */);
        bot.getEquipment().setBoots(/* Setta gli stivali di Steve */);
    }


    public static boolean isBot(LivingEntity entity) {
        List<MetadataValue> metadata = entity.getMetadata("bot_entity");
        for (MetadataValue value : metadata) {
            if (value.value() instanceof Boolean && (Boolean) value.value()) {
                return true;
            }
        }
        return false;
    }


//////////////////////////////////////////////

# Aggiunto lato economia [in progress]






