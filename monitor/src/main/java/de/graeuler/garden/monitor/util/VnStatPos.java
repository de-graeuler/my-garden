package de.graeuler.garden.monitor.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bernhard
 * 
 * vnstat --oneline returns an output like this:
 * 1;wlan0;2016-11-26;4.69 MB;424 KB;5.10 MB;0.52 kbit/s;2016-11;129.53 MB;25.61 MB;155.14 MB;0.57 kbit/s;129.53 MB;25.61 MB;155.14 MB
 * 
 * This enum defines a key for each result value.
 */
public enum VnStatPos {

	VERSION, INTERFACE,
	TODAY_TS, TODAY_RX, TODAY_TX, TODAY_TOTAL, TODAY_AVG,
	MONTH_TS, MONTH_RX, MONTH_TX, MONTH_TOTAL, MONTH_AVG,
	ALL_TS,   ALL_RX,   ALL_TX,   ALL_TOTAL;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public double fromVnStatResult(String vnStatOneLineResult, String vnStatLanguageTag) throws ParseException {
		if (null != vnStatOneLineResult && vnStatOneLineResult.split(";").length + 1 >= VnStatPos.values().length) { //
			String[] vnStatResult = vnStatOneLineResult.split(";");
			String requestedVnStatResult = vnStatResult[ordinal()];
			String[] resultValueParts = requestedVnStatResult.split("\\s");
			// resultValueParts now should contain two entries: the traffic consumption and the unit. 
			// In the above example: {"155.14", "MB"}
			if (resultValueParts.length != 2)
				throw new ParseException("Unable to split vnstat result value.", ordinal()); 
			String unit = Character.toString(resultValueParts[1].charAt(0));
			NumberFormat decFormat = DecimalFormat.getNumberInstance(Locale.forLanguageTag(vnStatLanguageTag));
			double bytes = Bytes.valueOf(unit).getBytes(decFormat.parse(resultValueParts[0]).doubleValue());
			log.debug("{} transferred on {}.", Bytes.formatSI(bytes), vnStatResult[1]);
			return bytes;
		} else {
			log.error("Unable to read result of process execution for {}", vnStatOneLineResult);
		}
		return -1;
	}
}