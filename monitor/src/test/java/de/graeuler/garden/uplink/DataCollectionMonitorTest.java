package de.graeuler.garden.uplink;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.data.JsonDataConverter;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.testhelpers.TestConfig;

public class DataUploaderTest {

	private TestConfig config = new TestConfig();
	private DataCollector dataCollector = mock(DataCollector.class);
	private JsonDataConverter dataconverter = mock(JsonDataConverter.class);
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Uplink<String> uplink = mock(HttpUplinkService.class);
	private List<DataRecord<?>> sample = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		this.config.set(AppConfig.Key.COLLECT_TIME_RATE, 1);
		this.config.set(AppConfig.Key.COLLECT_TIME_UNIT, TimeUnit.SECONDS);
		this.sample.add(new DataRecord<Serializable>("key", "1234"));
	}

	@Test
	public final void testEmptyDataCollector() {
		when(dataCollector.dataIsEmpty()).thenReturn(true);
		new DataCollectionMonitor(this.config, this.dataCollector, this.dataconverter, this.uplink, this.scheduler);
		wait(2, TimeUnit.SECONDS);
		verifyZeroInteractions(dataconverter, uplink);
	}
	
	@Test
	public final void testUploadIsCalled() {
		when(dataCollector.dataIsEmpty()).thenReturn(false,true);
		when(dataCollector.getCollectedDataset()).thenReturn(sample);
		when(dataconverter.convert(sample)).thenReturn("1234");
		when(uplink.pushData("1234")).thenReturn(true);
		new DataCollectionMonitor(this.config, this.dataCollector, this.dataconverter, this.uplink, this.scheduler);
		wait(1, TimeUnit.SECONDS);
		verify(dataconverter).convert(sample);
		verify(dataCollector).removeDataset(sample);
	}
	
	@Test
	public final void testUploadFails() {
		new DataCollectionMonitor(this.config, this.dataCollector, this.dataconverter, this.uplink, this.scheduler);
		when(dataCollector.dataIsEmpty()).thenReturn(false,true);
		when(uplink.pushData("1234")).thenReturn(false);
		wait(1, TimeUnit.SECONDS);
		verify(dataCollector,never()).removeDataset(sample);
	}

	private void wait(int timeout, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(timeout);
		} catch (InterruptedException e) {
			fail("Interrupted Testrun!");
		}
	}

}
