package de.graeuler.garden.monitor.util;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

import de.graeuler.garden.monitor.util.Bytes;
import de.graeuler.garden.monitor.util.VnStatPos;

public class VnStatPosTest {

	@Test
	public final void testReadValueFromVnStatResult() throws ParseException {
		String vnStatResult = "1;wlan0;2016-11-26;4.69 MB;424 KB;5.10 MB;0.52 kbit/s;2016-11;129.53 MB;25.61 MB;155.14 MB;0.57 kbit/s;129.53 MB;25.61 MB;155.14 MB";
		double monthTotal = VnStatPos.MONTH_TOTAL.fromVnStatResult(vnStatResult, "en");
		assertEquals(Bytes.M.getBytes(155.14), monthTotal, 0.001);
	}
	
	@Test(expected = ParseException.class)
	public final void testReadNonDoublePositionThrowsParseException() throws ParseException {
		String vnStatResult = "1;wlan0;2016-11-26;4.69 MB;424 KB;5.10 MB;0.52 kbit/s;2016-11;129.53 MB;25.61 MB;155.14 MB;0.57 kbit/s;129.53 MB;25.61 MB;155.14 MB";
		double monthTotal = VnStatPos.MONTH_TS.fromVnStatResult(vnStatResult, "en");
		assertEquals(Bytes.M.getBytes(155.14), monthTotal, 0.001);
	}

}
