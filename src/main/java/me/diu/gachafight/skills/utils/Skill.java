package me.diu.gachafight.skills.utils;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface Skill {
    /**
     * Use the skill.
     * @param player The player using the skill
     * @param slot The slot the skill is in
     */
    void useSkill(Player player, int slot);

    /**
     * Apply the skill effect. This method is called when the skill is used or when an active skill's effect is triggered.
     * @param player The player using the skill
     * @param target The target of the skill (can be null for AoE skills)
     * @return The damage multiplier (1.0 if the skill doesn't modify damage)
     */
    double applySkillEffect(Player player, LivingEntity target);


    /**
     * Check if the skill is currently active for the player.
     * @param player The player to check
     * @return true if the skill is active, false otherwise
     */
    default boolean isSkillActive(Player player) {
        return false;
    }

    /**
     * Deactivate the skill for the player.
     * @param player The player for whom to deactivate the skill
     */
    default void deactivateSkill(Player player) {
        // Do nothing by default
    }

    /**
     * Check if this skill has an active state.
     * @return true if the skill has an active state, false otherwise
     */
    default boolean hasActiveState() {
        return false;
    }
}
