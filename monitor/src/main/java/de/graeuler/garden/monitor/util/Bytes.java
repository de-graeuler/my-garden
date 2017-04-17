package de.graeuler.garden.monitor.util;

public enum Bytes {
	K(1), M(2), G(3);
	int power;
	Bytes(int power) {
		this.power=power;
	}
	public double getBytes(double value) {
		return value * Math.pow(1024, power);
	}
	public double convertBytes(double bytes){
		return bytes / Math.pow(1024, power);
	}
}