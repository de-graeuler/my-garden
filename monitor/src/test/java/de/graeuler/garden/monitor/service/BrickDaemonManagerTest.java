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

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.monitor.model.TFDevice;
import de.graeuler.garden.testhelpers.TestConfig;

public class BrickDaemonManagerTest {

	IPConnection connection = mock(IPConnection.class);
	DeviceListCallback deviceListCallback = mock(DeviceListCallback.class);
	TestConfig config = new TestConfig();
	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
	private BrickDaemonManager daemonManager;
	
	@Before
	public void setUp() throws Exception {
		daemonManager = new BrickDaemonManager(config, scheduler, connection);
		daemonManager.setDeviceListCallback(deviceListCallback);
	}

	@Test
	public final void testConnectDisconnect() {
		daemonManager.connect();
		try {
			TimeUnit.SECONDS.sleep(2);
			verify(connection, times(1)).connect(
					(String) AppConfig.Key.TF_DAEMON_HOST.from(config), 
					(Integer) AppConfig.Key.TF_DAEMON_PORT.from(config)
					);
			daemonManager.disconnect();
			verify(connection, times(1)).disconnect();
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
		ArgumentCaptor<TFDevice> deviceArgCaptor = ArgumentCaptor.forClass(TFDevice.class);
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
