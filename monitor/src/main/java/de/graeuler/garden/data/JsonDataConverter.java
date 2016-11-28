package de.graeuler.garden.data;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import de.graeuler.garden.data.model.DataRecord;
import de.graeuler.garden.interfaces.DataConverter;

public class JsonDataConverter implements DataConverter<List<DataRecord<?>>, String> {

	DateTimeFormatter isoFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
	
	/**
	 *  
	 *  example for combined result: 
	 *  { 
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
	public String convert(List<DataRecord<?>> input) {
		JsonObjectBuilder jsonObjectBuilder;
		Map<String, JsonArrayBuilder> jsonKeyGroupArrayBuilderMap = new HashMap<>();
		input.stream()
				.map(k -> k.getKey())
				.distinct()
				.forEach(k -> jsonKeyGroupArrayBuilderMap.put(k, Json.createArrayBuilder()));

		for(DataRecord<?> record : input) {
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
		
		jsonObjectBuilder = Json.createObjectBuilder();
		for(String jsonGroupKey : jsonKeyGroupArrayBuilderMap.keySet()) {
			jsonObjectBuilder.add(jsonGroupKey, jsonKeyGroupArrayBuilderMap.get(jsonGroupKey));
		}
		
		JsonObject result = jsonObjectBuilder.build();
		return result.toString();
	}
	
}
