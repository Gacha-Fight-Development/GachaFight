package me.diu.gachafight.guild;

import lombok.Getter;
import lombok.Setter;

import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.time.Instant;

@Getter
@Setter
public class GuildRequest {
    private UUID playerUUID;
    private String playerName;
    private String guildId;
    private Instant requestTime;

    public GuildRequest(UUID playerUUID, String playerName, String guildId) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.guildId = guildId;
        this.requestTime = Instant.now();
    }
    public boolean isExpired() {
        Instant now = Instant.now();
        long daysBetween = ChronoUnit.DAYS.between(requestTime, now);
        return daysBetween >= 3;
    }
}