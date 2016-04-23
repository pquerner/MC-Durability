package de.paskl.durability;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private Main plugin;

    public PlayerListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority= EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event){
        Player p = event.getPlayer();
        ItemStack heldItem = p.getInventory().getItemInMainHand();

        Material m = heldItem.getType();

        //TODO beautify this
        if(m == Material.DIAMOND_AXE
                || m == Material.GOLD_AXE
                || m == Material.IRON_AXE
                || m == Material.STONE_AXE

                || m == Material.DIAMOND_PICKAXE
                || m == Material.GOLD_PICKAXE
                || m == Material.IRON_PICKAXE
                || m == Material.STONE_PICKAXE
                || m == Material.WOOD_PICKAXE

                || m == Material.DIAMOND_HOE
                || m == Material.GOLD_HOE
                || m == Material.IRON_HOE
                || m == Material.STONE_HOE
                || m == Material.WOOD_HOE

                || m == Material.DIAMOND_SPADE
                || m == Material.GOLD_SPADE
                || m == Material.IRON_SPADE
                || m == Material.STONE_SPADE
                || m == Material.WOOD_SPADE

                || m == Material.DIAMOND_SWORD
                || m == Material.GOLD_SWORD
                || m == Material.IRON_SWORD
                || m == Material.STONE_SWORD
                || m == Material.WOOD_SWORD){


            double max = heldItem.getType().getMaxDurability();
            double uses = heldItem.getDurability();
            int leftDurability = ((int) max) - ((int) uses);

            //TODO make configurable
            if(uses > 0 && 20.0 >= max - uses) {
                p.sendMessage(ChatColor.RED + "Item has too little duration!\nLeft Duration: " + leftDurability);
                //event.setCancelled(true);
            }/* else { //TODO ONLY FOR DEBUGGING
                Integer integerObject = new Integer("20");
                short dura = (short)(max - integerObject.shortValue());
                heldItem.setDurability(dura);
            }*/
        }
    }
}
