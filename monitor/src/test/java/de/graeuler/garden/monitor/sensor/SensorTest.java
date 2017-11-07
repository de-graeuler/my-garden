package de.graeuler.garden.monitor.sensor;

import org.junit.Before;
import org.mockito.Mockito;

import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.monitor.model.TFDevice;
import de.graeuler.garden.testhelpers.MockIPConnection;
import de.graeuler.garden.testhelpers.TestConfig;

public class SensorTest {

	TestConfig config = new TestConfig();
	
	protected DataCollector mockCollector = Mockito.mock(DataCollector.class);
	protected TFDevice device = new TFDevice();
	protected MockIPConnection ipCon = new MockIPConnection();

	@Before
	public void setUp() {
		ipCon.setConnected(true);
		device.setUid("123");
	}
	
}
