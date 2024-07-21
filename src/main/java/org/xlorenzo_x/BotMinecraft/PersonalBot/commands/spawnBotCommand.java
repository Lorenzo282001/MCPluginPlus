package org.xlorenzo_x.BotMinecraft.PersonalBot.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.BotMinecraft.PersonalBot.PersonalBot;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.start.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class spawnBotCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("spawnPB")) {
            if (sender instanceof Player p)
            {
                if (p.hasPermission("botPersonal.spawn")) {

                    boolean find = false;

                    for (Map.Entry<Player, PersonalBot> entry : PersonalBot.listaPersonalBotServer.entrySet()) {
                        Player player = entry.getKey();
                        PersonalBot personalBot = entry.getValue();

                        if (player.equals(p)) {
                            p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_RED + personalBot.ownerBot.getDisplayName() + ChatColor.GOLD + " è già spawnato il tuo bot!");
                            find = true;
                            break;
                        }

                    }

                    if (!find) { // Se non lo trovo nella lista, allora posso spawnarlo!
                        Random random = new Random();
                        int randomX = random.nextInt(5) + 1; // Genera un numero casuale da 1 a 5 per x
                        int randomZ = random.nextInt(5) + 1; // Genera un numero casuale da 1 a 5 per z

                        Location location = new Location(p.getWorld(), p.getLocation().getX() + randomX, p.getLocation().getY() + 1, p.getLocation().getZ() + randomZ);
                        PersonalBot nuovoBot = new PersonalBot(p);
                        nuovoBot.spawnPersonalBot(p, location);
                        // Aggiungo effetto fumo e Suono
                        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 50, 0.5, 0.5, 0.5, 0.1);
                        // Audio entita`
                        Sound soundOnSpawn = MCPlugin_Util.soundOnSpawnPb;

                        // Supponiamo che "volume" e "pitch" siano il volume e il pitch del suono, rispettivamente
                        float volume = 0.6f;
                        float pitch = 1.0f;
                        nuovoBot.personalBotFollow.getWorld().playSound(nuovoBot.personalBotFollow.getLocation(), soundOnSpawn, volume, pitch);
                        // Rimuovo effetto fumo

                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                            location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 0); // Interrompe tutte le particelle in quella posizione
                        }, 15);

                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.BLUE + " Hai richiamato il tuo bot personale!");

                        // Give item to call your bot

                        Inventory inv = p.getInventory();

                        boolean stickFind = false;
                        for (ItemStack i : inv.getContents())
                        {
                            if (i != null) {
                                if (i.getType().equals(Material.STICK) && i.getItemMeta().getDisplayName().equals(MCPlugin_Util.richiamoNameOnItemStack)) {
                                    stickFind = true;
                                    break;
                                }
                            }
                        }

                        if (!stickFind){

                            ItemStack richiamo = MCPlugin_Util.personalBot_richiamo;
                            ItemMeta metaRichiamo = richiamo.getItemMeta();
                            metaRichiamo.setDisplayName(MCPlugin_Util.richiamoNameOnItemStack);
                            List<String> lore = new ArrayList<>();
                            lore.add(ChatColor.GREEN + "- " + ChatColor.GOLD + p.getName());
                            metaRichiamo.setLore(lore);
                            metaRichiamo.addEnchant(Enchantment.KNOCKBACK, 2, false);
                            richiamo.setItemMeta(metaRichiamo);

                            p.getInventory().addItem(richiamo);
                        }
                    }
                }
                else {
                    p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Non hai i permessi per eseguire questo comando!");
                }

            }
            else {
                sender.sendMessage(MCPlugin_Util.namePlugin + " Non puoi eseguire questo comando!");
            }
        }


        return false;
    }
}
