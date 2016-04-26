package de.paskl.durability.utils;

import org.bukkit.Material;

public class Helper {

    /**
     * Determines whether or not the output message about durability should take place
     * Ie. we do not want durability messages when user clicks a door
     *
     * @param blockType - Block which is about to get left/right clicked with an item
     * @return boolean - true Output can be made | false Output can not be made
     */
    public static boolean isBlockForOutput(Material blockType) {
        boolean r = true;

        if (blockType.toString().contains("_DOOR")) return false;
        if (blockType.toString().contains("_BUTTON")) return false;
        if (blockType.toString().contains("_GATE")) return false;
        if (blockType.toString().contains("_CHEST")) return false;
        //Ignore all REDSTONE_ but keep the ORE (see switch case)
        if (blockType.toString().contains("REDSTONE_")) r = false;

        switch (blockType) {
            case WORKBENCH:
            case DISPENSER:
            case NOTE_BLOCK:
            case LEVER:
            case IRON_TRAPDOOR:
            case DROPPER:
            case ENCHANTMENT_TABLE:
            case FURNACE:
            case BURNING_FURNACE:
            case JUKEBOX:
            case ANVIL:
            case ITEM_FRAME:
            case BED_BLOCK:

            case HOPPER:
            case CHEST:

            case SIGN:
            case SIGN_POST:
            case WALL_SIGN:

            case CAKE_BLOCK:
            case BEACON:
                r = false;
                break;
            case REDSTONE_ORE:
                r = true;
                break;

        }
        return r;
    }
}
