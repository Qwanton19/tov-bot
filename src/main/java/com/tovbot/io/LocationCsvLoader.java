package com.tovbot.io;

import com.tovbot.model.City;
import com.tovbot.model.Location;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class LocationCsvLoader {

	private static final String CONFIG_DIR = "config/tovbot";
	private static final String CSV_FILE = "tovchests.csv";
	
	private List<City> cities;

	public LocationCsvLoader() {
		this.cities = City.getAllCities();
	}

	public List<Location> loadLocations() {
		List<Location> locations = new ArrayList<>();

		Path configPath = Paths.get(CONFIG_DIR, CSV_FILE);
		if (Files.exists(configPath)) {
			locations = loadFromFile(configPath.toFile());
		}

		if (locations.isEmpty()) {
			Path devPath = Paths.get(CSV_FILE);
			if (Files.exists(devPath)) {
				locations = loadFromFile(devPath.toFile());
			}
		}

		if (locations.isEmpty()) {
			locations = loadFromResource();
		}

		return locations;
	}

	private List<Location> loadFromFile(File file) {
		List<Location> locations = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			boolean firstLine = true;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				
				if (line.isEmpty()) continue;
				if (firstLine && line.toLowerCase().startsWith("x")) {
					firstLine = false;
					continue;
				}
				firstLine = false;
				
				String[] parts = line.split(",");
				if (parts.length >= 3) {
					try {
						int x = Integer.parseInt(parts[0].trim());
						int y = Integer.parseInt(parts[1].trim());
						int z = Integer.parseInt(parts[2].trim());
						
						City closest = City.findClosest(x, y, z, cities);
						String cityName = closest != null ? closest.name : "";
						
						locations.add(new Location(x, y, z, cityName));
					} catch (NumberFormatException e) {
					}
				}
			}
		} catch (IOException e) {
			System.err.println("[TOV Bot] Error loading locations from file: " + e.getMessage());
		}

		return locations;
	}

	private List<Location> loadFromResource() {
		List<Location> locations = new ArrayList<>();
		
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(CSV_FILE)) {
			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				boolean firstLine = true;
				
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					
					if (line.isEmpty()) continue;
					if (firstLine && line.toLowerCase().startsWith("x")) {
						firstLine = false;
						continue;
					}
					firstLine = false;
					
					String[] parts = line.split(",");
					if (parts.length >= 3) {
						try {
							int x = Integer.parseInt(parts[0].trim());
							int y = Integer.parseInt(parts[1].trim());
							int z = Integer.parseInt(parts[2].trim());
							
							City closest = City.findClosest(x, y, z, cities);
							String cityName = closest != null ? closest.name : "";
							
							locations.add(new Location(x, y, z, cityName));
						} catch (NumberFormatException e) {
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println("[TOV Bot] Error loading locations from resource: " + e.getMessage());
		}

		return locations;
	}

	public void setupConfigDirectory() {
		try {
			Path configDir = Paths.get(CONFIG_DIR);
			if (!Files.exists(configDir)) {
				Files.createDirectories(configDir);
			}
			
			Path destPath = configDir.resolve(CSV_FILE);
			if (!Files.exists(destPath)) {
				InputStream is = getClass().getClassLoader().getResourceAsStream(CSV_FILE);
				if (is != null) {
					Files.copy(is, destPath);
				}
			}
		} catch (IOException e) {
			System.err.println("[TOV Bot] Error setting up config directory: " + e.getMessage());
		}
	}
}
