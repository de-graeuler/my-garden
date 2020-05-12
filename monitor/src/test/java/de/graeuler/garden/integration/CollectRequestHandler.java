package de.graeuler.garden.integration;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class CollectRequestHandler implements HttpRequestHandler {

	AtomicInteger requestCounter = new AtomicInteger(0);
	Collection<JsonObject> requestContent = Collections.synchronizedCollection(new ArrayDeque<>());
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		requestCounter.incrementAndGet();
		if(request instanceof HttpEntityEnclosingRequest) {
			try(JsonReader jsonReader = Json.createReader(new GZIPInputStream((((HttpEntityEnclosingRequest) request).getEntity().getContent())))) {
				JsonObject o;
				if((o = jsonReader.readObject()) != null) {
					requestContent.add(o);
				}
			}
		}
	}
	
	public int getHandledRequests() {
		return requestCounter.get();
	}
	
	public List<JsonArray> getReceivedRecords(String key) {
		return requestContent.stream().filter(o -> o.containsKey(key)).map(o -> o.getJsonArray(key)).collect(Collectors.toList());
//		return requestContent.stream().filter(o -> o.containsKey(key)).mapToInt(o -> o.getJsonArray(key).size()).sum();
	}

	public void reset() {
		requestCounter.set(0);
		requestContent.clear();
	}
	
}