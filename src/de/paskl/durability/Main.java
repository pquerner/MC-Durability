package de.paskl.durability;

import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {


    @Override
    public void onEnable() {
        new PlayerListener(this);

        getLogger().info("[Durability] Loading..");
        getLogger().info("[Durability] Loaded up plugin... Version 0.1.");
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " is now disabled.");
    }


    /**
     * En/Disabled the modules functionality
     *
     * @param sender - Where the command is send from
     * @param cmd - Command string
     * @param label - ?
     * @param args - ?
     * @return bool
     */
    /*public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {

            Player player = (Player) sender;

            TreeSet<String> disableCommands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

            disableCommands.add("du-d");
            disableCommands.add("durability_disable");
            disableCommands.add("durability-disable");

            TreeSet<String> enableCommands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

            enableCommands.add("du-e");
            enableCommands.add("durability_enable");
            enableCommands.add("durability-enable");

            if (disableCommands.contains(cmd.getName())) {
                player.sendMessage("[Durability] is now disabled for you.");
                return true;
            } else if(enableCommands.contains(cmd.getName())) {
                player.sendMessage("[Durability] is now enabled for you.");

                return true;
            } else {
                sender.sendMessage("Unknown command!");
            }
        }

        return false;
    }
    */
}
