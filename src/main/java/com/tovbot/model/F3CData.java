package com.tovbot.model;

// adapted from ninjabrain bot's f3cdata.java
public class F3CData {

	public final double x, y, z, horizontalAngle, verticalAngle;
	public final String dimension;

	private F3CData(double x, double y, double z, double horizontalAngle, double verticalAngle, String dimension) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.horizontalAngle = horizontalAngle;
		this.verticalAngle = verticalAngle;
		this.dimension = dimension;
	}

	public static F3CData tryParseF3CString(String string) {
		if (string == null || !(string.startsWith("/execute in "))) {
			return null;
		}
		String[] substrings = string.split(" ");
		if (substrings.length != 11) {
			return null;
		}
		try {
			String world = substrings[2];
			String dimension = getDimensionName(world);

			double x = Double.parseDouble(substrings[6]);
			double y = Double.parseDouble(substrings[7]);
			double z = Double.parseDouble(substrings[8]);
			double horizontalAngle = Double.parseDouble(substrings[9]);
			double verticalAngle = Double.parseDouble(substrings[10]);
			
			return new F3CData(x, y, z, horizontalAngle, verticalAngle, dimension);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String getDimensionName(String world) {
		if (world.endsWith("overworld")) {
			return "overworld";
		} else if (world.endsWith("the_nether")) {
			return "the_nether";
		} else if (world.endsWith("the_end")) {
			return "the_end";
		}
		return world;
	}

	@Override
	public String toString() {
		return String.format("F3CData[x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f, dim=%s]",
			x, y, z, horizontalAngle, verticalAngle, dimension);
	}
}
