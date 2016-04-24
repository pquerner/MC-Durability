package de.paskl.durability;

import de.paskl.durability.utils.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.TreeSet;


public class Main extends JavaPlugin {

    private PluginManager manager;
    public ArrayList<String> commandDisabledForPlayers = new ArrayList<>();
    public ArrayList<String> commandEnabledForPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        this.manager = this.getServer().getPluginManager();
        this.registerListener();

        getConfig().options().copyDefaults(true);
        saveConfig();

        getLogger().info(String.format("[%s] v%s loaded.", getDescription().getName(), getDescription().getVersion().toString()));
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " is now disabled.");
    }

    public void registerListener() {
        new PlayerListener(this);
    }


    /**
     * En/Disabled the modules functionality
     *
     * @param sender - Where the command is send from
     * @param cmd    - Command string
     * @param label  - ?
     * @param args   - ?
     * @return bool
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {

            if (args.length == 0 || args.length >= 2) {
                return false;
            }

            //Cast to Player object
            Player player = (Player) sender;
            String playerName = player.getName();

            //Generate TreeSet of valid commands for disabling this plugin
            TreeSet<String> commands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            commands.add("duw");
            commands.add("durabilitywarner");

            ConfigManager cm = ConfigManager.getConfig(player);
            FileConfiguration f = cm.getConfig();

            if (commands.contains(cmd.getName())) {
                if (args[0].toString().equalsIgnoreCase("0") || args[0].toString().equalsIgnoreCase("off")) {
                    player.sendMessage(ChatColor.RED + String.format("[%s] is now disabled for you.", getDescription().getName()));
                    if (!this.commandDisabledForPlayers.contains(playerName)) {
                        f.set("durability_warning_enabled", false);
                        cm.saveConfig();

                        this.commandDisabledForPlayers.add(playerName);
                        this.commandEnabledForPlayers.remove(playerName);
                    }
                } else if (args[0].toString().equalsIgnoreCase("1") || args[0].toString().equalsIgnoreCase("on")) {
                    player.sendMessage(ChatColor.GREEN + String.format("[%s] is now enabled for you.", getDescription().getName()));
                    if (!this.commandEnabledForPlayers.contains(playerName)) {
                        f.set("durability_warning_enabled", true);
                        cm.saveConfig();
                        this.commandEnabledForPlayers.add(playerName);
                        this.commandDisabledForPlayers.remove(playerName);
                    }
                }

                return true;

            }
        } else {
            sender.sendMessage("Only players can issue the plugin commands.");
        }

        return false;
    }

}
