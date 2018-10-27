package de.graeuler.garden.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.SerializableHashDelegate;

public class SqliteDataPersister extends AbstractDataPersister {

	public SqliteDataPersister(SerializableHashDelegate hashService, AppConfig config)
			throws WrappedDatabaseException {
		super(hashService, config);
	}

	@Override
	protected void startUpEmbeddedDatabase(AppConfig config) throws WrappedDatabaseException {
		// nothing to do here for SQLite
	}
	
	@Override
	protected PreparedStatement prepareQuerySomePersistedRecordsStatement() throws SQLException {
		return dbConnection.prepareStatement("SELECT object FROM persistedrecords WHERE fqcn = ? ORDER BY ROWID ASC LIMIT ?");
	}

}
