package com.tovbot.model;

import java.util.ArrayList;
import java.util.List;

public class SkrCity {
	
	public final String name;
	public final int x, y, z;
	
	public SkrCity(String name, int x, int y, int z) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public static List<SkrCity> getAllCities() {
		List<SkrCity> cities = new ArrayList<>();
		cities.add(new SkrCity("Starpoint", 0, 64, 850));
		cities.add(new SkrCity("Pelias' Keep", -470, 64, -77));
		cities.add(new SkrCity("Galengarde", -300, 64, -650));
		cities.add(new SkrCity("Port Monteau", -458, 64, -900));
		return cities;
	}
	
	public static SkrCity findClosest(int x, int y, int z, List<SkrCity> cities) {
		SkrCity closest = null;
		double minDist = Double.MAX_VALUE;
		
		for (SkrCity city : cities) {
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
