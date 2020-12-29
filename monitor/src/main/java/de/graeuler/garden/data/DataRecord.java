package de.graeuler.garden.data;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class DataRecord implements Serializable {

	private static final long serialVersionUID = -2801357326272785940L;

	public enum ValueType {
		OBJECT, STRING, NUMBER, BOOLEAN;
	}
	
	private ZonedDateTime timestamp;
	private String key;
	private Serializable value;
	private ValueType valueType;
	
	public DataRecord(String key, Serializable value) {
		this.timestamp = ZonedDateTime.now();
		this.key = key;
		this.value = value;
		if(value instanceof String) {
			this.valueType = ValueType.STRING;
		} else if (value instanceof Number) {
			this.valueType = ValueType.NUMBER;
		} else if (value instanceof Boolean) {
			this.valueType = ValueType.BOOLEAN;
		} else {
			this.valueType = ValueType.OBJECT;
		}
	}
	
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(ZonedDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Serializable getValue() {
		return value;
	}
	public void setValue(Serializable value) {
		this.value = value;
	}
	public ValueType getValueType() {
		return this.valueType;
	}

}
