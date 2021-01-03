package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.Test;

import de.graeuler.garden.config.StaticAppConfig;

public class JsonDataConverterTest {
	
	@Test
	public void testConvert() {
		JsonDataConverter converter = new JsonDataConverter(new StaticAppConfig());
		List<DataRecord> records = new ArrayList<>();
		records.add(new DataRecord("stringkey", "stringvalue1"));
		records.add(new DataRecord("doublekey", 1.234));
		records.add(new DataRecord("boolkey", false));
		records.add(new DataRecord("stringkey", "stringvalue2"));
		records.add(new DataRecord("doublekey", 2.345));
		records.add(new DataRecord("boolkey", true));
		JsonValue testResult = converter.convert(records);
		JsonReader jsonReader = Json.createReader(new StringReader(testResult.toString()));
		JsonObject jsonObject = jsonReader.readObject();
		assertTrue(jsonObject.containsKey("stringkey"));
		assertTrue(jsonObject.containsKey("doublekey"));
		assertTrue(jsonObject.containsKey("boolkey"));
		assertFalse(jsonObject.containsKey("####"));
		assertEquals("stringvalue1", jsonObject.getJsonArray("stringkey").getJsonObject(0).getString("v"));
		assertEquals("stringvalue2", jsonObject.getJsonArray("stringkey").getJsonObject(1).getString("v"));
		assertEquals(false, jsonObject.getJsonArray("boolkey").getJsonObject(0).getBoolean("v"));
	}
	
	@Test
	public void testFileConversion() throws IOException {
		JsonDataConverter converter = new JsonDataConverter(new StaticAppConfig());
		File tempFile = File.createTempFile("myGardenTest", ".dat");
		String expectedResult = tempFile.getName() + ":" + "VGVzdCBDb250ZW50IGZvciBKU09OIEJhc2U2NCBFbmNvZGluZw==";
		tempFile.deleteOnExit();
		Files.writeString(Paths.get(tempFile.getAbsolutePath()), "Test Content for JSON Base64 Encoding", StandardOpenOption.WRITE);
		List<DataRecord> records = new ArrayList<>();
		records.add(new DataRecord("file", tempFile));

		JsonValue testResult = converter.convert(records);
		
		JsonReader jsonReader = Json.createReader(new StringReader(testResult.toString()));
		JsonObject jsonObject = jsonReader.readObject();
		assertTrue(jsonObject.containsKey("file"));
		assertEquals(expectedResult, jsonObject.getJsonArray("file").getJsonObject(0).getString("v"));
	}

}
