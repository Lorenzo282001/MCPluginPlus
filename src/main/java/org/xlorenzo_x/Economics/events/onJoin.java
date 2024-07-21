package org.xlorenzo_x.Economics.events;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xlorenzo_x.Economics.savePlayers.playerServerYML;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class onJoin implements Listener {

    @EventHandler // Verifico se il player che sta entrando sia nuovo oppure no! Se è nuovo setto il suo balance iniziale
    public void onJoinNewPlayer(@NotNull PlayerJoinEvent event) throws IOException {
        Player player = event.getPlayer();

        boolean isAnewPlayer = false;
        HashMap<Object, Object> listPlayers = loadPlayersFromYAML(); // Carica i giocatori dal file YAML

        if (!listPlayers.isEmpty()) {
            for (Map.Entry<Object, Object> data : listPlayers.entrySet()) {
                // data keys conterrà il nome del player -- data values conterrà una mappa con balance = 0.0
                String playerName = (String) data.getKey();

                if (!playerName.equals(player.getName())) {
                    isAnewPlayer = true;
                } else {                                  // Se il nome del giocatore nel file è diverso dal giocatore nel
                    isAnewPlayer = false;              // server, allora è un nuovo giocatore. Questo, fino a quando non
                    break;                            // trova quel giocatore. Se lo trova, imposta a false e stoppa il for.
                }

            }
        }
        else {
            isAnewPlayer = true;
        }

        if (isAnewPlayer) { // Allora player è un giocatore nuovo appena entrato!
            // Lo aggiungo...

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(playerServerYML.file);

            conf.createSection("server_players." + player.getName());
            conf.set("server_players." + player.getName() + ".balance", MCPlugin_Util.e_balance);

            conf.save(playerServerYML.file);
            YamlConfiguration.loadConfiguration(playerServerYML.file);
        }
    }


    // Metodo per caricare i giocatori dal file YAML
    public static @Nullable HashMap<Object, Object> loadPlayersFromYAML() {
        try {
            FileInputStream dataServerPlayers = new FileInputStream(playerServerYML.file.getPath());
            Map<String, Object> data = new Yaml().load(dataServerPlayers);

            // Verificare se l'inzio del file abbia o meno la scritta server_players

            for (Map.Entry<String, Object> d : data.entrySet())
            {
                if (d.getKey().equals("server_players")){
                    if (d.getValue() != null){
                        return (HashMap<Object, Object>) d.getValue();
                    }
                    else {
                        return new HashMap<>();
                    }
                }
                else {
                    // Se non è cosi` vuol dire che poi darà mappa nulla
                    YamlConfiguration conf = YamlConfiguration.loadConfiguration(playerServerYML.file);

                    conf.createSection("server_players");
                    conf.save(playerServerYML.file);
                    YamlConfiguration.loadConfiguration(playerServerYML.file);

                    return new HashMap<>();
                }
            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("Errore nel caricare i giocatori dal file YAML: " + e.getMessage());
            return null;
        }
        return null;
    }


}
