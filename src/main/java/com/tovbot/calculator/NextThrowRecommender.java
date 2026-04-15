package com.tovbot.calculator;

import com.tovbot.model.City;
import com.tovbot.model.Location;
import com.tovbot.model.LocationResult;
import com.tovbot.model.SkrCity;

import java.util.ArrayList;
import java.util.List;

// adapted from ninjabrain bot's nextthrowdirectioninformationprovider.java
public class NextThrowRecommender {

	private final double standardDeviation;
	private final double targetCertainty = 0.95;

	public NextThrowRecommender(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	/**
	 * Recommends the best two TOV cities to throw from next.
	 */
	public CityRecommendation recommendTovCities(List<LocationResult> topResults) {
		return recommendCities(topResults, City.getAllCities());
	}

	/**
	 * Recommends the best two SKR cities to throw from next.
	 */
	public CityRecommendation recommendSkrCities(List<LocationResult> topResults) {
		return recommendSkrCities(topResults, SkrCity.getAllCities());
	}

	public CityRecommendation recommendCities(List<LocationResult> topResults, List<City> cities) {
		List<Location> topLocations = new ArrayList<>();
		double cumulative = 0;
		for (LocationResult result : topResults) {
			topLocations.add(result.location);
			cumulative += result.probability;
			if (cumulative > 0.99) break;
		}
		
		List<CityCertainty> cityCertainties = new ArrayList<>();
		
		for (City city : cities) {
			double certainty = simulateNextThrow(topLocations, city.x, city.z);
			cityCertainties.add(new CityCertainty(city.name, certainty));
		}
		
		cityCertainties.sort((a, b) -> Double.compare(b.certainty, a.certainty));
		
		String best = cityCertainties.size() > 0 ? cityCertainties.get(0).name : "";
		double bestCert = cityCertainties.size() > 0 ? cityCertainties.get(0).certainty : 0;
		String second = cityCertainties.size() > 1 ? cityCertainties.get(1).name : "";
		double secondCert = cityCertainties.size() > 1 ? cityCertainties.get(1).certainty : 0;
		
		return new CityRecommendation(best, bestCert, second, secondCert);
	}

	public CityRecommendation recommendSkrCities(List<LocationResult> topResults, List<SkrCity> cities) {
		List<Location> topLocations = new ArrayList<>();
		double cumulative = 0;
		for (LocationResult result : topResults) {
			topLocations.add(result.location);
			cumulative += result.probability;
			if (cumulative > 0.99) break;
		}
		
		List<CityCertainty> cityCertainties = new ArrayList<>();
		
		for (SkrCity city : cities) {
			double certainty = simulateNextThrow(topLocations, city.x, city.z);
			cityCertainties.add(new CityCertainty(city.name, certainty));
		}
		
		cityCertainties.sort((a, b) -> Double.compare(b.certainty, a.certainty));
		
		String best = cityCertainties.size() > 0 ? cityCertainties.get(0).name : "";
		double bestCert = cityCertainties.size() > 0 ? cityCertainties.get(0).certainty : 0;
		String second = cityCertainties.size() > 1 ? cityCertainties.get(1).name : "";
		double secondCert = cityCertainties.size() > 1 ? cityCertainties.get(1).certainty : 0;
		
		return new CityRecommendation(best, bestCert, second, secondCert);
	}

	private double simulateNextThrow(List<Location> predictions, double testX, double testZ) {
		double expectedCertainty = 0;
		double totalOriginalCertainty = 0;
		
		for (Location assumedTarget : predictions) {
			double phiToTarget = Math.atan2(
				assumedTarget.x - testX,
				assumedTarget.z - testZ
			);
			
			double certaintyThatPredictionHitsTarget = 0;
			double totalCertaintyAfterThrow = 0;
			
			for (Location other : predictions) {
				if (other == assumedTarget) {
					totalCertaintyAfterThrow += assumedTarget.weight * 0.9;
					certaintyThatPredictionHitsTarget += assumedTarget.weight * 0.9;
					continue;
				}
				
				double phiToOther = Math.atan2(
					other.x - testX,
					other.z - testZ
				);
				
				double errorRad = normalizeAngle(phiToOther - phiToTarget);
				double errorDeg = Math.toDegrees(errorRad);
				
				double errorLikelihood = Math.exp(-(errorDeg * errorDeg) / (2 * standardDeviation * standardDeviation));
				
				totalCertaintyAfterThrow += other.weight * errorLikelihood;
				
				if (isNeighboring(assumedTarget, other)) {
					certaintyThatPredictionHitsTarget += other.weight * errorLikelihood;
				}
			}
			
			double newCertainty = certaintyThatPredictionHitsTarget / totalCertaintyAfterThrow;
			expectedCertainty += newCertainty * assumedTarget.weight;
			totalOriginalCertainty += assumedTarget.weight;
		}
		
		return expectedCertainty / totalOriginalCertainty;
	}

	private double normalizeAngle(double angle) {
		while (angle > Math.PI) angle -= 2 * Math.PI;
		while (angle < -Math.PI) angle += 2 * Math.PI;
		return angle;
	}

	private boolean isNeighboring(Location a, Location b) {
		double dx = a.x - b.x;
		double dz = a.z - b.z;
		return Math.sqrt(dx * dx + dz * dz) < 100;
	}

	private static class CityCertainty {
		final String name;
		final double certainty;
		
		CityCertainty(String name, double certainty) {
			this.name = name;
			this.certainty = certainty;
		}
	}

	public static class CityRecommendation {
		public final String bestCity;
		public final double bestCertainty;
		public final String secondCity;
		public final double secondCertainty;

		public CityRecommendation(String bestCity, double bestCertainty, String secondCity, double secondCertainty) {
			this.bestCity = bestCity;
			this.bestCertainty = bestCertainty;
			this.secondCity = secondCity;
			this.secondCertainty = secondCertainty;
		}
	}
}
