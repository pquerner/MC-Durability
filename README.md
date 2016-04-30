## Durability Plugin for MineCraft
Version: v0.7

## What does it do?

This plugin will warn you with a message in chat if you get 20 (default) or less durability on any of these items:
- Craftitems (Axe, Pickaxe, Hoes, Swords)
- Armor (Leggings, Helmet, Chestplate, Boots)

## Installation
1. Build this plugin. (I've used maven)
2. Simply drop output .jar in /plugins folder of your bukkit/spigot installation
3. Start server

# Commands Overview:

Turn off/on:

    /durabilitywarner [1|0|on|off]
    OR: /duw [1|0|on|off]

Configuration:

    /durabilitywarnerconfig [<key>=<value>]
    OR: /duwc [<key>=<value>]

    The key must be defined in config.yml, otherwise it is ignored.
    The value must be not empty and may be a String or an Integer.

## TODO

Durability configuration (what items to have this plugin working with, ?)