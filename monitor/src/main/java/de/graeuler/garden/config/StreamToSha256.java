package de.graeuler.garden.config;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.interfaces.RecordHashDelegate;

public class StreamToSha256 implements RecordHashDelegate<InputStream> {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public String hash(InputStream data) {
		if(null == data) {
			return null;
		}
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[1024];
			int readBytes = 0;
			while ( (readBytes = data.read(buffer)) != -1) {
				md.update(buffer, 0, readBytes);
			}
			byte[] digest = md.digest();
			StringBuffer result = new StringBuffer();
			for (byte b : digest) {
				result.append(Integer.toHexString(0xFF & b));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException nae) {
			log.error("SHA-256 not available.", nae);
		} catch (IOException ie) {
			log.error("Cannot hash input stream.", ie);
		}
		return null;
	}

}
