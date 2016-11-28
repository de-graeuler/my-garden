package de.graeuler.garden.monitor.sensor;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tinkerforge.BrickletAccelerometer;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.monitor.model.TFDevice;
import de.graeuler.garden.testhelpers.MockIPConnection;

public class SchedulerSensorBrickTest {

	MockIPConnection conn = new MockIPConnection();
	private static DataCollector mockCollector = new DataCollector() {
		@Override
		public void collect(String string, Object valueOf) {}
		
		@Override
		public void collect(Map<String, Object> data) {}
	};

	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static SchedulerSensorBrick<BrickletTemperature> schedulerSensorBrick;  
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		schedulerSensorBrick = new SchedulerSensorBrick<BrickletTemperature>(mockCollector, scheduler) {
			@Override
			protected Class<BrickletTemperature> getBrickClass() {
				return BrickletTemperature.class;
			}
			@Override
			protected void initBrick(TFDevice device, IPConnection conn)
					throws TimeoutException, NotConnectedException {
			}
		};
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		scheduler.shutdown();
	}

	@Before
	public void setUp() throws Exception {
		schedulerSensorBrick.setBrick(null);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsAccepted() {
		TFDevice okDevice = TFDevice.create("123", "0", 'A', new short[] {1,2,3}, new short[] {2,3,4},
				BrickletTemperature.DEVICE_IDENTIFIER, (short) 0);
		TFDevice nokDevice = TFDevice.create("123", "0", 'A', new short[] {1,2,3}, new short[] {2,3,4},
				BrickletAccelerometer.DEVICE_IDENTIFIER, (short) 0);
		this.conn.setConnected(false);
		assertFalse(schedulerSensorBrick.isAccepted(nokDevice, conn));
		assertFalse(schedulerSensorBrick.isAccepted(okDevice, conn));
		this.conn.setConnected(true);
		assertFalse(schedulerSensorBrick.isAccepted(nokDevice, conn));
		assertTrue(schedulerSensorBrick.isAccepted(okDevice, conn));
	}

	@Test
	public void testGetBrick() {
		schedulerSensorBrick.setBrick(new BrickletTemperature("123", conn));
		assertTrue(schedulerSensorBrick.getBrick() instanceof BrickletTemperature);
	}

	@Test
	public void testSetBrick() {
		BrickletTemperature in =new BrickletTemperature("1234", conn);
		schedulerSensorBrick.setBrick(in);
		BrickletTemperature out = schedulerSensorBrick.getBrick();
		assertNotNull(out);
		assertEquals(in, out);
	}

}
