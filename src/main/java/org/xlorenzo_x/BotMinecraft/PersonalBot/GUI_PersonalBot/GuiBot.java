package org.xlorenzo_x.BotMinecraft.PersonalBot.GUI_PersonalBot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.xlorenzo_x.BotMinecraft.PersonalBot.holderGui.HolderPersonalBotGui;
import org.xlorenzo_x.Utility.MCPlugin_Util;


public class GuiBot implements Listener {

    private final Inventory inventory;
    public static final int inv_cellDimension = 27;

    public GuiBot () {
        inventory = Bukkit.createInventory(new HolderPersonalBotGui(), inv_cellDimension, MCPlugin_Util.invPersonalBot_Title);
    }

    public GuiBot (String titoloInv) {
        inventory = Bukkit.createInventory(new HolderPersonalBotGui(), inv_cellDimension, titoloInv);
    }

    public void openInventory (Player p) {
        p.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }


}
