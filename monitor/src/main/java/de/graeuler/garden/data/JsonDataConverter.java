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
import de.graeuler.garden.interfaces.DataConverter;
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
	 *  	"api-token": "0b0c023edeef60cb4b3197934d79755e20001136:feWeF"
	 *  	"key1" : 
	 *  	[ 
	 *  		{
	 *  			"t": "isodatetime",
	 *      		"v": "value" // or number
	 *      	}, 
	 *      	{
	 *      		"t": "isodatetime",
	 *      		"v": "value" // or number
	 *      	} 
	 *      ],
	 *     	"key2": 
	 *     	[ 
	 *     		{
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
		JsonObjectBuilder jsonObjectBuilder;
		Map<String, JsonArrayBuilder> jsonKeyGroupArrayBuilderMap = new HashMap<>();
		input.stream()
				.map(k -> k.getKey())
				.distinct()
				.forEach(k -> jsonKeyGroupArrayBuilderMap.put(k, Json.createArrayBuilder()));

		for(DataRecord record : input) {
			addJsonObjectToMap(jsonKeyGroupArrayBuilderMap, record);
		
		}
		
		jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add("api-token", ApiToken.buildApiToken(this.apiToken));
		for(String jsonGroupKey : jsonKeyGroupArrayBuilderMap.keySet()) {
			jsonObjectBuilder.add(jsonGroupKey, jsonKeyGroupArrayBuilderMap.get(jsonGroupKey));
		}
		
		JsonObject result = jsonObjectBuilder.build();
		return result;
	}

	private void addJsonObjectToMap(Map<String, JsonArrayBuilder> jsonKeyGroupArrayBuilderMap, DataRecord record) {
		JsonObjectBuilder jsonObjectBuilder;
		jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add("t", record.getTimestamp().format(isoFormat));
		switch (record.getValueType()) {
			case STRING:   jsonObjectBuilder.add("v", (String) record.getValue()); break;
			case NUMBER:   jsonObjectBuilder.add("v", (Double) record.getValue()); break;
			case OBJECT:   jsonObjectBuilder.add("v", (String) record.getValue().toString()); break;
			case BOOLEAN:  jsonObjectBuilder.add("v", (Boolean) record.getValue()); break;
			default: break;
		}

		jsonKeyGroupArrayBuilderMap.get(record.getKey()).add(jsonObjectBuilder.build());
	}
	
}
