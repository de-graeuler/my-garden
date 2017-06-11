package de.graeuler.garden.interfaces;

import java.io.Serializable;
import java.util.Map;

public interface DataCollector {
	
	public void collect(Map<String, Serializable> data);

	public void collect(String string, Serializable valueOf);
	
}
