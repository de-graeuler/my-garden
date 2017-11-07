package de.graeuler.garden.monitor.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiToken {

	private static final int SALT_LENGTH = 6;
	private static final Logger log = LoggerFactory.getLogger(ApiToken.class);

	private static String getRandomSalt(int length) {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		StringBuilder sb = new StringBuilder(length);
		Random random = new Random();
		for(int i = 0; i < length; i++) {
			sb.append(chars[random.nextInt(chars.length)]);
		}
		return sb.toString();
	}

	/**
	 * Build salted hash of given apiToken
	 * 
	 * @param apiToken
	 * @return
	 */
	public static String buildApiToken(String apiToken) {
		String salt = getRandomSalt(SALT_LENGTH);
		String encoding = "UTF-8";
		String algorithm = "SHA-1";
		String saltedApiTokenHash = "";
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(apiToken.getBytes(encoding));
			md.update(salt.getBytes(encoding));
			saltedApiTokenHash = Hex.encodeHexString(md.digest());
		} catch (UnsupportedEncodingException e) {
			log.error("This JVM does not support {} encoding.", encoding);
		} catch (NoSuchAlgorithmException e) {
			log.error("This JVM does not support {} algorithm.", algorithm);
		}
		return String.format("%s:%s", saltedApiTokenHash, salt);
	}

}
