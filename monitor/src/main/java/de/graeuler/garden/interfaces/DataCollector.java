package de.graeuler.garden.interfaces;

import java.util.Map;

public interface DataCollector {
	
	public void collect(Map<String, Object> data);

	public void collect(String string, Object valueOf);
	
}
