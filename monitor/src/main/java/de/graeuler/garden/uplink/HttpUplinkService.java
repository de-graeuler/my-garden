package de.graeuler.garden.uplink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.json.JsonValue;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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

public class HttpUplinkService implements Uplink<JsonValue> {

	private String uplink;
	private String uplinkStatus;
	CloseableHttpClient httpclient;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	HttpUplinkService(AppConfig config, CloseableHttpClient httpclient) {
		this.uplink = (String) ConfigurationKeys.UPLINK_ADRESS.from(config);
		this.uplinkStatus = (String) ConfigurationKeys.UPLINK_STATUS.from(config);
		this.httpclient = httpclient;
	}
	
	@Override
	public UplinkConnectionState getConnectionState() {
		HttpGet getRequest = new HttpGet(this.uplinkStatus);
		try(CloseableHttpResponse statusResponse = this.httpclient.execute(getRequest)) {
			if( statusResponse.getStatusLine().getStatusCode() < 300) {
				return UplinkConnectionState.ONLINE;
			} else {
				return UplinkConnectionState.UNAVAILABLE;
			}
		} catch (ClientProtocolException e) {
			return UplinkConnectionState.UNAVAILABLE;
		} catch (IOException e) {
			return UplinkConnectionState.UNREACHABLE;
		}
	}

	@Override
	public boolean pushData(JsonValue data) {
		try {
			byte[] compressedData = this.gZipData(data.toString());
			HttpEntity entity = new ByteArrayEntity(compressedData, ContentType.create("application/gzip")); 
			HttpPost postRequest = new HttpPost(this.uplink);
			postRequest.setEntity(entity);
			try(CloseableHttpResponse response = this.httpclient.execute(postRequest)) {
				String resultContent = EntityUtils.toString(response.getEntity());
	//          TODO resultContent parsing for more verbose result information.
				int httpStatusCode = response.getStatusLine().getStatusCode();
				if (200 == httpStatusCode || 201 == httpStatusCode) {
					log.info("{} bytes of data pushed to uplink.", compressedData.length);
					log.debug("response received: {}", resultContent);
					return true;
				} else {
					log.error("HTTP ERROR {}: {}", httpStatusCode, resultContent);
					return false;
				}
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
