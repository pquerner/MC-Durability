package de.paskl.durability;

import de.paskl.durability.utils.ConfigManager;
import de.paskl.durability.utils.Helper;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

public class GlobalListener implements Listener {

    private Main plugin;
    private FileConfiguration f;
    EnumMap originals;
    private Player p;
    //Whether or not this current interaction can be used
    private boolean canRun = false;
    //Contains a string in this format: <blockid>;<x-coordinate>;<y-coordinate>;<z-coordinate>;
    private ArrayList<String> xyzBlock = new ArrayList<>();

    public GlobalListener(Main plugin) {
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

    /**
     * Durability Method when the player took damage and the armor lost some durability
     *
     * @param p           Player - Player entity
     * @param event       EntityDamageEvent - Current event object
     * @param armorDamage Boolean - Armor took damage
     */
    @SuppressWarnings("deprecated")
    protected void durability(Player p, EntityDamageEvent event, Boolean armorDamage) {
        this.p = p;
        //foreach players armor
        //if any armor is below N durability, output warning message
        int x = 0;

        for (ItemStack armorItem : p.getInventory().getArmorContents()) {
            if (armorItem == null) continue;
            if (armorItem.getDurability() == 0) continue; //this armor piece took no damage
            if (armorItem.getDurability() > 0
                    && f.getInt("minimum-durability") >= armorItem.getType().getMaxDurability() - armorItem.getDurability()) {
                String s = String.format(ChatColor.RED + "[WARNING]: %s has too little duration left!\nDuration left: %d\n", WordUtils.capitalize(armorItem.getType().toString().toLowerCase().replace("_", " ")), calcLeftDurability(armorItem));
                p.sendMessage(s);
            }
        }
    }

    /**
     * Durability Method when the player does damage to an entity
     *
     * @param p     Player - Player entity
     * @param event EntityDamageEvent - Current event object
     */
    @SuppressWarnings("deprecated")
    protected void durability(Player p, EntityDamageEvent event) {
        this.p = p;
        ItemStack heldItem = p.getInventory().getItemInMainHand();
        //No need to check here, its always ok
        canRun = true;
        //TODO ignore when hitting the same target multiple times (but how, xyz changes all the time?)
        //TODO sometimes(?) it shows 15 durability, when in fact its 14 (maybe bc of multiple mob hits?)
        if (canRun) {
            checkDurability(heldItem, "");
        }
    }

    /**
     * Durability Method when the player tries to destroy a block
     *
     * @param p     Player - Player entity
     * @param event PlayerInteractEvent - Current event object
     */
    @SuppressWarnings("deprecated")
    protected void durability(Player p, PlayerInteractEvent event) {
        this.p = p;

        Block block = event.getClickedBlock();
        Action action = event.getAction();
        Material blockType = block.getType();
        Location blockLocation = block.getLocation();
        ItemStack heldItem = p.getInventory().getItemInMainHand();
        Material heldItemType = heldItem.getType();

        //left click = spade,hoe
        //right click = axe,pick axe,sword,spade,hoe,sword
        canRun = isCanCheckDurability(action, heldItemType);

        if (canRun && Helper.isBlockForOutput(blockType)) {
            //Only for blocks
            //TODO get type id nicer than with deprecated method
            final String blockLocationFormat = String.format("%d;%d;%d;%d",
                    block.getTypeId(),
                    blockLocation.getBlockX(),
                    blockLocation.getBlockY(),
                    blockLocation.getBlockZ());

            if (checkDurability(heldItem, blockLocationFormat)) {
                xyzBlock.add(blockLocationFormat);
                //Remove later on, so it can be added again
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (xyzBlock.contains(blockLocationFormat)) xyzBlock.remove(blockLocationFormat);
                    }

                }.runTaskLater(this.plugin, 1220); //1220 = 1 minute (in-game) //TODO make configurable?
            }
        }
    }

    /**
     * Whether or not the current event is allowed to output durability warning message
     *
     * @param action
     * @param heldItemType
     * @return boolean - True if current event is allowed to output durability, false if not
     */
    protected boolean isCanCheckDurability(Action action, Material heldItemType) {
        boolean canRun = false;
        if ((heldItemType.toString().contains("_HOE")
                || heldItemType.toString().contains("_SPADE"))
                && action == Action.RIGHT_CLICK_BLOCK) {
            canRun = true;
        } else if ((heldItemType.toString().contains("_AXE")
                //Pickaxe
                || heldItemType.toString().contains("_PICKAXE")

                //Hoe
                || heldItemType.toString().contains("_HOE")

                //Spade
                || heldItemType.toString().contains("_SPADE")

                //Sword
                || heldItemType.toString().contains("_SWORD")) && (action == Action.LEFT_CLICK_BLOCK)) {
            canRun = true;
        }
        return canRun;
    }

    protected int calcLeftDurability(ItemStack item) {
        //Get duration
        double max = item.getType().getMaxDurability();
        double uses = item.getDurability();
        return ((int) max) - ((int) uses);
    }

    /**
     * Checks the current durability and outputs a message if necessary
     *
     * @param heldItem
     * @param blockLocationFormat
     * @return true if durability warning was send, false if not
     */
    protected boolean checkDurability(ItemStack heldItem, String blockLocationFormat) {
        int leftDurability = calcLeftDurability(heldItem);

        // If the user did something bad here,
        // like destroying the configuration (by entereing non numeric values into the field etc.)
        // the value of "minimum-durability" will be (int)0. therefore it must be corrupt?!
        // Set it back to a reasonable number
        if (f.getInt("minimum-durability") == 0) {
            f.set("minimum-durability", this.plugin.getConfig().getInt("minimum-durability"));
        }

        if (heldItem.getDurability() > 0
                && f.getInt("minimum-durability") >= heldItem.getType().getMaxDurability() - heldItem.getDurability()
                && !xyzBlock.contains(blockLocationFormat)) {
            String s = String.format(ChatColor.RED + "[WARNING]: %s has too little duration left!\nDuration left: %d\n", WordUtils.capitalize(heldItem.getType().toString().toLowerCase().replace("_", " ")), leftDurability);
            p.sendMessage(s);
            return true;
        } /*else { //TODO ONLY FOR DEBUGGING
            Integer integerObject = new Integer("20");
            short dura = (short) (max - integerObject.shortValue());
            heldItem.setDurability(dura);
        } */
        return false;
    }

    /**
     * Eventhandler method for when a player attacks another entity
     *
     * @param event EntityDamageEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbeEvent = (EntityDamageByEntityEvent) event;
            Entity damager = edbeEvent.getDamager();
            if (damager instanceof Player) {
                //Player attacked some entity
                //Show info about players attack tool
                p = (Player) damager;

                if (p != null
                        && this.plugin.commandEnabledForPlayers.contains(p.getName())
                        && this.f.getBoolean("durability_warning_enabled")) {
                    durability(p, event);
                }
            } else if (edbeEvent.getEntity() != null && edbeEvent.getEntity() instanceof Player) {
                //Entity attacked player
                //Check if durability of armor is gone
                //Then check if armor is below critial level and output a warning message
                p = (Player) edbeEvent.getEntity();

                if (-0.0 != edbeEvent.getDamage(EntityDamageEvent.DamageModifier.ARMOR)
                        && this.plugin.commandEnabledForPlayers.contains(p.getName())
                        && this.f.getBoolean("durability_warning_enabled")) {
                    //The armor took damage
                    durability(p, event, true);
                }
            }

        }

    }

    /**
     * Eventhandler method for when a player tries to destroy a block
     *
     * @param event PlayerInteractEvent
     */
    //TODO make items configurable
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (this.plugin.commandEnabledForPlayers.contains(p.getName())
                && this.f.getBoolean("durability_warning_enabled")) {

            if (event.getClickedBlock() != null
                    && event.getAction() != null
                    && (event.getClickedBlock().getType() != null)) {

                durability(p, event);
            }
        }
    }
}
