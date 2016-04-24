package de.paskl.durability;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

            //Cast to Player object
            Player player = (Player) sender;
            String playerName = player.getName();

            //Generate TreeSet of valid commands for disabling this plugin
            TreeSet<String> disableCommands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            disableCommands.add("duwoff");
            disableCommands.add("durability_disable");
            disableCommands.add("durability-disable");

            //Generate TreeSet of valid commands for enabling this plugin
            TreeSet<String> enableCommands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            enableCommands.add("duwon");
            enableCommands.add("durabilitywarner_enable");
            enableCommands.add("durabilitywarner-enable");

            if (disableCommands.contains(cmd.getName())) {
                player.sendMessage(ChatColor.RED + String.format("[%s] is now disabled for you.", getDescription().getName()));
                if (!this.commandDisabledForPlayers.contains(playerName)) {
                    this.commandDisabledForPlayers.add(playerName);
                    this.commandEnabledForPlayers.remove(playerName);
                }
                return true;
            } else if (enableCommands.contains(cmd.getName())) {
                player.sendMessage(ChatColor.GREEN + String.format("[%s] is now enabled for you.", getDescription().getName()));
                if (!this.commandEnabledForPlayers.contains(playerName)) {
                    this.commandEnabledForPlayers.add(playerName);
                    this.commandDisabledForPlayers.remove(playerName);
                }

                return true;
            } else {
                //sender.sendMessage("Unknown command!");
            }
        } else {
            sender.sendMessage("Only players can issue the plugin commands.");
        }

        return false;
    }

}
