package org.xlorenzo_x.start;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.xlorenzo_x.Utility.MCPlugin_Util;

import java.util.List;

import static org.xlorenzo_x.Magic.MagicWand.modes;

public class newRecipes {

    protected newRecipes () {

        recipe_magicWand();

    }


    private void recipe_magicWand() {

        ItemStack magic_wand = MCPlugin_Util.magicWand;
        ItemMeta meta_magic_wand = magic_wand.getItemMeta();

        meta_magic_wand.setDisplayName(MCPlugin_Util.nameWand);
        meta_magic_wand.setLore(List.of(
                ChatColor.DARK_PURPLE + "           MCPlugin",
                ChatColor.WHITE + " Mode:     " + ChatColor.GOLD + modes[0].toUpperCase(),
                "",
                ChatColor.GRAY + "SHIFT + WHEEL to select modes."
        ));

        magic_wand.setItemMeta(meta_magic_wand);

        ShapedRecipe magicWandRecipe = new ShapedRecipe(magic_wand);

        magicWandRecipe.shape(" D ", "RER", " A ");

        magicWandRecipe.setIngredient('D', Material.DIAMOND);
        magicWandRecipe.setIngredient('R', Material.REDSTONE);
        magicWandRecipe.setIngredient('E', Material.END_ROD);
        magicWandRecipe.setIngredient('A', Material.AMETHYST_BLOCK);

        Bukkit.addRecipe(magicWandRecipe);

    }


}
