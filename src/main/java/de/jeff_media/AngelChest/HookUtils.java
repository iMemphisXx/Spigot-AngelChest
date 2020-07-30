package de.jeff_media.AngelChest;

import de.jeff_media.AngelChest.hooks.InventoryPagesHook;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HookUtils implements Listener {

    final Main main;
    final InventoryPagesHook inventoryPagesHook;
    //ArrayList<Entity> hologramsToBeSpawned = new ArrayList<Entity>();
    //boolean hologramToBeSpawned = false;

    HookUtils(Main main) {
        this.main=main;
        this.inventoryPagesHook = new InventoryPagesHook(main);
    }

    boolean isSlimefunSoulbound(ItemStack item) {
        if(item==null) return false;
        if(!main.getConfig().getBoolean("use-slimefun")) return false;

        try {
            Class.forName("io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils");
            return SlimefunUtils.isSoulbound(item);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            main.getConfig().set("use-slimefun",false);
            return false;
        }
    }

    boolean isDisabledMaterial(ItemStack item) {
        if(item==null) return false;
        String type = item.getType().name();
        for(String mat : main.disabledMaterials) {
            if(mat.equalsIgnoreCase(type)) return true;
        }
        return false;
    }

    boolean removeOnDeath(ItemStack item) {
        if(isDisabledMaterial(item)) return true;
        if(inventoryPagesHook.isButton(item)) return true;
        return false;
    }

    boolean keepOnDeath(ItemStack item) {
        if(item==null) return false;
        if( isSlimefunSoulbound(item)) return true;
        return false;
    }

}