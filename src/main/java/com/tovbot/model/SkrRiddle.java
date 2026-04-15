package com.tovbot.model;

public class SkrRiddle {
	
	public final String riddle;
	public final int x, y, z;
	public final String region;
	
	public SkrRiddle(String riddle, int x, int y, int z, String region) {
		this.riddle = riddle;
		this.x = x;
		this.y = y;
		this.z = z;
		this.region = region;
	}
	
	public String getNormalizedRiddle() {
		return normalizeText(riddle);
	}
	
	public static String normalizeText(String text) {
		String normalized = text.replaceAll("§[0-9a-fk-or]", "");
		normalized = normalized.replaceAll("[^a-zA-Z0-9\\s]", " ");
		normalized = normalized.toLowerCase();
		normalized = normalized.replaceAll("\\s+", " ").trim();
		return normalized;
	}
	
	public static double calculateSimilarity(String a, String b) {
		String[] wordsA = a.split("\\s+");
		String[] wordsB = b.split("\\s+");
		
		int matches = 0;
		for (String wordA : wordsA) {
			for (String wordB : wordsB) {
				if (wordA.equals(wordB)) {
					matches++;
					break;
				}
			}
		}
		
		int maxWords = Math.max(wordsA.length, wordsB.length);
		return maxWords > 0 ? (double) matches / maxWords : 0.0;
	}
	
	@Override
	public String toString() {
		return String.format("SkrRiddle[%s at (%d, %d, %d)]", region, x, y, z);
	}
}
