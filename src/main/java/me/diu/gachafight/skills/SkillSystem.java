package me.diu.gachafight.skills;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.listeners.MasterMageListener;
import me.diu.gachafight.skills.listeners.SkillClickListener;
import me.diu.gachafight.skills.listeners.SkillJoinListener;
import me.diu.gachafight.skills.listeners.SkillUseListener;
import me.diu.gachafight.skills.rarity.SkillList;
import me.diu.gachafight.skills.rarity.common.SwordChargeSkill;
import me.diu.gachafight.skills.rarity.common.SwordSlashSkill;
import me.diu.gachafight.skills.utils.ItemUtils;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

// Skills Info: All Skills are inside package gachafight.skills.rarity.
// Cooldown is handled by storing cooldown to the slot (15,16,17)
// Damage is checked inside package gachafight.combat (DamageListener). All Skill Damage Source is CACTUS
// target.damage() inside skills is just the MULTIPLIER DAMAGE. not the actual damage.

public class SkillSystem {
    public SkillSystem(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(new SkillClickListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new SkillJoinListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new SkillUseListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new MasterMageListener(plugin), plugin);
        new SkillList(plugin);
    }

    public static void setupSkillSlots(Player player) {
        ItemStack item15 = player.getInventory().getItem(15);
        ItemStack item16 = player.getInventory().getItem(16);
        ItemStack item17 = player.getInventory().getItem(17);
        ItemStack itemFrame = new ItemStack(Material.ITEM_FRAME); // Create an Item Frame
        ItemMeta meta = itemFrame.getItemMeta();
        meta.setDisplayName(ColorChat.chat("&bSkill Slot"));  // Set a custom display name for clarity
        itemFrame.setItemMeta(meta);
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("");
        lore.add(ColorChat.chat("&6Drag & Drop w/ Skill"));
        if (item15 == null) {
            lore.set(0, ColorChat.chat("&6How To Cast: &aRight-Click"));
            meta.setLore(lore);
            itemFrame.setItemMeta(meta);
            player.getInventory().setItem(15, itemFrame);
        } else if (item15.getType() != Material.ITEM_FRAME && !ItemUtils.isNetheriteUpgradeTemplate(item15)) {
            player.sendMessage(ColorChat.chat("&cClear Top Row, last 3 column items inside your inventory!"));
        }
        if (item16 == null) {
            lore.set(0, ColorChat.chat("&6How To Cast: &aShift Right-Click"));
            meta.setLore(lore);
            itemFrame.setItemMeta(meta);
            player.getInventory().setItem(16, itemFrame);
        } else if (item16.getType() != Material.ITEM_FRAME && !ItemUtils.isNetheriteUpgradeTemplate(item16)) {
            player.sendMessage(ColorChat.chat("&cClear Top Row, last 3 column items inside your inventory!"));
        }
        if (item17 == null) {
            lore.set(0, ColorChat.chat("&6How To Cast: &aShift Left-Click"));
            meta.setLore(lore);
            itemFrame.setItemMeta(meta);
            player.getInventory().setItem(17, itemFrame);
        } else if (item17.getType() != Material.ITEM_FRAME && !ItemUtils.isNetheriteUpgradeTemplate(item17)) {
            player.sendMessage(ColorChat.chat("&cClear Top Row, last 3 column items inside your inventory!"));
        }
    }
}
