package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.testhelpers.TestConfig;

public class GardenDataCollectorTest {

	private GardenDataCollector gardenDataCollector;
	private TestConfig config = new TestConfig();
	private DataPersister<DataRecord> persister;
	private DataRecordProcessor<DataRecord> processor;

	@SuppressWarnings("unchecked")
	public GardenDataCollectorTest() {
		config.set(ConfigurationKeys.DC_JDBC_CONNECTION, "jdbc:sqlite::memory:");
		processor = mock(DataRecordProcessor.class);
		persister = new MockedDataPersister();
	}

	@Before
	public void setUp() throws Exception {
		this.gardenDataCollector = new GardenDataCollector(config, persister);
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

	long counterRemover;

	@Test
	public final void testMultiThreadAccess() throws InterruptedException {
		assertFalse(persister.iterate().hasNext());

		List<AtomicInteger> counters = Collections.synchronizedList(new ArrayList<>());
		class CollectorThread extends Thread {
			@Override
			public void run() {
				AtomicInteger inThreadCounter = new AtomicInteger(0);
				counters.add(inThreadCounter);
				while (true) {
					gardenDataCollector.collect("foo", "bar");
					inThreadCounter.incrementAndGet();
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		};

		doReturn(true).when(processor).call(Mockito.any());
		counterRemover = new Integer(0);
		Thread remover = new Thread(() -> {
			long r = 0;
			while ((r = gardenDataCollector.processCollectedRecords(processor, 50)) > 0) {
				counterRemover += r;
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					return;
				}
			}
		});

		final int COLLECTOR_THREADS = 50;
		Queue<Thread> threads = new LinkedList<>();
		for(int i = 0; i < COLLECTOR_THREADS; i++) {
			Thread t = new CollectorThread();
			t.start();
			threads.add(t);
		}
		TimeUnit.MILLISECONDS.sleep(250);
		remover.start();
		TimeUnit.SECONDS.sleep(5);

		for(int i = 0; i < COLLECTOR_THREADS; i++) {
			threads.poll().interrupt();
		}
		remover.join();

		int collected = counters.stream().mapToInt(AtomicInteger::intValue).sum();
		assertEquals(collected, counterRemover);
	}

	private class MockedDataPersister implements DataPersister<DataRecord> {

		List<DataRecord> database = new CopyOnWriteArrayList<>();

		@Override
		public void write(DataRecord record) {
			database.add(record);
		}

		@Override
		public long deleteAll(Collection<DataRecord> records) {
			synchronized (database) {
				if (this.database.removeAll(records)) {
					return records.size();
				} else {
					return -1;
				}
			}
		}

		@Override
		public long deleteAll() {
			synchronized (database) {
				int size = database.size();
				database.clear();
				return size;
			}
		}

		@Override
		public long countAll() {
			return database.size();
		}

		@Override
		public Collection<DataRecord> readFirst(int _blocksize) {
			try {
				synchronized (database) {
					int lastPos = database.size();
					int firstPos = lastPos - _blocksize;
					firstPos = firstPos < 0 ? 0 : firstPos;
					return database.subList(firstPos, lastPos);
				}
			} catch (IndexOutOfBoundsException e) {
				return Collections.emptyList();
			}
		}

		@Override
		public DataIterator<DataRecord> iterate() {
			Iterator<DataRecord> i = database.iterator();
			return new DataIterator<DataRecord>() {

				@Override
				public boolean hasNext() {
					return i.hasNext();
				}

				@Override
				public DataRecord next() {
					// TODO Auto-generated method stub
					return i.next();
				}

				@Override
				public void close() {
					// do nothing.
				}
			};
		}

	};

}
