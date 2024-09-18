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
}
