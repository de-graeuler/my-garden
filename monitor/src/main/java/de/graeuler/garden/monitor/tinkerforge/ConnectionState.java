package de.graeuler.garden.monitor.tinkerforge;

import com.tinkerforge.IPConnection;

public enum ConnectionState {
	CONNECTED (IPConnection.CONNECTION_STATE_CONNECTED, "connected"),
	PENDING   (IPConnection.CONNECTION_STATE_PENDING, "pending"),
	DISCONNECTED (IPConnection.CONNECTION_STATE_DISCONNECTED, "disconnected");

	private short state;
	private String output;

	ConnectionState(short reason, String output) {
		this.state = reason;
		this.output = output;
	}

	public static ConnectionState by (short state) {
		for (ConnectionState r : ConnectionState.values()) {
			if (r.state != state) continue;
			else return r;
		}
		return null;
	}

	public String getOutput() {
		return output;
	}
}