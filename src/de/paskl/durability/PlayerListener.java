package de.paskl.durability;

import de.paskl.durability.utils.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlayerListener implements Listener {

    private Main plugin;
    private FileConfiguration f;

    public PlayerListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() != null && this.plugin.getConfig().getBoolean("enabled-by-default")) {
            Player p = event.getPlayer();

            ConfigManager cm = ConfigManager.getConfig(p);
            Date now = new Date();

            if (!cm.exists()) {
                FileConfiguration f = cm.getConfig();
                this.f = f;
                f.set("name", p.getName());
                f.set("uuid", p.getUniqueId().toString());
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
                f.set("join_date", format.format(now));
                f.set("last_join", format.format(now));
                f.set("durability_warning_enabled", this.plugin.getConfig().getBoolean("enabled-by-default"));
                f.set("minimum-durability", this.plugin.getConfig().getInt("minimum-durability"));
                cm.saveConfig();
            } else {
                FileConfiguration f = cm.getConfig();
                this.f = f;
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
                f.set("last_join", format.format(now));
                cm.saveConfig();
            }

            //Only enable if user wanted to, or default says so
            if (f.getBoolean("durability_warning_enabled")) {
                this.plugin.getServer().dispatchCommand(p, "duw 1");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (this.plugin.commandEnabledForPlayers.contains(p.getName()) && this.f.getBoolean("durability_warning_enabled")) {
            ItemStack heldItem = p.getInventory().getItemInMainHand();

            if (event.getClickedBlock() != null && (event.getClickedBlock().getType() != null)) {
                Block block = event.getClickedBlock();
                Material blockType = block.getType();
                Material heldItemType = heldItem.getType();

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

                    // If the user did something bad here,
                    // like destroying the configuration (by entereing non numeric values into the field etc.)
                    // the value of "minimum-durability" will be (int)0. therefore it must be corrupt?!
                    // Set it back to a reasonable number
                    if (f.getInt("minimum-durability") == 0) {
                        f.set("minimum-durability", this.plugin.getConfig().getInt("minimum-durability"));
                    }

                    if (uses > 0 && f.getInt("minimum-durability") >= max - uses) {
                        p.sendMessage(ChatColor.RED + "[WARNING]: Item has too little duration left!\nDuration left: " + leftDurability + "\n");
                        //event.setCancelled(true); //If you want to cancel the event. Can be bad in combat, so its disabled.
                    }/* else { //TODO ONLY FOR DEBUGGING
                        Integer integerObject = new Integer("20");
                        short dura = (short) (max - integerObject.shortValue());
                        heldItem.setDurability(dura);
                    }*/
                }
            }
        }
    }
}
