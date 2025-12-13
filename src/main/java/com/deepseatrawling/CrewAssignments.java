package com.deepseatrawling;

public class CrewAssignments {
    int uniqueId;
    String name;
    boolean isPlayer;

    int index;

    enum Assignment
    {
        OTHER(-1),
        TRAWLING_NET_PORT(13),
        TRAWLING_NET_STARBOARD(14);

        private final int id;

        Assignment(int id) {
            this.id = id;
        }

        static Assignment fromId(int id) {
            for (Assignment assignment : values()) {
                if (assignment.id == id) {
                    return  assignment;
                }
            }
            return OTHER;
        }

        public int getNetIndex()
        {
            switch (this) {
                case TRAWLING_NET_PORT: return 0;
                case TRAWLING_NET_STARBOARD: return 1;
                default: return -1;
            }
        }

        public boolean isNet() {
            return this == TRAWLING_NET_PORT || this == TRAWLING_NET_STARBOARD;
        }
    }

    Assignment assignment;

}
