package com.tovbot.model;

public class Location {
	
	public final int x, y, z;
	public String closestCity;
	public double weight;

	public Location(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.weight = 1.0;
		this.closestCity = "";
	}

	public Location(int x, int y, int z, String closestCity) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.weight = 1.0;
		this.closestCity = closestCity;
	}

	@Override
	public String toString() {
		return String.format("Location[x=%d, y=%d, z=%d, weight=%.4f]", x, y, z, weight);
	}
}
