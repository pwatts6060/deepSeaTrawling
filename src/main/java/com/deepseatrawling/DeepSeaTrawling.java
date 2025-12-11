package com.deepseatrawling;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.ChatMessageType;

import javax.inject.Inject;
import java.io.IOException;
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

	public final Set<Integer> trackedShoals = new HashSet<>();
	public final List<ShoalData.shoalSpecies> shoalTypes = List.of(
			ShoalData.shoalSpecies.GIANT_KRILL,
			ShoalData.shoalSpecies.HADDOCK,
			ShoalData.shoalSpecies.HALIBUT,
			ShoalData.shoalSpecies.YELLOWFIN,
			ShoalData.shoalSpecies.BLUEFIN,
			ShoalData.shoalSpecies.MARLIN
	);

	private ShoalData nearestShoal;

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
		nearestShoal = null;
		rebuildTrackedShoals();
		log.info("Deep Sea Trawling Plugin Started");

	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		trackedShoals.clear();
		log.info("Deep Sea Trawling Plugin Stopped");
	}

	public ShoalData getNearestShoal() {
		return nearestShoal;
	}

	public boolean netFull = false;

	public int currentNetDepthLeft = 0;
	public int currentNetDepthRight = 0;

	@Subscribe
	public void onWorldEntitySpawned(WorldEntitySpawned event) throws IOException {
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

		ShoalData shoal = new ShoalData(worldViewId, entity);

		/*LocalPoint current = entity.getLocalLocation();
		data.setCurrent(current);
		data.setNext(entity.getTargetLocation());
		data.setLast(current);
		if (current != null) {
			data.getPathPoints().add(current);
		}
		 */

		nearestShoal = shoal;

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
		ShoalData shoal = nearestShoal;
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
		ShoalData shoal = getNearestShoal();

		WorldEntity entity = shoal.getWorldEntity();

		LocalPoint current = entity.getLocalLocation();
		LocalPoint next = entity.getTargetLocation();

		shoal.setCurrent(current);
		shoal.setNext(next);

		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, current);
		WorldPoint last = shoal.getLast();

		boolean isMoving = (current != null && next != null && !current.equals(next));

		shoal.setWasMoving(isMoving);
		shoal.setLast(currentWorldPoint);

	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String msg = event.getMessage().replaceAll("<[^>]*>","");
		ChatMessageType type = event.getType();

		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM)
		{
			return;
		}

		if (msg.startsWith("The nearby ")) {
			handleShoalSwimsMessage(msg);
		} else if (msg.contains(" net is too deep to catch fish ") || msg.contains(" net is not deep enough to catch fish ")) {
			handleDepthMismatchMessage(msg);
		} else if (msg.startsWith("Your net has no more space for any ")) {
			netFull = true;
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
/*
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

 */

	private void rebuildTrackedShoals() {
		trackedShoals.clear();

		if(config.showGiantKrill()) {
			trackedShoals.add(26);
			trackedShoals.add(27);
			trackedShoals.add(28);
			trackedShoals.add(29);
		}
		if(config.showHaddock()) {
			trackedShoals.add(23);
			trackedShoals.add(24);
			trackedShoals.add(25);
		}
		if(config.showHalibut()) {
			trackedShoals.add(18);
			trackedShoals.add(19);
		}
		if(config.showYellowfin()) {
			trackedShoals.add(20);
			trackedShoals.add(21);
			trackedShoals.add(22);
		}
		if(config.showBluefin()) {
			trackedShoals.add(16);
			trackedShoals.add(17);
		}
		if(config.showMarlin()) {
			trackedShoals.add(14);
			trackedShoals.add(15);
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("deepseatrawling")) {
			return;
		}
		rebuildTrackedShoals();
	}

	private void handleShoalSwimsMessage(String msg)
	{
		ShoalData shoal = getNearestShoal();
		if (shoal == null) {
			return;
		}

		if (msg.contains(" closer to the surface"))
		{
			adjustShoalDepthRelative(shoal, 1);

		} else if (msg.contains(" deeper into the depths")) {
			adjustShoalDepthRelative(shoal, -1);
		}
	}

	private void handleDepthMismatchMessage(String msg)
	{
		ShoalData shoal = getNearestShoal();
		if (shoal == null) {
			return;
		}

		boolean tooShallow = msg.contains("not deep enough");
		boolean tooDeep = msg.contains("too deep");

		String operatorName;

		if(msg.startsWith("Your net"))
		{
			operatorName = client.getLocalPlayer().getName();
		} else {
			int index = msg.indexOf("'s net");
			if (index <= 0) {
				return;
			}
			operatorName = msg.substring(0, index);
		}

		int netDepthIndex = getNetDepth(operatorName);

	}

	private int getNetDepth(String operatorName) {
		return 0; //handle getting the net depth depending on who it is
	}

	private void adjustShoalDepthRelative(ShoalData shoal, int delta)
	{
		if (shoal.getDepth() == ShoalData.shoalDepth.UNKNOWN)
		{
			EnumSet<ShoalData.shoalDepth> allowed = shoal.getSpecies().allowedDepths();
			EnumSet<ShoalData.shoalDepth> shifted = EnumSet.noneOf(ShoalData.shoalDepth.class);

			for (ShoalData.shoalDepth depth : allowed)
			{
				int possibleNewDepth = ShoalData.shoalDepth.asInt(depth) + 1;
				for (ShoalData.shoalDepth candidateNewDepth : allowed)
				{
					if (ShoalData.shoalDepth.asInt(candidateNewDepth) == possibleNewDepth)
					{
						shifted.add(candidateNewDepth);
					}
				}
			}

			if (!shifted.isEmpty())
			{
				shoal.getPossibleDepths().retainAll(shifted);
				if (shoal.getPossibleDepths().size() == 1)
				{
					shoal.setDepth(shoal.getPossibleDepths().iterator().next());
				}
			}
			return;
		}

		int currentDepth = ShoalData.shoalDepth.asInt(shoal.getDepth());
		ShoalData.shoalDepth newDepth = ShoalData.shoalDepth.UNKNOWN;

		for (ShoalData.shoalDepth depth : shoal.getSpecies().allowedDepths())
		{
			if (ShoalData.shoalDepth.asInt(depth) == currentDepth + delta) {
				newDepth = depth;
				break;
			}
		}

		if (newDepth != ShoalData.shoalDepth.UNKNOWN)
		{
			shoal.setDepth(newDepth);
		}
	}

}
