package com.tovbot.calculator;

import com.tovbot.model.F3CData;
import com.tovbot.model.Location;
import com.tovbot.model.LocationResult;
import com.tovbot.model.ThrowData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// adapted from ninjabrain bot's posterior.java - conditions all throws together.
public class MultiThrowCalculator {

	private final double standardDeviation;
	private final double angleThreshold;
	private final int maxResults;

	public MultiThrowCalculator(double standardDeviation, double angleThreshold, int maxResults) {
		this.standardDeviation = standardDeviation;
		this.angleThreshold = angleThreshold;
		this.maxResults = maxResults;
	}

	public List<LocationResult> calculateProbabilities(List<ThrowData> throwList, List<Location> locations) {
		List<Location> workingLocations = new ArrayList<>();
		
		for (Location loc : locations) {
			Location working = new Location(loc.x, loc.y, loc.z);
			workingLocations.add(working);
		}
		
		for (ThrowData t : throwList) {
			conditionOnThrow(workingLocations, t);
		}
		
		normalizeWeights(workingLocations);
		
		List<LocationResult> results = new ArrayList<>();
		ThrowData lastThrow = throwList.get(throwList.size() - 1);
		
		for (Location loc : workingLocations) {
			double expectedYaw = AngleCalculator.calculateExpectedYaw(
				lastThrow.x, lastThrow.z, loc.x, loc.z
			);
			double angleDiff = AngleCalculator.calculateAngleDifference(
				lastThrow.horizontalAngle, expectedYaw
			);
			
			if (angleDiff <= angleThreshold && loc.weight > 0) {
				double distance = AngleCalculator.calculateDistance2D(
					lastThrow.x, lastThrow.z, loc.x, loc.z
				);
				results.add(new LocationResult(loc, loc.weight, distance, angleDiff));
			}
		}
		
		results.sort((a, b) -> Double.compare(b.probability, a.probability));
		
		results.removeIf(r -> r.probability < 0.01);
		
		if (results.size() > maxResults) {
			results = new ArrayList<>(results.subList(0, maxResults));
		}
		
		return results;
	}

	private void conditionOnThrow(List<Location> locations, ThrowData t) {
		for (Location loc : locations) {
			double expectedYaw = AngleCalculator.calculateExpectedYaw(
				t.x, t.z, loc.x, loc.z
			);
			
			double angleDiff = AngleCalculator.calculateAngleDifference(
				t.horizontalAngle, expectedYaw
			);
			
			double variance = standardDeviation * standardDeviation;
			double likelihood = Math.exp(-(angleDiff * angleDiff) / (2.0 * variance));
			
			loc.weight *= likelihood;
		}
	}

	private void normalizeWeights(List<Location> locations) {
		double sum = 0.0;
		for (Location loc : locations) {
			sum += loc.weight;
		}
		
		if (sum > 0) {
			for (Location loc : locations) {
				loc.weight /= sum;
			}
		}
	}

	public List<LocationResult> calculateProbabilities(F3CData f3cData, List<Location> locations) {
		List<ThrowData> singleThrow = new ArrayList<>();
		singleThrow.add(new ThrowData(f3cData));
		return calculateProbabilities(singleThrow, locations);
	}
}
