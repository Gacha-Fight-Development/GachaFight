package me.diu.gachafight.combat;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntity;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.afk.AFKManager;
import me.diu.gachafight.combat.mobdrops.BulbDeathReward;
import me.diu.gachafight.combat.mobdrops.GoblinDeathReward;
import me.diu.gachafight.combat.mobdrops.RPGDeathReward;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.quest.listeners.QuestKillListener;
import me.diu.gachafight.skills.managers.MobDropSelector;
import me.diu.gachafight.skills.managers.SkillManager;
import me.diu.gachafight.skills.utils.RandomSkillUtils;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.DungeonUtils;
import me.diu.gachafight.utils.GiveItemUtils;
import me.diu.gachafight.utils.TextDisplayUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DamageListener implements Listener {

    private final int weaponDelay = 0;
    public static List<String> BOSS_NAMES = List.of("rpg_sand_golem", "rpg_stone_golem", "Goblin King", "Shadow Sorcerer");

    public DamageListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (DungeonUtils.isSafezone(event.getEntity().getLocation())) {
            if (event.getEntity().getType() == EntityType.PLAYER || event.getEntity().getType() == EntityType.PIG) {
                event.setCancelled(true);
                event.getDamager().sendMessage(ColorChat.chat("&aSafezone. &cPvP only in Dungeon."));
                return;
            }
        }
        // when arrow hits player
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            PlayerStats stats = PlayerStats.getPlayerStats(((Player) event.getEntity()));
            event.setDamage(0);
            stats.syncHealthWithHearts((Player) event.getEntity());
        }

        // Handle PvE - Player attacks a mob
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity && event.getEntity().getType() != EntityType.PLAYER && event.getEntity().getType() != EntityType.ARMOR_STAND) {
            handlePlayerVsEntity(event, (Player) event.getDamager(), (LivingEntity) event.getEntity());
        }

        // Handle PvP - Player attacks another player
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            handlePlayerVsPlayer(event, (Player) event.getDamager(), (Player) event.getEntity());
        }

    }

    // Handle Player vs Entity (PvE)
    private void handlePlayerVsEntity(EntityDamageByEntityEvent event, Player player, LivingEntity entity) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        //AFK Dummy
        if (entity.getName().equals("AFK Dummy")) {
            event.setCancelled(true);
            return;
        }

        // Retrieve player's damage stat
        if (player.getCooldown(player.getInventory().getItemInMainHand().getType()) != 0) {
            if (!event.getDamageSource().getDamageType().equals(DamageType.CACTUS)) {
                return;
            }
        }
        player.setCooldown(player.getInventory().getItemInMainHand().getType(), weaponDelay);
        double playerDamage = stats.getDamage() + stats.getWeaponStats().getDamage() + stats.getGearStats().getTotalDamage();
        if (DungeonUtils.isRPG(entity.getLocation())) {
            if (playerDamage > 15) {
                playerDamage = 15;
            }
        }
        // Checks for SKILL Damage
        if (event.getDamageSource().getDamageType().equals(DamageType.CACTUS)) {
            playerDamage *= event.getDamage();
        } else {
            playerDamage *= SkillManager.applyActiveSkills(player, entity);
        }
        double mobArmor = getMobArmor(entity);
        // ==============Calc PvE================
        double totalDamage = playerDamage - mobArmor; // armor
        if (totalDamage < 0.5) {
            totalDamage = 0.5;
        }
        double random = Math.random();
        if (random < stats.getCritChance()) {
            totalDamage *= stats.getCritDmg();
        }
        if (isCritical(player)) {
            totalDamage = totalDamage*1.2;
        }
        boolean isCrit = random < stats.getCritChance();
        TextDisplayUtils.summonDamageTextDisplay(entity, totalDamage, isCrit);
        // Check if the mob will die from this hit
        if (entity.getHealth() - totalDamage <= 0) {
            handleMobDeath(player, entity);  // Handle mob death, rewards, etc.
            QuestKillListener.questKillMob(player, entity); //handle quest
        }
        if (player.hasPermission("gachafight.toggledamage")) {
            player.sendMessage(ColorChat.chat("&cFinal Damage: &f" + String.format("%.1f",totalDamage)));
        }
        // Cancel the default damage and apply custom damage
        event.setDamage(0);
        double newHealth = entity.getHealth() - totalDamage;
        if (newHealth <= 0) {
            entity.setHealth(0);
        } else {
            entity.setHealth(newHealth);
        }
    }

    // Handle Player vs Player (PvP)
    private void handlePlayerVsPlayer(EntityDamageByEntityEvent event, Player attacker, Player target) {
        // Get attacker and target stats
        PlayerStats attackerStats = PlayerStats.getPlayerStats(attacker);
        PlayerStats targetStats = PlayerStats.getPlayerStats(target);
        if (attacker.getCooldown(attacker.getInventory().getItemInMainHand().getType()) != 0) {
            return;
        }
        attacker.setCooldown(attacker.getInventory().getItemInMainHand().getType(), weaponDelay);
        // Calculate the attacker's total damage
        double attackerDamage = attackerStats.getDamage() + attackerStats.getWeaponStats().getDamage() + attackerStats.getGearStats().getTotalDamage();
        if (DungeonUtils.isRPG(target.getLocation())) {
            if (attackerDamage > 15) {
                attackerDamage = 15;
            }
        }
        // Calculate the target's total armor
        double targetArmor = targetStats.getArmor() + targetStats.getGearStats().getTotalArmor();
        if (DungeonUtils.isRPG(target.getLocation())) {
            if (targetArmor > 18) {
                targetArmor = 18;
            }
        }

        // Calculate the custom damage for PvP
        double totalDamage = attackerDamage - (targetArmor/4); // Adjust the armor effect as needed

        //Check for if player dodged
        if (Math.random() > targetStats.getDodge()) { //below triggers if not dodged
            //reduce damage for target if attacker level is above target's level
            if (attackerStats.getLevel() > targetStats.getLevel()) {
                double levelDiff = attackerStats.getLevel() - targetStats.getLevel();
                //sets a minimum of 10% damage
                if (levelDiff >= 10) {
                    levelDiff = 0.5;
                }
                totalDamage = totalDamage*(1-(levelDiff*0.07));
            }
            //sets minimum damage of 0.5
            if (totalDamage < 0.5) {
                totalDamage = 0.5;
            }
            double random = Math.random();
            if (random < attackerStats.getCritChance()) {
                totalDamage *= attackerStats.getCritDmg();
            }
            if (isCritical(attacker)) {
                totalDamage = totalDamage*1.2;
            }
            boolean isCrit = random < attackerStats.getCritChance();
            TextDisplayUtils.summonDamageTextDisplay(target, totalDamage, isCrit);
        } else { //below triggers when attack dodged
            totalDamage = 0;
            target.sendMessage(ColorChat.chat("&aDodged!"));
        }
        // Apply custom damage to the target
        event.setDamage(0); // Cancel the default damage
        if (attacker.hasPermission("gachafight.toggledamage")) {
            attacker.sendMessage(ColorChat.chat("&cFinal Damage: &f" + totalDamage));
        }
        targetStats.setHp(targetStats.getHp() - totalDamage);
        targetStats.syncHealthWithHearts(target);

        // Check if the target will die from this hit
        if (targetStats.getHp()+targetStats.getGearStats().getTotalMaxHp() <= 0) {
            target.setHealth(0); // Trigger death event
            target.sendMessage(ColorChat.chat("&4You have Died to " + attacker.getName()));
        }
    }

    //Handles EvP Interactions | When Entity Hits Player. (Only Applies to MythicMobs)
    @EventHandler
    public void onMobDamage(MythicDamageEvent event) { //damage by mob
        if (event.getCaster() instanceof ActiveMob && event.getTarget() instanceof BukkitEntity) {
            if (event.getTarget().getBukkitEntity() instanceof Player) {
                Player player = ((BukkitEntity) event.getTarget()).getEntityAsPlayer();
                // Check if the damager is a MythicMob
                ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(event.getCaster().getEntity());
                if (mythicMob != null) {
                    // Get the MythicMob's damage
                    double mobDamage = event.getDamage();

                    // Get the player's armor stat
                    PlayerStats stats = PlayerStats.getPlayerStats(player);
                    double playerArmor = stats.getArmor() + stats.getGearStats().getTotalArmor();
                    if (DungeonUtils.isRPG(player.getLocation())) {
                        if (playerArmor > 18) {
                            playerArmor = 18;
                        }
                    }

                    // Calculate the total damage received by the player
                    double totalDamage = mobDamage - (playerArmor * 0.4); // Calc EvP

                    //Check for if player dodged
                    if (Math.random() > stats.getDodge()) { //below triggers if not dodged
                        //sets minimum damage of 0.5
                        if (totalDamage < 0.5) {
                            totalDamage = 0.5;
                        }
                        double random = Math.random();
                        if (random < 0.15) {
                            totalDamage *= 1.5;
                        }
                    } else { //below triggers when attack dodged
                        totalDamage = 0;
                        player.sendMessage(ColorChat.chat("&aDodged!"));
                    }


                    // Cancel the default damage and apply custom damage
                    event.setDamage(0);
                    stats.setHp(stats.getHp() - totalDamage);
                    stats.syncHealthWithHearts(player);
                    if (stats.getHp()+stats.getGearStats().getTotalMaxHp() <= 0) {
                        player.setHealth(0); // This will trigger the death event
                        stats.syncHealthWithHearts(player);
                        stats.updateActionbar(player);
                        player.sendMessage(ColorChat.chat("&4You have Died to " + mythicMob.getName()));
                    } else {
                        // Sync the player's hearts with the current HP
                        stats.syncHealthWithHearts(player);
                        stats.updateActionbar(player);
                    }
                }
            }
        } else {
            event.setDamage(0);
        }
    }

    // New method to handle mob death and give rewards
    private void handleMobDeath(Player player, Entity entity) {
        if (entity instanceof LivingEntity) {

            double petGoldMulti = 1.0;
            double petExpMulti = 1.0;
            // Uncomment these to activate if pets effect gold/exp rewards
            // petGoldMulti = PetEffects.checkGoldMulti(player);
            // petExpMulti = PetEffects.checkExpMulti(player);

            double mobHp = ((LivingEntity) entity).getMaxHealth();
            double expGained = mobHp / 7.5 * petExpMulti;
            double moneyGained = mobHp / 20 * petGoldMulti;
            double multi = calculateMultiplier(player);

            OfflinePlayer offlinePlayer = player;
            OfflinePlayer partyLeader = PartyManager.getPartyLeader(offlinePlayer);
            Set<OfflinePlayer> partyMembers = new HashSet<>();

            if (partyLeader != null) {
                partyMembers = PartyManager.getPartyMembers(partyLeader);
            }

            PlayerStats playerStats = PlayerStats.getPlayerStats(player);

            if (!partyMembers.isEmpty()) {
                handlePartyBossRewards(offlinePlayer, entity, expGained, moneyGained, multi, partyMembers);
            }

            playerStats.addExp(expGained * multi, player);
            VaultHook.addMoney(player, (moneyGained * multi));

            handleMobSpecificRewards(entity, player);
            handleRandomRewards(player, entity);

            // Notify the player of the exp and money gained
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>+ <dark_aqua>Exp: <aqua>" + String.format("%.2f", expGained) + "<black> | <gold> Money: <yellow>" + String.format("%.2f", moneyGained) + "</green>"));
        }
    }

    private void handlePartyBossRewards(OfflinePlayer killer, Entity entity, double expGained, double moneyGained, double multi, Set<OfflinePlayer> partyMembers) {
        if (isBoss(entity)) {
            for (OfflinePlayer member : partyMembers) {
                if (member.isOnline()) {
                    Player onlineMember = member.getPlayer();
                    if (onlineMember != null && isInSameDungeon(killer, onlineMember)) {
                        PlayerStats memberStats = PlayerStats.getPlayerStats(onlineMember);
                        memberStats.addExp(expGained * multi, onlineMember);
                        VaultHook.addMoney(onlineMember, (moneyGained * multi));

                        if (!member.equals(killer)) {
                            onlineMember.sendMessage(ColorChat.chat("&aReceived Gold & XP from " + killer.getName() + " killing " + entity.getName()));
                        }
                    } else if (onlineMember != null) {
                        onlineMember.sendMessage(ColorChat.chat("&cParty Member killed boss but you're not in the same Dungeon."));
                    }
                }
                // You might want to handle offline players here, perhaps by storing their rewards for later
            }
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        stats.setHp(stats.getMaxhp());
        //clear Damage Indicator
        TextDisplay display = TextDisplayUtils.activeDisplays.get(player.getUniqueId());
        if (display != null) {
            display.remove();
        }
        // Create a list to store items that should be removed from the inventory
        List<ItemStack> itemsToRemove = new ArrayList<>();
        List<String> itemNamesToRemove = new ArrayList<>();
        if (stats.getLevel() > 2) {
            for (ItemStack item : player.getInventory()) {
                if (item != null && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasLore()) {
                        for (String lore : meta.getLore()) {
                            if (lore.contains("Drop On Death")) {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                                itemsToRemove.add(item);
                                itemNamesToRemove.add("&e"+item.getAmount() + "x " + item.getItemMeta().getDisplayName() + "&r");
                                break;
                            }
                        }
                    }
                }
            }
            player.sendMessage(ColorChat.chat("&cItems you lost when killed... &r" + itemNamesToRemove));
        } else {
            player.sendMessage(ColorChat.chat("&cItems earned from Dungeon are dropped on death for player's Level above 2!"));
        }

        // Remove the marked items from the player's inventory
        for (ItemStack itemToRemove : itemsToRemove) {
            player.getInventory().remove(itemToRemove);
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "spawn " + player.getName());
        event.setCancelled(true);
    }
    // ===============Grabs Mythic Mobs Armor Value================
    private double getMobArmor(Entity entity) {
        ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
        if (mythicMob == null) {
            return 1.0;
        }
        if (mythicMob.getArmor() > 1) {
            return mythicMob.getArmor();
        } else {
            return 1.0; // Default armor value if not using MythicMobs or no armor stat found
        }
    }
    // ===============Check For Vanilla Crit===============
    private boolean isCritical(Player player)
    {
        return
                player.getFallDistance() > 0.0F &&
                        !player.isOnGround() &&
                        !player.isInsideVehicle() &&
                        !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                        player.getLocation().getBlock().getType() != Material.LADDER &&
                        player.getLocation().getBlock().getType() != Material.VINE;
    }

    public static void handleFireTicks() {
        new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.getFireTicks() > 1) {
                        PlayerStats stats = PlayerStats.getPlayerStats(player);
                        player.damage(0);
                        double damage = stats.getMaxhp()/40;
                        stats.setHp(stats.getHp()-damage);
                        stats.syncHealthWithHearts(player);
                    }
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 20, 20);
    }
    private double calculateMultiplier(Player player) {
        double multi = 1;
        if (player.hasPermission("gacha.vip")) {
            multi += 0.2;
        }
        if (PartyManager.isInParty(player)) {
            for (OfflinePlayer offlinePlayer : PartyManager.getPartyMembers(PartyManager.getPartyLeader(player))) {
                if (offlinePlayer.isOnline()) {
                    Player onlinePlayer = offlinePlayer.getPlayer();
                    if (onlinePlayer.hasPermission("gacha.vip")) {
                        multi += 0.1;
                    } else {
                        multi += 0.05;
                    }
                }
            }
        }
        return multi;
    }
    private void distributePartyRewards(Player player, Entity entity, double expGained, double moneyGained, double multi, Set<Player> partyMembers) {
        for (Player member : partyMembers) {
            if (DungeonUtils.getDungeonName(player.getLocation()).equals(DungeonUtils.getDungeonName(member.getLocation()))) {
                PlayerStats memberStats = PlayerStats.getPlayerStats(member);
                memberStats.addExp(expGained * multi, member);
                VaultHook.addMoney(member, moneyGained * multi);
                if (member != player) {
                }
            } else {
                member.sendMessage(ColorChat.chat("&cParty Member killed boss but you're not in the same Dungeon."));
            }
        }
    }
    private void handleMobSpecificRewards(Entity entity, Player player) {
        String entityName = entity.getName();
        if (entityName.contains("Goblin")) {
            GoblinDeathReward.MobDeath(entityName, player);
        } else if (entityName.contains("rpg")) {
            RPGDeathReward.MobDeath(entityName, player);
        } else if (entityName.contains("Bulb")) {
            BulbDeathReward.MobDeath(entityName, player);
        }
    }

    private void handleRandomRewards(Player player, Entity entity) {
        if (Math.random() < 0.0002 && ChatColor.stripColor(entity.getName()).equalsIgnoreCase(MobDropSelector.getMob())) {
            player.getInventory().addItem(MobDropSelector.getDrop(player));
        }
        if (Math.random() < 0.0015) {
            giveSkillReward(player, RandomSkillUtils.getRandomCommonSkill(), "&f&lCommon");
        }
        if (Math.random() < 0.0005) {
            giveSkillReward(player, RandomSkillUtils.getRandomUncommonSkill(), "&7&lUncommon");
        }
    }

    private void giveSkillReward(Player player, ItemStack skill, String rarity) {
        player.getInventory().addItem(skill);
        player.sendMessage(ColorChat.chat("&a&l Received " + rarity + " &a&lSkill!"));
    }
    private boolean isBoss(Entity entity) {
        return BOSS_NAMES.stream().anyMatch(bossName -> entity.getName().contains(bossName));
    }
    private boolean isInSameDungeon(OfflinePlayer player1, Player player2) {
        // Assuming both players are online
        if (player1.isOnline() && player1.getPlayer() != null) {
            return DungeonUtils.getDungeonName(player1.getPlayer().getLocation())
                    .equals(DungeonUtils.getDungeonName(player2.getLocation()));
        }
        return false;
    }

}

