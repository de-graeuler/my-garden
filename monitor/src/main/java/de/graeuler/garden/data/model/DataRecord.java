package de.graeuler.garden.data.model;

import java.time.ZonedDateTime;

public class DataRecord<T> {

	public enum ValueType {
		OBJECT, STRING, NUMBER, BOOLEAN;
	}
	
	ZonedDateTime timestamp;
	String key;
	T value;
	ValueType valueType;
	
	public DataRecord(String key, T value) {
		this.timestamp = ZonedDateTime.now();
		this.key = key;
		this.value = value;
		if(value instanceof String) {
			this.valueType = ValueType.STRING;
		} 
		else if (value instanceof Number) {
			this.valueType = ValueType.NUMBER;
		}
		else if (value instanceof Boolean) {
			this.valueType = ValueType.BOOLEAN;
		}
		else {
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
	public T getValue() {
		return value;
	}
	public void setValue(T value) {
		this.value = value;
	}
	public ValueType getValueType() {
		return this.valueType;
	}

}
