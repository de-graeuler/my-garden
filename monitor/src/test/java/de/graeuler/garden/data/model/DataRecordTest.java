package de.graeuler.garden.data.model;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class DataRecordTest {
	
	@Test
	public void testString() {
		DataRecord<String> sdr = new DataRecord<>("stringkey", "StringValue");
		assertEquals("stringkey", sdr.getKey());
		assertEquals("StringValue", sdr.getValue());
		assertEquals(DataRecord.ValueType.STRING, sdr.getValueType());
	}

	@Test
	public void testDouble() {
		DataRecord<Double> ddr = new DataRecord<>("double", 2.12);
		assertEquals("double", ddr.getKey());
		assertEquals(Double.valueOf(2.12), ddr.getValue());
		assertEquals(DataRecord.ValueType.NUMBER, ddr.getValueType());
	}
	
	@Test
	public void testBoolean() {
		DataRecord<Boolean> bdr = new DataRecord<>("boolkey", true);
		assertEquals("boolkey", bdr.getKey());
		assertEquals(true, bdr.getValue());
		assertEquals(DataRecord.ValueType.BOOLEAN, bdr.getValueType());
	}

	@Test
	public void testObject() {
		Date d = new Date();
		DataRecord<Date> odr = new DataRecord<>("object", d);
		assertEquals("object", odr.getKey());
		assertEquals(d, odr.getValue());
		assertEquals(DataRecord.ValueType.OBJECT, odr.getValueType());
	}
}

