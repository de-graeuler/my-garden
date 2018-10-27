package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.testhelpers.TestConfig;

public abstract class AbstractDataPersisterTest<T extends AbstractDataPersister> {

	private static final int PREFILL_LIMIT = 1000;
	private TestConfig config = new TestConfig();
	protected T dataPersister;
	
	protected abstract String getDatabaseConnectionString();

	@Before
	public void setUp() {
		config.set(ConfigurationKeys.DC_JDBC_CONNECTION, getDatabaseConnectionString());
		startupDatabase();
		prefillDatabase();
	}

	@After
	public void tearDown() {
		dataPersister.deleteAll();
	}
	
	@Test
	public final void testDeleteAll() {
		long allRecords = dataPersister.countAll();
		assertTrue(allRecords > 0); // test should fail if not prepared correctly.
		long deleteAllResult = dataPersister.deleteAll();
		assertEquals(allRecords, deleteAllResult);
		allRecords = dataPersister.countAll();
		assertTrue(allRecords == 0);
	}

	@Test
	public final void testDeleteAllCollectionOfDataRecordOfSerializable() {
		long allRecords = dataPersister.countAll();
		assertTrue(allRecords > 0); // test should fail if not prepared correctly.
		Iterator<DataRecord> iterator = dataPersister.iterate();
		List<DataRecord> evenRecords = new ArrayList<>();
		iterator.forEachRemaining(r -> {if ("even".equals(r.getKey())) evenRecords.add(r);});
		long deleteAllOfList = dataPersister.deleteAll(evenRecords);
		assertEquals(evenRecords.size(), deleteAllOfList);
	}

	@Test
	public final void testWrite() {
		assertTrue(dataPersister.deleteAll() > 0);
		assertFalse(dataPersister.iterate().hasNext());
		DataRecord record = new DataRecord("test", "1234");
		dataPersister.write(record);
		Iterator<DataRecord> iterator = dataPersister.iterate();
		assertEquals("1234", iterator.next().getValue());
		assertFalse(iterator.hasNext());
	}

	@Test
	public final void testCount() {
		assertEquals(PREFILL_LIMIT, dataPersister.countAll());
	}
	
	@Test
	public final void testReadFirst() {
		Collection<DataRecord> collection = dataPersister.readFirst(50);
		assertNotNull(collection);
		assertEquals(50, collection.size());
	}

	@Test
	public void testDeletingIterator() {
		DataIterator<DataRecord> dataIterator = dataPersister.iterate();
		while(dataIterator.hasNext()) {
			assertNotNull(dataIterator.next());
			dataIterator.remove();
		}
		assertEquals(0, dataPersister.countAll());
	}
	
	private void startupDatabase() {
		try {
			dataPersister = buildDataPersisterInstance(config);
		} catch (WrappedDatabaseException e) {
			fail(e.getMessage());
		}
	}

	abstract protected T buildDataPersisterInstance(TestConfig config) throws WrappedDatabaseException;

	protected void prefillDatabase() {
		prefillDatabase(PREFILL_LIMIT);
	}
	
	protected void prefillDatabase(int loadSize) {
		String keyEven = "even";
		String keyOdd = "odd";
		String key;
		for (int i = 0; i < loadSize; i++) {
			key = (i % 2 == 0) ? keyEven : keyOdd; 
			dataPersister.write(new DataRecord(key, Integer.valueOf(i)));
		}
	}

}
