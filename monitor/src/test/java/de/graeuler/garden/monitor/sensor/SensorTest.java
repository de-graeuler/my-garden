package de.graeuler.garden.monitor.sensor;

import org.junit.Before;
import org.mockito.Mockito;

import de.graeuler.garden.data.GardenDataCollector;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;
import de.graeuler.garden.testhelpers.MockIPConnection;
import de.graeuler.garden.testhelpers.TestConfig;

public abstract class SensorTest {

	TestConfig config = new TestConfig();
	
	protected GardenDataCollector mockCollector = Mockito.mock(GardenDataCollector.class);
	protected TinkerforgeDevice device = new TinkerforgeDevice();
	protected MockIPConnection ipCon = new MockIPConnection();

	@Before
	public void setUp() {
		ipCon.setConnected(true);
		device.setUid("123");
	}
	
}
