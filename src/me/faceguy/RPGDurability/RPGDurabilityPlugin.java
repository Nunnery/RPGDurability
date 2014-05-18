// This is ToppleTheNun's version of the class
package me.faceguy.RPGDurability;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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

    // SO INEFFICIENT ITS PAINFUL YO!
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event)
    {
        if(event.getEntity() == null)
            return;

        if(!(event.getEntity() instanceof LivingEntity))
            return;

        if(event.getEntityType() == EntityType.PLAYER)
        {
            PlayerInventory inv = ((Player)event.getEntity()).getInventory();
            final String name = ((Player)event.getEntity()).getName();

            final Short arr[] = new Short[4];
            if(inv.getHelmet() != null)
                arr[0] = inv.getHelmet().getDurability();
            if(inv.getChestplate() != null)
                arr[1] = inv.getChestplate().getDurability();
            if(inv.getLeggings() != null)
                arr[2] = inv.getLeggings().getDurability();
            if(inv.getBoots() != null)
                arr[3] = inv.getBoots().getDurability();

            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
            {
                public void run()
                {
                    Player pa = getServer().getPlayerExact(name);
                    if(pa != null)
                    {
                        PlayerInventory inv2 = pa.getInventory();
                        if(inv2 != null)
                        {
                            if(inv2.getHelmet() != null)
                                inv2.getHelmet().setDurability(arr[0]);
                            if(inv2.getChestplate() != null)
                                inv2.getChestplate().setDurability(arr[1]);
                            if(inv2.getLeggings() != null)
                                inv2.getLeggings().setDurability(arr[2]);
                            if(inv2.getBoots() != null)
                                inv2.getBoots().setDurability(arr[3]);
                        }
                    }
                }
            }, 1L);
        }

        if(event.getDamager() == null)
            return;

        if(event.getDamager().getType() != EntityType.PLAYER)
            return;

        if(event.getDamager() instanceof Player)
        {
            Player p = (Player)event.getDamager();
            if(p.getItemInHand() != null)
            {
                ItemStack stack = p.getItemInHand();
                if(stack.getType() == Material.WOOD_AXE || stack.getType() == Material.STONE_AXE || stack.getType() == Material.IRON_AXE ||stack.getType() == Material.GOLD_AXE || stack.getType() == Material.DIAMOND_AXE || stack.getType() == Material.DIAMOND_SWORD || stack.getType() == Material.GOLD_SWORD || stack.getType() == Material.IRON_SWORD || stack.getType() == Material.STONE_SWORD || stack.getType() == Material.WOOD_SWORD)
                {
                    final short prevDura = stack.getDurability();
                    final String name = p.getName();
                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
                    {
                        public void run()
                        {
                            Player pa = getServer().getPlayerExact(name);
                            if(pa != null)
                            {
                                PlayerInventory inv2 = pa.getInventory();
                                if(inv2.getItemInHand() != null)
                                {
                                    inv2.getItemInHand().setDurability(prevDura);
                                }
                            }
                        }
                    }, 1L);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        if (!items.containsKey(id)) {
            return;
        }
        List<ItemStack> itemStacks = items.get(id);
        for (ItemStack itemStack : itemStacks) {
            player.getInventory().addItem(itemStack);
        }
        items.remove(id);
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
            int droppedAmount = amount - newAmount;
            if (newAmount < 0 && amount > 1) {
                continue;
            }
            if (amount > 1) {
                itemStack.setAmount(droppedAmount);
                ItemStack dropItemStack = itemStack.clone();
                dropItemStack.setAmount(Math.max(droppedAmount, 1));
                drops.add(dropItemStack);
                itemStack.setAmount(newAmount);
                keeps.add(itemStack);
            } else {
                if (newDurability >= maxDurability) {
                    player.sendMessage(ChatColor.RED + "Your " + ChatColor.WHITE + getItemName(itemStack) + ChatColor
                            .RED + " has been destroyed!");
                } else {
                    itemStack.setDurability(newDurability);
                    keeps.add(itemStack);
                }
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
