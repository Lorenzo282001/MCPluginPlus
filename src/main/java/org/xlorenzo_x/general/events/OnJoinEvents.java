package org.xlorenzo_x.general.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.xlorenzo_x.Utility.TextOnScreen;
import org.xlorenzo_x.start.Main;

public class OnJoinEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e)
    {
        String salutoColorato = String.valueOf(ChatColor.BOLD);
        String saluto = Main.getInstance().getConfig().getString("settings.saluto-msg");

        assert saluto != null;
        salutoColorato = salutoColorato.concat(saluto);
        if (salutoColorato.contains("[player]"))
        {
            salutoColorato = salutoColorato.replace("[player]", ChatColor.RED + "" + ChatColor.BOLD + e.getPlayer().getDisplayName() + ChatColor.WHITE + ChatColor.BOLD);
        }

        if (Boolean.parseBoolean(Main.getInstance().getConfig().getString("settings.saluto")))
        {
            new TextOnScreen().showTextOnScreenPlayer(e.getPlayer(), salutoColorato);
        }
    }


}
