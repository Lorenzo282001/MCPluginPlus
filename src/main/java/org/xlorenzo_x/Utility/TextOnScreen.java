package org.xlorenzo_x.Utility;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TextOnScreen {

    public void showTextOnScreenPlayer(@NotNull Player player, String message)
    {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }

    public void showTextOnScreenAll(String message){ // Invio un messaggio ti tipo ACTION_BAR a tutti!

        Player[] OnlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        // Itera attraverso la lista dei giocatori online e stampa i loro nomi
        for (Player player : OnlinePlayers) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        }
    }

}

