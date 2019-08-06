package de.graeuler.garden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.integration.AbstractIntegrationTest;

public class IntegrationTest extends AbstractIntegrationTest {


	private static Logger log = LoggerFactory.getLogger(IntegrationTest.class);

	
	@Test
	public void testSimpleValue() throws InterruptedException {
		getSensorHandlers().iterator().next().sendToCollector("simpleKey", new Double(1));
		waitForAllDataPushed(5000);
		assertEquals(1, getRequestHandler().getHandledRequests());
		assertEquals(0, dataPersisterInstance().countAll());
	}

	@Test
	public void testMultipleValues() throws InterruptedException {
		final int valuesToSentPerSensor = 50000;
		final String datakey = "key";
		simulateSensorData(valuesToSentPerSensor, datakey);
		waitForAllDataPushed(30000);
		isAllDataProcessed(valuesToSentPerSensor, datakey);
		assertEquals(0, dataPersisterInstance().countAll());
	}

	@Test
	public void testUplinkInstability() throws InterruptedException {
		final int valuesToSentPerSensor = 20000;
		final String datakey = "key";
		stopHttpServer();
		simulateSensorData(valuesToSentPerSensor, datakey);
		startHttpServer();
		TimeUnit.SECONDS.sleep(2);
		assertFalse(isAllDataProcessed(valuesToSentPerSensor, datakey));
		stopHttpServer();
		TimeUnit.SECONDS.sleep(2);
		startHttpServer();
		assertFalse(isAllDataProcessed(valuesToSentPerSensor, datakey));
		waitForAllDataPushed(10000);
		assertTrue(isAllDataProcessed(valuesToSentPerSensor, datakey));
	}

	protected boolean isAllDataProcessed(final int valuesToSentPerSensor, final String datakey) {
		return valuesToSentPerSensor * getSensorHandlers().size() == getRequestHandler().getReceivedRecords(datakey);
	}

	private void simulateSensorData(int valuesToSentPerSensor, final String datakey) {
		long starttime = System.currentTimeMillis();
		log.info("Sensor value generation started.");
		getSensorHandlers().parallelStream().forEach(handler -> 
			Stream.generate(() -> new Double(Math.random()))
				.limit(valuesToSentPerSensor)
				.forEach(d -> handler.sendToCollector(datakey,  d))
		);
		log.info("All sensor values generated in {}ms", System.currentTimeMillis() - starttime);
	}
	
	protected void waitForAllDataPushed(long millis) throws InterruptedException {
		waitForCondition(millis, this::dataPersisterInstance, persister -> persister.countAll() == 0);
	}

}
