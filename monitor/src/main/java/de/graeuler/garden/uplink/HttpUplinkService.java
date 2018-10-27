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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;

public class HttpUplinkService implements Uplink<String> {

	private String uplink;
	CloseableHttpClient httpclient;
	HttpPost postRequest;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	HttpUplinkService(AppConfig config, CloseableHttpClient httpclient) {
		this.uplink = (String) ConfigurationKeys.UPLINK_ADRESS.from(config);
		this.httpclient = httpclient;
	}
	
	@Override
	public boolean pushData(String data) {
		if (this.postRequest == null)
			this.postRequest = new HttpPost(this.uplink);
		try {
			byte[] compressedData = this.gZipData(data);
			HttpEntity entity = new ByteArrayEntity(compressedData, ContentType.create("application/gzip")); 
			this.postRequest.setEntity(entity);
			CloseableHttpResponse response = this.httpclient.execute(postRequest);
			String resultContent = EntityUtils.toString(response.getEntity());
//          TODO resultContent parsing for more verbose result information.
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
