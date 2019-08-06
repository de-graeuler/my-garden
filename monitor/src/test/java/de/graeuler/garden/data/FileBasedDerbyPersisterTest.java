package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.graeuler.garden.testhelpers.DataRecordBuilder;
import de.graeuler.garden.testhelpers.TestConfig;

public class FileBasedDerbyPersisterTest extends AbstractDataPersisterTest<DerbyDataPersister> {

	@Override
	protected String getDatabaseConnectionString() {
		return "jdbc:derby:testdata;create=true";
	}

	@Override
	protected DerbyDataPersister buildDataPersisterInstance(TestConfig config) throws WrappedDatabaseException {
		return new DerbyDataPersister(new SerializableToSha256(), config);
	}

	@Override
	public void tearDown() {
		super.tearDown();
		dataPersister.shutdown();
	}

	@Test
	public final void testBigLoadHandling() {

		final int LOAD_SIZE = 100000;
		final int blockSize = 500;

		dataPersister.deleteAll();
		DataRecordBuilder.stream(LOAD_SIZE).parallel().forEach(dataPersister::write);
		long inserted = dataPersister.countAll();
		// assertEquals(LOAD_SIZE, );
		List<DataRecord> recordBlock = new ArrayList<>();
		try (DataIterator<DataRecord> recordIterator = dataPersister.iterate()) {
			long result = 0;
			while (recordIterator.hasNext()) {
				long i = blockSize;
				while (i-- > 0 && recordIterator.hasNext()) {
					recordBlock.add(recordIterator.next());
				}
				result += dataPersister.deleteAll(recordBlock);
				recordBlock.clear();
			}
			assertEquals(inserted, result);
		}
	}

	@Test
	public final void testHugeLoad() {
		final int LOAD_SIZE = 100000;
		dataPersister.deleteAll();
		prefillDatabase(LOAD_SIZE);
		try(DataIterator<DataRecord> dataIterator = dataPersister.iterate()) {;
			Serializable s;
			while(dataIterator.hasNext()) {
				assertNotNull(s = dataIterator.next().getKey());
				assertTrue("even".equals(s) || "odd".equals(s));
				dataIterator.remove();
			}
		}
	}

}
