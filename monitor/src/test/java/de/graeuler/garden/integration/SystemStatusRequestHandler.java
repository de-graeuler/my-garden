package de.graeuler.garden.integration;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class SystemStatusRequestHandler implements HttpRequestHandler {
	
	public enum ResponseState {
		OK, HTTP_EXCEPTION, IO_EXCEPTION;
	}
	
	private ResponseState responseState = ResponseState.OK;
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		// all good. 
	}

	public ResponseState getResponseState() {
		return responseState;
	}

	public void setResponseState(ResponseState responseState) {
		this.responseState = responseState;
	}
	
}