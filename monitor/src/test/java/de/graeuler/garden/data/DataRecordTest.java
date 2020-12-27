package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class DataRecordTest {
	
	@Test
	public void testString() {
		DataRecord sdr = new DataRecord("stringkey", "StringValue");
		assertEquals("stringkey", sdr.getKey());
		assertEquals("StringValue", sdr.getValue());
		assertEquals(DataRecord.ValueType.STRING, sdr.getValueType());
	}

	@Test
	public void testDouble() {
		DataRecord ddr = new DataRecord("double", 2.12);
		assertEquals("double", ddr.getKey());
		assertEquals(Double.valueOf(2.12), ddr.getValue());
		assertEquals(DataRecord.ValueType.NUMBER, ddr.getValueType());
	}
	
	@Test
	public void testBoolean() {
		DataRecord bdr = new DataRecord("boolkey", true);
		assertEquals("boolkey", bdr.getKey());
		assertEquals(true, bdr.getValue());
		assertEquals(DataRecord.ValueType.BOOLEAN, bdr.getValueType());
	}

	@Test
	public void testObject() {
		Date d = new Date();
		DataRecord odr = new DataRecord("object", d);
		assertEquals("object", odr.getKey());
		assertEquals(d, odr.getValue());
		assertEquals(DataRecord.ValueType.OBJECT, odr.getValueType());
	}
}

