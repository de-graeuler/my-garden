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
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.data.model.DataRecord;
import de.graeuler.garden.monitor.util.ObjectSerializationUtil;

/**
 *
 * @author media
 */
public class DerbyDataPersister implements DataPersister <DataRecord<Serializable>>{
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	Connection dbConnection;

	private final PreparedStatement insertPersistedRecords;
	private final PreparedStatement queryPersistedRecords;
	private final PreparedStatement deletePersistedRecords;
	
	ObjectSerializationUtil oSerial = new ObjectSerializationUtil();
	
	public DerbyDataPersister() throws ClassNotFoundException, SQLException {

		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			
		this.dbConnection = DriverManager.getConnection("jdbc:derby:data;create=true");
		dbConnection.setAutoCommit(false);
			
		PreparedStatement stmt = dbConnection.prepareStatement("CREATE TABLE persistedrecords (fqcn VARCHAR(32000), object BLOB)");
		try {stmt.execute();} catch(SQLException e) {}; // should only fail if table exists.

		insertPersistedRecords = dbConnection.prepareStatement("INSERT INTO persistedrecords (fqcn, object) values (?, ?)");
		queryPersistedRecords  = dbConnection.prepareStatement("SELECT object FROM persistedrecords where fqcn = ?");
		deletePersistedRecords = dbConnection.prepareStatement("DELETE FROM persistedrecords where fqcn = ?");
		
	}
	
	public void shutdown() {
		try {
			dbConnection.close();
			DriverManager.getConnection(
				    "jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if ("XJ015".equals(e.getSQLState()))
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
	public void write(DataRecord<Serializable> r) {
		try {
			InputStream stream = oSerial.serializeToByteStream(r);
			
			if( stream == null ) 
				return;

			insertPersistedRecords.setString(1, r.getClass().getCanonicalName());
			insertPersistedRecords.setBinaryStream(2, stream);

			insertPersistedRecords.execute();

			dbConnection.commit();
			
		} catch (SQLException e) {
			log.error("Database error while writing objects.", e);
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
