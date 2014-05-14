// This is ToppleTheNun's version of the class
package me.faceguy.RPGDurability;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RPGDurabilityPlugin extends JavaPlugin implements Listener {

    private double damagePercentage;
    private double amountToKeep;
    private Map<UUID, List<ItemStack>> items;

    @Override
    public void onEnable() {
        items = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this);
        File f = new File(getDataFolder(), "config.yml");
        if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
            return;
        }
        damagePercentage = getConfig().getDouble("damage-amount", 0.25);
        amountToKeep = getConfig().getDouble("amount-to-keep", 0.25);
    }

    @Override
    public void onDisable() {
        getConfig().set("damage-amount", damagePercentage);
        getConfig().set("amount-to-keep", amountToKeep);
        saveConfig();
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        if (!items.containsKey(id)) {
            return;
        }
        List<ItemStack> itemStacks = items.get(id);
        items.remove(id);
        for (ItemStack itemStack : itemStacks) {
            player.getInventory().addItem(itemStack);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        // players keep all armor
        // players lose 75% of itemstack, keep 25%
        // itemstack gets durability damage
        Player player = event.getEntity();
        PlayerInventory playerInventory = player.getInventory();
        List<ItemStack> drops = new ArrayList<>();
        List<ItemStack> keeps = new ArrayList<>();
        event.getDrops().clear();
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack itemStack = playerInventory.getItem(i);
            if (itemStack == null) {
                continue;
            }
            // if the index is greater than 8, then it's in the main part of the inventory
            if (i >= 9) {
                drops.add(itemStack);
                continue;
            }
            // since we used a continue in the previous if statement, then we don't need to add another if
            // to see if the index is less than 9
            short maxDurability = itemStack.getType().getMaxDurability();
            short curDurability = itemStack.getDurability();
            short newDurability = (short) (curDurability + damagePercentage * maxDurability);

            int amount = itemStack.getAmount();
            int newAmount = (int) (amountToKeep * amount);
            if (newAmount < 0 && amount > 1) {
                continue;
            }
            if (amount > 1) {
                itemStack.setAmount(newAmount);
            }
            if (newDurability >= maxDurability) {
                player.sendMessage(ChatColor.RED + "Your " + ChatColor.WHITE + getItemName(itemStack) + ChatColor
                        .RED + " has broken!");
            } else {
                itemStack.setDurability(newDurability);
                keeps.add(itemStack);
            }
        }
        // I intentionally didn't make it so that it autoputs armor back
        // since that's relatively simple with the basis I've set here.
        // All you need to do is make another HashMap to track the things from here
        // and then in the respawn, add the armor pieces into their respective spots.
        for (ItemStack itemStack : playerInventory.getArmorContents()) {
            if (itemStack == null) {
                continue;
            }
            short maxDurability = itemStack.getType().getMaxDurability();
            short curDurability = itemStack.getDurability();
            short newDurability = (short) (curDurability + Math.round(damagePercentage * maxDurability));
            if (newDurability >= maxDurability) {
                player.sendMessage(ChatColor.RED + "Your " + ChatColor.WHITE + getItemName(itemStack) + ChatColor
                        .RED + " has broken!");
            } else {
                itemStack.setDurability(newDurability);
                keeps.add(itemStack);
            }
        }

        items.put(player.getUniqueId(), keeps);
        for (ItemStack itemStack : drops) {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }

    private String getItemName(ItemStack itemStack) {
        String name;
        String materialName = itemStack.getType().name();
        String[] split = materialName.split("_");
        // it's better to use a stringbuilder than adding to a string endlessly
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : split) {
            stringBuilder.append(s.substring(0, 1).toUpperCase()).append(s.substring(1, s.length()).toLowerCase());
        }
        name = stringBuilder.toString();
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            name = itemStack.getItemMeta().getDisplayName();
        }
        return name;
    }

}
