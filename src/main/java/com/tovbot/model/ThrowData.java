package com.tovbot.model;

public class ThrowData {
	
	public final double x, y, z;
	public final double horizontalAngle;
	public final double verticalAngle;
	public final long timestamp;
	
	public ThrowData(double x, double y, double z, double horizontalAngle, double verticalAngle) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.horizontalAngle = horizontalAngle;
		this.verticalAngle = verticalAngle;
		this.timestamp = System.currentTimeMillis();
	}
	
	public ThrowData(F3CData f3c) {
		this.x = f3c.x;
		this.y = f3c.y;
		this.z = f3c.z;
		this.horizontalAngle = f3c.horizontalAngle;
		this.verticalAngle = f3c.verticalAngle;
		this.timestamp = System.currentTimeMillis();
	}
	
	public boolean isExpired(long timeoutMillis) {
		return System.currentTimeMillis() - timestamp > timeoutMillis;
	}
	
	@Override
	public String toString() {
		return String.format("ThrowData[x=%.1f, z=%.1f, yaw=%.1f]", x, z, horizontalAngle);
	}
}
