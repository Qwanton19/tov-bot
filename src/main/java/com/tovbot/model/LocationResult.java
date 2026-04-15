package com.tovbot.model;

public class LocationResult {
	
	public final Location location;
	public final double probability;
	public final double distance;
	public final double angleDifference;

	public LocationResult(Location location, double probability, double distance, double angleDifference) {
		this.location = location;
		this.probability = probability;
		this.distance = distance;
		this.angleDifference = angleDifference;
	}
}
