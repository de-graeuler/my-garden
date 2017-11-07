/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.graeuler.garden.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.RecordHashDelegate;
import de.graeuler.garden.monitor.util.ObjectSerializationUtil;

/**
 *
 * @author media
 */
public class DerbyDataPersister implements DataPersister<DataRecord<Serializable>> {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private Connection dbConnection;

	private final PreparedStatement insertPersistedRecords;
	private final PreparedStatement queryPersistedRecords;
	private final PreparedStatement deletePersistedRecords;
	private final PreparedStatement deleteOnePersistedRecord;

	private ObjectSerializationUtil oSerial = new ObjectSerializationUtil();
	private final RecordHashDelegate<InputStream> hashService;

	@Inject
	DerbyDataPersister(RecordHashDelegate<InputStream> hashService, AppConfig config) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

		this.hashService = hashService;
		
		log.debug("Derby Initialization");
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		
		this.dbConnection = DriverManager.getConnection((String)AppConfig.Key.DC_JDBC_DERBY_URL.from(config));
		dbConnection.setAutoCommit(false);

		log.debug("DB Setup");
		PreparedStatement stmt = dbConnection.prepareStatement("CREATE TABLE persistedrecords (fqcn VARCHAR(32000), objecthash VARCHAR(64), object BLOB)");
		try {stmt.execute();} catch(SQLException e) {}; // should only fail if table exists.

		log.debug("Peparing");
		insertPersistedRecords = dbConnection.prepareStatement("INSERT INTO persistedrecords (fqcn, objecthash, object) values (?, ?, ?)");
		queryPersistedRecords  = dbConnection.prepareStatement("SELECT object FROM persistedrecords where fqcn = ?");
		deletePersistedRecords = dbConnection.prepareStatement("DELETE FROM persistedrecords where fqcn = ?");
		deleteOnePersistedRecord = dbConnection.prepareStatement("DELETE FROM persistedrecords where fqcn = ? and objecthash = ?");
		
		log.info("Database ready");
	}
	
	public void shutdown() {
		try {
			dbConnection.close();
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if ("XJ015".equals(e.getSQLState())) // is thrown by Derby DB on system shutdown.
				log.info("DB shutdown successfully");
			else
				log.error(e.getMessage());
		}	
	}

	@Override
	public int deleteAll() {
		try {
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
	public int deleteAll(final Collection<DataRecord<Serializable>> records) {
		try{
			for(DataRecord<Serializable> r : records) {
				InputStream stream = oSerial.serializeToByteStream(r);
				if( stream == null ) {
					return -1;
				}
				deleteOnePersistedRecord.setString(1, r.getClass().getCanonicalName());
				deleteOnePersistedRecord.setString(2, hashService.hash(stream));
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
	public void write(DataRecord<Serializable> r) {
		try {
			InputStream stream = oSerial.serializeToByteStream(r);
			
			if( stream == null ) 
				return;

			String hash;
			if (stream.markSupported()) {
				stream.mark(Integer.MAX_VALUE); 
				hash = hashService.hash(stream);
				stream.reset();
			} else {
				InputStream streamForHash = oSerial.serializeToByteStream(r);
				hash = hashService.hash(streamForHash);
			}
			// else read second stream... or read stream a second time!
			
			insertPersistedRecords.setString(1, r.getClass().getCanonicalName());
			insertPersistedRecords.setString(2, hash);
			insertPersistedRecords.setBinaryStream(3, stream);
			insertPersistedRecords.execute();

			dbConnection.commit();
			
		} catch (SQLException e) {
			log.error("Database error while writing objects.", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Unable to reset ByteStream for db insert after hashing.", e);
		}
	}

	@Override
	public Collection<DataRecord<Serializable>> readAll() {
		
		Collection<DataRecord<Serializable>> records = new ArrayList<>();
		
		try {
			
			queryPersistedRecords.setString(1, DataRecord.class.getCanonicalName());
			
			ResultSet rs = queryPersistedRecords.executeQuery();
			
			while(rs.next()) {
				InputStream stream = rs.getBinaryStream(1);
				
				DataRecord<Serializable> record = oSerial.deserializeFromByteStream(stream);
				
				if( null != record )
					records.add(record);

				stream.close();
			}

		} catch (SQLException | IOException e) {
			log.error("Error reading objects from database.", e);
		}
		return records;
	}
}