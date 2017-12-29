package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;

import de.graeuler.garden.config.StaticAppConfig;

public class JsonDataConverterTest {
	
	@Test
	public void testConvert() {
		JsonDataConverter converter = new JsonDataConverter(new StaticAppConfig());
		List<DataRecord<?>> records = new ArrayList<>();
		records.add(new DataRecord<String>("stringkey", "stringvalue1"));
		records.add(new DataRecord<Double>("doublekey", 1.234));
		records.add(new DataRecord<Boolean>("boolkey", false));
		records.add(new DataRecord<String>("stringkey", "stringvalue2"));
		records.add(new DataRecord<Double>("doublekey", 2.345));
		records.add(new DataRecord<Boolean>("boolkey", true));
		String testResult = converter.convert(records);
		JsonReader jsonReader = Json.createReader(new StringReader(testResult));
		JsonObject jsonObject = jsonReader.readObject();
		assertTrue(jsonObject.containsKey("stringkey"));
		assertTrue(jsonObject.containsKey("doublekey"));
		assertTrue(jsonObject.containsKey("boolkey"));
		assertFalse(jsonObject.containsKey("####"));
		assertEquals("stringvalue1", jsonObject.getJsonArray("stringkey").getJsonObject(0).getString("v"));
		assertEquals("stringvalue2", jsonObject.getJsonArray("stringkey").getJsonObject(1).getString("v"));
		assertEquals(false, jsonObject.getJsonArray("boolkey").getJsonObject(0).getBoolean("v"));
	}

}
