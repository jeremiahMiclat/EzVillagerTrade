package com.jeremiahMiclat.ezvillagertrade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;


public class EzVillagerTrade extends JavaPlugin implements Listener {
    private final Map<UUID, TradeData> playerTradeData = new HashMap<>();

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
        int itemindex = event.getIndex();
        Merchant merchant = event.getMerchant();
        MerchantInventory merchantInventory = event.getInventory();
        MerchantRecipe recipe = merchant.getRecipe(itemindex);
        Material recipeMaterial = Objects.requireNonNull(recipe.getAdjustedIngredient1()).getType();
        int ingredientCount = recipe.getAdjustedIngredient1().getAmount();
        if  (Material.getMaterial(recipeMaterial.name()) == Material.EMERALD) {
            return;
        }


            Player player = (Player) event.getWhoClicked();
            PlayerInventory playerInventory = player.getInventory();

            int playerIngredientCount =
                    getItemCount(playerInventory, Material.getMaterial(recipeMaterial.name()))
                    + getItemCount(event.getInventory(), Material.getMaterial(recipeMaterial.name()));

            int tradesAvailable = getNumberOfTradesAvailable(recipe.getMaxUses(), playerIngredientCount,ingredientCount);
        if (tradesAvailable < 1) {
            return;
        }

        TradeData tradeData = new TradeData(recipe.getMaxUses(), playerIngredientCount, recipeMaterial,ingredientCount,merchantInventory,tradesAvailable,recipe);
        playerTradeData.put(player.getUniqueId(), tradeData);


    }


    @EventHandler
    public void onEmeraldClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getCurrentItem().getType() == Material.EMERALD) {
            // Check if the clicked inventory belongs to a Villager
            if (event.getInventory().getHolder() instanceof Villager) {
                Player player = (Player) event.getWhoClicked();
                PlayerInventory playerInventory = player.getInventory();
                int emeraldCount = getItemCount(playerInventory, Material.EMERALD)
                        + getItemCount(event.getInventory(), Material.EMERALD);

                // Retrieve the trade data
                TradeData tradeData = playerTradeData.get(player.getUniqueId());

                if (tradeData != null) {
                    player.sendMessage("You clicked on an emerald.");

                    int tradeTotalCost = tradeData.tradeAvailable;
                    int tradeIngredientCost = tradeData.ingredientCount * tradeData.tradeAvailable;

                    ItemStack itemToBeRemoved = new ItemStack(tradeData.recipeMaterial);
                    removeAllItems(player, itemToBeRemoved);
                    removeAllItemsOfMaterialFromMerchant(tradeData.merchantInventory, itemToBeRemoved);
                    ItemStack emeraldForPlayer = new ItemStack(Material.EMERALD, tradeData.tradeAvailable);
                    ItemStack ingredientForPlayer = new ItemStack(tradeData.recipeMaterial, tradeData.playerIngredientCount-tradeIngredientCost);



//                    for (int i = 0; i < tradeData.tradeAvailable; i++) {
//
//                        ItemStack result = tradeData.merchantRecipe.getResult();
//
//                        player.getInventory().addItem(result);
//
//                    }

                    playerInventory.addItem(emeraldForPlayer);
                    playerInventory.addItem(ingredientForPlayer);

                        tradeData.merchantRecipe.setUses(tradeData.tradeAvailable);

                } else {
                    player.sendMessage("No trade data found.");
                }
                playerTradeData.remove(player.getUniqueId());
            }
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

    private static class TradeData {
        int maxUses;
        int playerIngredientCount;
        Material recipeMaterial;
        int ingredientCount;
        MerchantInventory merchantInventory;
        int tradeAvailable;
        MerchantRecipe merchantRecipe;

        TradeData(int maxUses, int playerIngredientCount, Material recipeMaterial, int ingredientCount, MerchantInventory merchantInventory, int tradeAvailable, MerchantRecipe merchantRecipe) {
            this.maxUses = maxUses;
            this.playerIngredientCount = playerIngredientCount;
            this.recipeMaterial = recipeMaterial;
            this.ingredientCount = ingredientCount;
            this.merchantInventory = merchantInventory;
            this.tradeAvailable = tradeAvailable;
            this.merchantRecipe = merchantRecipe;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Villager) {
            Player player = (Player) event.getPlayer();
            playerTradeData.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerTradeData.remove(event.getPlayer().getUniqueId());
    }




    public static void removeAllItems(Player player, ItemStack item) {
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.isSimilar(item)) {
                player.getInventory().setItem(i, null);
            }
        }

        player.updateInventory();
    }

    public static void removeAllItemsOfMaterialFromMerchant(MerchantInventory merchantInventory, ItemStack item) {
        ItemStack[] contents = merchantInventory.getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.isSimilar(item)) {
                merchantInventory.setItem(i, null);
            }
        }
    }

    public int getNumberOfTradesAvailable (int recipeMaxUse, int playerIngredientCount, int ingredientCountPerRecipe) {
        int maxTradesWithIngredient = playerIngredientCount / ingredientCountPerRecipe;
        return Math.min(recipeMaxUse, maxTradesWithIngredient);
    }

}
