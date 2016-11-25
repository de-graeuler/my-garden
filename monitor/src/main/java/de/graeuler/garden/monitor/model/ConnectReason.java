package de.graeuler.garden.monitor.model;

import com.tinkerforge.IPConnection;

public enum ConnectReason {
	REQUEST        (IPConnection.CONNECT_REASON_REQUEST, "user request"),
	AUTO_RECONNECT (IPConnection.CONNECT_REASON_AUTO_RECONNECT, "automatic reconnect");

	private short reason;
	private String output;

	ConnectReason(short reason, String output) {
		this.reason = reason;
		this.output = output;
	}
	public static ConnectReason by (short reason) {
		for (ConnectReason r : ConnectReason.values()) {
			if (r.reason != reason) continue;
			else return r;
		}
		return null;
	}

	public String getOutput() {
		return output;
	}
}