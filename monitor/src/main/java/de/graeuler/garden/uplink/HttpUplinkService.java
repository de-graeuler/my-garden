package de.graeuler.garden.uplink;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;

public class HttpUplinkService implements Uplink<String> {

	private AppConfig config;
	private String uplink;
	CloseableHttpClient httpclient = HttpClients.createDefault();
	HttpPost postRequest;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	public HttpUplinkService(AppConfig config) {
		this.config = config;
		this.uplink = (String) this.config.get(AppConfig.Key.UPLINK_ADRESS);
	}
	
	@Override
	public boolean pushData(String data) {
		if (null == this.postRequest)
			this.postRequest = new HttpPost(this.uplink);
		try {
			HttpEntity entity = new StringEntity(data);
			this.postRequest.setEntity(entity);
			this.postRequest.setHeader("Content-Type", "application/json");
			CloseableHttpResponse response = this.httpclient.execute(postRequest);
//          optional result content parsing for more verbose result information: @TODO.
			String resultContent = EntityUtils.toString(response.getEntity());
			int httpStatusCode = response.getStatusLine().getStatusCode();
			response.close();
			if (200 == httpStatusCode || 201 == httpStatusCode) {
				return true;
			} else {
				log.error("HTTP ERROR {}: {}", httpStatusCode, resultContent);
				return false;
			}
		} catch (IOException e) {
			log.error("{}", e.getMessage());
			return false;
		}
	}

}
