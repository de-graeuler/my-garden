package de.graeuler.garden.monitor.sensor;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class TemperatureSensorTest extends SensorTest {

	TemperatureSensor tempSensor;
	private BrickletTemperature brickletTemperature = mock(BrickletTemperature.class);
	
	@Before
	public void setUp() {
		super.setUp();
		tempSensor = new TemperatureSensor(config, mockCollector) {
			@Override
			protected BrickletTemperature constructBrick(String uid, IPConnection conn) {
				return brickletTemperature;
			};
		};
		try {
			when(brickletTemperature.getTemperature()).thenReturn((short) 202);
		}catch (NotConnectedException | TimeoutException e) {
			fail("Bricklet Temperature not successfully mocked: " + e.getMessage());
		}
		device.setDeviceClass(BrickletTemperature.class);
	}
	
	@Test
	public final void testTemperatureReached() {
		assertTrue(tempSensor.isAccepted(device, ipCon));
		tempSensor.temperatureReached((short)100);
		verify(mockCollector).collect("outside-temperature", 1.0);
	}

}
