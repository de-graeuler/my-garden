package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.StreamToSha256;
import de.graeuler.garden.testhelpers.TestConfig;

public class DerbyDataPersisterTest {

	TestConfig config = new TestConfig();
	private DerbyDataPersister dataPersister;
	
	@Before
	public void setUp() {
		config.set(AppConfig.Key.DC_JDBC_DERBY_URL, "jdbc:derby:memory:data;create=true");
		startupDatabase();
		prefillDatabase();
	}

	@After
	public void tearDown() {
		dataPersister.deleteAll();
		dataPersister.shutdown();
	}
	
	@Test
	public final void testDeleteAll() {
		Collection<DataRecord<Serializable>> allRecords = dataPersister.readAll();
		assertTrue(allRecords.size() > 0); // test should fail if not prepared correctly.
		int deleteAllResult = dataPersister.deleteAll();
		assertEquals(allRecords.size(), deleteAllResult);
		allRecords = dataPersister.readAll();
		assertTrue(allRecords.size() == 0);
	}

	@Test
	public final void testDeleteAllCollectionOfDataRecordOfSerializable() {
		Collection<DataRecord<Serializable>> allRecords = dataPersister.readAll();
		assertTrue(allRecords.size() > 0); // test should fail if not prepared correctly.
		List<DataRecord<Serializable>> evenRecords = allRecords.stream()
				.filter(r -> "even".equals(r.getKey()))
				.collect(Collectors.toList());
		int deleteAllOfList = dataPersister.deleteAll(evenRecords);
		assertEquals(evenRecords.size(), deleteAllOfList);
	}

	@Test
	public final void testWrite() {
		assertTrue(dataPersister.deleteAll() > 0);
		assertThat(dataPersister.readAll(), IsEmptyCollection.empty());
		DataRecord<Serializable> record = new DataRecord<Serializable>("test", "1234");
		dataPersister.write(record);
		Collection<DataRecord<Serializable>> all = dataPersister.readAll();
		assertTrue(all.size() == 1);
		for (DataRecord<Serializable> r : all) {
			assertEquals("1234", r.getValue());
		}
	}

	@Test
	public final void testReadAll() {
		Collection<DataRecord<Serializable>> all = dataPersister.readAll();
		assertTrue(all.size() > 1);
		dataPersister.deleteAll(all);
		all = dataPersister.readAll();
		assertTrue(all.isEmpty());
	}

	private void startupDatabase() {
		try {
			dataPersister = new DerbyDataPersister(new StreamToSha256(), config);
		} catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
			fail(e.getMessage());
		}
	}
	
	private void prefillDatabase() {
		String keyEven = "even";
		String keyOdd = "odd";
		String key;
		for (int i = 0; i < 1000; i++) {
			key = (i % 2 == 0) ? keyEven : keyOdd; 
			dataPersister.write(new DataRecord<Serializable>(key, Integer.valueOf(i)));
		}
	}

}
