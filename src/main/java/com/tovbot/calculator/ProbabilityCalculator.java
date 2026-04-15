package com.tovbot.calculator;

import com.tovbot.model.F3CData;
import com.tovbot.model.Location;
import com.tovbot.model.LocationResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// adapted from ninjabrain bot's posterior.java.
public class ProbabilityCalculator {

	private final double standardDeviation;
	private final double angleThreshold;
	private final int maxResults;

	public ProbabilityCalculator(double standardDeviation, double angleThreshold, int maxResults) {
		this.standardDeviation = standardDeviation;
		this.angleThreshold = angleThreshold;
		this.maxResults = maxResults;
	}

	public List<LocationResult> calculateProbabilities(F3CData f3cData, List<Location> locations) {
		List<LocationResult> results = new ArrayList<>();
		double totalLikelihood = 0.0;

		for (Location loc : locations) {
			LocationResult result = calculateSingleProbability(f3cData, loc);
			
			if (result.angleDifference <= angleThreshold && result.probability > 0) {
				results.add(result);
				totalLikelihood += result.probability;
			}
		}

		if (totalLikelihood > 0) {
			for (LocationResult result : results) {
				result = new LocationResult(
					result.location,
					result.probability / totalLikelihood,
					result.distance,
					result.angleDifference
				);
			}
		}

		results.clear();
		for (Location loc : locations) {
			LocationResult result = calculateSingleProbability(f3cData, loc);
			
			if (result.angleDifference <= angleThreshold && result.probability > 0) {
				results.add(result);
			}
		}

		results.sort((a, b) -> Double.compare(b.probability, a.probability));

		double sum = results.stream().mapToDouble(r -> r.probability).sum();
		if (sum > 0) {
			List<LocationResult> normalized = new ArrayList<>();
			for (LocationResult r : results) {
				normalized.add(new LocationResult(
					r.location,
					r.probability / sum,
					r.distance,
					r.angleDifference
				));
			}
			results = normalized;
		}

		if (results.size() > maxResults) {
			results = results.subList(0, maxResults);
		}

		return results;
	}

	private LocationResult calculateSingleProbability(F3CData f3cData, Location location) {
		double expectedYaw = AngleCalculator.calculateExpectedYaw(
			f3cData.x, f3cData.z,
			location.x, location.z
		);

		double angleDiff = AngleCalculator.calculateAngleDifference(
			f3cData.horizontalAngle,
			expectedYaw
		);

		double distance = AngleCalculator.calculateDistance2D(
			f3cData.x, f3cData.z,
			location.x, location.z
		);

		double variance = standardDeviation * standardDeviation;
		double likelihood = Math.exp(-(angleDiff * angleDiff) / (2.0 * variance));

		return new LocationResult(location, likelihood, distance, angleDiff);
	}
}
