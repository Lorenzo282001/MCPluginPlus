package org.xlorenzo_x.Economics.commands.Shops.Markets.YAML_Market;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xlorenzo_x.start.Main;

import java.io.File;
import java.io.IOException;

public class Market_GuiYaml {

    public static File file = new File(Main.getInstance().getDataFolder() + "/market.yml");
    public static FileConfiguration conf;

    public Market_GuiYaml () {

        if (!file.exists()) {

            try {
                file.createNewFile();
                conf = YamlConfiguration.loadConfiguration(file);
                conf.createSection("market");
                conf.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return;
        }

        conf = YamlConfiguration.loadConfiguration(file);
    }

}
