package me.diu.gachafight.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Prefix {
    public static String getDamagePrefix() {
        return "<!i><red>🗡 <gray>Damage: <red>";
    }
    public static String getArmorPrefix() {
        return "<!i><green>🛡 <gray>Armor: <green>";
    }
    public static String getHealthPrefix() {
        return "<!i><color:#FB035F>❤ <gray>HP: <color:#FB035F>";
    }
    public static String getSpeedPrefix() { return "<!i><white>⏩ <gray>Speed: <white>"; }
    public static String getLuckPrefix() { return "<!i><dark_green>☘ <gray>Luck: <dark_green>"; }
    public static String getCritChancePrefix() {return "<!i><color:#FFA500>🌠 <gray>Crit Chance: <color:#FFA500>:";}
    public static String getCritDmgPrefix() {return "<!i><color:#9B870C>💥 <gray>Crit Dmg: <color:#9B870C>";}
    public static String getDodgePrefix() {return "<!i><gray>👻 <gray>Dodge: <gray>";}
    public static String getPrefixForStat(String statType) {
        switch (statType.toLowerCase()) {
            case "damage":
                return getDamagePrefix();
            case "armor":
                return getArmorPrefix();
            case "hp":
            case "health":
                return getHealthPrefix();
            case "speed":
                return getSpeedPrefix();
            case "luck":
                return getLuckPrefix();
            case "crit chance":
                return getCritChancePrefix();
            case "crit damage":
                return getCritDmgPrefix();
            case "dodge":

            default:
                return "<!i><gray>Unknown Stat: <gray>"; // Default case for unknown stat
        }
    }
}