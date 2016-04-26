package de.paskl.durability;

import de.paskl.durability.utils.ConfigManager;
import de.paskl.durability.utils.Helper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerListener implements Listener {

    private Main plugin;
    private FileConfiguration f;
    //Contains a string in this format: <blockid>;<x-coordinate>;<y-coordinate>;<z-coordinate>;
    private ArrayList<String> xyzBlock = new ArrayList<>();

    public PlayerListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() != null && this.plugin.getConfig().getBoolean("enabled-by-default")) {
            Player p = event.getPlayer();

            ConfigManager cm = ConfigManager.getConfig(p);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
            Date now = new Date();

            if (!cm.exists()) {
                FileConfiguration f = cm.getConfig();
                this.f = f;
                f.set("name", p.getName());
                f.set("uuid", p.getUniqueId().toString());
                f.set("join_date", sdf.format(now));
                f.set("last_join", sdf.format(now));
                f.set("durability_warning_enabled", this.plugin.getConfig().getBoolean("enabled-by-default"));
                f.set("minimum-durability", this.plugin.getConfig().getInt("minimum-durability"));
                cm.saveConfig();
            } else {
                FileConfiguration f = cm.getConfig();
                this.f = f;
                f.set("last_join", sdf.format(now));
                cm.saveConfig();
            }

            //Only enable if user wanted to, or default says so
            if (f.getBoolean("durability_warning_enabled")) {
                this.plugin.getServer().dispatchCommand(p, "duw 1");
            }
        }
    }

    //TODO make items configurable
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (this.plugin.commandEnabledForPlayers.contains(p.getName())
                && this.f.getBoolean("durability_warning_enabled")) {

            ItemStack heldItem = p.getInventory().getItemInMainHand();
            //Whether or not this current interaction can be used
            boolean canRun = false;

            if (event.getClickedBlock() != null && event.getAction() != null && (event.getClickedBlock().getType() != null)) {
                Block block = event.getClickedBlock();
                Material blockType = block.getType();
                Location blockLocation = block.getLocation();
                Material heldItemType = heldItem.getType();

                //left click = spade,hoe
                //right click = axe,pick axe,sword,spade,hoe,sword
                if ((heldItemType.toString().contains("_HOE")
                        || heldItemType.toString().contains("_SPADE"))
                        && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    canRun = true;
                } else if ((heldItemType.toString().contains("_AXE")
                        //Pickaxe
                        || heldItemType.toString().contains("_PICKAXE")

                        //Hoe
                        || heldItemType.toString().contains("_HOE")

                        //Spade
                        || heldItemType.toString().contains("_SPADE")

                        //Sword
                        || heldItemType.toString().contains("_SWORD")) && (event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    canRun = true;
                }

                if (canRun && Helper.isBlockForOutput(blockType)) {

                    //Get duration
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

                    //TODO get type id nicer than with deprecated method
                    final String blockLocationFormat = String.format("%d;%d;%d;%d",
                            block.getTypeId(),
                            blockLocation.getBlockX(),
                            blockLocation.getBlockY(),
                            blockLocation.getBlockZ());

                    if (uses > 0 && f.getInt("minimum-durability") >= max - uses && !xyzBlock.contains(blockLocationFormat)) {
                        p.sendMessage(ChatColor.RED + "[WARNING]: Item has too little duration left!\nDuration left: " + leftDurability + "\n");

                        //event.setCancelled(true); //If you want to cancel the event. Can be bad in combat, so its disabled.
                        xyzBlock.add(blockLocationFormat);

                        //Remove later on, so it can be added again
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                if (xyzBlock.contains(blockLocationFormat)) xyzBlock.remove(blockLocationFormat);
                            }

                        }.runTaskLater(this.plugin, 1220); //1220 = 1 minute (in-game) //TODO make configurable?

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
