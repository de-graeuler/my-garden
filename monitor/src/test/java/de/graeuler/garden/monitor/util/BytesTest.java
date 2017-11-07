package de.graeuler.garden.monitor.util;


import static org.junit.Assert.assertEquals;

import java.text.DecimalFormatSymbols;
import java.util.EnumSet;

import org.junit.Test;

public class BytesTest {

	@Test
	public final void testGetBytes() {
		double oneKByte = 1000.0;
		double oneKiByte = 1024.0;
		assertEquals(oneKByte, Bytes.K.getBytes(1), 0);
		assertEquals(oneKiByte, Bytes.Ki.getBytes(1), 0);
	}

	@Test
	public final void testConvertBytes() {
		double oneMiByteInBytes = 1024*1024;
		assertEquals(1, Bytes.Mi.convertBytes(oneMiByteInBytes), 0);
	}

	@Test
	public final void testBestFit() {
		assertEquals(Bytes.b, Bytes.bestFit(-1, Bytes.SI));
		assertEquals(Bytes.b, Bytes.bestFit(0, Bytes.SI));
		assertEquals(Bytes.b, Bytes.bestFit(1, Bytes.SI));
		assertEquals(Bytes.b, Bytes.bestFit(512, Bytes.SI));
		assertEquals(Bytes.bi, Bytes.bestFit(512, Bytes.IEC));
		assertEquals(Bytes.b, Bytes.bestFit(999, Bytes.SI));
		assertEquals(Bytes.K, Bytes.bestFit(1000, Bytes.SI));
		assertEquals(Bytes.K, Bytes.bestFit(1001, Bytes.SI));
		assertEquals(Bytes.bi, Bytes.bestFit(1023, Bytes.IEC));
		assertEquals(Bytes.Ki, Bytes.bestFit(1024, Bytes.IEC));
		assertEquals(Bytes.Y, Bytes.bestFit(Math.pow(1024, 10), Bytes.SI));
		assertEquals(Bytes.Yi, Bytes.bestFit(Math.pow(1024, 10), Bytes.IEC));
		assertEquals(Bytes.Mi, Bytes.bestFit(-2049*1048, Bytes.IEC));
		assertEquals(Bytes.M, Bytes.bestFit(-2049*1048, Bytes.SI));
		assertEquals(Bytes.b, Bytes.bestFit(1, EnumSet.of(Bytes.M)));
	}

	@Test
	public final void testFormat() {
		char ds = DecimalFormatSymbols.getInstance().getDecimalSeparator();
		assertEquals("1"+ds+"00 MiB", Bytes.format(1024*1024));
	}

}
