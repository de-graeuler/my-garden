/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.graeuler.garden.data;

import de.graeuler.garden.data.model.DataRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author media
 */
public class DataPersister {
	
	final static int BULKSIZE =  1000;
	final static int LIMIT    = 100000;

	Connection dbConnection;

	private final PreparedStatement insertPersistedRecords;
	private final PreparedStatement queryPersistedRecords;
	
	public DataPersister() throws ClassNotFoundException, SQLException {

		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			
		this.dbConnection = DriverManager.getConnection("jdbc:derby:data;create=true");
		dbConnection.setAutoCommit(false);
			
		PreparedStatement stmt = dbConnection.prepareStatement("CREATE TABLE persistedrecords (fqcn VARCHAR(32000), object BLOB)");
		try {stmt.execute();} catch(SQLException e) {}; // should only fail if table exists.

		insertPersistedRecords = dbConnection.prepareStatement("INSERT INTO persistedrecords (fqcn, object) values (?, ?)");
		queryPersistedRecords  = dbConnection.prepareStatement("SELECT object FROM persistedrecords where fqcn = ?");
		
	}
	
	public static void main(String[] args) {
		
		try {

			DataPersister persister = new DataPersister();

			System.out.println("INIT");

			List<DataRecord<?>> records = new ArrayList<>();

			for (int i = 0; i < LIMIT; i++) {
				records.add(new DataRecord<>("outside-temperature", Math.random() * 20 ));
				if(i % BULKSIZE == 0) {
					System.out.print(".");
					System.out.flush();
				}
			}

			System.out.println();
			System.out.println("WRITE");
			
			persister.writeAll(records);
			
			System.out.println();
			System.out.println("READ");
		
			records.clear();

			records.addAll(persister.readAll());
			
			System.out.println();
			System.out.println(records.size());
			
		} catch (ClassNotFoundException e) {
			System.err.println("JavaDB embedded driver class not found.");
		} catch (SQLException e) {
			System.err.println("Connect not possible. " + e.getMessage());
		}
	}

	private ByteArrayInputStream serializeToByteStream(DataRecord<? extends Serializable> record) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(record);
		oos.flush();
		byte[] recordAsByteArray = bos.toByteArray();
		oos.close();
		bos.close();

		return new ByteArrayInputStream(recordAsByteArray);
		
	}

	private void writeAll(List<DataRecord<?>> records) {
		try {
			int i = 0;
			for(DataRecord<?> r : records)
			{

				InputStream stream = serializeToByteStream(r);

				insertPersistedRecords.setString(1, r.getClass().getCanonicalName());
				insertPersistedRecords.setBinaryStream(2, stream);
				insertPersistedRecords.addBatch();

				if ( ++i % BULKSIZE == 0) {
					insertPersistedRecords.executeBatch();
					System.out.print(".");
					i = 0;
				}
			}
			if (i > 0)
				insertPersistedRecords.executeBatch();

			dbConnection.commit();
			
		} catch (SQLException | IOException ex) {
			Logger.getLogger(DataPersister.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private Collection<DataRecord<?>> readAll() {
		
		Collection<DataRecord<?>> records = new ArrayList<>();
		
		try {
			
			queryPersistedRecords.setString(1, DataRecord.class.getCanonicalName());
			
			ResultSet rs = queryPersistedRecords.executeQuery();
			
			int i = 0;
			while(rs.next()) {
				InputStream stream = rs.getBinaryStream(1);
				
				DataRecord<?> record = deserializeFromByteStream(stream);
				
				records.add(record);

				if ( ++i % BULKSIZE == 0) {
					System.out.print(".");
					i = 0;
				}
				stream.close();
			}
		} catch (SQLException | IOException ex) {
			Logger.getLogger(DataPersister.class.getName()).log(Level.SEVERE, null, ex);
		}
		return records;
	}

	private DataRecord<?> deserializeFromByteStream(InputStream stream) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] chunk = new byte[1024];
			int nRead;
			bos.reset();
			while ((nRead = stream.read(chunk)) > 0){
				bos.write(chunk,0, nRead);
			}
			bos.flush();
			byte [] buf = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bis);
			DataRecord dr = (DataRecord) ois.readObject();
			return dr;
		}
		catch (ClassNotFoundException | IOException ex) {
			Logger.getLogger(DataPersister.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
