package org.xlorenzo_x.start;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.xlorenzo_x.BotMinecraft.PersonalBot.GUI_PersonalBot.SaveInventory.InventoryGui_YAMLConf;
import org.xlorenzo_x.BotMinecraft.PersonalBot.PersonalBot;
import org.xlorenzo_x.BotMinecraft.PersonalBot.commands.DestroyBotCommand;
import org.xlorenzo_x.BotMinecraft.PersonalBot.commands.TakeInventoryOfPlayer;
import org.xlorenzo_x.BotMinecraft.PersonalBot.commands.spawnBotCommand;
import org.xlorenzo_x.Economics.commands.MoneyToHandCommand;
import org.xlorenzo_x.Economics.commands.Shops.Markets.Market;
import org.xlorenzo_x.Economics.commands.Shops.Markets.PlayerPutOnMarketCommand;
import org.xlorenzo_x.Economics.commands.Shops.Markets.YAML_Market.Market_GuiYaml;
import org.xlorenzo_x.Economics.commands.getBalanceCommand;
import org.xlorenzo_x.Economics.events.OnClickMoneySymbol;
import org.xlorenzo_x.Economics.events.onJoin;
import org.xlorenzo_x.Economics.savePlayers.playerServerYML;
import org.xlorenzo_x.Magic.MagicWand;
import org.xlorenzo_x.Magic.commands.WandCommand;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.general.commands.FindPlayers;
import org.xlorenzo_x.general.commands.FlyCommand;
import org.xlorenzo_x.general.commands.SondaggioCommand;
import org.xlorenzo_x.general.commands.sendAllToBarCommand;
import org.xlorenzo_x.general.commands.trackPlayer.Gui_Track;
import org.xlorenzo_x.general.commands.trackPlayer.TrackCommand;
import org.xlorenzo_x.general.events.OnJoinEvents;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Main extends JavaPlugin {

    public static Main plugin;
    private static boolean statePl = false; // Diventa true quando reload è azionato da codice
    private static TrackCommand trackCommandInstance;
    private static SondaggioCommand sondaggioCommand;
    @Override
    public void onEnable() {
        // Salvo i config!
        saveDefaultConfig();

        plugin = this;
        new InventoryGui_YAMLConf(); // File YAML per salvare gli inventari del Personal Gui
        new Market_GuiYaml(); // Crea il file Market.yml
        new playerServerYML();

        // Inizializzazione del plugin
        trackCommandInstance = TrackCommand.getInstance(); // Mi salvo questa istanza per recuperare alcune variabili!
        sondaggioCommand = SondaggioCommand.getInstance();

        // Events in General
        Bukkit.getPluginManager().registerEvents(new OnJoinEvents(), this); // Events When a Player join in the server!
        Bukkit.getPluginManager().registerEvents(getSondaggioCommandInstance(), this); // EventHandler per il sondaggio!
        Bukkit.getPluginManager().registerEvents(getTrackCommandInstance(), this); // Evitare di prendere gli oggetti dalla gui
        Bukkit.getPluginManager().registerEvents(new Gui_Track(), this); // Gui To Manage Player

        //Events Personal Bot
        Bukkit.getPluginManager().registerEvents(new PersonalBot(), this);

        // Economics Events
        Bukkit.getPluginManager().registerEvents(new onJoin(), this);
        Bukkit.getPluginManager().registerEvents(new OnClickMoneySymbol(), this);
        Bukkit.getPluginManager().registerEvents(new Market(), this); // asta
        Bukkit.getPluginManager().registerEvents(new MoneyToHandCommand(), this); // Eventi per controllare i soldi!

        // Magic Part
        Bukkit.getPluginManager().registerEvents(new MagicWand(), this);

        // ----------------------------------------------------------------------------

        // Commands in General
        Objects.requireNonNull(getCommand("getallplayers")).setExecutor(new FindPlayers());
        Objects.requireNonNull(getCommand("fly")).setExecutor(new FlyCommand());
        Objects.requireNonNull(getCommand("telechat")).setExecutor(getSondaggioCommandInstance());
        Objects.requireNonNull(getCommand("sendToAll")).setExecutor(new sendAllToBarCommand());
        Objects.requireNonNull(getCommand("trackP")).setExecutor(getTrackCommandInstance());

        // Commands Personal Bot
        Objects.requireNonNull(getCommand("spawnPB")).setExecutor(new spawnBotCommand());
        Objects.requireNonNull(getCommand("despawnPB")).setExecutor(new DestroyBotCommand());
        getCommand("pbgui").setExecutor(new TakeInventoryOfPlayer());
        /////////

        // Commands Economic
        getCommand("balance").setExecutor(new getBalanceCommand());
        getCommand("takemoney").setExecutor(new MoneyToHandCommand());
        getCommand("market").setExecutor(new Market()); // Asta dei materiali venduti dai players!
        getCommand("putmarket").setExecutor(new PlayerPutOnMarketCommand()); // Inserisco un oggetto (della mano) in vendita nel market!

        // Commands Magic
        getCommand("wand").setExecutor(new WandCommand()); // Spawna una wand in mano al player

        ///////////////////

        // Prima di iniziare i timer devo prendere il [current_item] della classe PlayerPutOnMarketCommand.java
        countCurrentItemOnMarket();

        // Start Market timers
        startTimersOfMarket();

        // Avvio il task periodico ogni 10 minuti
        int delayTicks = 10 * 60 * 20; // 10 minuti in ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                onReload();
            }
        }.runTaskTimer(this, delayTicks, delayTicks);

        double versionPlugin = 2.0;

        if (!statePl) {
            Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Version:" + versionPlugin);
            Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_PURPLE + "----------------------------------------");
            Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_PURPLE + "| Caricamento effettuato con successo! |");
            Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_PURPLE + "----------------------------------------");
        }

        new newRecipes(); // Creo le nuove ricette!
    }

    @Override
    public void onDisable() {

        try {
            PersonalBot.saveMapInventoryPersonalGui();
            if (!statePl)
                Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + " [INVENTORY PERSONAL GUI] - Salvando gli inventari");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Plugin shutdown logic
        if (!statePl)
            Bukkit.getServer().getConsoleSender().sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Unloaded...");

    }

    public static Main getInstance() {
        return plugin;
    }


    // Metodo per ottenere l'istanza di TrackCommand
    public static TrackCommand getTrackCommandInstance() {
        return trackCommandInstance;
    }

    public static SondaggioCommand getSondaggioCommandInstance() {
        return sondaggioCommand;
    }

    public static void onReload()
    {
        // Market.addOneMinuteOnMarket(); // Aggiungo un minuto al timer default per tutti gli oggetti del market!

        statePl = true;

        plugin.reloadConfig();
        plugin.getServer().getPluginManager().disablePlugin(plugin);
        plugin.getServer().getPluginManager().enablePlugin(plugin);

    }



    public static void countCurrentItemOnMarket() {

        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(Market_GuiYaml.file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> data = new Yaml().load(inputStream);

        LinkedHashMap<String, Object> market = (LinkedHashMap<String, Object>) data.get("market");

        int x = 0;
        if (market != null && !market.isEmpty()) {
            for (Map.Entry<String, Object> i : market.entrySet()) {
                x++;
            }
        }

        PlayerPutOnMarketCommand.current_item = x;

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // onEnable creare dei timer per il market, per tutti gli oggetti!
    public void startTimersOfMarket()
    {
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
                int countMarket = Integer.parseInt(counting.getKey());
                LinkedHashMap<Object, Object> realMarket = (LinkedHashMap<Object, Object>) counting.getValue();

                for (Map.Entry<Object, Object> i : realMarket.entrySet()) {
                    Material m = Material.getMaterial((String) i.getKey());

                    Market.findItemToCancel.put(countMarket, m);
                    LinkedHashMap<Object, Object> Materialproperty = (LinkedHashMap<Object, Object>) i.getValue();

                    int minutes = 0;
                    int seconds = 0;
                    for (Map.Entry<Object, Object> p : Materialproperty.entrySet()) {

                        if (p.getKey().equals("time")) {
                            LinkedHashMap<Object, Object> times = (LinkedHashMap<Object, Object>) p.getValue();

                            for (Map.Entry<Object, Object> t : times.entrySet()) {
                                if (t.getKey().equals("minutes"))
                                    minutes = (int) t.getValue();
                                if (t.getKey().equals("seconds"))
                                    seconds = (int) t.getValue();
                            }
                        }

                        if (seconds > 0)
                        {
                            // Aggiungo i secondi ai minuti!
                            minutes = ((minutes * 60) + seconds) / 60;
                        }

                        PlayerPutOnMarketCommand.startTimer(Integer.parseInt(counting.getKey()),m.name(), minutes * 60 * 20);
                    }

                }
            }

        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
