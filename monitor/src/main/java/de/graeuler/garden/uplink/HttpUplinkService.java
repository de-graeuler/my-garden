package de.graeuler.garden.uplink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
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
			byte[] compressedData = this.gZipData(data);
			HttpEntity entity = new ByteArrayEntity(compressedData, ContentType.create("application/gzip")); 
			this.postRequest.setEntity(entity);
			CloseableHttpResponse response = this.httpclient.execute(postRequest);
			String resultContent = EntityUtils.toString(response.getEntity());
//          optional result content parsing for more verbose result information: @TODO.
			int httpStatusCode = response.getStatusLine().getStatusCode();
			response.close();
			if (200 == httpStatusCode || 201 == httpStatusCode) {
				log.info("{} bytes of data pushed to uplink.", compressedData.length);
				log.debug("response received: {}", resultContent);
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

	private byte[] gZipData(String data) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzos = new GZIPOutputStream(baos);
			gzos.write(data.getBytes("UTF-8"));
			gzos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			log.error("Error compressing data. {}", e.getMessage());
		}
		return new byte[] {};
	}

}
