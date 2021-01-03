package de.graeuler.garden.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.monitor.util.ApiToken;

public class JsonDataConverter implements DataConverter<Collection<DataRecord>, JsonValue> {

	private String apiToken;

	@Inject
	JsonDataConverter(AppConfig config) {
		this.apiToken = (String) ConfigurationKeys.API_TOKEN.from(config);
	}
	
	DateTimeFormatter isoFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 *  example for combined result: 
	 *  { 
	 *  	"api-token": "0b0c023edeef60cb4b3197934d79755e20001136:feWeF",
	 *  	"key1" : [ {
	 *  			"t": "isodatetime",
	 *      		"v": "value" // or number
	 *      	}, {
	 *      		"t": "isodatetime",
	 *      		"v": "value" // or number
	 *      	} ],
	 *  "key2": [ {
	 *  			"t": "isodatetime",
	 *      		"v": "value" // or number
	 *      	}, {
	 *      		"t": "isodatetime",
	 *      		"v": "value" // or number
	 *      	} ]
	 *  }
	 */
	@Override
	public JsonValue convert(Collection<DataRecord> input) {
		Map<String, JsonArrayBuilder> timeValueArrayBuilders = new HashMap<>();
		for(DataRecord record : input) {
			try {
				addJsonObjectToMap(timeValueArrayBuilders, record);
			} catch (ConversionException e) {
				log.error("Dataloss! Conversion to JSON failed for record (%s, %s): %s", record.getKey(), record.getValueType(), e.getMessage());
			}
		}
		
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add("api-token", ApiToken.buildApiToken(this.apiToken));
		for(String jsonGroupKey : timeValueArrayBuilders.keySet()) {
			jsonObjectBuilder.add(jsonGroupKey, timeValueArrayBuilders.get(jsonGroupKey));
		}
		
		JsonObject result = jsonObjectBuilder.build();
		return result;
	}

	private void addJsonObjectToMap(Map<String, JsonArrayBuilder> timeValueArrayBuilders, DataRecord record) throws ConversionException {
		JsonObjectBuilder timeValueObject = Json.createObjectBuilder();
		timeValueObject.add("t", record.getTimestamp().format(isoFormat));
		switch (record.getValueType()) {
			case STRING:   timeValueObject.add("v", (String) record.getValue()); break;
			case NUMBER:   timeValueObject.add("v", (Double) record.getValue()); break;
			case BOOLEAN:  timeValueObject.add("v", (Boolean) record.getValue()); break;
			case FILE:     timeValueObject.add("v", encodeFile((File) record.getValue())); break;
			case OBJECT:   timeValueObject.add("v", (String) record.getValue().toString()); break;
			default: break;
		}
		timeValueArrayBuilders.computeIfAbsent(record.getKey(), (k) -> Json.createArrayBuilder()).add(timeValueObject.build());
	}

	private String encodeFile(File file) throws ConversionException {
		StringJoiner stringJoiner = new StringJoiner(":");
		stringJoiner.add(file.getName());
		stringJoiner.add(convertToBase64(file)); 
		return stringJoiner.toString();
	}

	private String convertToBase64(File value) throws ConversionException {
		byte[] fileData;
		try {
			fileData = Files.readAllBytes(Paths.get(value.getAbsolutePath()));
			return Base64.getEncoder().encodeToString(fileData);
		} catch (IOException e) {
			throw new ConversionException(e);
		}
	}
	
}
