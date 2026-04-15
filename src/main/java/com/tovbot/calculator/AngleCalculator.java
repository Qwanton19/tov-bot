package com.tovbot.calculator;

// adapted from ninjabrain bot.
// minecraft yaw convention: 0° = south, 90° = west, -90° = east, ±180° = north.
public class AngleCalculator {

	public static double calculateExpectedYaw(double sourceX, double sourceZ, double targetX, double targetZ) {
		double deltaX = targetX - sourceX;
		double deltaZ = targetZ - sourceZ;
		
		double expectedYaw = -180.0 / Math.PI * Math.atan2(deltaX, deltaZ);
		
		return clampToPlusMinus180Degrees(expectedYaw);
	}

	public static double calculateAngleDifference(double measured, double expected) {
		double delta = Math.abs((expected - measured) % 360.0);
		delta = Math.min(delta, 360.0 - delta);
		return delta;
	}

	public static double clampToPlusMinus180Degrees(double angleInDegrees) {
		angleInDegrees %= 360.0;
		if (angleInDegrees < -180.0) {
			angleInDegrees += 360.0;
		} else if (angleInDegrees > 180.0) {
			angleInDegrees -= 360.0;
		}
		return angleInDegrees;
	}

	public static double calculateDistance2D(double x1, double z1, double x2, double z2) {
		double dx = x2 - x1;
		double dz = z2 - z1;
		return Math.sqrt(dx * dx + dz * dz);
	}

	public static double calculateDistance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
}
