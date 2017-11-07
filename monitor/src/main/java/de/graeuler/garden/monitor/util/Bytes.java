package de.graeuler.garden.monitor.util;

import java.util.EnumSet;

/**
 * The Bytes enum provides simple byte formatting and calculation operations. 
 * Bytes provide both SI and IEC (default) based conversion.
 * This  
 * 
 * @author bernhard
 *
 */
public enum Bytes {
	// SI 
	b(0, 1000,    "B"),  K(1, 1000,  "kB"),  M(2, 1000,  "MB"), 
	G(3, 1000,   "GB"),  T(4, 1000,  "TB"),  P(5, 1000,  "PB"),
	E(6, 1000,   "EB"),  Z(7, 1000,  "ZB"),  Y(8, 1000,  "YB"),

	// IEC
	bi(0, 1024,   "B"), Ki(1, 1024, "KiB"), Mi(2, 1024, "MiB"), 
	Gi(3, 1024, "GiB"), Ti(4, 1024, "TiB"), Pi(5, 1024, "PiB"),
	Ei(6, 1024, "EiB"), Zi(7, 1024, "ZiB"), Yi(8, 1024, "YiB");

	/**
	 * Convenience value according to SI spec (1000 bytes = 1kB) 
	 */
	public static EnumSet<Bytes> SI = EnumSet.range(b, Y); 
	
	/**
	 * Convenience value according to IEC spec (1024 bytes = 1KiB)
	 */
	public static EnumSet<Bytes> IEC = EnumSet.range(bi, Yi); 
	
	private int power;
	private int factor;
	private String unit;
	
	Bytes(int power, int factor, String unit) {
		this.power = power;
		this.factor = factor;
		this.unit  = unit;
	}
	
	/**
	 * Calculates the amount of bytes for the given value. 
	 * For example Bytes.K.getBytes(2) would return 2048, as that's 2kB 
	 * @param value 
	 * @return 
	 */
	public double getBytes(double value) {
		return value * Math.pow(factor, power);
	}
	
	/**
	 * Devides the given bytes to this enums unit.
	 * For example Bytes.Ki.convertBytes(2048) would return 2. 
	 * @param bytes
	 * @return
	 */
	public double convertBytes(double bytes){
		return bytes / Math.pow(factor, power);
	}
	
	/**
	 * Find the best scaled Bytes instance.
	 * 
	 * @param bytes Used to filter the given 
	 * @param set Take Bytes.SI or Bytes.IEC here, or build oyur own.
	 * @return A Bytes instance. Bytes.b by default, if set does not contain a better match.
	 */
	public static Bytes bestFit(double bytes, EnumSet<Bytes> set) {
		Bytes result = b;
		for(Bytes b : set){
			if (b.convertBytes(Math.abs(bytes)) >= 1)
				result = b;
			else 
				break;
		}
		return result;
	}
	
	/**
	 * Converts the best fitting IEC Byte converted to a string. 
	 * Example: Bytes.format(1024) == 1.00KiB (decimal separator depending on system locale)
	 * @param bytes Amount of bytes.
	 * @return A nicely looking String value presentation.
	 */
	public static String format(double bytes) {
		Bytes b = Bytes.bestFit(bytes, IEC);
		return String.format("%#.2f %s", b.convertBytes(bytes), b.unit);
	}
	

}