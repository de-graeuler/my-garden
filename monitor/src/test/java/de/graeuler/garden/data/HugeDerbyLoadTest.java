package de.graeuler.garden.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class HugeDerbyLoadTest extends DerbyDataPersisterTest {

	@Override
	protected String getDatabaseConnectionString() {
		return "jdbc:derby:tmpHugeLoadData;create=true";
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
