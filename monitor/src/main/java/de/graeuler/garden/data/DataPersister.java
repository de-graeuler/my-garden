package de.graeuler.garden.data;

import java.io.Serializable;
import java.util.Collection;

public interface DataPersister<T extends Serializable> {

	DataIterator<T> iterate();
	
	void write(T record);

	long deleteAll();

	long deleteAll(final Collection<T> records);

	long countAll();

	Collection<T> readFirst(int _blocksize);

}
