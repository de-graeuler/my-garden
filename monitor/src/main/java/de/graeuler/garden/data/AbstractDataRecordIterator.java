package de.graeuler.garden.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataRecordIterator<T> implements DataIterator<T> {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	T nextRecord = null;
	
	ResultSet resultSet;
	
	public AbstractDataRecordIterator(ResultSet resultSet) {
		this.resultSet = resultSet;
	}
	
	protected abstract T readObjectFromResultSet(ResultSet rs);

	@Override
	public boolean hasNext() {
		
		if (resultSet != null && nextRecord != null) {
			return true;
		}
		
		nextRecord = loadNext();

		return nextRecord != null;
	}

	@Override
	public T next() {
		if (nextRecord == null && ! hasNext()) {
			throw new NoSuchElementException();
		}
		T result = nextRecord;
		nextRecord = null;
		return result;
	}
	
	@Override
	public void remove() {
		try {
			if (resultSet.rowDeleted()) {
				throw new IllegalStateException("Row was already removed from the result set.");
			} else {
				resultSet.deleteRow();
			}
		} catch (SQLFeatureNotSupportedException notSupported) {
			throw new UnsupportedOperationException(notSupported);
		} catch (SQLException otherSqlIssue) {
			throw new UnsupportedOperationException(otherSqlIssue);
		}
	}
	
	@Override
	public void close() {
		try {
			if (this.resultSet != null && ! resultSet.isClosed()) {
				this.resultSet.close();
			}
		} catch (SQLException e) {
			log.error("Closing the result set failed.", e);
		}
	}

	private T loadNext() {
		assert resultSet != null;
		T dataRecord = null;
		try {
			if ( ! resultSet.isClosed() && resultSet.next() ) {
				dataRecord = readObjectFromResultSet( resultSet);
			} else {
				resultSet.close();
			}
		} catch (SQLException e) {
			log.error("Error reading objects from database.", e);
		}
		return dataRecord;
	}

}
