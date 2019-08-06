/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.graeuler.garden.data;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.SerializableHashDelegate;

/**
 *
 * @author media
 */
@Singleton
public class DerbyDataPersister extends AbstractDataPersister {
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	DerbyDataPersister(SerializableHashDelegate hashService, AppConfig config) throws WrappedDatabaseException {
		super(hashService, config);
	}
	
	@Override
	protected void startUpEmbeddedDatabase(AppConfig config) throws WrappedDatabaseException {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new WrappedDatabaseException(e);
		}
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
	
	protected PreparedStatement prepareQueryPersistedRecordsStatement() throws SQLException {
		return dbConnection.prepareStatement("SELECT object FROM persistedrecords WHERE fqcn = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
	}


}