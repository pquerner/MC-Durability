package de.paskl.durability;

import de.paskl.durability.utils.ConfigManager;
import org.apache.commons.lang.StringUtils;
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

        getLogger().info(String.format("[%s] v%s loaded.", getDescription().getName(), getDescription().getVersion()));
    }

    private void registerListener() {
        new PlayerListener(this);
    }


    /**
     * Handles the plugins commands
     *
     * @param sender - Where the command is send from
     * @param cmd    - Command string
     * @param label  - ?
     * @param args   - Arguments passed to the method
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

            //Generate TreeSet of valid commands for this plugin
            TreeSet<String> commands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            commands.add("duw");
            commands.add("durabilitywarner");

            ConfigManager cm = ConfigManager.getConfig(player);
            FileConfiguration f = cm.getConfig();

            if (commands.contains(cmd.getName())) {
                if (args[0].equalsIgnoreCase("0") || args[0].equalsIgnoreCase("off")) {
                    player.sendMessage(ChatColor.RED + String.format("[%s] is now disabled for you.", getDescription().getName()));
                    if (!this.commandDisabledForPlayers.contains(playerName)) {
                        f.set("durability_warning_enabled", false);
                        cm.saveConfig();

                        this.commandDisabledForPlayers.add(playerName);
                        this.commandEnabledForPlayers.remove(playerName);
                    }
                } else if (args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("on")) {
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


            //Generate TreeSet of valid commands for this plugin
            TreeSet<String> pluginConfigurationCommand = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            pluginConfigurationCommand.add("duwc");
            pluginConfigurationCommand.add("durabilitywarnerconfig");

            if (pluginConfigurationCommand.contains(cmd.getName())) {
                //I expect the value in key=value format.
                String split[] = args[0].split("=");
                String key = split[0];
                String value = split[1];

                //Check if the key is even valid by looking it up in the config.yml
                if (this.getConfig().isString(key) || this.getConfig().isInt(key)) {
                    player.sendMessage(ChatColor.AQUA + String.format("[%s] Set '%s' to '%s'.", getDescription().getName(), key, value));
                    if (StringUtils.isNumeric(value)) {
                        f.set(key, Integer.parseInt(value));
                    } else if (StringUtils.isNotEmpty(value)) {
                        f.set(key, value);
                    }
                    cm.saveConfig();
                } else {
                    player.sendMessage(ChatColor.RED + String.format("[%s] Key '%s' does not exist. Ignoring this input.", getDescription().getName(), key));
                }
                return true;

            }
        } else {
            sender.sendMessage("Only players can issue the plugin commands.");
        }

        return false;
    }

}
