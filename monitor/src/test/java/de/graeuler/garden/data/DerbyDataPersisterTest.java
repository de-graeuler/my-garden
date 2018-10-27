package de.graeuler.garden.data;

import de.graeuler.garden.testhelpers.TestConfig;

public class DerbyDataPersisterTest extends AbstractDataPersisterTest<DerbyDataPersister> {

	@Override
	protected String getDatabaseConnectionString() {
		return "jdbc:derby:memory:data;create=true";
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
	
}
