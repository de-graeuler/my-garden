package de.graeuler.garden.testhelpers;

import com.tinkerforge.IPConnection;

public class MockIPConnection extends IPConnection {

	private boolean isConnected = false;
	
	@Override
	public short getConnectionState() {
		if (this.isConnected)
			return CONNECTION_STATE_CONNECTED;
		else
			return CONNECTION_STATE_DISCONNECTED;
	}

	public void setConnected (boolean isConnected) {
		this.isConnected = isConnected;
	}
	
}
