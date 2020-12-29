package de.graeuler.garden.data;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

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
	
	/**
	 *  
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
	 *
	 */
	@Override
	public JsonValue convert(Collection<DataRecord> input) {
		Map<String, JsonArrayBuilder> timeValueArrayBuilders = new HashMap<>();
		for(DataRecord record : input) {
			addJsonObjectToMap(timeValueArrayBuilders, record);
		}
		
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add("api-token", ApiToken.buildApiToken(this.apiToken));
		for(String jsonGroupKey : timeValueArrayBuilders.keySet()) {
			jsonObjectBuilder.add(jsonGroupKey, timeValueArrayBuilders.get(jsonGroupKey));
		}
		
		JsonObject result = jsonObjectBuilder.build();
		return result;
	}

	private void addJsonObjectToMap(Map<String, JsonArrayBuilder> timeValueArrayBuilders, DataRecord record) {
		JsonObjectBuilder timeValueObject = Json.createObjectBuilder();
		timeValueObject.add("t", record.getTimestamp().format(isoFormat));
		switch (record.getValueType()) {
			case STRING:   timeValueObject.add("v", (String) record.getValue()); break;
			case NUMBER:   timeValueObject.add("v", (Double) record.getValue()); break;
			case OBJECT:   timeValueObject.add("v", (String) record.getValue().toString()); break;
			case BOOLEAN:  timeValueObject.add("v", (Boolean) record.getValue()); break;
			default: break;
		}
		timeValueArrayBuilders.computeIfAbsent(record.getKey(), (k) -> Json.createArrayBuilder()).add(timeValueObject.build());
	}
	
}
