package org.xlorenzo_x.Utility;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class MCPlugin_Util {

    public static String namePlugin = ChatColor.GOLD + "[MC_PluginPlus]" + ChatColor.WHITE;

    // Li sto usando nelle gui !
    public static Material empty_cell_invGui = Material.PURPLE_STAINED_GLASS_PANE;
    public static Material exitButton = Material.REDSTONE_BLOCK; // Exit Button for GUI

    public static Material confirmButton = Material.GREEN_CONCRETE; // To confirm a choice

    public static Material rejectButton = Material.RED_CONCRETE; // To reject a choice

    public static Material backTo = Material.YELLOW_CONCRETE; // Per tornare indietro nelle gui
    ///////////////////////////////////////////////////////////////////////////////////////////

    // Personal Bot Skin
    public static EntityType personalBotSkin = EntityType.BAT; // Cambia l'aspetto to del personal Bot
    public static ItemStack personalBot_richiamo = new ItemStack(Material.STICK);
    public static String richiamoNameOnItemStack = ChatColor.DARK_PURPLE + "Call Your Personal Bot";
    public static String invPersonalBot_Title = ChatColor.DARK_PURPLE  +  "Inventory Personal Bot";
    public static Sound soundOnSpawnPb = Sound.ENTITY_LIGHTNING_BOLT_IMPACT;

    /////////////////////////////////////////////////////////////////////////////////////////
    // Economics Part
    public static float e_balance = 100.0f;
    public static Material money = Material.SUNFLOWER;
    public static int marketInventoryCells = 54;
    public static String marketName = ChatColor.RED + "" + ChatColor.MAGIC + "## " + ChatColor.DARK_PURPLE + "The Market" + ChatColor.RED + ChatColor.MAGIC + " ##";

    public static String loreMoney = ChatColor.DARK_PURPLE + "MCEconomy";
    public static String displayNameMoney = ChatColor.GOLD + "Money " + ChatColor.DARK_GREEN;

    // Magic Part
    public static ItemStack magicWand = new ItemStack(Material.END_ROD, 1);
    public static String nameWand = "   " + ChatColor.BLACK + "" +  ChatColor.MAGIC + "#" +  ChatColor.DARK_RED + " The Magic Wand" + ChatColor.BLACK + ChatColor.MAGIC + " #";


    ////////////////////////////////////////////////////////////////////////////////////////

    // Metodo per verificare se un materiale è presente in un array di Materials
    @Contract(pure = true)
    public static boolean isInListMats(Material material, Material @NotNull [] array) {
        for (Material mat : array) {
            if (material == mat) {
                return true;
            }
        }
        return false;
    }

    // Definisci i tipi di blocchi che sono considerati interagibili
    // Tutti i blocchi seguenti NON verranno considerati dal RIGHT_CLICK! [guarda SU]
    public static boolean isInteractable(Material material) {

        Material[] listaMaterialiInteragibili = {Material.CHEST,
                Material.FURNACE,
                Material.ORANGE_BED,
                Material.CRAFTING_TABLE};

        for (Material m : listaMaterialiInteragibili)  {

            if (m.equals(material)){
                return true;
            }

        }

        return false;
    }

    // PLAY SOUNDS
    public static void playSoundOrbThrow (@NotNull Player p)
    {
        Sound s = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        // Supponiamo che "volume" e "pitch" siano il volume e il pitch del suono, rispettivamente
        float volume = 1f;
        float pitch = 1.0f;
        p.playSound(p.getLocation(), s, volume, pitch);
    }

}
