package com.tovbot;

import com.tovbot.calculator.AngleCalculator;
import com.tovbot.calculator.MultiThrowCalculator;
import com.tovbot.calculator.NextThrowRecommender;
import com.tovbot.io.ClipboardReader;
import com.tovbot.io.LocationCsvLoader;
import com.tovbot.io.SkrRiddleLoader;
import com.tovbot.model.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TovBotMod implements ClientModInitializer {

	private static final String MOD_ID = "tov-bot";
	private static final double DEFAULT_STANDARD_DEVIATION = 0.6;
	private static final double DEFAULT_ANGLE_THRESHOLD = 3.0;
	private static final int DEFAULT_MAX_RESULTS = 10;
	private static final long CHAT_HISTORY_TIMEOUT_MS = 60 * 1000;

	private List<Location> tovLocations;
	private List<SkrRiddle> skrRiddles;
	private ThrowManager tovManager;
	private ThrowManager skrManager;
	private List<ChatMessage> recentChatMessages;

	@Override
	public void onInitializeClient() {
		LocationCsvLoader tovLoader = new LocationCsvLoader();
		tovLocations = tovLoader.loadLocations();
		
		SkrRiddleLoader skrLoader = new SkrRiddleLoader();
		skrRiddles = skrLoader.loadRiddles();
		
		tovManager = new ThrowManager();
		skrManager = new ThrowManager();
		
		recentChatMessages = new ArrayList<>();
		
		ClientSendMessageEvents.ALLOW_COMMAND.register(this::onAllowCommand);
		
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			String text = message.getString();
			long now = System.currentTimeMillis();
			recentChatMessages.add(new ChatMessage(text, now));
			recentChatMessages.removeIf(m -> now - m.timestamp > CHAT_HISTORY_TIMEOUT_MS);
		});
		
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			tovManager.clearThrows();
			skrManager.clearThrows();
			recentChatMessages.clear();
			System.out.println("[TOV Bot] All throws cleared on disconnect");
		});
		
		System.out.println("[TOV Bot] Initialized with " + tovLocations.size() + " TOV locations and " + skrRiddles.size() + " SKR riddles");
	}

	private boolean onAllowCommand(String command) {
		if (command.equals("tov")) {
			processTovCommand();
			return false;
		}
		if (command.equals("tovreset")) {
			processTovReset();
			return false;
		}
		if (command.equals("skr")) {
			processSkrCommand();
			return false;
		}
		if (command.equals("skrreset")) {
			processSkrReset();
			return false;
		}
		return true;
	}

	private void processTovReset() {
		MinecraftClient client = MinecraftClient.getInstance();
		tovManager.clearThrows();
		sendMessage(client, "TOV throws reset.");
	}

	private void processSkrReset() {
		MinecraftClient client = MinecraftClient.getInstance();
		skrManager.clearThrows();
		sendMessage(client, "SKR throws reset.");
	}

	private void processSkrCommand() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		SkrRiddle matchedRiddle = findMatchingRiddle();
		if (matchedRiddle != null) {
			double playerX = client.player.getX();
			double playerZ = client.player.getZ();
			double dist = Math.sqrt(
				Math.pow(matchedRiddle.x - playerX, 2) + 
				Math.pow(matchedRiddle.z - playerZ, 2)
			);
			
			sendMessage(client, String.format("Riddle Found: %d, %d, %d (%s)", 
				matchedRiddle.x, matchedRiddle.y, matchedRiddle.z, matchedRiddle.region));
			sendMessage(client, String.format("Distance: %.0f blocks", dist));
			return;
		}

		String clipboardContent = ClipboardReader.readClipboard();
		if (clipboardContent == null || clipboardContent.isEmpty()) {
			sendMessage(client, "Error: No riddle in chat history and no F3+C in clipboard");
			return;
		}

		F3CData f3cData = F3CData.tryParseF3CString(clipboardContent);
		if (f3cData == null) {
			sendMessage(client, "Error: No riddle in chat history and no valid F3+C in clipboard");
			return;
		}

		processSkrTriangulation(client, f3cData);
	}

	private SkrRiddle findMatchingRiddle() {
		long now = System.currentTimeMillis();
		
		for (ChatMessage msg : recentChatMessages) {
			if (now - msg.timestamp > CHAT_HISTORY_TIMEOUT_MS) continue;
			
			String normalizedMsg = SkrRiddle.normalizeText(msg.text);
			
			for (SkrRiddle riddle : skrRiddles) {
				double similarity = SkrRiddle.calculateSimilarity(normalizedMsg, riddle.getNormalizedRiddle());
				if (similarity > 0.8) {
					return riddle;
				}
			}
		}
		
		return null;
	}

	private void processSkrTriangulation(MinecraftClient client, F3CData f3cData) {
		skrManager.cleanupExpiredThrows();

		skrManager.addThrow(f3cData);
		int throwNum = skrManager.getThrowCount();

		List<Location> skrLocations = new ArrayList<>();
		List<SkrCity> skrCities = SkrCity.getAllCities();
		for (SkrRiddle riddle : skrRiddles) {
			Location loc = new Location(riddle.x, riddle.y, riddle.z);
			SkrCity closest = SkrCity.findClosest(riddle.x, riddle.y, riddle.z, skrCities);
			loc.closestCity = closest != null ? closest.name : "";
			skrLocations.add(loc);
		}

		MultiThrowCalculator calculator = new MultiThrowCalculator(
			DEFAULT_STANDARD_DEVIATION,
			DEFAULT_ANGLE_THRESHOLD,
			DEFAULT_MAX_RESULTS
		);
		List<LocationResult> results = calculator.calculateProbabilities(skrManager.getThrows(), skrLocations);

		if (results.isEmpty()) {
			sendMessage(client, "No SKR locations found within " + DEFAULT_ANGLE_THRESHOLD + "° threshold");
			return;
		}

		double topCertainty = results.get(0).probability;
		
		sendMessage(client, String.format("Calculating SKR Location... (Throw %d) - Probability: %.1f%%", throwNum, topCertainty * 100));
		
		NextThrowRecommender recommender = new NextThrowRecommender(DEFAULT_STANDARD_DEVIATION);
		NextThrowRecommender.CityRecommendation rec = recommender.recommendSkrCities(results);
		sendMessage(client, String.format("Best Next Throw: %s (%.0f%%) or %s (%.0f%%)", 
			rec.bestCity, rec.bestCertainty * 100, rec.secondCity, rec.secondCertainty * 100));
		
		for (int i = 0; i < results.size(); i++) {
			LocationResult result = results.get(i);
			String line = String.format("#%d: (%d,%d,%d) %.1f%% - %.0fb from %s",
				i + 1,
				result.location.x,
				result.location.y,
				result.location.z,
				result.probability * 100,
				result.distance,
				result.location.closestCity != null ? result.location.closestCity : ""
			);
			sendMessage(client, line);
		}
	}

	private void processTovCommand() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		tovManager.cleanupExpiredThrows();

		String clipboardContent = ClipboardReader.readClipboard();
		if (clipboardContent == null || clipboardContent.isEmpty()) {
			sendMessage(client, "Error: Could not read clipboard");
			return;
		}

		F3CData f3cData = F3CData.tryParseF3CString(clipboardContent);
		if (f3cData == null) {
			sendMessage(client, "Error: No valid F3+C location in clipboard. Press F3+C first.");
			return;
		}

		tovManager.addThrow(f3cData);
		int throwNum = tovManager.getThrowCount();

		MultiThrowCalculator calculator = new MultiThrowCalculator(
			DEFAULT_STANDARD_DEVIATION,
			DEFAULT_ANGLE_THRESHOLD,
			DEFAULT_MAX_RESULTS
		);
		List<LocationResult> results = calculator.calculateProbabilities(tovManager.getThrows(), tovLocations);

		if (results.isEmpty()) {
			sendMessage(client, "No locations found within " + DEFAULT_ANGLE_THRESHOLD + "° threshold");
			return;
		}

		double topCertainty = results.get(0).probability;
		
		sendMessage(client, String.format("Calculating Location... (Throw %d) - Probability: %.1f%%", throwNum, topCertainty * 100));
		
		NextThrowRecommender recommender = new NextThrowRecommender(DEFAULT_STANDARD_DEVIATION);
		NextThrowRecommender.CityRecommendation rec = recommender.recommendTovCities(results);
		sendMessage(client, String.format("Best Next Throw: %s (%.0f%%) or %s (%.0f%%)", 
			rec.bestCity, rec.bestCertainty * 100, rec.secondCity, rec.secondCertainty * 100));
		
		for (int i = 0; i < results.size(); i++) {
			LocationResult result = results.get(i);
			String city = result.location.closestCity != null ? result.location.closestCity : "";
			String line = String.format("#%d: (%d,%d,%d) %.1f%% - %.0fb from %s",
				i + 1,
				result.location.x,
				result.location.y,
				result.location.z,
				result.probability * 100,
				result.distance,
				city
			);
			sendMessage(client, line);
		}
	}

	private void sendMessage(MinecraftClient client, String message) {
		if (client.player != null) {
			client.player.sendMessage(Text.literal("§6[TOV Bot]§r " + message), false);
		}
	}

	private static class ChatMessage {
		final String text;
		final long timestamp;
		
		ChatMessage(String text, long timestamp) {
			this.text = text;
			this.timestamp = timestamp;
		}
	}
}
