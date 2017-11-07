package de.graeuler.garden.config;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

public class StreamHasherTest {

	@Test
	public final void testHash() {
		String referenceHash = "d6ec6898de87ddac6e5b3611708a7aa1c2d298293349cc1a6c299a1db7149d38";
		InputStream referenceStream = new ByteArrayInputStream("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
		StreamToSha256 streamHasher = new StreamToSha256();
		assertEquals(referenceHash, streamHasher.hash(referenceStream));
	}

}
