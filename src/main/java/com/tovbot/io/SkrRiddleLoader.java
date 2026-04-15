package com.tovbot.io;

import com.tovbot.model.SkrRiddle;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class SkrRiddleLoader {

	private static final String CSV_FILE = "skriddles.csv";

	public List<SkrRiddle> loadRiddles() {
		List<SkrRiddle> riddles = new ArrayList<>();
		
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(CSV_FILE)) {
			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				boolean firstLine = true;
				
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					
					if (line.isEmpty()) continue;
					if (firstLine && line.toLowerCase().startsWith("riddle")) {
						firstLine = false;
						continue;
					}
					firstLine = false;
					
					int firstQuote = line.indexOf('"');
					int lastQuote = line.lastIndexOf('"');
					
					if (firstQuote == 0 && lastQuote > firstQuote) {
						String riddle = line.substring(1, lastQuote);
						String rest = line.substring(lastQuote + 2);
						
						String[] parts = rest.split(",");
						if (parts.length >= 4) {
							try {
								int x = Integer.parseInt(parts[0].trim());
								int y = Integer.parseInt(parts[1].trim());
								int z = Integer.parseInt(parts[2].trim());
								String region = parts[3].trim();
								
								riddles.add(new SkrRiddle(riddle, x, y, z, region));
							} catch (NumberFormatException e) {
							}
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println("[TOV Bot] Error loading SKR riddles: " + e.getMessage());
		}
		
		return riddles;
	}
}
