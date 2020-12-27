package de.graeuler.garden.monitor.tinkerforge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ConnectReasonTest {

	@Test
	public final void validByReturnsReason() {
		ConnectReason r = ConnectReason.by( (short) 1 );
		assertEquals(ConnectReason.AUTO_RECONNECT, r);
	}
	
	@Test
	public final void invalidByReturnsNull() {
		ConnectReason r = ConnectReason.by( (short) Short.MAX_VALUE );
		assertNull(r);
	}

	@Test
	public final void getAnOutput() {
		assertEquals("user request", ConnectReason.REQUEST.getOutput());
	}

}
