package de.graeuler.garden.data;

import de.graeuler.garden.testhelpers.TestConfig;

public class SqliteDataPersisterTest extends AbstractDataPersisterTest<SqliteDataPersister> {

	@Override
	protected String getDatabaseConnectionString() {
		return "jdbc:sqlite::memory:";
	}

	@Override
	protected SqliteDataPersister buildDataPersisterInstance(TestConfig config) throws WrappedDatabaseException {
		return new SqliteDataPersister(new SerializableToSha256(), config);
	}

	@Override
	public void testDeletingIterator() {
		// Sqlite does not support updatable result sets.
	}
}
