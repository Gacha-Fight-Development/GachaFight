package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.gui.RaritySelectionGUI;
import me.diu.gachafight.skills.utils.SkillFileUtils;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.RarityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillCommand implements CommandExecutor {

    private final GachaFight plugin;

    public SkillCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("skill").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorChat.chat("&cUsage: /skill <get/give/drop>"));
            return true;
        }

        String action = args[0].toLowerCase();
        String skillName;
        Player targetPlayer = null;

        switch (action) {
            case "get":
                if (args.length < 3) {
                    sender.sendMessage(ColorChat.chat("&cUsage: /skill get <rarity> <skill>"));
                    return true;
                }
                skillName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).toLowerCase();
                break;
            case "give":
                if (args.length < 3) {
                    sender.sendMessage(ColorChat.chat("&cUsage: /skill give <skill> <player>"));
                    return true;
                }
                skillName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1)).toLowerCase();
                targetPlayer = Bukkit.getPlayer(args[args.length - 1]);
                if (targetPlayer == null) {
                    sender.sendMessage(ColorChat.chat("&cPlayer not found: " + args[args.length - 1]));
                    return true;
                }
                break;
            case "drop":
                if (args.length < 5) {
                    sender.sendMessage(ColorChat.chat("&cUsage: /skill drop <skill> <x> <y> <z>"));
                    return true;
                }
                skillName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 3)).toLowerCase();
                break;
            default:
                sender.sendMessage(ColorChat.chat("&cInvalid action. Use 'get', 'give', or 'drop'."));
                return true;
        }

        // Load skill configuration
        FileConfiguration skillConfig = SkillFileUtils.loadSkillConfig(skillName);
        if (skillConfig == null) {
            sender.sendMessage(ColorChat.chat("&cSkill not found: " + skillName));
            return true;
        }

        ItemStack skillItem = createSkillItem(skillName, skillConfig);

        switch (action) {
            case "get":
                if (sender instanceof Player) {
                    ((Player) sender).getInventory().addItem(skillItem);
                    sender.sendMessage(ColorChat.chat("&aYou've received the " + skillName + " skill item!"));
                } else {
                    sender.sendMessage(ColorChat.chat("&cOnly players can use the 'get' action!"));
                }
                break;
            case "give":
                targetPlayer.getInventory().addItem(skillItem);
                targetPlayer.sendMessage(ColorChat.chat("&aYou've received the " + skillName + " skill item!"));
                sender.sendMessage(ColorChat.chat("&aGave " + skillName + " skill item to " + targetPlayer.getName()));
                break;
            case "drop":
                try {
                    double x = Double.parseDouble(args[args.length - 3]);
                    double y = Double.parseDouble(args[args.length - 2]);
                    double z = Double.parseDouble(args[args.length - 1]);
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        player.getWorld().dropItemNaturally(new org.bukkit.Location(player.getWorld(), x, y, z), skillItem);
                        sender.sendMessage(ColorChat.chat("&aDropped " + skillName + " skill item at x: " + x + ", y: " + y + ", z: " + z));
                    } else {
                        sender.sendMessage(ColorChat.chat("&cOnly players can use the 'drop' action!"));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorChat.chat("&cInvalid coordinates. Please use numbers for x, y, and z."));
                }
                break;
        }

        return true;
    }

    public static ItemStack createSkillItem(String skillName, FileConfiguration skillConfig) {
        ItemStack skillItem = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
        ItemMeta meta = skillItem.getItemMeta();
        skillName = skillName.toLowerCase();
        File configFile = SkillFileUtils.loadSkillFile(skillName);
        System.out.println(configFile);
        String rarityName = capitalizeFirstLetter(configFile.getName().replace(".yml", ""));
        String rarityColor = RarityUtils.getRarityColor(rarityName);
        if (meta != null) {
            meta.setDisplayName(ColorChat.chat(rarityColor + capitalizeEachWord(skillName) + " &b&l(Skill)"));

            List<String> lore = new ArrayList<>();
            System.out.println(skillConfig.getInt(skillName + ".cooldown"));
            lore.add(ColorChat.chat("&6Cooldown: &e" + skillConfig.getInt(skillName + ".cooldown", 0)));

            // Convert damage multiplier to percentage
            double damageMultiplier = skillConfig.getDouble(skillName + ".damage", 1.0);
            int damagePercentage = (int) (damageMultiplier * 100);
            lore.add(ColorChat.chat("&6Damage: &e" + damagePercentage + "%"));

            String description = skillConfig.getString(skillName + ".description", "");
            description = replacePlaceholders(description, skillConfig, skillName);
            addMultiLineLore(lore, "&6Info: &7", description, 40);
            lore.add("");
            lore.add(ColorChat.chat(rarityColor + "⮞ " + rarityName));

            meta.setLore(lore);

            // Hide additional tooltips
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setMaxStackSize(1);

            skillItem.setItemMeta(meta);
        }

        // Set max stack size to 1
        skillItem.setAmount(1);

        return skillItem;
    }

    public static void addMultiLineLore(List<String> lore, String prefix, String text, int maxLineLength) {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder(prefix);

        for (String word : words) {
            if (line.length() + word.length() > maxLineLength && line.length() > prefix.length()) {
                lore.add(ColorChat.chat(line.toString().trim()));
                line = new StringBuilder("&7"); // Use two spaces for continuation lines, with color code
            }
            line.append(word).append(" ");
        }

        if (line.length() > prefix.length()) {
            lore.add(ColorChat.chat(line.toString().trim()));
        }
    }

    public static String replacePlaceholders(String description, FileConfiguration config, String skillName) {
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(description);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = String.valueOf(config.get(skillName + "." + placeholder, "<" + placeholder + ">"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
    public static String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}
