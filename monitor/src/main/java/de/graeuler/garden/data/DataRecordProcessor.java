package de.graeuler.garden.data;

import java.util.Collection;

public interface DataRecordProcessor<T> {

	boolean call(Collection<T> recordBlock);

}
