package de.graeuler.garden.data;

import java.io.Serializable;
import java.util.Collection;

public interface DataPersister<T extends Serializable> {

	Collection<T> readAll();

	void write(T record);

	int deleteAll();

}
