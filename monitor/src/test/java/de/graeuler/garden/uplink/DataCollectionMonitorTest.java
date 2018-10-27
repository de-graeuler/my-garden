package de.graeuler.garden.uplink;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.data.DataRecordProcessor;
import de.graeuler.garden.data.JsonDataConverter;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.MonitorService;
import de.graeuler.garden.testhelpers.TestConfig;

public class DataCollectionMonitorTest {

	private static final int BLOCK_SIZE = 10;
	private TestConfig config = new TestConfig();
	private DataCollector dataCollector = mock(DataCollector.class);
	private JsonDataConverter dataConverter = mock(JsonDataConverter.class);
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Uplink<String> uplink = mock(HttpUplinkService.class);
	private List<DataRecord> sample = new ArrayList<>();
	private MonitorService dataCollectionMonitor;

	@Before
	public void setUp() throws Exception {
		this.config.set(ConfigurationKeys.COLLECT_TIME_RATE, 1);
		this.config.set(ConfigurationKeys.COLLECT_TIME_UNIT, TimeUnit.SECONDS);
		this.config.set(ConfigurationKeys.COLLECT_BLOCK_SIZE, BLOCK_SIZE);
		this.sample.add(new DataRecord("key", "1234"));
		dataCollectionMonitor = new DataCollectionMonitor(this.config, this.dataCollector, this.dataConverter, this.uplink, this.scheduler);
	}
	
	@After
	public void tearDown() {
		dataCollectionMonitor.shutdown();
	}

	@Test
	public final void testEmptyDataCollector() {
		dataCollectionMonitor.monitor();
		wait(2, TimeUnit.SECONDS); // should not interact with converter or when no data was collected.
		verifyZeroInteractions(dataConverter, uplink);
	}
	
	@Test
	public final void testUploadIsCalled() {
		when(dataConverter.convert(sample)).thenReturn("1234");
		when(uplink.pushData("1234")).thenReturn(true);
		doAnswer(new Answer<Void>() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if(arguments.length>0 && arguments[0] instanceof DataRecordProcessor) {
					assertTrue(((DataRecordProcessor)arguments[0]).call(sample));
				}
				return null;
			}
			
		}).when(dataCollector).processCollectedRecords(Mockito.any(), Mockito.anyLong());
		dataCollectionMonitor.monitor();
		wait(2, TimeUnit.SECONDS);
		verify(dataConverter,atLeastOnce()).convert(sample);
		verify(uplink,atLeastOnce()).pushData("1234");
	}
	
	@Test
	public final void testUploadFails() {
		new DataCollectionMonitor(this.config, this.dataCollector, this.dataConverter, this.uplink, this.scheduler);
		when(dataConverter.convert(sample)).thenReturn("1234");
		when(uplink.pushData("1234")).thenReturn(false);
		dataCollectionMonitor.monitor();
		wait(1, TimeUnit.SECONDS);
//		verify(up,never()).removeDataset(sample);
	}

	private void wait(int timeout, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(timeout);
		} catch (InterruptedException e) {
			fail("Interrupted Testrun!");
		}
	}

}
