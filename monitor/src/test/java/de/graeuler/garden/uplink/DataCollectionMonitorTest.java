package de.graeuler.garden.uplink;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataCollector;
import de.graeuler.garden.data.DataProcessor;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.data.JsonDataConverter;
import de.graeuler.garden.monitor.service.MonitorService;
import de.graeuler.garden.testhelpers.TestConfig;

public class DataCollectionMonitorTest {

	private static final int BLOCK_SIZE = 10;
	private TestConfig config = new TestConfig();
	@SuppressWarnings("unchecked")
	private DataCollector<DataRecord> dataCollector = (DataCollector<DataRecord>) mock(DataCollector.class);
	private JsonDataConverter dataConverter = mock(JsonDataConverter.class);
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Uplink<JsonValue> uplink = mock(HttpUplinkService.class);
	private List<DataRecord> sample = new ArrayList<>();
	private MonitorService dataCollectionMonitor;
	private JsonValue jsonValue1234;

	@Before
	public void setUp() throws Exception {
		config.set(ConfigurationKeys.COLLECT_TIME_RATE, 1);
		config.set(ConfigurationKeys.COLLECT_TIME_UNIT, TimeUnit.SECONDS);
		config.set(ConfigurationKeys.COLLECT_BLOCK_SIZE, BLOCK_SIZE);
		sample.add(new DataRecord("key", "1234"));
		dataCollectionMonitor = new DataCollectionMonitor(this.config, this.dataCollector, this.dataConverter,
				this.uplink, this.scheduler);
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonValue1234 = jsonBuilder.add("v", 1234).build();
	}

	@After
	public void tearDown() {
		dataCollectionMonitor.shutdown();
	}

	@Test
	public final void testEmptyDataCollector() {
		dataCollectionMonitor.monitor();
		wait(2, TimeUnit.SECONDS); // should not interact with converter or when no data was collected.
		verifyZeroInteractions(dataConverter);
		verify(uplink).getConnectionState();
	}
	
	@Test
	public final void testUploadIsCalled() {
		when(dataConverter.convert(sample)).thenReturn(jsonValue1234);
		when(uplink.pushData(jsonValue1234)).thenReturn(true);
		when(uplink.getConnectionState()).thenReturn(UplinkConnectionState.ONLINE);
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments.length > 0 && arguments[0] instanceof DataProcessor) {
					assertTrue(((DataProcessor<DataRecord>) arguments[0]).apply(sample));
				}
				return null;
			}

		}).when(dataCollector).processCollectedRecords(Mockito.any(), Mockito.anyLong());
		dataCollectionMonitor.monitor();
		wait(2, TimeUnit.SECONDS);
		verify(dataConverter, atLeastOnce()).convert(sample);
		verify(uplink, atLeastOnce()).pushData(jsonValue1234);
	}

	@Test
	public final void testUploadFails() {
		new DataCollectionMonitor(this.config, this.dataCollector, this.dataConverter, this.uplink, this.scheduler);
		when(dataConverter.convert(sample)).thenReturn(jsonValue1234);
		when(uplink.pushData(jsonValue1234)).thenReturn(false);
		dataCollectionMonitor.monitor();
		wait(1, TimeUnit.SECONDS);
		verify(dataCollector, never()).removeDataset(sample);
	}

	private void wait(int timeout, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(timeout);
		} catch (InterruptedException e) {
			fail("Interrupted Testrun!");
		}
	}

}
