package de.graeuler.garden.monitor.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;

import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;
import de.graeuler.garden.testhelpers.TestConfig;

public class BrickDaemonFacadeTest {

	IPConnection connection = mock(IPConnection.class);
	NewDeviceCallback deviceListCallback = mock(NewDeviceCallback.class);
	TestConfig config = new TestConfig();
	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
	private BrickDaemonFacade daemonManager;
	
	@Before
	public void setup() throws Exception {
		daemonManager = new BrickDaemonFacade(config, scheduler, connection);
		daemonManager.setNewDeviceCallback(deviceListCallback);
	}

	@Test
	public final void testConnectDisconnect() {
		daemonManager.connect();
		try {
			TimeUnit.SECONDS.sleep(2);
			verify(connection, times(1)).connect(
					(String) ConfigurationKeys.TF_DAEMON_HOST.from(config), 
					(Integer) ConfigurationKeys.TF_DAEMON_PORT.from(config)
					);
			daemonManager.disconnect();
			verify(connection, times(1)).close();
		} catch (Exception e) {
			// connect and disconnect are mocked and should not fail...
			fail(e.getMessage());
		} 
	}

	@Test
	public final void testEnumerate() {
		short[] hwv = {1, 2, 3};
		short[] fwv = hwv;
		daemonManager.enumerate("123", "234", '2', hwv, fwv, 15, IPConnection.ENUMERATION_TYPE_CONNECTED);
		ArgumentCaptor<TinkerforgeDevice> deviceArgCaptor = ArgumentCaptor.forClass(TinkerforgeDevice.class);
		verify(deviceListCallback).onDeviceFound(deviceArgCaptor.capture(), eq(connection));
		assertEquals("123", deviceArgCaptor.getValue().getUid());
	}
	
	@Test
	public final void testEnumerateDisconnectedDevice() {
		short[] hwv = {1, 2, 3};
		short[] fwv = hwv;
		daemonManager.enumerate("123", "234", '2', hwv, fwv, 15, IPConnection.ENUMERATION_TYPE_DISCONNECTED);
		verifyZeroInteractions(deviceListCallback);
	}

	@Test
	public final void testConnected() {
		daemonManager.connected(IPConnection.CONNECT_REASON_REQUEST);
		try {
			verify(connection, times(1)).enumerate();
		} catch (NotConnectedException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public final void testDisconnected() {
		daemonManager.disconnected(IPConnection.DISCONNECT_REASON_REQUEST);
		verify(deviceListCallback).reset();
	}

}
