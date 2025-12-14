package com.deepseatrawling;

import net.runelite.api.gameval.ObjectID;
import org.apache.commons.lang3.ArrayUtils;

public enum NetTiers {
    ROPE(new int[]{
            ObjectID.SAILING_ROPE_TRAWLING_NET,
            ObjectID.SAILING_ROPE_TRAWLING_NET_3X8_PORT,
            ObjectID.SAILING_ROPE_TRAWLING_NET_3X8_STARBOARD
        }
    ),
    LINEN(new int[]{
            ObjectID.SAILING_LINEN_TRAWLING_NET,
            ObjectID.SAILING_LINEN_TRAWLING_NET_3X8_PORT,
            ObjectID.SAILING_LINEN_TRAWLING_NET_3X8_STARBOARD
        }
    ),
    HEMP(new int[]{
            ObjectID.SAILING_HEMP_TRAWLING_NET,
            ObjectID.SAILING_HEMP_TRAWLING_NET_3X8_PORT,
            ObjectID.SAILING_HEMP_TRAWLING_NET_3X8_STARBOARD,
        }
    ),
    COTTON(new int[]{
            ObjectID.SAILING_COTTON_TRAWLING_NET,
            ObjectID.SAILING_COTTON_TRAWLING_NET_3X8_PORT,
            ObjectID.SAILING_COTTON_TRAWLING_NET_3X8_STARBOARD,
        }
    );

    private final int[] gameObjectIds;

    NetTiers(int[] gameObjectIds) {
        this.gameObjectIds = gameObjectIds;
    }

    public int[] getGameObjectIds() {
        return gameObjectIds;
    }

}
