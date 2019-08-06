package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.interfaces.SerializableHashDelegate;
import de.graeuler.garden.monitor.util.Bytes;
import de.graeuler.garden.testhelpers.TestConfig;

public class GardenDataCollectorTest {

	private GardenDataCollector gardenDataCollector;
	private DataPersister<DataRecord> persister;
	private DataProcessor<DataRecord> processor = (Collection<DataRecord> r) -> {
		return true;
	};
	private static File dbFile;

	Logger log = LoggerFactory.getLogger(GardenDataCollectorTest.class);

	class CollectorThread extends Thread {

		private List<AtomicInteger> counters;

		public CollectorThread(List<AtomicInteger> counters) {
			this.counters = counters;
		}

		@Override
		public void run() {
			AtomicInteger inThreadCounter = new AtomicInteger(0);
			counters.add(inThreadCounter);
			final String counterName = "Counter-" + counters.indexOf(inThreadCounter);

			while (!isInterrupted()) {
				gardenDataCollector.collect(counterName, inThreadCounter.incrementAndGet());
			}
		}
	}

	class BulkWritingPersister extends DerbyDataPersister {

		private Logger log = LoggerFactory.getLogger(this.getClass());

		public BulkWritingPersister(SerializableHashDelegate hashService, AppConfig config)
				throws WrappedDatabaseException {
			super(hashService, config);
		}

		public synchronized void bulkWrite(Collection<DataRecord> records) {
			PreparedStatement insertPersistedRecords = preparedStatements.get(Statements.InsertPersistedRecord);
			try {
				log.info("Building insert batch");
				for (DataRecord record : records) {
					setInsertRecordValues(record, insertPersistedRecords);
					insertPersistedRecords.addBatch();
				}
				log.info("Executing insert batch");
				insertPersistedRecords.executeBatch();
				log.info("Batch commit");
				dbConnection.commit();
			} catch (SQLException e) {
				log.error("Database error while writing objects.", e);
			}
		}

		public long getTableSize() {
			try {
				Statement getSizeQuery = dbConnection.createStatement();
				ResultSet resultSet = getSizeQuery.executeQuery(
						"select (select sum(numallocatedpages * pagesize) from new org.apache.derby.diag.SpaceTable('APP', t.tablename) x) as size "
								+ "from SYS.SYSTABLES t where t.tablename = 'PERSISTEDRECORDS' order by size desc");
				if (! resultSet.next())
					return -1;
				return resultSet.getLong(1);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return -1;
		}

		public void shutdown() {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	@BeforeClass
	public final static void setUpBeforeClass() throws IOException {
		dbFile = File.createTempFile("GardenDataCollectorTest", ".sqlitedb");
	}

	@AfterClass
	public final static void tearDownAfterClass() {
		dbFile.deleteOnExit();
	}

	@Before
	public void setUp() throws Exception {
		try {
			TestConfig config = new TestConfig();
			config.set(ConfigurationKeys.DC_JDBC_CONNECTION, "jdbc:derby:memory:myDB;create=true");
			// config.set(ConfigurationKeys.DC_JDBC_CONNECTION, "jdbc:sqlite:memory");
			// config.set(ConfigurationKeys.DC_JDBC_CONNECTION,
			// "jdbc:sqlite:"+dbFile.getAbsolutePath());
			persister = new BulkWritingPersister(new SerializableToSha256(), config);
			this.gardenDataCollector = new GardenDataCollector(persister);
		} catch (WrappedDatabaseException e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		persister.deleteAll();
		((BulkWritingPersister) persister).shutdown();
	}

	@Test
	public final void testCollect() {
		final String expectedKey = "aString";
		final String expectedValue = "aValue";
		assertFalse(persister.iterate().hasNext());

		this.gardenDataCollector.collect(expectedKey, expectedValue);

		Collection<DataRecord> persistedValues = new ArrayList<>();
		this.persister.iterate().forEachRemaining(persistedValues::add);
		assertTrue(persistedValues.size() == 1);
		DataRecord record = persistedValues.iterator().next();
		assertEquals(expectedKey, record.getKey());
		assertEquals(expectedValue, record.getValue());
	}

	@Test
	public final void testMultiThreadWrite() throws InterruptedException {
		Collection<Thread> threads = new ArrayDeque<>();
		assertEquals(persister.countAll(), 0);
		List<AtomicInteger> counters = Collections.synchronizedList(new ArrayList<>());
		final int COLLECTOR_THREADS = 4;
		startCollectorThreads(COLLECTOR_THREADS, threads, counters);
		TimeUnit.SECONDS.sleep(5);
		interruptThreads(threads);
		long countAll = persister.countAll();
		assertEquals(countAll, sumOf(counters));
		log.info("Allrecords: {}", countAll);
	}

	@Test
	public final void testDatasetIteratorRemoval() {
		System.gc();
		logMemUsage("Start");
		final int maxRecords = 250000;
		final int blockSize = 100;
		log.info("Creating {} records", maxRecords);
		Collection<DataRecord> records = new ArrayDeque<>(maxRecords);
		for (int i = 0; i < maxRecords; i++) {
			records.add(new DataRecord("loop", i));
		}
		((BulkWritingPersister) persister).bulkWrite(records);
		logMemUsage("Dataset in DB");
		log.info("Size of DB {}", Bytes.M.format(((BulkWritingPersister) persister).getTableSize()));
		records.clear();
		records = null;

		assertEquals(persister.countAll(), maxRecords);
		System.gc();
		logMemUsage("After GC");

		DataProcessor<DataRecord> processorSpy = spy(new DataProcessor<DataRecord>() {
			@Override
			public Boolean apply(Collection<DataRecord> t) {
				return true;
			};
		});
		long count = gardenDataCollector.processCollectedRecords(processorSpy, blockSize);
		logMemUsage("After Record Processing");
		System.gc();
		log.info("Size of DB {}", Bytes.M.format(((BulkWritingPersister) persister).getTableSize()));
		logMemUsage("After GC");
		log.info("{} records processed", count);
		verify(processorSpy, times(maxRecords / blockSize)).apply(ArgumentMatchers.anyCollection());
		assertEquals(0, persister.countAll());
		assertEquals(maxRecords, count);
	}

	private void logMemUsage(String prepend) {
		Runtime rt = Runtime.getRuntime();
		long usage = rt.totalMemory() - rt.freeMemory();
		log.info("{} - Memory Usage: {}", prepend, Bytes.M.format(usage));
	}

	long counterRemover = 0;

	@Test
	public final void testMultiThreadAccess() throws InterruptedException {
		assertFalse(persister.iterate().hasNext());

		Thread remover = new Thread(() -> {
			long r = 0;
			while ((r = gardenDataCollector.processCollectedRecords(processor, 1000)) > 0) {
				counterRemover += r;
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (InterruptedException e) {
					return;
				}
			}
		});

		Queue<Thread> threads = new LinkedList<>();
		List<AtomicInteger> counters = Collections.synchronizedList(new ArrayList<>());
		final int COLLECTOR_THREADS = 16;
		startCollectorThreads(COLLECTOR_THREADS, threads, counters);
		TimeUnit.MILLISECONDS.sleep(250);
		remover.start();
		TimeUnit.SECONDS.sleep(5);
		interruptThreads(threads);
		remover.join();

		int collected = sumOf(counters);
		log.info("{} datasets collected.", collected);
		assertEquals(collected, counterRemover);
	}

	private int sumOf(List<AtomicInteger> counters) {
		return counters.parallelStream().mapToInt(AtomicInteger::intValue).sum();
	}

	private void interruptThreads(Collection<Thread> threads) throws InterruptedException {
		log.info("Interrupt/join {} collector threads", threads.size());
		for (Thread t : threads)
			t.interrupt();
		for (Thread t : threads)
			t.join();
	}

	private void startCollectorThreads(int amount, Collection<Thread> threads, List<AtomicInteger> counters) {
		log.info("Starting {} Collector Threads", amount);
		for (int i = 0; i < amount; i++) {
			Thread t = new CollectorThread(counters);
			threads.add(t);
		}
		threads.parallelStream().forEach(Thread::start);

	};

}
