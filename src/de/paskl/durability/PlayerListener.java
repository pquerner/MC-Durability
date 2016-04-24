package de.paskl.durability;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (this.plugin.commandEnabledForPlayers.contains(p.getName())) {
            ItemStack heldItem = p.getInventory().getItemInMainHand();

            if (event.getClickedBlock() != null && (event.getClickedBlock().getType() != null)) {
                Block block = event.getClickedBlock();
                Material blockType = block.getType();
                Material heldItemType = heldItem.getType();

                //TODO beautify this
                if (//Axe
                        (heldItemType.toString().contains("_AXE")
                                //Pickaxe
                                || heldItemType.toString().contains("_PICKAXE")

                                //Hoe
                                || heldItemType.toString().contains("_HOE")

                                //Spade
                                || heldItemType.toString().contains("_SPADE")

                                //Sword
                                || heldItemType.toString().contains("_SWORD"))


                                && blockType != Material.AIR) {


                    double max = heldItem.getType().getMaxDurability();
                    double uses = heldItem.getDurability();
                    int leftDurability = ((int) max) - ((int) uses);

                    //TODO make configurable
                    if (uses > 0 && 20.0 >= max - uses) {
                        p.sendMessage(ChatColor.RED + "[WARNING]: Item has too little duration left!\nDuration left: " + leftDurability + "\n");
                        //event.setCancelled(true); //If you want to cancel the event. Can be bad in combat, so its disabled.
                    }/* else { //TODO ONLY FOR DEBUGGING
                Integer integerObject = new Integer("20");
                short dura = (short)(max - integerObject.shortValue());
                heldItem.setDurability(dura);
            }*/
                }
            }
        }
    }
}
