package de.graeuler.garden.data;

import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

public class DataRecord implements Serializable {

	private static final long serialVersionUID = -2801357326272785940L;

	public enum ValueType {
		OBJECT(null), STRING(String.class), NUMBER(Number.class), BOOLEAN(Boolean.class), FILE(File.class);

		private Class<? extends Serializable> type;

		<T extends Serializable> ValueType(Class<T> type) {
			this.type = type;
		}

		public static ValueType of(Class<? extends Serializable> requestedType) {
			return Stream.of(ValueType.values())
					.filter(valueType -> valueType.type != null && requestedType.isAssignableFrom(valueType.type))
					.findFirst().orElse(OBJECT);
		}
	}

	private ZonedDateTime timestamp;
	private String key;
	private Serializable value;
	private ValueType valueType;

	public DataRecord(String key, Serializable value) {
		this.timestamp = ZonedDateTime.now();
		this.key = key;
		this.value = value;
		this.valueType = ValueType.of(value.getClass());
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
