package de.graeuler.garden.data;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.interfaces.SerializableHashDelegate;
import de.graeuler.garden.monitor.util.ObjectSerializationUtil;

public abstract class AbstractDataPersister implements DataPersister<DataRecord> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	protected Connection dbConnection;

	protected final SerializableHashDelegate hashService;

	private AppConfig config;

	public enum Statements {
		InsertPersistedRecord, 
		QueryPersistedRecords,
		QuerySomePersistedRecords,
		QueryCountPersistedRecords,
		DeletePersistedRecords, DeleteOnePersistedRecord, 
	}

	private EnumMap<Statements, PreparedStatement> preparedStatements = new EnumMap<>(Statements.class);

	public AbstractDataPersister(SerializableHashDelegate hashService, AppConfig config)
			throws WrappedDatabaseException {

		this.hashService = hashService;
		this.config = config;
		log.info("Database Initialization");
		this.setUpDatabaseConnection();

		this.initializeDatabase();

		prepareStatements();

		log.info("Database ready");
	}

	abstract protected void startUpEmbeddedDatabase(AppConfig config) throws WrappedDatabaseException;

	protected void configureDbConnection(Connection dbConnection) throws WrappedDatabaseException {
		try {
			dbConnection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new WrappedDatabaseException(e);
		}
	};



	@Override
	public long deleteAll() {
		try {
			PreparedStatement deletePersistedRecords = preparedStatements.get(Statements.DeletePersistedRecords);
			deletePersistedRecords.setString(1, DataRecord.class.getCanonicalName());
			int r = deletePersistedRecords.executeUpdate();
			dbConnection.commit();
			return r;
		} catch (SQLException ex) {
			log.error(ex.getMessage());
			return -1;
		}
	}

	@Override
	public long deleteAll(final Collection<DataRecord> records) {
		if (records == null) {
			return -1;
		}
		try{
			PreparedStatement deleteOnePersistedRecord = preparedStatements.get(Statements.DeleteOnePersistedRecord);
			for(DataRecord r : records) {
				StringBuffer hash = new StringBuffer();
				hashService.hash(r, hash);

				deleteOnePersistedRecord.setString(1, r.getClass().getCanonicalName());
				deleteOnePersistedRecord.setString(2, hash.toString());
				deleteOnePersistedRecord.addBatch();
			}
			int[] resultCounts = deleteOnePersistedRecord.executeBatch();
			dbConnection.commit();
			return Arrays.stream(resultCounts).filter(i -> i >= 0).sum();
		} catch ( SQLException ex) {
			log.error(ex.getMessage());
			return -1;
		}
	}

	@Override
	public synchronized void write(DataRecord r) {
		try {
			
			StringBuffer hash = new StringBuffer();
			byte[] serializedObject = hashService.hash(r, hash);
			
			PreparedStatement insertPersistedRecords = preparedStatements.get(Statements.InsertPersistedRecord);
			insertPersistedRecords.setString(1, r.getClass().getCanonicalName());
			insertPersistedRecords.setString(2, hash.toString());
			insertPersistedRecords.setBytes(3, serializedObject);
			insertPersistedRecords.execute();

			dbConnection.commit();
			
		} catch (SQLException e) {
			log.error("Database error while writing objects.", e);
		}
	}

	@Override
	public DataIterator<DataRecord> iterate() {
		
		try {
			PreparedStatement queryPersistedRecords = preparedStatements.get(Statements.QueryPersistedRecords);
			queryPersistedRecords.setFetchSize(1);
			queryPersistedRecords.setString(1, DataRecord.class.getCanonicalName());
			ResultSet rs = queryPersistedRecords.executeQuery();
			return buildDataIterator(rs);
		} catch (SQLException e) {
			log.error("Error reading objects from database.", e);
		}
		return buildEmptyDataRecordIterator();
	}
	
	private DataIterator<DataRecord> buildEmptyDataRecordIterator() {
		return new DataIterator<DataRecord>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public DataRecord next() {
				throw new NoSuchElementException();
			}

			@Override
			public void close() {
				// no resultset to close 
			}
			
		};
	}

	private DataIterator<DataRecord> buildDataIterator(ResultSet rs) {
		class DataRecordIterator extends AbstractDataRecordIterator<DataRecord> {

			public DataRecordIterator(ResultSet resultSet) {
				super(resultSet);
			}

			@Override
			protected DataRecord readObjectFromResultSet(ResultSet rs) {
				return deserializeObjectFromResultSet(rs);
			}
			
		}
		return new DataRecordIterator(rs);
	}

	@Override
	public Collection<DataRecord> readFirst(int blocksize) {
		Collection<DataRecord> records = new ArrayList<>();
		if (blocksize < 1) return records; 
		try {
			PreparedStatement queryFirstRecords = preparedStatements.get(Statements.QuerySomePersistedRecords);
			queryFirstRecords.setString(1, DataRecord.class.getCanonicalName());
			queryFirstRecords.setInt(2, blocksize);
			ResultSet rs = queryFirstRecords.executeQuery();
			deserializeResultSetToCollection(rs, records);
		} catch (SQLException | IOException e) {
			log.error("Error reading first objects from database.", e);
		}
		return records;
	}
	
	@Override
	public long countAll() {
		try {
			PreparedStatement queryCountRecords = preparedStatements.get(Statements.QueryCountPersistedRecords);
			ResultSet rs = queryCountRecords.executeQuery();
			long result = -1;
			if (rs.next() && (result = rs.getInt(1)) >= 0) {
				return result;
			}
		} catch (SQLException e) {
			log.error("Error counting objects in database.", e);
		} 
		return -1;
	}

	private void deserializeResultSetToCollection(ResultSet rs, Collection<DataRecord> records)
			throws SQLException, IOException {
		while(rs.next()) {
			DataRecord record = null;
			record = deserializeObjectFromResultSet(rs);
			if( null != record ) {
				records.add(record);
			}
		}
	}

	private DataRecord deserializeObjectFromResultSet(ResultSet rs) {
		DataRecord record = null;
		try(InputStream stream = rs.getBinaryStream(1)) {
			record = ObjectSerializationUtil.deserializeFromByteStream(stream, DataRecord.class);
		} catch (SQLException | IOException e) {
			log.error("Unable to deserialize record from result set.", e);
		}
		return record;
	}

	private void setUpDatabaseConnection() throws WrappedDatabaseException {
		startUpEmbeddedDatabase(this.config);
		try {
			this.dbConnection = DriverManager.getConnection((String) ConfigurationKeys.DC_JDBC_CONNECTION.from(config));
			configureDbConnection(this.dbConnection);
		} catch (SQLException e) {
			throw new WrappedDatabaseException(e);
		}
	}

	protected PreparedStatement prepareInsertPersistedRecordStatement() throws SQLException {
		return dbConnection.prepareStatement("INSERT INTO persistedrecords (fqcn, objecthash, object) VALUES (?, ?, ?)");
	}

	protected PreparedStatement prepareQueryPersistedRecordsStatement() throws SQLException {
		return dbConnection.prepareStatement("SELECT object FROM persistedrecords WHERE fqcn = ?");
	}
	
	protected PreparedStatement prepareQuerySomePersistedRecordsStatement() throws SQLException {
		return dbConnection.prepareStatement("SELECT object FROM persistedrecords WHERE fqcn = ? FETCH FIRST ? ROWS ONLY");
	}
	
	protected PreparedStatement prepareQueryCountPersistedRecordsStatement() throws SQLException {
		return dbConnection.prepareStatement("SELECT count(*) FROM persistedrecords");
	}

	protected PreparedStatement prepareDeletePersistedRecordsStatement() throws SQLException {
		return dbConnection.prepareStatement("DELETE FROM persistedrecords WHERE fqcn = ?");
	}

	protected PreparedStatement prepareDeleteOnePersistedRecordStatement() throws SQLException {
		return dbConnection.prepareStatement("DELETE FROM persistedrecords where fqcn = ? and objecthash = ?");
	}

	protected void prepareStatements () throws WrappedDatabaseException {
		log.debug("Peparing");
		try {
			for(Statements key : Statements.values()) {
				switch(key) { 
					case InsertPersistedRecord: preparedStatements.put(key, prepareInsertPersistedRecordStatement());
						break;
					case QueryPersistedRecords: preparedStatements.put(key, prepareQueryPersistedRecordsStatement());
						break;
					case QuerySomePersistedRecords: preparedStatements.put(key,  prepareQuerySomePersistedRecordsStatement());
						break;
					case QueryCountPersistedRecords: preparedStatements.put(key, prepareQueryCountPersistedRecordsStatement());
						break;
					case DeletePersistedRecords: preparedStatements.put(key, prepareDeletePersistedRecordsStatement());
						break;
					case DeleteOnePersistedRecord: preparedStatements.put(key, prepareDeleteOnePersistedRecordStatement());
				}
			}
		} catch (SQLException e) {
			log.error("Unable to prepare Statements.", e);
			throw new WrappedDatabaseException(e);
		}
	}

	protected void initializeDatabase() throws WrappedDatabaseException {
		log.debug("DB Setup");
		PreparedStatement stmt;
		try {
			stmt = dbConnection.prepareStatement("CREATE TABLE persistedrecords (fqcn VARCHAR(32000), objecthash VARCHAR(64) UNIQUE, object BLOB)");
			try {stmt.execute();} catch(SQLException e) {}; // should only fail if table exists.
		} catch (SQLException e) {
			throw new WrappedDatabaseException(e); 
		}
	}

}
