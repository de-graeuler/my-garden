package de.graeuler.garden.monitor.sensor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.tinkerforge.BrickletAccelerometer;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;
import de.graeuler.garden.testhelpers.MockIPConnection;
import de.graeuler.garden.testhelpers.TestConfig;

public class SchedulerSensorBrickTest {

	MockIPConnection conn = new MockIPConnection();
	@SuppressWarnings("unchecked")
	private DataCollector<DataRecord> mockCollector = (DataCollector<DataRecord>) Mockito.mock(DataCollector.class); 

	private AppConfig appConfig = new TestConfig();

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private AbstractSensorHandler<BrickletTemperature> schedulerSensorBrick;  
	
	@Before
	public void setUp() throws Exception {
		schedulerSensorBrick = new AbstractSensorHandler<BrickletTemperature>(appConfig, mockCollector) {
			@Override
			protected Class<BrickletTemperature> getBrickClass() {
				return BrickletTemperature.class;
			}
			@Override
			protected void initBrick()
					throws TimeoutException, NotConnectedException {
			}
			@Override
			protected BrickletTemperature constructBrick(String uid, IPConnection conn) {
				return new BrickletTemperature(uid, conn);
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		scheduler.shutdown();
	}

	@Test
	public void testIsAccepted() {
		TinkerforgeDevice okDevice = TinkerforgeDevice.create("123", "0", 'A', new short[] {1,2,3}, new short[] {2,3,4},
				BrickletTemperature.DEVICE_IDENTIFIER, (short) 0);
		TinkerforgeDevice nokDevice = TinkerforgeDevice.create("123", "0", 'A', new short[] {1,2,3}, new short[] {2,3,4},
				BrickletAccelerometer.DEVICE_IDENTIFIER, (short) 0);
		this.conn.setConnected(false);
		assertFalse(schedulerSensorBrick.doesAccept(nokDevice, conn));
		assertFalse(schedulerSensorBrick.doesAccept(okDevice, conn));
		this.conn.setConnected(true);
		assertFalse(schedulerSensorBrick.doesAccept(nokDevice, conn));
		assertTrue(schedulerSensorBrick.doesAccept(okDevice, conn));
		assertFalse(schedulerSensorBrick.doesAccept(null, conn));
	}

	@Test
	public void testDataCollectorcall() {
		this.schedulerSensorBrick.sendToCollector("ABCD", "EFGH");
		Mockito.verify(mockCollector).collect("ABCD", "EFGH");
	}
	
}
