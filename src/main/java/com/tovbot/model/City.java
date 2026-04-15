package com.tovbot.model;

import java.util.ArrayList;
import java.util.List;

public class City {
	
	public final String name;
	public final int x, y, z;
	
	public City(String name, int x, int y, int z) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public static List<City> getAllCities() {
		List<City> cities = new ArrayList<>();
		cities.add(new City("Mistport", -784, 81, 1313));
		cities.add(new City("Molta", 184, 100, 80));
		cities.add(new City("Alnera", 373, 80, 770));
		cities.add(new City("Rahkeri", -141, 171, 443));
		cities.add(new City("Frostgate", -1494, 98, 952));
		cities.add(new City("Nightroost", -1316, 130, 494));
		cities.add(new City("Wispervale", -1762, 133, -32));
		cities.add(new City("Steelmeld", -585, 8, -475));
		cities.add(new City("Carnival", -467, 82, 1566));
		cities.add(new City("Horseman", -1214, 80, -458));
		return cities;
	}
	
	public static City findClosest(int x, int y, int z, List<City> cities) {
		City closest = null;
		double minDist = Double.MAX_VALUE;
		
		for (City city : cities) {
			double dx = city.x - x;
			double dy = city.y - y;
			double dz = city.z - z;
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
			
			if (dist < minDist) {
				minDist = dist;
				closest = city;
			}
		}
		
		return closest;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%d, %d, %d)", name, x, y, z);
	}
}
