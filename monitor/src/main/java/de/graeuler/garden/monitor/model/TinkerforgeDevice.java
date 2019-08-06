package de.graeuler.garden.monitor.model;

import java.util.EnumMap;

import com.tinkerforge.Device;
import com.tinkerforge.DeviceFactory;
import com.tinkerforge.IPConnection;

public class TinkerforgeDevice {

	enum Version {
		MAJOR,MINOR,RELEASE;
	};
	
	enum State {
		AVAILABLE (IPConnection.ENUMERATION_TYPE_AVAILABLE),
		CONNECTED (IPConnection.ENUMERATION_TYPE_CONNECTED),
		DISCONNECTED (IPConnection.ENUMERATION_TYPE_DISCONNECTED);
		
		private short state;

		private State(short state) {
			this.state=state;
		}
		
		public static State of(short state) {
			for(State s : State.values()) {
				if (s.state != state) continue;
				else return s;
			}
			return null;
		}
	};
	
	String uid;
	String connectedTo;
	char position;
	EnumMap<Version, Short> hardwareVersion = new EnumMap<Version, Short>(Version.class);
	EnumMap<Version, Short> firmwareVersion = new EnumMap<Version, Short>(Version.class);
	Class<? extends Device> deviceClass; 
	State state;	
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getConnectedTo() {
		return connectedTo;
	}

	public void setConnectedTo(String connectedTo) {
		this.connectedTo = connectedTo;
	}

	public char getPosition() {
		return position;
	}

	public void setPosition(char position) {
		this.position = position;
	}

	public EnumMap<Version, Short> getHwv() {
		return hardwareVersion;
	}

	public void setHwv(Version version, short value) {
		this.hardwareVersion.put(version, value);
	}

	public EnumMap<Version, Short> getFwv() {
		return firmwareVersion;
	}

	public void setFwv(Version version, Short value) {
		this.firmwareVersion.put(version, value);
	}

	public Class<? extends Device> getDeviceClass() {
		return deviceClass;
	}

	public void setDeviceClass(Class<? extends Device> deviceClass) {
		this.deviceClass = deviceClass;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public static TinkerforgeDevice create(String uid, String connectedUid,char position, 
			short[] hwv, short[] fwv, int deviceIdentifier, 
			short enumerationType) {
		TinkerforgeDevice dev = new TinkerforgeDevice();
		dev.setUid(uid);
		dev.setConnectedTo(connectedUid);
		dev.setPosition(position);
		if (null != hwv) {
			dev.setHwv(Version.MAJOR, hwv[Version.MAJOR.ordinal()]);
			dev.setHwv(Version.MINOR, hwv[Version.MINOR.ordinal()]);
			dev.setHwv(Version.RELEASE, hwv[Version.RELEASE.ordinal()]);
		}
		if (null != fwv) {
			dev.setFwv(Version.MAJOR, fwv[Version.MAJOR.ordinal()]);
			dev.setFwv(Version.MINOR, fwv[Version.MINOR.ordinal()]);
			dev.setFwv(Version.RELEASE, fwv[Version.RELEASE.ordinal()]);
		}
		dev.setState(State.of(enumerationType));
		try {
			dev.setDeviceClass(DeviceFactory.getDeviceClass(deviceIdentifier));
		} 
		catch (IllegalArgumentException e) {
			return null;
		}
		return dev;
	}

	public boolean classIsA(Class<? extends Device> deviceClass) {
		return this.getDeviceClass().equals(deviceClass);
	}
}
