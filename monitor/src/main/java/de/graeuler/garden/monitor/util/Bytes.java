package de.graeuler.garden.monitor.util;

public enum Bytes {
	K(1, "kB"), M(2, "MB"), G(3, "GB");
	
	int power;
	String unit;
	
	Bytes(int power, String unit) {
		this.power = power;
		this.unit  = unit;
	}
	public double getBytes(double value) {
		return value * Math.pow(1024, power);
	}
	public double convertBytes(double bytes){
		return bytes / Math.pow(1024, power);
	}
	public static Bytes bestFit(double bytes) {
		Bytes result = K;
		for(Bytes b : values()){
			if (b.convertBytes(bytes) > 1)
				result = b;
			else 
				break;
		}
		return result;
	}
	public static String format(double bytes) {
		Bytes b = Bytes.bestFit(bytes);
		return String.format("%#.2f %s", b.convertBytes(bytes), b.unit);
	}

}