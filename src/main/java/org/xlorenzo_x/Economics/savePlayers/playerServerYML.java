package org.xlorenzo_x.Economics.savePlayers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xlorenzo_x.start.Main;

import java.io.File;
import java.io.IOException;

public class playerServerYML {

    public static File file = new File(Main.getInstance().getDataFolder() + "/playerOnServer.yml");
    public static FileConfiguration conf;

    public playerServerYML () {

        if (!file.exists()) {

            try {
                file.createNewFile();
                conf = YamlConfiguration.loadConfiguration(file);
                conf.createSection("server_players");
                conf.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return;
        }

        conf = YamlConfiguration.loadConfiguration(file);
    }

}
