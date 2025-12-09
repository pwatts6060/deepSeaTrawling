package com.deepseatrawling;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;


import java.util.ArrayList;
import java.util.List;

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

    /*
    public enum shoalDepth
    {
        SHALLOW,
        MEDIUM,
        DEEP,
        UNKNOWN;
    }


    private shoalDepth depth = shoalDepth.UNKNOWN;

    public void setDepth(shoalDepth depth) {
        this.depth = depth;
    }

    public shoalDepth getDepth() {
        return depth;
    }*/

    private final WorldEntity worldEntity;
    private final int worldViewId;
    private shoalSpecies species;

    private GameObject shoalObject;

    private WorldPoint last;
    private LocalPoint current;
    private LocalPoint next;
    private boolean wasMoving;

    private final List<WorldPoint> pathPoints = new ArrayList<>();
    private final List<WorldPoint> stopPoints = new ArrayList<>();

    public ShoalData(int worldViewId, WorldEntity worldEntity) {
        this.worldViewId = worldViewId;
        this.worldEntity = worldEntity;
    }

    public void setSpecies(shoalSpecies species) {
        this.species = species;
    }

    public shoalSpecies getSpecies() {
        return species;
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

}
