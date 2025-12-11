package com.deepseatrawling;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ShoalData {

    public enum shoalSpecies
    {
        GIANT_KRILL (ObjectID.GIANT_KRILL_SHOAL),
        HADDOCK (ObjectID.HADDOCK_SHOAL),
        YELLOWFIN (ObjectID.YELLOWFIN_SHOAL),
        HALIBUT (ObjectID.HALIBUT_SHOAL),
        BLUEFIN (ObjectID.BLUEFIN_SHOAL),
        MARLIN (ObjectID.MARLIN_SHOAL),
        SHIMMERING (ObjectID.SHIMMERING_SHOAL),
        GLISTENING (ObjectID.GLISTENING_SHOAL),
        VIBRANT (ObjectID.VIBRANT_SHOAL);

        private final int objectID;

        public EnumSet<shoalDepth> allowedDepths()
        {
            switch(this)
            {
                case GIANT_KRILL:
                case HADDOCK:
                case SHIMMERING:
                    return EnumSet.of(shoalDepth.SHALLOW);

                case YELLOWFIN:
                case HALIBUT:
                case GLISTENING:
                    return EnumSet.of(shoalDepth.SHALLOW, shoalDepth.MEDIUM);

                case BLUEFIN:
                case MARLIN:
                case VIBRANT:
                default:
                    return  EnumSet.of(shoalDepth.SHALLOW, shoalDepth.MEDIUM, shoalDepth.DEEP, shoalDepth.UNKNOWN);
            }
        }

        public shoalDepth defaultDepth()
        {
            switch (this)
            {
                case GIANT_KRILL:
                case HADDOCK:
                case SHIMMERING:
                    return shoalDepth.SHALLOW;
                default:
                    return shoalDepth.MEDIUM;
            }
        }

        shoalSpecies(int objectID) {
            this.objectID = objectID;
        }

        public static shoalSpecies fromGameObjectId(int id)
        {
            for (shoalSpecies s : values())
            {
                if (s.objectID == id) {
                    return s;
                }
            }
            return null;
        }

    }


    public enum shoalDepth
    {
        SHALLOW,
        MEDIUM,
        DEEP,
        UNKNOWN;

        public static int asInt(shoalDepth depth)
        {
            switch(depth) {
                case SHALLOW:
                    return 1;
                case MEDIUM:
                    return 2;
                case DEEP:
                    return 3;
                default:
                    return -1;
            }
        }
    }

    private shoalDepth depth = shoalDepth.UNKNOWN;
    private EnumSet<shoalDepth> possibleDepths;

    private static final String RESOURCE_NAME = "shoals.properties";

    private final WorldEntity worldEntity;
    private final int worldViewId;
    private shoalSpecies species;

    private GameObject shoalObject;

    private WorldPoint last;
    private LocalPoint current;
    private LocalPoint next;
    private boolean wasMoving;

    private List<WorldPoint> pathPoints = new ArrayList<>();
    private List<WorldPoint> stopPoints = new ArrayList<>();

    public ShoalData(int worldViewId, WorldEntity worldEntity) throws IOException {
        this.worldViewId = worldViewId;
        this.worldEntity = worldEntity;
        load();
    }

    public void setSpecies(shoalSpecies species) {
        this.species = species;
        this.possibleDepths = EnumSet.copyOf(species.allowedDepths());
        if(possibleDepths.size() == 1) {
            this.depth = possibleDepths.iterator().next();
        }
    }

    public shoalSpecies getSpecies() {
        return species;
    }

    public shoalDepth getDepth() {
        return depth;
    }

    public void setDepth(shoalDepth depth) {
        this.depth = depth;
        if (depth != shoalDepth.UNKNOWN) {
            this.possibleDepths = EnumSet.of(depth);
        }
    }

    public EnumSet<shoalDepth> getPossibleDepths() {
        return possibleDepths;
    }

    public void setShoalObject(GameObject shoalObject) {
        this.shoalObject = shoalObject;
    }

    public GameObject getShoalObject() {
        return shoalObject;
    }

    public WorldEntity getWorldEntity() {
        return worldEntity;
    }

    public int getWorldViewId() {
        return worldViewId;
    }

    public LocalPoint getCurrent() {
        return current;
    }

    public LocalPoint getNext() {
        return next;
    }

    public WorldPoint getLast() {
        return last;
    }

    public void setCurrent(LocalPoint current) {
        this.current = current;
    }

    public void setNext(LocalPoint next) {
        this.next = next;
    }

    public void setLast(WorldPoint last) {
        this.last = last;
    }

    public List<WorldPoint> getPathPoints() {
        return pathPoints;
    }

    public List<WorldPoint> getStopPoints() {
        return stopPoints;
    }

    public void setWasMoving(boolean wasMoving) {
        this.wasMoving = wasMoving;
    }

    public boolean getWasMoving() {
        return wasMoving;
    }

    private void load() throws IOException
    {
        Properties properties = new Properties();
        String idStr;
        int id;
        String value;

        try (InputStream in = getClass().getResourceAsStream("/" + RESOURCE_NAME))
        {
            if (in == null) {
                throw new IOException("Could not find resource: " + RESOURCE_NAME);
            }
            properties.load(in);
        }

        for (String key : properties.stringPropertyNames())
        {
            if (key.startsWith("shoalpath."))
            {
                idStr = key.substring("shoalpath.".length());
                if(Integer.parseInt(idStr) == getWorldViewId()) {
                    value = properties.getProperty(key);
                    this.pathPoints = parsePoints(value);
                }
            }

            if (key.startsWith("shoalstops.")) {
                idStr = key.substring("shoalstops.".length());
                if (Integer.parseInt(idStr) == getWorldViewId()) {
                    value = properties.getProperty(key);
                    this.stopPoints = parsePoints(value);
                }
            }
        }
    }

    private List<WorldPoint> parsePoints(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parsePoint)
                .collect(Collectors.toList());
    }

    private WorldPoint parsePoint(String token)
    {
        String[] parts = token.split(", ");
        if(parts.length < 2)
        {
            throw new IllegalArgumentException("Invalid Point: " + token);
        }

        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        int plane = Integer.parseInt(parts[2].trim());

        return new WorldPoint(x, y, plane);
    }

}
