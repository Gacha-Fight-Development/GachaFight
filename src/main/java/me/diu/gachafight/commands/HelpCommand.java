package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.commands.tabs.HelpTabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpCommand implements CommandExecutor {

    private final GachaFight plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public static Map<String, String> helpTopics;

    public HelpCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("help").setExecutor(this);
        plugin.getCommand("help").setTabCompleter(new HelpTabCompleter());
        this.helpTopics = initializeHelpTopics();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendAvailableCommands(player);
        } else if (args.length >= 1) {
            String helpTopic = args[0].toLowerCase();
            String section = null;
            boolean broadcast = false;

            if (args.length >= 2) {
                if (args[args.length - 1].equalsIgnoreCase("true")) {
                    broadcast = true;
                    if (args.length >= 3) {
                        section = args[1];
                        sendHelpTopic(player, helpTopic, section, broadcast);
                        return true;
                    }
                } else {
                    section = args[1];
                    sendHelpTopic(player, helpTopic, section, broadcast);
                    return true;
                }
            } else {
                sendHelpTopic(player, helpTopic, section, broadcast);
                return true;
            }
        } else {
            player.sendMessage(mm.deserialize("<red>Usage: /help [topic] [section] [true]"));
        }

        return true;
    }

    private void sendAvailableCommands(Player player) {
        player.sendMessage(mm.deserialize("<green>Available /help commands:"));
        for (String topic : helpTopics.keySet()) {
            player.sendMessage(mm.deserialize("<yellow>/help " + topic + " <gray>- Explains the <gold>" + topic + "</gold> feature."));
        }
        player.sendMessage(mm.deserialize("<yellow>/help <topic> <section> <gray>- Shows a specific section of a topic."));
        player.sendMessage(mm.deserialize("<yellow>/help <topic> [section] true <gray>- Broadcasts the help message to all players."));
    }

    private void sendHelpTopic(Player player, String topic, String section, boolean broadcast) {
        String helpMessage = helpTopics.get(topic);
        if (helpMessage != null) {
            Component titleComponent = mm.deserialize("<green>" + topic.substring(0, 1).toUpperCase() + topic.substring(1) + " Help:");
            Component messageComponent;

            if (section != null) {
                String sectionContent = extractSection(helpMessage, section);
                if (sectionContent != null) {
                    messageComponent = mm.deserialize(sectionContent);
                } else {
                    player.sendMessage(mm.deserialize("<red>Section not found. Showing full topic."));
                    messageComponent = mm.deserialize(helpMessage);
                }
            } else {
                messageComponent = mm.deserialize(helpMessage);
            }

            if (broadcast) {
                Bukkit.broadcast(titleComponent);
                Bukkit.broadcast(messageComponent);
                player.sendMessage(mm.deserialize("<green>Help message for '" + topic + "' has been broadcast to all players."));
            } else {
                player.sendMessage(titleComponent);
                player.sendMessage(messageComponent);
            }
        } else {
            player.sendMessage(mm.deserialize("<red>Unknown help topic. Use /help to see available topics."));
        }
    }

    private String extractSection(String helpMessage, String sectionNumber) {
        Pattern pattern = Pattern.compile("<aqua>" + sectionNumber + "\\. [^<]+</aqua>.*?(?=<aqua>\\d+\\.|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(helpMessage);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }
    public static List<String> getSections(String helpMessage) {
        List<String> sections = new ArrayList<>();
        Pattern pattern = Pattern.compile("<aqua>(\\d+)\\. [^<]+</aqua>");
        Matcher matcher = pattern.matcher(helpMessage);
        while (matcher.find()) {
            sections.add(matcher.group(1));
        }
        return sections;
    }

    private Map<String, String> initializeHelpTopics() {
        Map<String, String> topics = new HashMap<>();
        topics.put("dungeon", "<gray>The <gold>Dungeon System</gold>:\n" +
                "<aqua>1. Dungeon Selector:</aqua> Interact with the Dungeon Master to open the Dungeon Selector GUI.\n" +
                "<aqua>2. Available Dungeons:</aqua>\n" +
                "  • <yellow>Underground City:</yellow>\n" +
                "    - Level: 1-10\n" +
                "    - PvP Enabled\n" +
                "    - Damage Cap: 15, Armor Cap: 18\n" +
                "    - Multiple spawn points available\n" +
                "  • <yellow>Goblin Camp:</yellow>\n" +
                "    - Level: 10-20\n" +
                "    - PvP Enabled\n" +
                "    - No Damage or Armor Cap\n" +
                "    - Multiple spawn points available\n" +
                "<aqua>3. Dungeon Selection:</aqua> Click on a dungeon icon in the GUI to select it.\n" +
                "<aqua>4. Party System:</aqua>\n" +
                "    - Solo players can teleport instantly\n" +
                "    - Party leaders can teleport their entire party\n" +
                "    - Party members will be teleported by their leader\n" +
                "<aqua>5. Teleportation:</aqua> Players are teleported to the next available spawn point in the chosen dungeon.\n" +
                "<aqua>6. Confirmation:</aqua> Party leaders must confirm the teleportation for their entire party.\n" +
                "<aqua>7. Rewards:</aqua> Completing dungeons grants rewards and increases your reputation.\n" +
                "<aqua>8. Caution:</aqua> Be prepared for challenging enemies and potential PvP encounters.\n\n" +
                "<yellow>Tip:</yellow> Form a party to tackle harder dungeons and share the rewards!");
        topics.put("sell", "<gray>The <gold>Sell System</gold>:\n" +
                "<aqua>1. Accessing the Sell Shop:</aqua>\n" +
                "   - Right-click the 'sell shop' NPC to open the Sell GUI\n" +
                "<aqua>2. Sell GUI Layout:</aqua>\n" +
                "   - 54-slot inventory with a sell button in the middle of the last row\n" +
                "<aqua>3. Adding Items to Sell:</aqua>\n" +
                "   - Place items you want to sell in the first 45 slots of the GUI\n" +
                "<aqua>4. Item Rarity:</aqua>\n" +
                "   - Items are categorized as Common, Uncommon, Rare, Unique, Legendary, or Mythic\n" +
                "<aqua>5. Sell Button:</aqua>\n" +
                "   - Shows the count of items by rarity\n" +
                "   - Click to sell all items in the GUI\n" +
                "<aqua>6. Selling Process:</aqua>\n" +
                "   - The system calculates the total value of all items\n" +
                "   - Money is added to your balance\n" +
                "   - Sold items are removed from the GUI\n" +
                "<aqua>7. Safety Feature:</aqua>\n" +
                "   - If you try to close the GUI with items still in it, it will reopen\n" +
                "   - This prevents accidental loss of items\n" +
                "<aqua>8. Sell Price Calculation:</aqua>\n" +
                "   - Prices are calculated based on item rarity and other factors\n\n" +
                "<yellow>Tip:</yellow> Use the sell shop to convert unwanted items into money quickly and easily!");
        topics.put("gacha", "<gray>The <gold>Gacha System</gold> allows you to obtain various items and equipment:\n" +
                "<aqua>1. How to Use:</aqua> Use Gacha keys to open Gacha chests and receive rewards.\n" +
                "<aqua>2. Rarity System:</aqua> Rewards are categorized into different rarity levels.\n" +
                "<aqua>3. Types of Rewards:</aqua>\n" +
                "   - Equipment: Weapons and armor with customized stats\n" +
                "   - Potions: Various magical brews with different effects\n" +
                "   - Crystals: Permanent stat upgrades (may be removed in the future)\n" +
                "<aqua>4. Equipment Customization:</aqua>\n" +
                "   - Stats are tailored to your player level\n" +
                "   - Each item has a quality percentage based on its stats\n" +
                "   - Stats include Damage, Armor, and HP\n" +
                "<aqua>5. Inventory Management:</aqua>\n" +
                "   - Ensure you have empty inventory slots before opening Gacha\n" +
                "   - VIP players can use auto-sell feature for unwanted items\n" +
                "<aqua>6. Special Features:</aqua>\n" +
                "   - Persistent Data Container (PDC) stores item's original stat ranges\n" +
                "   - Auto-sell cutoffs can be set for each rarity level\n" +
                "<aqua>7. Quests and Guides:</aqua>\n" +
                "   - Opening Gacha chests may progress certain quests\n" +
                "   - Tutorial guides are available for new players\n" +
                "Use the Gacha system to enhance your equipment and boost your character's power. Remember, higher rarity items are generally more powerful!");
        topics.put("mobs", "<gray>Mobs and Rewards System:\n" +
                "<aqua>1. Mob Variety:</aqua> Fight different types of mobs throughout the game world and in dungeons.\n" +
                "<aqua>2. Experience Gains:</aqua> Defeating mobs grants <aqua>experience (exp)</aqua> based on their max health.\n" +
                "<aqua>3. Money Drops:</aqua> Mobs also drop <yellow>money</yellow>, with the amount proportional to their max HP.\n" +
                "<aqua>4. Special Drops:</aqua> Some mobs may drop special items or skill books as rare rewards.\n" +
                "<aqua>5. Boss Rewards:</aqua> Defeating bosses offers greater rewards, often shared among party members.\n" +
                "<aqua>6. Party Sharing:</aqua> When in a party, boss rewards are shared if members are in the same dungeon.\n" +
                "<aqua>7. Dungeon Specifics:</aqua> Dungeons may have unique mobs and reward structures.\n" +
                "<aqua>8. Scaling Difficulty:</aqua> Generally, tougher mobs with higher HP offer better rewards.\n" +
                "<aqua>9. Strategic Farming:</aqua> Choose your battles wisely to maximize exp and money gains.\n\n" +
                "<yellow>Tip:</yellow> Balance your time between regular mobs and dungeon bosses for optimal progression!");
        topics.put("level", "<gray>The <gold>Leveling System</gold>:\n" +
                "<aqua>1. Experience (EXP):</aqua>\n" +
                "   • Gain EXP by defeating mobs, completing quests, and finishing dungeons\n" +
                "   • EXP required for each level increases as you progress\n" +
                "<aqua>2. Leveling Up:</aqua>\n" +
                "   • Your level increases automatically when you gain enough EXP\n" +
                "   • A level-up notification will appear when you reach a new level\n" +
                "<aqua>3. Level Benefits:</aqua>\n" +
                "   • Each level-up improves your base stats:\n" +
                "     - Increased max HP\n" +
                "     - Higher base damage\n" +
                "     - Improved armor\n" +
                "   • Some levels may unlock new abilities or features\n" +
                "<aqua>4. Level Caps:</aqua>\n" +
                "   • Different areas and dungeons may have level requirements or caps\n" +
                "   • Higher-level areas offer greater challenges and rewards\n" +
                "<aqua>5. Gear Scaling:</aqua>\n" +
                "   • Some gear and weapons may scale with your level\n" +
                "   • Higher levels allow you to use more powerful equipment\n" +
                "<aqua>6. Viewing Your Level:</aqua>\n" +
                "   • Use <yellow>/stats</yellow> to see your current level and EXP\n" +
                "   • The EXP bar at the bottom of your screen shows progress to next level\n\n" +
                "<yellow>Tip:</yellow> Balance your time between combat, quests, and dungeons for optimal leveling!");
        topics.put("party", "<gray>The <gold>Party System</gold>:\n" +
                "<aqua>1. Basic Commands:</aqua>\n" +
                "   • <yellow>/party create</yellow> - Start a new party\n" +
                "   • <yellow>/party invite <player></yellow> - Invite a player to your party\n" +
                "   • <yellow>/party accept</yellow> - Join a party you've been invited to\n" +
                "   • <yellow>/party leave</yellow> - Leave your current party\n" +
                "   • <yellow>/party info</yellow> - View information about your current party\n" +
                "<aqua>2. Party Size:</aqua> Parties can have up to 4 players\n" +
                "<aqua>3. Leadership:</aqua>\n" +
                "   • The party creator is the leader by default\n" +
                "   • Leaders can invite new members and manage the party\n" +
                "<aqua>4. Dungeon Benefits:</aqua>\n" +
                "   • Boss rewards are shared among party members in the same dungeon\n" +
                "   • Party leaders can teleport the entire party to dungeons\n" +
                "<aqua>5. Communication:</aqua>\n" +
                "   • Use <yellow>/p <message></yellow> for party chat\n" +
                "<aqua>6. Limitations:</aqua>\n" +
                "   • You can only be in one party at a time\n" +
                "   • Invites expire after a short period\n\n" +
                "<yellow>Tip:</yellow> Parties are great for tackling challenging dungeons and sharing rewards!");
        topics.put("death", "<gray>Death mechanics in the game:\n" +
                "<aqua>1. HP Reset:</aqua> Upon <red>death</red>, your HP is reset to maximum.\n" +
                "<aqua>2. Teleportation:</aqua> You're automatically teleported to the spawn point.\n" +
                "<aqua>3. Item Drop:</aqua> Players above level 2 will drop items marked as 'Drop On Death'.\n" +
                "<aqua>4. Level Protection:</aqua> Players at level 2 or below don't drop items on death.\n" +
                "<aqua>5. Inventory Management:</aqua> Items marked for dropping are removed from your inventory.\n" +
                "<aqua>6. Notification:</aqua> You'll receive a message listing all items lost upon death.\n" +
                "<aqua>7. Damage Indicator:</aqua> Any active damage indicators are cleared upon death.\n" +
                "<aqua>8. Dungeon Warning:</aqua> Be extra cautious in dungeons as death can be more punishing!\n" +
                "<aqua>9. Event Cancellation:</aqua> The default death event is cancelled to allow for custom handling.\n\n" +
                "<yellow>Remember:</yellow> Always be prepared and carry only necessary items when venturing into dangerous areas!");
        topics.put("guide", "<gray>The <gold>Guide System</gold>:\n" +
                "<aqua>Usage:</aqua> <yellow>/guide <location></yellow>\n" +
                "<aqua>Description:</aqua> Helps you navigate to important locations in the game.\n" +
                "<aqua>Available Locations:</aqua>\n" +
                "  • <yellow>dungeon</yellow> - Find the nearest dungeon entrance\n" +
                "  • <yellow>healer</yellow> - Locate the nearest healing NPC\n" +
                "  • <yellow>potion</yellow> - Find the potion shop\n" +
                "  • <yellow>buyshop</yellow> - Locate the general store\n" +
                "  • <yellow>overseer</yellow> - Find the dungeon overseer\n" +
                "  • <yellow>quest</yellow> - Locate the quest giver\n" +
                "  • <yellow>equipment</yellow> - Find the equipment manager\n" +
                "<aqua>Note:</aqua> The guide only works in safe zones.\n\n" +
                "<yellow>Tip:</yellow> Use the guide to quickly find important NPCs and locations!");
        topics.put("quests", "<gray>The <gold>Quest</gold> system offers various tasks for you to complete. There are three types of quests:\n" +
                "<aqua>1. Kill Mobs:</aqua> Defeat specific monsters.\n" +
                "<aqua>2. Online Time:</aqua> Stay online for a certain duration.\n" +
                "<aqua>3. Key Open:</aqua> Use specific keys to open chests.\n" +
                "Complete quests to earn rewards! Some quests can be repeated, while others can only be done once. " +
                "Check your active quests and progress at the Quest NPC.");
        topics.put("dailyquests", "<gray>The <gold>Daily Quest</gold> system offers a new challenge every day:\n" +
                "<aqua>1. New Quest Daily:</aqua> You'll receive a random quest each day.\n" +
                "<aqua>2. One-Time Completion:</aqua> You can only complete one daily quest per day.\n" +
                "<aqua>3. Reset at Midnight:</aqua> Daily quests reset at midnight, giving you a fresh start.\n" +
                "<aqua>4. Varied Objectives:</aqua> Daily quests can involve different tasks like killing mobs, staying online, or using keys.\n" +
                "Complete your daily quest for special rewards! Remember, you can't carry over daily quests to the next day, so try to complete them before they reset.");
        topics.put("sidequests", "<gray>The <gold>Side Quest</gold> system offers additional challenges:\n" +
                "<aqua>1. Multiple Quests:</aqua> You can have up to 5 side quests at a time.\n" +
                "<aqua>2. Various Objectives:</aqua> Side quests can involve different tasks like killing specific mobs, staying online, or using keys.\n" +
                "<aqua>3. No Time Limit:</aqua> Unlike daily quests, side quests don't expire at the end of the day.\n" +
                "<aqua>4. Repeatable:</aqua> Some side quests can be repeated, while others are one-time only.\n" +
                "<aqua>5. Rewards:</aqua> Complete side quests for additional rewards and experience.\n" +
                "Check your active side quests at the Quest NPC. You can work on multiple side quests simultaneously, so try to progress in all of them!");
        topics.put("buyshop", "<gray>The <gold>Buy Shop</gold> is where you can purchase various items:\n" +
                "<aqua>1. Two Currencies:</aqua> Items can be bought with either Gold or Gems.\n" +
                "<aqua>2. Gold Shop:</aqua> Contains items purchasable with Gold, the common currency.\n" +
                "<aqua>3. Gem Shop:</aqua> Offers premium items that can be bought with Gems, a rarer currency.\n" +
                "<aqua>4. Diverse Items:</aqua> The shops offer a variety of items, including equipment, consumables, and special items.\n" +
                "<aqua>5. Regular Updates:</aqua> The shop inventory may be updated periodically, so check back often for new items.\n" +
                "Visit the Buy Shop NPC to browse and purchase items. Make sure you have enough currency before attempting to buy!");
        topics.put("equipment", "<gray>The <gold>Equipment Specialist</gold> offers various services to improve your gear:\n" +
                "<aqua>1. Level Up Equipment:</aqua> Increase your equipment's level to improve its stats.\n" +
                "<aqua>2. Reroll Stats:</aqua> Randomize your equipment's stats for a chance at better attributes.\n" +
                "<aqua>3. Coming Soon:</aqua> A mysterious new feature is in development!\n" +
                "<aqua>4. How to Access:</aqua> Find the NPC named 'Equipment' and right-click to interact.\n" +
                "<aqua>5. Main Menu:</aqua> The Equipment Specialist's menu offers three options:\n" +
                "   - <yellow>Anvil:</yellow> Level Up Equipment\n" +
                "   - <yellow>Grindstone:</yellow> Reroll Stats\n" +
                "   - <yellow>Barrier:</yellow> Coming Soon feature\n" +
                "Visit the Equipment Specialist regularly to keep your gear in top condition. Remember, better equipment means better performance in battles!");
        topics.put("overseer", "<gray>The <gold>Overseer Shop</gold> allows you to upgrade your character's stats:\n" +
                "<aqua>1. Stat Upgrades:</aqua> Improve various attributes of your character:\n" +
                "   - <red>HP (Red Dye):</red> Increase your maximum health\n" +
                "   - <gold>Damage (Orange Dye):</gold> Boost your attack power\n" +
                "   - <green>Armor (Lime Dye):</green> Enhance your defense\n" +
                "   - <yellow>Crit Rate (Yellow Dye):</yellow> Improve chance of critical hits\n" +
                "   - <yellow>Crit Damage (Glowstone Dust):</yellow> Increase critical hit damage\n" +
                "   - <white>Speed (Sugar):</white> Boost your movement speed\n" +
                "   - <gray>Dodge Chance (Light Gray Dye):</gray> Increase chance to avoid attacks\n" +
                "<aqua>2. Costs:</aqua> Each upgrade has a cost in coins. The cost increases as your stats improve.\n" +
                "<aqua>3. How to Use:</aqua> Click on the item representing the stat you want to upgrade.\n" +
                "<aqua>4. Guide:</aqua> A compass item provides additional information about the Overseer Shop.\n" +
                "<aqua>5. No Refunds:</aqua> Be careful with your upgrades, as there are no charge backs!\n" +
                "Visit the Overseer regularly to keep your character strong. Remember, balanced stat growth is key to success!");
        topics.put("potionshop", "<gray>The <gold>Potion Shop</gold> offers a variety of magical brews:\n" +
                "<aqua>1. Rarity Levels:</aqua> Potions are categorized into 7 types:\n" +
                "   - Small Potion\n" +
                "   - Medium Potion\n" +
                "   - Large Potion\n" +
                "   - Extra Large Potion\n" +
                "   - Premium Potion\n" +
                "   - Golden Potion\n" +
                "   - Eagle Potion\n" +
                "<aqua>2. Shop Interface:</aqua> Each rarity has its own shop with up to 27 different potions.\n" +
                "<aqua>3. Purchasing:</aqua> Potions can be bought with either money ($) or gems.\n" +
                "<aqua>4. Price Information:</aqua> The cost is displayed in the item's lore (5th line).\n" +
                "<aqua>5. Inventory Management:</aqua> Purchased potions are added directly to your inventory.\n" +
                "<aqua>6. Currency Check:</aqua> The system ensures you have enough money or gems before purchase.\n" +
                "<aqua>7. Confirmation:</aqua> You'll receive a message confirming your purchase and its cost.\n" +
                "Visit the Potion Shop to enhance your abilities in battle. Remember, higher rarity potions are more powerful but may be more expensive!");
        topics.put("mastermage", "<gray>The <light_purple>Master Mage</light_purple> offers mystical services to reveal rare skill book drops:\n" +
                "<aqua>1. Level Requirement:</aqua> You must be at least level 10 to use the Master Mage's services.\n" +
                "<aqua>2. Magical Orb (Private):</aqua>\n" +
                "   - Cost: $10,000\n" +
                "   - Reveals which mob will drop a Rare or Higher Skill Book\n" +
                "   - Information is private to you\n" +
                "   - 30-minute window to obtain the skill book\n" +
                "<aqua>3. Foresee (Broadcast):</aqua>\n" +
                "   - Cost: $5,000\n" +
                "   - Broadcasts to all players which mob will drop a Rare or Higher Skill Book\n" +
                "   - 30-minute window for anyone to obtain the skill book\n" +
                "<aqua>4. Time Limit:</aqua> Both services end when a player obtains the Skill Book or after 30 minutes.\n" +
                "<aqua>5. One Active Reveal:</aqua> Only one reveal can be active at a time.\n" +
                "Visit the Master Mage to increase your chances of obtaining rare skill books. Choose wisely between private information or a server-wide hunt!");
        topics.put("skills", "<gray>The <gold>Skill System</gold> allows you to use powerful abilities in combat:\n" +
                "<aqua>1. Available Skills:</aqua> There are 8 skills you can acquire and use:\n" +
                "   Sword Charge, Sword Slash, Sword Spin, Sword Burst, Thunder Strike, Fire Strike, Dush, Life Steal\n" +
                "<aqua>2. Skill Rarities:</aqua> Skills are categorized into different rarities:\n" +
                "   - <white>Common:</white> Sword Charge, Sword Slash\n" +
                "   - <green>Uncommon:</green> Sword Spin, Sword Burst\n" +
                "   - <blue>Rare:</blue> Thunder Strike, Fire Strike, Dush\n" +
                "   - <purple>Epic:</purple> Life Steal\n" +
                "   - <gold>Unique, Legendary, Mythic:</gold> Coming soon!\n" +
                "<aqua>3. Skill Configuration:</aqua> Each rarity has its own configuration file for easy management.\n" +
                "<aqua>4. Using Skills:</aqua> Once acquired, you can use skills in combat for various effects.\n" +
                "<aqua>5. Skill Books:</aqua> You can obtain new skills through skill books dropped by mobs.\n" +
                "<aqua>6. Skill Management:</aqua> You can view and manage your skills through the skill interface.\n" +
                "Experiment with different skills to find the best combination for your playstyle. Remember, rarer skills are generally more powerful!");
        topics.put("stats", "<gray>Player Statistics System:\n" +
                "<aqua>1. Level and Experience:</aqua>\n" +
                "   - Your level determines your overall power\n" +
                "   - Gain exp to level up and become stronger\n" +
                "<aqua>2. Health (HP):</aqua>\n" +
                "   - Max HP determines how much damage you can take\n" +
                "   - HP can be increased with gear\n" +
                "<aqua>3. Combat Stats:</aqua>\n" +
                "   - <yellow>Strength:</yellow> Base damage you deal\n" +
                "   - <yellow>Armor:</yellow> Reduces damage taken\n" +
                "   - <yellow>Crit Chance:</yellow> Chance to deal critical hits\n" +
                "   - <yellow>Crit Damage:</yellow> Extra damage on critical hits\n" +
                "   - <yellow>Dodge:</yellow> Chance to avoid incoming attacks\n" +
                "<aqua>4. Utility Stats:</aqua>\n" +
                "   - <yellow>Speed:</yellow> Affects movement speed\n" +
                "   - <yellow>Luck:</yellow> Improves chances for better loot\n" +
                "<aqua>5. Equipment Bonuses:</aqua>\n" +
                "   - Gear provides additional stats (shown in parentheses)\n" +
                "   - Offhand items can provide both damage and armor\n" +
                "<aqua>6. Currency:</aqua>\n" +
                "   - <yellow>Money:</yellow> Main in-game currency\n" +
                "   - <yellow>Gems:</yellow> Special currency for premium items\n" +
                "<aqua>7. Viewing Stats:</aqua>\n" +
                "   - Use the stats command to view your current statistics\n" +
                "   - You can also view other players' stats\n\n" +
                "<yellow>Tip:</yellow> Balance your stats and gear to create a powerful character build!");

        return topics;
    }
}
