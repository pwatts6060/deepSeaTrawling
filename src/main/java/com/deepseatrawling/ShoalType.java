package com.deepseatrawling;

import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldEntityConfig;

import java.awt.*;
import java.util.Arrays;
import java.util.Set;

@Getter
public enum ShoalType {
    GIANT_KRILL("Giant Krill Shoal", 0xFF7878, 59734),
    HADDOCK("Haddock Shoal", 0xC8C8FF, 59735),
    YELLOWFIN("Yellowfin Shoal", 0xFFFF78, 59736),
    HALIBUT("Halibut Shoal", 0xC8FFC8, 4),
    BLUEFIN("Bluefin Shoal", 0x7890FF, 59738),
    MARLIN("Marlin Shoal", 0xFFB450,  59739),

    SHIMMERING("Shimmering Shoal", 0xAAFFFF, 59740),
    GLISTENING("Glistening Shoal", 0xC8FFF0, 59741),
    VIBRANT("Vibrant Shoal", 0xDCFFC8, 59742);

    private final String displayName;
    private final int rgb;
    private final int entityId;

    ShoalType(String displayName, int rgb, int entityId)
    {
        this.displayName = displayName;
        this.rgb = rgb;
        this.entityId = entityId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColour() {
        return new Color(rgb);
    }

    public int getEntityId() {
        return entityId;
    }

    public static ShoalType fromWorldEntity(WorldEntity worldEntity)
    {
        WorldEntityConfig cfg = worldEntity.getConfig();
        if (cfg == null) {
            return null;
        }
        int id = cfg.getId();
        for(ShoalType shoalType : values())
        {
            if (shoalType.entityId == id) {
                return shoalType;
            }
        }
        return null;
    }
}
