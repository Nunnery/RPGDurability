// This is Faceguy's version of the main class

// Packages should be all lowercase. Most of the time.
package me.faceguy.RPGDurability;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Command;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Not really bad, but Main is not a good name for a class. It's nondescriptive of what the class does.
// need to implement org.bukkit.event.Listener for events
public class Main extends JavaPlugin implements Listener {

//    Map<String, List<ItemStack>> deathQueue = new HashMap();
    // The literal only thing that could be improved with this is what I did below. You're using a Java 7 idiom
    // called a diamond. In order to use the diamond properly, you need to have the angle brackets.
    // And maybe access priority.
    private Map<String, List<ItemStack>> deathQueue = new HashMap<>();

    // While not required, it's nice to always put @Override if you're overriding something.
    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " is now disabled, just like its author!");
    }

    // While not required, it's nice to always put @Override if you're overriding something.
    @Override
    public void onEnable() {
        // need to register events for events to work
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info(getDescription().getName() + " has been enabled brah!");
    }

    // I honestly don't remember how to do commands without my command framework. So I'm no help here.
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("repair")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ItemStack item = player.getItemInHand();
                item.setDurability((short) 0);
                player.sendMessage("Item repaired!");
            } else {
                sender.sendMessage("You are not a player, fool!");
            }
        }
        return true;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Again, Java 7 diamonds.
        // These start off empty, no need to clear them.
        List<ItemStack> droppedItems = new ArrayList<>();
        List<ItemStack> keptItems = new ArrayList<>();

        PlayerInventory test = event.getEntity().getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = test.getItem(i);
            if (keepOnDeath(stack)) {
                String displayName = stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName() :
                                     stack.getType().name().replace("_", " ").toLowerCase();
                short maxdura = stack.getType().getMaxDurability();
                short dura = (short) (int) (maxdura * 0.25D);
                dura = (short) (stack.getDurability() + dura);
                stack.setDurability(dura);
                if (dura > maxdura) {
                    // Remember to use the ChatColor instead of the section symbol
                    event.getEntity().sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + displayName +
                                                  ChatColor.YELLOW + "has broken!");
                } else {
                    keptItems.add(stack);
                }
            } else if (stack != null) {
                if (stack.getAmount() > 1) {
                    double oldAmount = stack.getAmount();
                    int keptAmount = (int) Math.ceil(oldAmount / 4.0D);
                    int droppedAmount = (int) oldAmount - keptAmount;
                    ItemStack keptStack = new ItemStack(stack);
                    keptStack.setAmount(keptAmount);
                    keptItems.add(keptStack);
                    stack.setAmount(droppedAmount);
                    droppedItems.add(stack);
                } else {
                    droppedItems.add(stack);
                }
            }
        }
        for (int i = 9; i < test.getSize(); i++) {
            ItemStack stack = test.getItem(i);
            if (stack != null) {
                droppedItems.add(stack);
            }
        }
        ItemStack armorTemp = test.getHelmet();
        if (keepOnDeath(armorTemp)) {
            String displayName = armorTemp.getItemMeta().hasDisplayName() ? armorTemp.getItemMeta().getDisplayName() :
                                 armorTemp.getType().name().replace("_", " ").toLowerCase();

            short maxdura = armorTemp.getType().getMaxDurability();
            short dura = (short) (int) (maxdura * 0.25D);
            dura = (short) (armorTemp.getDurability() + dura);
            armorTemp.setDurability(dura);
            if (dura > maxdura) {
                event.getEntity().sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + displayName +
                                              ChatColor.YELLOW + "has broken!");
            } else {
                keptItems.add(armorTemp);
            }
        }
        armorTemp = test.getChestplate();
        if (keepOnDeath(armorTemp)) {
            String displayName = armorTemp.getItemMeta().hasDisplayName() ? armorTemp.getItemMeta().getDisplayName() :
                                 armorTemp.getType().name().replace("_", " ").toLowerCase();

            short maxdura = armorTemp.getType().getMaxDurability();
            short dura = (short) (int) (maxdura * 0.25D);
            dura = (short) (armorTemp.getDurability() + dura);
            armorTemp.setDurability(dura);
            if (dura > maxdura) {
                event.getEntity().sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + displayName +
                                              ChatColor.YELLOW + "has broken!");
            } else {
                keptItems.add(armorTemp);
            }
        }
        armorTemp = test.getLeggings();
        if (keepOnDeath(armorTemp)) {
            String displayName = armorTemp.getItemMeta().hasDisplayName() ? armorTemp.getItemMeta().getDisplayName() :
                                 armorTemp.getType().name().replace("_", " ").toLowerCase();

//            short maxdura = Material.getMaterial(armorTemp.getTypeId()).getMaxDurability();
            // Not sure why you need to use the convoluted Material stuff here... Just use armorTemp.getType()
            // .getMaxDurability().
            short maxdura = armorTemp.getType().getMaxDurability();
            short dura = (short) (int) (maxdura * 0.25D);
            dura = (short) (armorTemp.getDurability() + dura);
            armorTemp.setDurability(dura);
            if (dura > maxdura) {
                event.getEntity().sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + displayName +
                                              ChatColor.YELLOW + "has broken!");
            } else {
                keptItems.add(armorTemp);
            }
        }
        armorTemp = test.getBoots();
        if (keepOnDeath(armorTemp)) {
            String displayName = armorTemp.getItemMeta().hasDisplayName() ? armorTemp.getItemMeta().getDisplayName() :
                                 armorTemp.getType().name().replace("_", " ").toLowerCase();

            short maxdura = armorTemp.getType().getMaxDurability();
            short dura = (short) (int) (maxdura * 0.25D);
            dura = (short) (armorTemp.getDurability() + dura);
            armorTemp.setDurability(dura);
            if (dura > maxdura) {
                event.getEntity().sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + displayName +
                                              ChatColor.YELLOW + "has broken!");
            } else {
                keptItems.add(armorTemp);
            }
        }
        event.getDrops().clear();
        event.getDrops().addAll(droppedItems);

        this.deathQueue.put(event.getEntity().getName(), keptItems);
    }

    public boolean keepOnDeath(ItemStack stack) {
        return false;
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        if (this.deathQueue.containsKey(event.getPlayer().getName())) {
            List<ItemStack> items = this.deathQueue.get(event.getPlayer().getName());
            for (int i = 0; i < items.size(); i++) {
//                event.getPlayer().getInventory().addItem(new ItemStack[]{(ItemStack) items.get(i)});
                // ew
                // Okay, so what's wrong here is that you're casting unnecessarily and unnecessarily creating an array
                // What the addItem(ItemStack...) method does is it accepts an array and a comma separated list of
                // items. It's referred to as varargs. So addItem(item1, item2, item3) works too.
                event.getPlayer().getInventory().addItem(items.get(i));
            }
            this.deathQueue.remove(event.getPlayer().getName());
        }
    }
}