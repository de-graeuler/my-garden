package de.graeuler.garden.data;

import java.util.Iterator;

public interface DataIterator<T> extends Iterator<T>, AutoCloseable {

	@Override
	void close();
	
}
