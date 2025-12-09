package com.deepseatrawling;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.*;


@Slf4j
@PluginDescriptor(
	name = "Deep Sea Trawling Helper",
	description = "Tracks Shoals and predicts their next movement",
	tags = {"trawl", "trawling", "sailing", "fishing", "shoal", "deep", "sea"}
)
public class DeepSeaTrawling extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private DeepSeaTrawlingConfig config;

	@Inject
	private DeepSeaTrawlingOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	private final Map<Integer, ShoalData> trackedShoals = new HashMap<>();

	public

	@Provides
	DeepSeaTrawlingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DeepSeaTrawlingConfig.class);
	}

	private static final int SHOAL_WORLD_ENTITY_TYPE = 4;
	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		log.info("Deep Sea Trawling Plugin Started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		trackedShoals.clear();
		log.info("Deep Sea Trawling Plugin Stopped");
	}

	public Collection<ShoalData> getShoals() {
		return trackedShoals.values();
	}

	public boolean isTypeEnabled(ShoalData.shoalSpecies species)
	{
		if (species == null) {
			return false;
		}
		switch (species)
		{
			case GIANT_KRILL: return config.showGiantKrill();
			case HADDOCK:     return config.showHaddock();
			case YELLOWFIN:   return config.showYellowfin();
			case HALIBUT:     return config.showHalibut();
			case BLUEFIN:     return config.showBluefin();
			case MARLIN:      return config.showMarlin();
			case SHIMMERING:  return config.showShimmering();
			case GLISTENING:  return config.showGlistening();
			case VIBRANT:     return config.showVibrant();
			default:          return false;
		}
	}

	@Subscribe
	public void onWorldEntitySpawned(WorldEntitySpawned event) {
		WorldEntity entity = event.getWorldEntity();
		WorldEntityConfig cfg = entity.getConfig();

		if (cfg == null || cfg.getId() != SHOAL_WORLD_ENTITY_TYPE) {
			return;
		}

		WorldView view = entity.getWorldView();
		if (view == null) {
			return;
		}

		int worldViewId = view.getId();

		ShoalData data = new ShoalData(worldViewId, entity);

		/*LocalPoint current = entity.getLocalLocation();
		data.setCurrent(current);
		data.setNext(entity.getTargetLocation());
		data.setLast(current);
		if (current != null) {
			data.getPathPoints().add(current);
		}
		 */

		trackedShoals.put(worldViewId, data);

		//debugging
		log.debug("Registered shoal entity worldViewId={} typeId={}", worldViewId, cfg.getId());
	}

	@Subscribe
	public void onWorldEntityDespawned(WorldEntityDespawned event)
	{
		/*
		WorldEntity entity = event.getWorldEntity();
		WorldView view = entity.getWorldView();
		if (view == null) {
			;
		}

		//int worldviewId = view.getId();
		//ShoalData removed = trackedShoals.remove(worldviewId);
		//if (removed != null) {
		//	log.debug("Removed shoal entity worldViewId={}", worldviewId);
		//}*/
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject object = event.getGameObject();
		if(object == null || object.getWorldView() == null) {
			return;
		}

		int objectId = object.getId();
		ShoalData.shoalSpecies species = ShoalData.shoalSpecies.fromGameObjectId(objectId);
		if (species == null) {
			return;
		}

		int worldViewId = object.getWorldView().getId();
		ShoalData shoal = trackedShoals.get(worldViewId);
		if (shoal == null) {
			return;
		}

		shoal.setSpecies(species);
		shoal.setShoalObject(object);

		log.debug("Shoal worldViewId={} species={} objectId={}", worldViewId, species, objectId);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		for (ShoalData shoal : trackedShoals.values())
		{
			WorldEntity entity = shoal.getWorldEntity();

			LocalPoint current = entity.getLocalLocation();
			LocalPoint next = entity.getTargetLocation();

			shoal.setCurrent(current);
			shoal.setNext(next);

			WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, current);
			WorldPoint last = shoal.getLast();

			boolean isMoving = (current != null && next != null && !current.equals(next));

			if (current != null && isMoving) {
				if ((last == null || worldDistanceSq(last, currentWorldPoint) < 40 * 40)) {
					shoal.getPathPoints().add(currentWorldPoint);
				} else {
					shoal.getPathPoints().add(currentWorldPoint);
				}
			}

			if (shoal.getWasMoving() && !isMoving && current != null)
			{
				shoal.getStopPoints().add(currentWorldPoint);
			}

			shoal.setWasMoving(isMoving);
			shoal.setLast(currentWorldPoint);

		}
	}

/*
	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		int idx = e.getVarbitId(); //5123 for net, VarpId -
		int value = e.getValue(); //0, raised, 1, shallow, 2, medium, 3, deep,

		// Quick filter: only log small values that look like states (0–5)

			log.debug("Varbit Changed idx={} value={}", idx, value);

	}
*/
	private int worldDistanceSq(WorldPoint a, WorldPoint b)
	{
		int dx = a.getX() - b.getX();
		int dy = a.getY() - b.getY();
		return dx * dx + dy * dy;
	}

	public void dumpPathsToLog()
	{
		for (ShoalData shoal : trackedShoals.values())
		{
			StringBuilder builder = new StringBuilder();
			builder.append("Shoal wv=").append(shoal.getWorldViewId()).append(" species=").append(shoal.getSpecies()).append(" path=[");

			boolean first = true;
			for (WorldPoint worldPoint : shoal.getPathPoints())
			{
				if (!first)
				{
					builder.append(";");
				}
				first = false;
				builder.append(worldPoint.getX()).append(",").append(worldPoint.getY());
			}
			builder.append("] stops=[");
			first = true;
			for (WorldPoint worldPoint : shoal.getStopPoints())
			{
				if (!first) {
					builder.append(";");
				}
				first = false;
				builder.append(worldPoint.getX()).append(",").append(worldPoint.getY());
			}

			log.info(builder.toString());
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged config) {
		dumpPathsToLog();
	}
}
