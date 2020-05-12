package de.graeuler.garden.monitor.tinkerforge;

import static org.junit.Assert.*;

import org.junit.Test;

import de.graeuler.garden.monitor.tinkerforge.DisconnectReason;

public class DisconnectReasonTest {

	@Test
	public final void validByReturnsReason () {
		assertEquals(DisconnectReason.ERROR, DisconnectReason.by((short) 1));
	}
	
	@Test
	public final void invalidByReturnsNull () {
		assertNull(DisconnectReason.by(Short.MAX_VALUE));
	}
	
	@Test
	public final void getAnOutput () {
		assertEquals("device shutdown", DisconnectReason.SHUTDOWN.getOutput());
	}

}
