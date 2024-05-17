package com.jeremiahMiclat.ezvillagertrade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EzVillagerTrade extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "EzVillagerTrade Plugin Enabled");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "EzVillagerTrade Plugin Disabled");
    }

    @EventHandler
    public void onInventoryClick(TradeSelectEvent event) {
        // Check if the clicked inventory belongs to a Villager
        if (event.getInventory().getHolder() instanceof Villager) {
            Player player = (Player) event.getWhoClicked();
            int itemindex = event.getIndex();
            Merchant merchant = event.getMerchant();
            MerchantRecipe recipe = merchant.getRecipe(itemindex);
            Material recipeMaterial = recipe.getAdjustedIngredient1().getType();


            player.sendMessage("You have " + recipeMaterial.name() + " in your inventory.");
        }
    }

    private int getItemCount(Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
}
