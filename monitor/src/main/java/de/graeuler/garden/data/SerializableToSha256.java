package de.graeuler.garden.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.monitor.util.ObjectSerializationUtil;

public class SerializableToSha256 implements SerializableHashDelegate {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public byte[] serializeAndHash(Serializable serializableObject, StringBuffer hash) {
		if(null == serializableObject || null == hash) {
			return null;
		}
		
		byte[] bytes;
		if(serializableObject instanceof byte[]) {
			bytes = (byte[]) serializableObject;
		} else {
			bytes = ObjectSerializationUtil.serializeToByteArray(serializableObject);
		}
		if( bytes.length == 0 ) {
			return null;
		}
		try (InputStream inputStream = new ByteArrayInputStream(bytes)){
			byte[] digest = calculateDigest(inputStream);
			hash.setLength(0);
			for (byte b : digest) hash.append(Integer.toHexString(0xFF & b));
			return bytes;
		} catch (NoSuchAlgorithmException nae) {
			log.error("SHA-256 not available.", nae);
		} catch (IOException ie) {
			log.error("Cannot hash input stream.", ie);
		}
		return null;
	}

	private byte[] calculateDigest(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] buffer = new byte[1024];
		int readBytes = 0;
		while ( (readBytes = inputStream.read(buffer)) != -1) {
			md.update(buffer, 0, readBytes);
		}
		byte[] digest = md.digest();
		return digest;
	}

}
