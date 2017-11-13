package de.graeuler.garden.monitor.sensor;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class WaterLevelSensorTest extends SensorTest {

	private static final String WATER_LVL_DISTANCE = "water-lvl-distance";
	private WaterLevelSensor waterLvlSensor;
	private BrickletDistanceUS brickletDistanceUS = mock(BrickletDistanceUS.class);

	@Before
	public void setUp() {
		super.setUp();
		waterLvlSensor = new WaterLevelSensor(config, mockCollector) {
			@Override
			protected BrickletDistanceUS constructBrick(String uid, IPConnection conn) {
				return brickletDistanceUS ;
			}
		};
		device.setDeviceClass(BrickletDistanceUS.class);
	}

	@Test
	public final void testIsAccepted() {
		try {
			when(brickletDistanceUS.getDistanceValue()).thenReturn(100);
		} catch (NotConnectedException | TimeoutException e) {
			fail("Bricklet VoltageCurrent not successfully mocked: " + e.getMessage());
		}
		assertTrue(waterLvlSensor.isAccepted(device, ipCon));
		verify(mockCollector).collect(WATER_LVL_DISTANCE, 10.0);
	}
	
	@Test
	public final void testDistanceReached() {
		waterLvlSensor.isAccepted(device, ipCon);
		waterLvlSensor.distanceReached(150);
		verify(mockCollector).collect(WATER_LVL_DISTANCE, 15.0);
	}

}
