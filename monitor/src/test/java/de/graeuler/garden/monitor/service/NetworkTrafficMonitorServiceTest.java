package de.graeuler.garden.monitor.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.monitor.util.Bytes;
import de.graeuler.garden.monitor.util.CommandLineReader;
import de.graeuler.garden.testhelpers.TestConfig;

/**
 * @author bernhard
 *
 */
public class NetworkTrafficMonitorServiceTest {

	private TestConfig config;
	@SuppressWarnings("unchecked")
	private DataCollector<DataRecord> dataCollector = (DataCollector<DataRecord>)mock(DataCollector.class);
	private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
	private NetworkTrafficMonitorService testee;
	private CommandLineReader commandLineReader = mock(CommandLineReader.class);

	@Before
	public void setUp() throws Exception {
		config = new TestConfig();
	}

	@Test
	public final void testReadTrafficBytes() throws InterruptedException, IOException {
		config.set(ConfigurationKeys.NET_TIME_UNIT, TimeUnit.SECONDS);
		config.set(ConfigurationKeys.NET_TIME_RATE, 1);
		when(commandLineReader.readFromCommand(anyList())).thenReturn(
				"1;wlan0;2016-11-26;4.69 MB;424 KB;5.10 MB;0.52 kbit/s;2016-11;129.53 MB;25.61 MB;154.14 MB;0.57 kbit/s;129.53 MB;25.61 MB;155.14 MB",
				"1;wlan0;2016-11-26;4.69 MB;424 KB;5.10 MB;0.52 kbit/s;2016-11;129.53 MB;25.61 MB;155.14 MB;0.57 kbit/s;129.53 MB;25.61 MB;155.14 MB"
				);
		testee = new NetworkTrafficMonitorService(config, dataCollector, scheduler, commandLineReader);
		testee.monitor();
		TimeUnit.SECONDS.sleep(2);
		testee.shutdown();
		verify(dataCollector).collect("month-total-traffic", Bytes.M.getBytes(155.14));
	}

}
