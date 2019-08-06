package de.graeuler.garden.uplink;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.StaticAppConfig;

public class HttpUplinkServiceTest {

	AppConfig config = new StaticAppConfig();
//	@Before public void initMocks() {
//        MockitoAnnotations.initMocks(this);
//    }
	
	@Test
	public final void testPushData() throws ClientProtocolException, IOException {
		
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("value", "Test");
	    JsonValue jsonTestValue = jsonBuilder.build();
		
		
		byte[] compressedTest = {
				31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -85, 86, 42, 75, -52, 41, 77, 85, -78, 82, 10, 73, 45, 46, 81, -86, 5, 0, 124, 76, -1, -104, 16, 0, 0, 0
		};
		
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(statusLine.getStatusCode()).thenReturn(200, 201, 500);
		
		CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
		Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
		Mockito.when(httpResponse.getEntity()).thenReturn(new StringEntity("mocked"));
		
		CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
		Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenAnswer(new Answer<CloseableHttpResponse>() {

			@Override
			public CloseableHttpResponse answer(InvocationOnMock invocation) throws Throwable {
				byte[] compressedCompare = EntityUtils.toByteArray(((HttpPost)invocation.getArgument(0)).getEntity());
				Assert.assertArrayEquals(compressedTest, compressedCompare);
				return httpResponse;
			}
		});
		
		HttpUplinkService uplinkService = new HttpUplinkService(config, httpClient);
		assertTrue(uplinkService.pushData(jsonTestValue)); // status code 200
		assertTrue(uplinkService.pushData(jsonTestValue)); // status code 201
		assertFalse(uplinkService.pushData(jsonTestValue)); // status code 500 (see statusLine above)
	}

}
