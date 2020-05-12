package de.graeuler.garden.monitor.tinkerforge;

import com.tinkerforge.IPConnection.ConnectedListener;
import com.tinkerforge.IPConnection.DisconnectedListener;
import com.tinkerforge.IPConnection.EnumerateListener;

public interface TinkerforgeSystemListeners extends EnumerateListener, ConnectedListener, DisconnectedListener {

}
