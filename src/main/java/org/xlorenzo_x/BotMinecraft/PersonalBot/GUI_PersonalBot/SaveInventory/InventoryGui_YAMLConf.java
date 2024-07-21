package org.xlorenzo_x.BotMinecraft.PersonalBot.GUI_PersonalBot.SaveInventory;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xlorenzo_x.start.Main;

import java.io.File;
import java.io.IOException;

public class InventoryGui_YAMLConf {

    public static File file = new File(Main.getInstance().getDataFolder() + "/inventoryPersonalGuis.yml");
    public static FileConfiguration conf;

    public InventoryGui_YAMLConf () {

        if (!file.exists()) {

            try {
                file.createNewFile();
                conf = YamlConfiguration.loadConfiguration(file);
                conf.createSection("inventories");
                conf.set("inventories.size", 0);
                conf.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return;
        }

        conf = YamlConfiguration.loadConfiguration(file);
    }


}
