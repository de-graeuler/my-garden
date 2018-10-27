package de.graeuler.garden.monitor.sensor;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.tinkerforge.BrickletVoltageCurrent;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.config.ConfigurationKeys;

public class VoltageCurrentSensorTest extends SensorTest{

	VoltageCurrentSensor vcSensor;
	private BrickletVoltageCurrent brickletVoltageCurrent = mock(BrickletVoltageCurrent.class);

	@Before
	public void setUp() {
		super.setUp();
		config.set(ConfigurationKeys.CURRENT_CHG_THD, 100);
		config.set(ConfigurationKeys.VOLTAGE_CHG_THD, 100);
		vcSensor = new VoltageCurrentSensor(config, mockCollector) {
			@Override
			protected BrickletVoltageCurrent constructBrick(String uid, IPConnection conn) {
				return brickletVoltageCurrent;
			}
		};
		try {
			when(brickletVoltageCurrent.getCurrent()).thenReturn(5000);
			when(brickletVoltageCurrent.getVoltage()).thenReturn(5000);
		} catch (NotConnectedException | TimeoutException e) {
			fail("Bricklet VoltageCurrent not successfully mocked: " + e.getMessage());
		}
		device.setDeviceClass(BrickletVoltageCurrent.class);
	}
	
	@Test
	public final void testCurrentReached() {
		assertTrue(vcSensor.doesAccept(device, ipCon));
		vcSensor.currentReached(5001);
		verify(mockCollector).collect("current", 5.001);
	}

	@Test
	public final void testVoltageReached() {
		assertTrue(vcSensor.doesAccept(device, ipCon));
		vcSensor.voltageReached(5251);
		verify(mockCollector).collect("voltage", 5.251);
	}

}
