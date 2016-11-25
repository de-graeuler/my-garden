package de.graeuler.garden.monitor.model;

import com.tinkerforge.IPConnection;

public enum DisconnectReason {
	REQUEST  (IPConnection.DISCONNECT_REASON_REQUEST,  "request"),
	ERROR    (IPConnection.DISCONNECT_REASON_ERROR,    "unsolvable error"),
	SHUTDOWN (IPConnection.DISCONNECT_REASON_SHUTDOWN, "device shutdown");
	
	private short reason;
	private String output;

	DisconnectReason(short reason, String output) {
		this.reason = reason;
		this.output = output;
	}
	
	public static DisconnectReason by (short reason) {
		for (DisconnectReason r : DisconnectReason.values()) {
			if (r.reason != reason) continue;
			else return r;
		}
		return null;
	}

	public String getOutput() {
		return output;
	}
}