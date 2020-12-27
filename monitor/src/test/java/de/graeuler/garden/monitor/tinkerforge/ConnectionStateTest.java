package de.graeuler.garden.monitor.tinkerforge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.tinkerforge.IPConnection;

public class ConnectionStateTest {

	@Test
	public void testExistingByCall() {
		assertEquals(ConnectionState.CONNECTED, ConnectionState.by(IPConnection.CONNECTION_STATE_CONNECTED));
		assertEquals(ConnectionState.PENDING, ConnectionState.by(IPConnection.CONNECTION_STATE_PENDING));
		assertEquals(ConnectionState.DISCONNECTED, ConnectionState.by(IPConnection.CONNECTION_STATE_DISCONNECTED));
	}
	
	@Test
	public void testNonExistingByCall() {
		assertNull(ConnectionState.by(Short.MAX_VALUE));
	}

	@Test
	public void testGetOutput() {
		assertEquals ("connected", ConnectionState.CONNECTED.getOutput());
	}

}
